-- Opcional: ficha ficticia del paciente que ya existía en la versión Room.
-- No crea cuentas ni asigna roles. No necesita UID de Firebase.
insert into public.pacientes (
    id, nombre, apellido_paterno, apellido_materno, ci, email, celular, fecha_nacimiento, diagnosticos
) values (
    'paciente-josue-01', 'Josue', 'Balbontin', 'Perez', '8493021',
    'josue.balbontin@email.com', '70012345', '1999-05-14',
    '[{"id":"diag-demo-1","fecha":"01/10/2026","titulo":"Control demo","descripcion":"Ejemplo académico ficticio"}]'
)
on conflict (id) do nothing;
