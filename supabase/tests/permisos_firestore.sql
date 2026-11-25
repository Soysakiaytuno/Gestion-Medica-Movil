-- Tras las tres migraciones. La función Edge comprueba Firebase/Firestore antes
-- de llamar esta RPC con service_role. Aquí se prueba la frontera de la base.
begin;
insert into public.pacientes(id,nombre,apellido_paterno,apellido_materno,ci,email,celular,fecha_nacimiento)
values ('__test_a','Prueba','A','Demo','90000001','__test_a@example.com','70000001','2000-01-01'),
       ('__test_b','Prueba','B','Demo','90000002','__test_b@example.com','70000002','2000-01-01');
insert into public.citas(id,paciente_id,fecha,hora,motivo)
values ('__test_cita_a','__test_a',current_date+2,'10:00','Control A'),
       ('__test_cita_b','__test_b',current_date+2,'11:00','Control B');

-- Un cliente no puede saltarse la función ni falsificar el contexto.
set local role authenticated;
do $$
begin
    begin
        perform public.api_medica('datos_medicos','{"uid":"intruso","rol":"MEDICO","activo":true}', '{}');
        raise exception 'FALLO: el cliente accedió a la RPC interna';
    exception when insufficient_privilege then null; end;
    begin
        perform set_config('app.perfil_verificado','{"uid":"intruso","rol":"MEDICO","activo":true}',true);
        perform public.datos_medicos();
        raise exception 'FALLO: el cliente accedió a la función antigua';
    exception when insufficient_privilege then null; end;
    begin
        perform count(*) from public.pacientes;
        raise exception 'FALLO: lectura directa permitida';
    exception when insufficient_privilege then null; end;
end;
$$;
set local role anon;
do $$
begin
    begin
        perform public.api_medica('datos_medicos','{"uid":"intruso","rol":"MEDICO","activo":true}', '{}');
        raise exception 'FALLO: acceso anónimo a RPC';
    exception when insufficient_privilege then null; end;
end;
$$;

set local role service_role;
do $$
declare
    paciente jsonb := '{"uid":"usuario-dinamico","rol":"PACIENTE","activo":true,"paciente_id":"__test_a"}';
    medico jsonb := '{"uid":"otro-usuario","rol":"MEDICO","activo":true,"paciente_id":null}';
    datos jsonb;
begin
    datos := public.api_medica('datos_medicos',paciente,'{}');
    if jsonb_array_length(datos->'pacientes') <> 1 or jsonb_array_length(datos->'citas') <> 1
        or datos->'pacientes'->0->>'id' <> '__test_a' then
        raise exception 'FALLO: aislamiento del paciente';
    end if;
    if coalesce(current_setting('app.perfil_verificado',true),'') <> '' then
        raise exception 'FALLO: contexto no limpiado';
    end if;
    begin
        perform public.api_medica('eliminar_cita',paciente,'{"p_id":"__test_cita_b"}');
        raise exception 'FALLO: paciente eliminó una cita';
    exception when insufficient_privilege then null; end;
    begin
        perform public.api_medica('responder_cita',paciente,'{"p_id":"__test_cita_b","p_estado":"ACEPTADA"}');
        raise exception 'FALLO: paciente respondió cita ajena';
    exception when unique_violation then null; end;
    perform public.api_medica('responder_cita',paciente,'{"p_id":"__test_cita_a","p_estado":"ACEPTADA"}');
    begin
        perform public.api_medica('responder_cita',paciente,'{"p_id":"__test_cita_a","p_estado":"RECHAZADA"}');
        raise exception 'FALLO: segunda respuesta';
    exception when unique_violation then null; end;
    begin
        perform public.api_medica('guardar_cita',medico,jsonb_build_object('p_id','__test_cita_a',
            'p_paciente_id','__test_a','p_fecha',(current_date+2)::text,'p_hora','10:00','p_motivo','Tarde'));
        raise exception 'FALLO: médico editó cita respondida';
    exception when unique_violation then null; end;
    perform public.api_medica('guardar_cita',medico,jsonb_build_object('p_id',null,
        'p_paciente_id','__test_b','p_fecha',(current_date+2)::text,'p_hora','15:00','p_motivo','Nueva'));
    datos := public.api_medica('datos_medicos',medico,'{}');
    if jsonb_array_length(datos->'citas') <> 3 then raise exception 'FALLO: médico no creó cita'; end if;
    datos := public.api_medica('guardar_paciente',medico,'{"p_paciente":{
        "id":"__test_un_apellido","nombre":"Ana","apellido_paterno":"López",
        "ci":"90000003","email":"un-apellido@example.com","celular":"70000003",
        "diagnosticos":[],"fecha_nacimiento":"2000-01-01"}}');
    if not exists(select 1 from jsonb_array_elements(datos->'pacientes') p
        where p->>'id' = '__test_un_apellido' and p->>'apellido_materno' = '') then
        raise exception 'FALLO: registro con un solo apellido';
    end if;
    begin
        perform public.api_medica('guardar_paciente',medico,'{"p_paciente":{
            "id":"__test_sin_apellido","nombre":"Ana","apellido_paterno":"",
            "ci":"90000004","email":"sin-apellido@example.com","celular":"70000004",
            "diagnosticos":[],"fecha_nacimiento":"2000-01-01"}}');
        raise exception 'FALLO: se aceptó un paciente sin apellido';
    exception when check_violation then null; end;
    perform public.api_medica('eliminar_cita',medico,'{"p_id":"__test_cita_b"}');
    begin
        perform public.api_medica('datos_medicos',medico || '{"activo":false}', '{}');
        raise exception 'FALLO: perfil inactivo';
    exception when insufficient_privilege then null; end;
    begin
        perform public.api_medica('datos_medicos',paciente - 'paciente_id', '{}');
        raise exception 'FALLO: paciente sin vinculación';
    exception when insufficient_privilege then null; end;
end;
$$;
reset role;
-- Ninguna operación creó una copia de perfiles de Firestore.
do $$ begin
    if to_regclass('public.perfiles_medicos') is not null then raise exception 'FALLO: quedó la tabla de perfiles sin uso'; end if;
end; $$;
rollback;
select 'Comprobaciones Firestore/almacenamiento completadas' as resultado;
