import { test } from 'node:test';
import assert from 'node:assert/strict';
import { crearHandler } from './index.ts';

const config = { firebaseProjectId: 'proyecto-test', supabaseUrl: 'https://datos.example', serviceKey: 'solo-servidor' };
const snapshot = { pacientes: [], citas: [] };
const perfil = (rol = 'PACIENTE', activo = true) => ({ fields: {
  rol: { stringValue: rol }, activo: { booleanValue: activo }, pacienteId: { stringValue: 'pac-propio' },
} });
const token = (cambios = {}) => ['header', Buffer.from(JSON.stringify({
  sub: 'usuario-cualquiera', aud: config.firebaseProjectId,
  iss: `https://securetoken.google.com/${config.firebaseProjectId}`, exp: Math.floor(Date.now() / 1000) + 300,
  ...cambios,
})).toString('base64url'), 'firma-que-debe-validar-firestore'].join('.');

function entorno({ documento = perfil(), firestoreStatus = 200, dbStatus = 200 } = {}) {
  const llamadas = [];
  const handler = crearHandler(config, async (url, options) => {
    llamadas.push({ url, options });
    if (url.startsWith('https://firestore.googleapis.com/')) return Response.json(documento, { status: firestoreStatus });
    return Response.json(snapshot, { status: dbStatus });
  });
  const request = (operacion = 'datos_medicos', body = {}, authorization = `Bearer ${token()}`) =>
    handler(new Request(`https://edge.example/functions/v1/api-medica/${operacion}`, {
      method: 'POST', headers: authorization ? { Authorization: authorization } : {}, body: JSON.stringify(body),
    }));
  return { llamadas, request, handler };
}

test('usa UID de la sesión y rol de Firestore sin claim especial ni perfiles PostgreSQL', async () => {
  const env = entorno();
  const response = await env.request('datos_medicos', { uid: 'otro', rol: 'MEDICO', p_perfil: { rol: 'MEDICO' } });
  assert.equal(response.status, 200);
  const recibido = await response.json();
  assert.deepEqual(recibido, snapshot);
  assert.match(env.llamadas[0].url, /documents\/perfiles\/usuario-cualquiera$/);
  assert.match(env.llamadas[0].options.headers.Authorization, /^Bearer header\./);
  const rpc = JSON.parse(env.llamadas[1].options.body);
  assert.deepEqual(rpc.p_perfil, { uid: 'usuario-cualquiera',
    rol: 'PACIENTE', activo: true, paciente_id: 'pac-propio' });
  assert.deepEqual(rpc.p_datos, {});
  assert.equal(env.llamadas[1].options.headers.apikey, config.serviceKey);
  assert.equal(JSON.stringify(recibido).includes(config.serviceKey), false);
});

test('un UID decodificado no autoriza si Firestore rechaza la firma', async () => {
  const env = entorno({ firestoreStatus: 401 });
  assert.equal((await env.request()).status, 401);
  assert.equal(env.llamadas.length, 1); // Nunca llegó a la base.
});

test('sin token no consulta Firestore ni la base', async () => {
  const env = entorno();
  assert.equal((await env.request('datos_medicos', {}, '')).status, 401);
  assert.equal(env.llamadas.length, 0);
});

test('rechaza tokens vencidos o de otro proyecto', async () => {
  for (const cambios of [{ exp: 1 }, { aud: 'otro' }, { iss: 'https://ajeno.example' }]) {
    const env = entorno();
    assert.equal((await env.request('datos_medicos', {}, `Bearer ${token(cambios)}`)).status, 401);
    assert.equal(env.llamadas.length, 0);
  }
});

test('rechaza perfiles desactivados, ausentes y sin paciente vinculado', async () => {
  for (const options of [{ documento: perfil('PACIENTE', false) }, { firestoreStatus: 404 },
    { documento: { fields: { rol: { stringValue: 'PACIENTE' }, activo: { booleanValue: true } } } }]) {
    const env = entorno(options);
    assert.equal((await env.request()).status, 403);
    assert.equal(env.llamadas.length, 1);
  }
});

test('paciente no puede guardar ni eliminar aunque envíe rol MEDICO', async () => {
  for (const operation of ['guardar_cita', 'eliminar_cita', 'guardar_paciente']) {
    const env = entorno();
    assert.equal((await env.request(operation, { rol: 'MEDICO' })).status, 403);
    assert.equal(env.llamadas.length, 1);
  }
});

test('respuesta de paciente ignora el pacienteId y perfil enviados por el cliente', async () => {
  const env = entorno();
  assert.equal((await env.request('responder_cita', { p_id: 'cita-1', p_estado: 'ACEPTADA',
    p_paciente_id: 'ajeno', p_perfil: { rol: 'MEDICO' } })).status, 200);
  const rpc = JSON.parse(env.llamadas[1].options.body);
  assert.deepEqual(rpc.p_datos, { p_id: 'cita-1', p_estado: 'ACEPTADA' });
  assert.equal(rpc.p_perfil.paciente_id, 'pac-propio');
});

test('médico guarda citas con ID nulo y no puede responder como paciente', async () => {
  const env = entorno({ documento: perfil('MEDICO') });
  assert.equal((await env.request('guardar_cita', { p_paciente_id: 'p', p_fecha: '2030-01-01',
    p_hora: '10:00', p_motivo: 'Demo' })).status, 200);
  const rpc = JSON.parse(env.llamadas[1].options.body);
  assert.equal(rpc.p_datos.p_id, null);
  assert.equal(rpc.p_perfil.paciente_id, null);
  assert.equal((await env.request('responder_cita', { p_id: 'c' })).status, 403);
});

test('fallos de servicios no filtran claves ni habilitan consultas sin perfil', async () => {
  const env = entorno({ firestoreStatus: 503 });
  assert.equal((await env.request()).status, 502);
  assert.equal(env.llamadas.length, 1);
  const db = entorno({ dbStatus: 409 });
  const result = await db.request();
  assert.equal(result.status, 409);
  assert.equal((await result.text()).includes(config.serviceKey), false);
});

test('rechaza métodos y operaciones desconocidas', async () => {
  const env = entorno();
  assert.equal((await env.handler(new Request('https://edge.example/api-medica/datos_medicos'))).status, 405);
  assert.equal((await env.request('ejecutar_sql')).status, 404);
  assert.equal((await env.request('eliminar_paciente')).status, 404);
  assert.equal(env.llamadas.length, 0);
});
