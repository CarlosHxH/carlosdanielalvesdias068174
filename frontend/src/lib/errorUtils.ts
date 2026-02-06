import { toast } from 'sonner';

const RATE_LIMIT_TOAST_ID = 'rate-limit';

/**
 * Exibe toast de erro para falhas de API.
 * Evita toasts duplicados: 429 usa id fixo; erros já tratados pelo interceptor são ignorados.
 */
export function showApiErrorToast(error: unknown, fallback = 'Ocorreu um erro inesperado'): void {
  const err = error as { _rateLimitHandled?: boolean; response?: { status?: number } };
  if (err?._rateLimitHandled) return;

  const msg = getErrorMessage(error, fallback);
  const is429 = err?.response?.status === 429;
  toast.error(msg, { id: is429 ? RATE_LIMIT_TOAST_ID : undefined });
}

/**
 * Extrai mensagem de erro de respostas da API (Axios) ou erros genéricos.
 * Compatível com ErrorResponse do backend: { timestamp, status, error, message, path }
 * e outros formatos: { message }, { error }, { detail }, { errors: string[] }
 */
export function getErrorMessage(error: unknown, fallback = 'Ocorreu um erro inesperado'): string {
  if (!error) return fallback;

  // Erro Axios com response (ErrorResponse do backend usa campo 'message')
  const err = error as { response?: { data?: unknown; status?: number; headers?: Record<string, string> } };
  const data = err.response?.data;

  // 429 Rate Limit: mensagem amigável
  if (err.response?.status === 429) {
    const retryAfter = err.response?.headers?.['retry-after'];
    const segundos = retryAfter ? parseInt(retryAfter, 10) : 6;
    return `Muitas requisições. Aguarde ${segundos} segundos e tente novamente.`;
  }

  if (typeof data === 'string') return data;
  if (data && typeof data === 'object') {
    const obj = data as Record<string, unknown>;
    if (typeof obj.message === 'string' && obj.message.trim()) return obj.message;
    if (typeof obj.error === 'string' && obj.error.trim()) return obj.error;
    if (typeof obj.detail === 'string' && obj.detail.trim()) return obj.detail;
    if (Array.isArray(obj.errors) && obj.errors.length > 0 && typeof obj.errors[0] === 'string')
      return obj.errors[0];
  }

  // Erro de rede (sem response)
  if (!err.response && error instanceof Error) {
    const msg = error.message;
    if (msg === 'Network Error' || msg.includes('fetch')) return 'Erro de conexão. Verifique sua internet.';
    return msg;
  }

  // ApiError (interface do projeto)
  const apiError = error as { message?: string };
  if (typeof apiError?.message === 'string' && apiError.message.trim()) return apiError.message;

  // Error nativo
  if (error instanceof Error) return error.message;

  return fallback;
}
