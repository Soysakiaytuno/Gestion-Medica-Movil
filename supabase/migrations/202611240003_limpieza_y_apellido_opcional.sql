-- Ejecutar tras 202611240001 y 202611240002. Conserva pacientes y citas.
-- Elimina el esquema de perfiles en desuso y admite un segundo apellido vacío.
begin;
alter table public.pacientes drop constraint if exists pacientes_apellido_materno_check;
alter table public.pacientes alter column apellido_materno set default '';

-- Tipo temporal de contexto: no es una tabla ni almacena perfiles.
create type public.perfil_verificado as (uid text, rol text, activo boolean, paciente_id text);
drop function public.mi_perfil();
create function public.mi_perfil() returns public.perfil_verificado
language sql stable security definer set search_path = '' as $$
    select row(p->>'uid', p->>'rol', true, nullif(p->>'paciente_id', ''))::public.perfil_verificado
    from (select nullif(current_setting('app.perfil_verificado', true), '')::jsonb as p) contexto
    where p->>'uid' <> '' and p->>'rol' in ('MEDICO', 'PACIENTE') and p->>'activo' = 'true';
$$;
revoke all on function public.mi_perfil() from public, anon, authenticated, service_role;


create or replace function public.datos_medicos() returns jsonb
language plpgsql security definer set search_path = '' as $$
declare perfil public.perfil_verificado := public.mi_perfil();
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

create or replace function public.responder_cita(p_id text, p_estado text) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare perfil public.perfil_verificado := public.mi_perfil(); cita public.citas;
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

create or replace function public.guardar_paciente(p_paciente jsonb) returns jsonb
language plpgsql security definer set search_path = '' as $$
begin
    if (public.mi_perfil()).rol is distinct from 'MEDICO' then
        raise insufficient_privilege using message = 'Solo el médico puede guardar pacientes';
    end if;
    insert into public.pacientes(id, nombre, apellido_paterno, apellido_materno, ci, email, celular, diagnosticos, fecha_nacimiento)
    values (p_paciente->>'id', trim(p_paciente->>'nombre'), trim(p_paciente->>'apellido_paterno'),
        coalesce(trim(p_paciente->>'apellido_materno'), ''), trim(p_paciente->>'ci'), trim(p_paciente->>'email'),
        trim(p_paciente->>'celular'), p_paciente->'diagnosticos', (p_paciente->>'fecha_nacimiento')::date)
    on conflict (id) do update set nombre = excluded.nombre, apellido_paterno = excluded.apellido_paterno,
        apellido_materno = excluded.apellido_materno, ci = excluded.ci, email = excluded.email,
        celular = excluded.celular, diagnosticos = excluded.diagnosticos, fecha_nacimiento = excluded.fecha_nacimiento;
    return public.datos_medicos();
end;
$$;

create or replace function public.api_medica(p_operacion text, p_perfil jsonb, p_datos jsonb default '{}') returns jsonb
language plpgsql security definer set search_path = '' as $$
declare resultado jsonb;
begin
    if p_perfil is null or jsonb_typeof(p_perfil) <> 'object'
        or coalesce(p_perfil->>'uid', '') = ''
        or coalesce(p_perfil->>'rol', '') not in ('MEDICO', 'PACIENTE')
        or p_perfil->>'activo' is distinct from 'true'
        or (p_perfil->>'rol' = 'PACIENTE' and coalesce(p_perfil->>'paciente_id', '') = '') then
        raise insufficient_privilege using message = 'Falta el perfil verificado por Firestore';
    end if;
    perform set_config('app.perfil_verificado', p_perfil::text, true);
    case p_operacion
        when 'datos_medicos' then resultado := public.datos_medicos();
        when 'guardar_paciente' then resultado := public.guardar_paciente(p_datos->'p_paciente');
        when 'guardar_cita' then resultado := public.guardar_cita(p_datos->>'p_id', p_datos->>'p_paciente_id',
            (p_datos->>'p_fecha')::date, p_datos->>'p_hora', p_datos->>'p_motivo');
        when 'responder_cita' then resultado := public.responder_cita(p_datos->>'p_id', p_datos->>'p_estado');
        when 'eliminar_cita' then resultado := public.eliminar_cita(p_datos->>'p_id');
        else raise invalid_parameter_value using message = 'Operación desconocida';
    end case;
    -- El contexto nunca se conserva como perfil del usuario en la base.
    perform set_config('app.perfil_verificado', '', true);
    return resultado;
end;
$$;

drop function public.eliminar_paciente(text);
drop table public.perfiles_medicos;
commit;
