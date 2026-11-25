-- Ejecutar una vez desde el SQL Editor de un proyecto Supabase nuevo.
begin;

create table public.pacientes (
    id text primary key check (length(trim(id)) > 0),
    nombre text not null check (length(trim(nombre)) > 0),
    apellido_paterno text not null check (length(trim(apellido_paterno)) > 0),
    apellido_materno text not null check (length(trim(apellido_materno)) > 0),
    ci text not null unique check (ci ~ '^[0-9]{7,8}$'),
    email text not null check (email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'),
    celular text not null check (celular ~ '^[0-9]{8,}$'),
    diagnosticos jsonb not null default '[]' check (jsonb_typeof(diagnosticos) = 'array'),
    fecha_nacimiento date not null check (fecha_nacimiento <= current_date)
);
create unique index pacientes_email_unico on public.pacientes (lower(email));

-- Administrado exclusivamente desde SQL Editor. UID de Firebase, no UUID de Supabase.
create table public.perfiles_medicos (
    uid text primary key,
    firebase_project_id text not null,
    rol text not null check (rol in ('MEDICO', 'PACIENTE')),
    activo boolean not null default true,
    paciente_id text references public.pacientes(id) on delete restrict,
    check ((rol = 'PACIENTE' and paciente_id is not null) or (rol = 'MEDICO' and paciente_id is null))
);

create table public.citas (
    id text primary key,
    paciente_id text not null references public.pacientes(id) on delete restrict,
    fecha date not null,
    hora text not null check (hora ~ '^([01][0-9]|2[0-3]):[0-5][0-9]$'),
    motivo text not null check (length(trim(motivo)) > 0),
    estado text not null default 'PENDIENTE' check (estado in ('PENDIENTE', 'ACEPTADA', 'RECHAZADA'))
);
create index citas_paciente_fecha on public.citas(paciente_id, fecha, hora);

alter table public.pacientes enable row level security;
alter table public.citas enable row level security;
alter table public.perfiles_medicos enable row level security;
revoke all on public.pacientes, public.citas, public.perfiles_medicos from anon, authenticated;
grant select on public.pacientes, public.citas, public.perfiles_medicos to authenticated;

create function public.mi_perfil() returns public.perfiles_medicos
language sql stable security definer set search_path = '' as $$
    select p from public.perfiles_medicos p
    where p.uid = (auth.jwt()->>'sub') and p.activo
      and auth.jwt()->>'iss' = 'https://securetoken.google.com/' || p.firebase_project_id
      and auth.jwt()->>'aud' = p.firebase_project_id;
$$;
create policy perfil_propio on public.perfiles_medicos for select to authenticated
using (uid = (auth.jwt()->>'sub')
    and auth.jwt()->>'iss' = 'https://securetoken.google.com/' || firebase_project_id
    and auth.jwt()->>'aud' = firebase_project_id);
create policy pacientes_autorizados on public.pacientes for select to authenticated
using ((select (public.mi_perfil()).rol) = 'MEDICO'
    or id = (select (public.mi_perfil()).paciente_id));
create policy citas_autorizadas on public.citas for select to authenticated
using ((select (public.mi_perfil()).rol) = 'MEDICO'
    or paciente_id = (select (public.mi_perfil()).paciente_id));

create function public.datos_medicos() returns jsonb
language plpgsql security definer set search_path = '' as $$
declare perfil public.perfiles_medicos := public.mi_perfil();
begin
    if perfil.uid is null then raise insufficient_privilege using message = 'Perfil no autorizado'; end if;
    return jsonb_build_object(
        'pacientes', (select coalesce(jsonb_agg(to_jsonb(p) order by p.nombre, p.id), '[]')
            from public.pacientes p where perfil.rol = 'MEDICO' or p.id = perfil.paciente_id),
        'citas', (select coalesce(jsonb_agg(to_jsonb(c) order by c.fecha, c.hora, c.id), '[]')
            from public.citas c where perfil.rol = 'MEDICO' or c.paciente_id = perfil.paciente_id)
    );
end;
$$;

create function public.guardar_paciente(p_paciente jsonb) returns jsonb
language plpgsql security definer set search_path = '' as $$
begin
    if (public.mi_perfil()).rol is distinct from 'MEDICO' then
        raise insufficient_privilege using message = 'Solo el médico puede guardar pacientes';
    end if;
    insert into public.pacientes(id, nombre, apellido_paterno, apellido_materno, ci, email, celular, diagnosticos, fecha_nacimiento)
    values (p_paciente->>'id', trim(p_paciente->>'nombre'), trim(p_paciente->>'apellido_paterno'),
        trim(p_paciente->>'apellido_materno'), trim(p_paciente->>'ci'), trim(p_paciente->>'email'),
        trim(p_paciente->>'celular'), p_paciente->'diagnosticos', (p_paciente->>'fecha_nacimiento')::date)
    on conflict (id) do update set nombre = excluded.nombre, apellido_paterno = excluded.apellido_paterno,
        apellido_materno = excluded.apellido_materno, ci = excluded.ci, email = excluded.email,
        celular = excluded.celular, diagnosticos = excluded.diagnosticos, fecha_nacimiento = excluded.fecha_nacimiento;
    return public.datos_medicos();
end;
$$;

create function public.eliminar_paciente(p_id text) returns jsonb
language plpgsql security definer set search_path = '' as $$
begin
    if (public.mi_perfil()).rol is distinct from 'MEDICO' then
        raise insufficient_privilege using message = 'Solo el médico puede eliminar pacientes';
    end if;
    delete from public.pacientes where id = p_id;
    -- La FK impide eliminar pacientes con citas o cuentas vinculadas.
    return public.datos_medicos();
end;
$$;

create function public.guardar_cita(p_id text, p_paciente_id text, p_fecha date, p_hora text, p_motivo text) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare cita public.citas;
begin
    if (public.mi_perfil()).rol is distinct from 'MEDICO' then
        raise insufficient_privilege using message = 'Solo el médico puede guardar citas';
    end if;
    if p_fecha is null or p_hora is null or p_hora !~ '^([01][0-9]|2[0-3]):[0-5][0-9]$' then
        raise invalid_parameter_value using message = 'Fecha u hora inválida';
    end if;
    if (p_fecha + p_hora::time) at time zone 'America/La_Paz' <= now() then
        raise invalid_parameter_value using message = 'La cita debe ser futura';
    end if;
    if p_id is null then
        insert into public.citas(id, paciente_id, fecha, hora, motivo)
        values (gen_random_uuid()::text, p_paciente_id, p_fecha, p_hora, trim(p_motivo));
    else
        select * into cita from public.citas where id = p_id for update;
        if not found or cita.estado <> 'PENDIENTE' then
            raise exception using errcode = '23505', message = 'La cita ya no está pendiente o fue eliminada';
        end if;
        update public.citas set paciente_id = p_paciente_id, fecha = p_fecha, hora = p_hora, motivo = trim(p_motivo)
        where id = p_id;
    end if;
    return public.datos_medicos();
end;
$$;

create function public.responder_cita(p_id text, p_estado text) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare perfil public.perfiles_medicos := public.mi_perfil(); cita public.citas;
begin
    if perfil.rol is distinct from 'PACIENTE' then
        raise insufficient_privilege using message = 'Solo el paciente puede responder';
    end if;
    if p_estado is null or p_estado not in ('ACEPTADA', 'RECHAZADA') then
        raise invalid_parameter_value using message = 'Respuesta inválida';
    end if;
    select * into cita from public.citas where id = p_id and paciente_id = perfil.paciente_id for update;
    if not found or cita.estado <> 'PENDIENTE' then
        raise exception using errcode = '23505', message = 'La cita no pertenece al paciente o ya fue respondida';
    end if;
    update public.citas set estado = p_estado where id = p_id;
    return public.datos_medicos();
end;
$$;

create function public.eliminar_cita(p_id text) returns jsonb
language plpgsql security definer set search_path = '' as $$
begin
    if (public.mi_perfil()).rol is distinct from 'MEDICO' then
        raise insufficient_privilege using message = 'Solo el médico puede eliminar citas';
    end if;
    delete from public.citas where id = p_id;
    return public.datos_medicos();
end;
$$;

-- Los SECURITY DEFINER solo exponen operaciones que comprueban el JWT y el perfil.
revoke all on function public.mi_perfil(), public.datos_medicos(), public.guardar_paciente(jsonb),
    public.eliminar_paciente(text), public.guardar_cita(text,text,date,text,text),
    public.responder_cita(text,text), public.eliminar_cita(text) from public, anon;
grant execute on function public.mi_perfil(), public.datos_medicos(), public.guardar_paciente(jsonb),
    public.eliminar_paciente(text), public.guardar_cita(text,text,date,text,text),
    public.responder_cita(text,text), public.eliminar_cita(text) to authenticated;
commit;
