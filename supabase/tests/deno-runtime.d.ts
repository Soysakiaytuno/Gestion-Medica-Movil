// Tipos mínimos del entorno para comprobar index.ts con tsc instalado localmente.
// En producción, estos tipos los aporta Deno; no se copia este archivo al editor.
declare namespace Deno {
  namespace env { function get(name: string): string | undefined; }
  function serve(handler: (request: Request) => Response | Promise<Response>): unknown;
}
