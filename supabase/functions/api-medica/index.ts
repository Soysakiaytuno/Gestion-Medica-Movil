// Sin dependencias externas ni credenciales administrativas de Firebase.
// Firestore valida el ID token de Firebase y aplica sus reglas al leer el perfil.
type Config = { firebaseProjectId: string; supabaseUrl: string; serviceKey: string };
type Perfil = { uid: string; rol: string; activo: boolean; paciente_id: string | null };
type Fetch = typeof fetch;
class ErrorApi extends Error {
  status: number;
  constructor(status: number, message: string) { super(message); this.status = status; }
}

const operaciones = new Set([
  "datos_medicos", "guardar_paciente",
  "guardar_cita", "responder_cita", "eliminar_cita",
]);

function respuesta(status: number, body: unknown): Response {
  return Response.json(body, { status, headers: { "Cache-Control": "no-store" } });
}

function uidCandidato(token: string, projectId: string): string {
  try {
    const partes = token.split(".");
    if (partes.length !== 3) throw new Error();
    const payload = partes[1].replace(/-/g, "+").replace(/_/g, "/");
    const jwt = JSON.parse(atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, "=")));
    if (jwt.iss !== `https://securetoken.google.com/${projectId}` || jwt.aud !== projectId ||
        typeof jwt.sub !== "string" || !jwt.sub || jwt.sub.length > 128 ||
        typeof jwt.exp !== "number" || jwt.exp <= Date.now() / 1000) throw new Error();
    // Decodificar NO verifica una firma. Este UID solo se usa para pedir el
    // documento propio; no se autoriza ni se consulta Supabase hasta que Firestore
    // haya aceptado el token enviado en Authorization.
    return jwt.sub;
  } catch { throw new ErrorApi(401, "Sesión de Firebase inválida o vencida."); }
}

async function perfilFirebase(token: string, projectId: string, solicitar: Fetch): Promise<Perfil> {
  const uid = uidCandidato(token, projectId);
  const url = `https://firestore.googleapis.com/v1/projects/${encodeURIComponent(projectId)}` +
    `/databases/(default)/documents/perfiles/${encodeURIComponent(uid)}`;
  const consulta = await solicitar(url, {
    headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(10_000),
  });
  if (consulta.status === 401) throw new ErrorApi(401, "Firebase rechazó la sesión.");
  if (consulta.status === 403 || consulta.status === 404) {
    throw new ErrorApi(403, "No se puede consultar el perfil propio de Firestore.");
  }
  if (!consulta.ok) throw new ErrorApi(502, "Firestore no está disponible.");
  const documento = await consulta.json();
  const fields = documento.fields ?? {};
  const rol = fields.rol?.stringValue;
  const pacienteId = fields.pacienteId?.stringValue?.trim() || null;
  if (fields.activo?.booleanValue !== true || !["MEDICO", "PACIENTE"].includes(rol) ||
      (rol === "PACIENTE" && !pacienteId)) {
    throw new ErrorApi(403, "El perfil está desactivado o incompleto.");
  }
  return { uid, rol, activo: true,
    paciente_id: rol === "PACIENTE" ? pacienteId : null };
}

function parametros(operacion: string, body: Record<string, unknown>): Record<string, unknown> {
  // Lista explícita: el cliente no puede enviar un perfil ni contexto de permisos.
  const campos: Record<string, string[]> = {
    datos_medicos: [], guardar_paciente: ["p_paciente"],
    guardar_cita: ["p_id", "p_paciente_id", "p_fecha", "p_hora", "p_motivo"],
    responder_cita: ["p_id", "p_estado"], eliminar_cita: ["p_id"],
  };
  return Object.fromEntries(campos[operacion].map(campo => [campo, body[campo] ?? null]));
}

export function crearHandler(config: Config, solicitar: Fetch = fetch) {
  return async (request: Request): Promise<Response> => {
    try {
      if (request.method !== "POST") return respuesta(405, { error: "Usa POST." });
      const pathname = new URL(request.url).pathname;
      const operacion = pathname.split("/").filter(Boolean).pop() ?? "";
      if (!operaciones.has(operacion)) throw new ErrorApi(404, "Operación desconocida.");
      if (!config.serviceKey || !config.supabaseUrl || !config.firebaseProjectId) {
        throw new ErrorApi(503, "Falta la configuración del servidor.");
      }
      const authorization = request.headers.get("Authorization") ?? "";
      if (!authorization.startsWith("Bearer ")) throw new ErrorApi(401, "Falta la sesión de Firebase.");
      const rawBody = await request.text();
      if (rawBody.length > 100_000) throw new ErrorApi(413, "Petición demasiado grande.");
      let body: Record<string, unknown>;
      try {
        body = rawBody ? JSON.parse(rawBody) : {};
        if (!body || typeof body !== "object" || Array.isArray(body)) throw new Error();
      } catch { throw new ErrorApi(400, "JSON inválido."); }

      const perfil = await perfilFirebase(authorization.slice(7), config.firebaseProjectId, solicitar);
      if (operacion === "responder_cita") {
        if (perfil.rol !== "PACIENTE") throw new ErrorApi(403, "Solo el paciente puede responder.");
      } else if (operacion !== "datos_medicos" && perfil.rol !== "MEDICO") {
        throw new ErrorApi(403, "Solo el médico puede modificar estos datos.");
      }

      const resultado = await solicitar(`${config.supabaseUrl.replace(/\/$/, "")}/rest/v1/rpc/api_medica`, {
        method: "POST",
        headers: { "Content-Type": "application/json", apikey: config.serviceKey,
          Authorization: `Bearer ${config.serviceKey}` },
        body: JSON.stringify({ p_operacion: operacion, p_perfil: perfil, p_datos: parametros(operacion, body) }),
        signal: AbortSignal.timeout(15_000),
      });
      if (!resultado.ok) {
        const status = [400, 401, 403, 404, 409].includes(resultado.status) ? resultado.status : 502;
        throw new ErrorApi(status, status === 409 ? "Registro duplicado o cita modificada." : "No se pudo completar la operación en la base de datos.");
      }
      return respuesta(200, await resultado.json());
    } catch (error) {
      if (error instanceof ErrorApi) return respuesta(error.status, { error: error.message });
      // No exponer tokens, claves ni respuestas de otros servicios.
      return respuesta(502, { error: "No se pudo conectar con los servicios remotos." });
    }
  };
}

// Las claves internas de Supabase solo existen aquí, nunca en Android.
if (typeof Deno !== "undefined") {
  Deno.serve(crearHandler({
    firebaseProjectId: Deno.env.get("FIREBASE_PROJECT_ID") ?? "",
    supabaseUrl: Deno.env.get("SUPABASE_URL") ?? "",
    serviceKey: Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "",
  }));
}
