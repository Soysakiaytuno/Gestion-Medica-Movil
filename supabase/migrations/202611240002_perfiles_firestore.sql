-- Ejecutar DESPUÉS de 202611240001. Conserva pacientes y citas existentes.
-- Los perfiles se consultan en Firestore por api-medica; no se copian a PostgreSQL.
begin;

-- Ya no se permite que el teléfono llame directamente las funciones ni las tablas.
revoke all on public.pacientes, public.citas, public.perfiles_medicos from anon, authenticated;
revoke all on function public.mi_perfil(), public.datos_medicos(), public.guardar_paciente(jsonb),
    public.eliminar_paciente(text), public.guardar_cita(text,text,date,text,text),
    public.responder_cita(text,text), public.eliminar_cita(text) from public, anon, authenticated, service_role;
drop policy if exists perfil_propio on public.perfiles_medicos;
drop policy if exists pacientes_autorizados on public.pacientes;
drop policy if exists citas_autorizadas on public.citas;

-- Se conserva el tipo de retorno de la migración anterior para reutilizar las
-- operaciones ya existentes. La tabla perfiles_medicos queda sin uso: no se lee
-- ni se inserta en ella, ni se toma su contenido como fuente de permisos.
create or replace function public.mi_perfil() returns public.perfiles_medicos
language sql stable security definer set search_path = '' as $$
    select row(p->>'uid', p->>'firebase_project_id', p->>'rol', true,
        nullif(p->>'paciente_id', ''))::public.perfiles_medicos
    from (select nullif(current_setting('app.perfil_verificado', true), '')::jsonb as p) contexto
    where p->>'uid' <> '' and p->>'rol' in ('MEDICO', 'PACIENTE')
      and p->>'activo' = 'true';
$$;

-- Solo la Edge Function, con su clave interna, puede suministrar este contexto.
create function public.api_medica(p_operacion text, p_perfil jsonb, p_datos jsonb default '{}') returns jsonb
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
        when 'eliminar_paciente' then resultado := public.eliminar_paciente(p_datos->>'p_id');
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
revoke all on function public.api_medica(text,jsonb,jsonb) from public, anon, authenticated;
grant execute on function public.api_medica(text,jsonb,jsonb) to service_role;
comment on table public.perfiles_medicos is 'Legado sin uso: los perfiles y roles se consultan en Firestore. No llenar esta tabla.';
commit;
