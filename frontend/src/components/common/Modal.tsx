import { type ReactNode } from 'react';
import { X } from 'lucide-react';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
  title?: string;
}

/**
 * Modal genérico com overlay e botão de fechar
 * @param open - Controla visibilidade
 * @param onClose - Callback ao fechar
 * @param children - Conteúdo do modal
 * @param title - Título opcional
 */
export default function Modal({ open, onClose, children, title }: ModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in-0 duration-200">
      {/* Backdrop com blur */}
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden
      />
      {/* Container do modal */}
      <div
        className="relative w-full max-w-lg bg-slate-900/95 backdrop-blur-md rounded-2xl border border-slate-700/80 shadow-2xl shadow-black/50 animate-in fade-in-0 zoom-in-95 duration-200"
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? 'modal-title' : undefined}
      >
        {/* Header */}
        <div className="flex items-center justify-between gap-4 px-6 py-4 border-b border-slate-700/80">
          {title && (
            <h2 id="modal-title" className="text-xl font-semibold text-white tracking-tight">
              {title}
            </h2>
          )}
          <button
            onClick={onClose}
            className={`p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700/80 transition-colors focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2 focus:ring-offset-slate-900 ${!title ? 'ml-auto' : ''}`}
            aria-label="Fechar"
          >
            <X className="size-5" />
          </button>
        </div>
        {/* Conteúdo */}
        <div className="px-6 py-5 max-h-[calc(100vh-12rem)] overflow-y-auto">
          {children}
        </div>
      </div>
    </div>
  );
}
