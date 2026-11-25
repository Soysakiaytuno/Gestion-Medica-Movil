// PostgreSQL temporal en memoria. Recibe la ruta de @electric-sql/pglite instalado
// fuera del proyecto; no se añade ninguna dependencia a la app Android.
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { PGlite } = require(process.argv[2] || '@electric-sql/pglite');

async function main() {
  const db = new PGlite();
  try {
    await db.exec(`
      create role anon;
      create role authenticated;
      create role service_role;
      create schema auth;
      create function auth.jwt() returns jsonb language sql stable as
        $$ select coalesce(nullif(current_setting('request.jwt.claims', true), ''), '{}')::jsonb $$;
      grant usage on schema auth to anon, authenticated;
      grant execute on function auth.jwt() to anon, authenticated;
    `);
    await db.exec(readFileSync(resolve(__dirname, '../supabase/migrations/202611240001_datos_medicos.sql'), 'utf8'));
    await db.exec(`insert into public.pacientes(id,nombre,apellido_paterno,apellido_materno,ci,email,celular,fecha_nacimiento)
      values ('__pre_migracion','Antes','Demo','Prueba','89999999','previo@example.com','70000000','2000-01-01');`);
    await db.exec(readFileSync(resolve(__dirname, '../supabase/migrations/202611240002_perfiles_firestore.sql'), 'utf8'));
    await db.exec(readFileSync(resolve(__dirname, '../supabase/migrations/202611240003_limpieza_y_apellido_opcional.sql'), 'utf8'));
    await db.exec(readFileSync(resolve(__dirname, '../supabase/tests/permisos_firestore.sql'), 'utf8'));
    const result = await db.query('select count(*)::int as cantidad from public.pacientes');
    if (result.rows[0].cantidad !== 1) throw new Error('La migración no conservó los datos previos o las pruebas dejaron datos');
    console.log('SQL válido: tres migraciones, conservación de datos, RPC interna, roles de Firestore, apellido opcional, transiciones y rollback verificados en PostgreSQL local.');
  } finally { await db.close(); }
}
main().catch(error => { console.error(error.code, error.message); process.exitCode = 1; });
