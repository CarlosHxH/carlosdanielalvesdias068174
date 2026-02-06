import { useState } from 'react';
import { Trash2, Loader2 } from 'lucide-react';
import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';

export interface DeleteConfirmModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => Promise<void>;
  title: string;
  description: string;
  itemName?: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

/**
 * Modal de confirmação de exclusão
 * Design elegante com ícone de alerta e botões de ação
 */
export function DeleteConfirmModal({
  open,
  onOpenChange,
  onConfirm,
  title,
  description,
  itemName,
  confirmLabel = 'Excluir',
  cancelLabel = 'Cancelar',
}: DeleteConfirmModalProps) {
  const [loading, setLoading] = useState(false);

  async function handleConfirm() {
    setLoading(true);
    try {
      await onConfirm();
      onOpenChange(false);
    } finally {
      setLoading(false);
    }
  }

  function handleOpenChange(newOpen: boolean) {
    if (!loading) {
      onOpenChange(newOpen);
    }
  }

  return (
    <AlertDialog open={open} onOpenChange={handleOpenChange}>
      <AlertDialogContent className="border-slate-700 bg-slate-900/98 backdrop-blur-xl shadow-2xl shadow-black/50 max-w-md mx-4 sm:mx-0">
        <AlertDialogHeader className="flex flex-col sm:flex-row gap-4 sm:gap-5 text-center sm:text-left pb-2">
          <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-red-500/20 ring-2 ring-red-500/40 mx-auto sm:mx-0">
            <Trash2 className="size-8 text-red-400" />
          </div>
          <div className="space-y-2 flex-1 min-w-0">
            <AlertDialogTitle className="text-lg sm:text-xl font-semibold text-white">
              {title}
            </AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400 text-sm leading-relaxed">
              {description}
              {itemName && (
                <span className="mt-2 block font-medium text-red-400">
                  &quot;{itemName}&quot;
                </span>
              )}
            </AlertDialogDescription>
          </div>
        </AlertDialogHeader>

        <AlertDialogFooter className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end sm:gap-3 mt-6 pt-4 border-t border-slate-700/80">
          <AlertDialogCancel
            className="border-slate-600 bg-slate-800/50 text-slate-200 hover:bg-slate-700 hover:text-white m-0 w-full sm:w-auto"
            disabled={loading}
          >
            {cancelLabel}
          </AlertDialogCancel>
          <Button
            onClick={handleConfirm}
            disabled={loading}
            className="bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-900/30 m-0 w-full sm:w-auto min-h-[44px] sm:min-h-0"
          >
            {loading ? (
              <>
                <Loader2 className="size-4 animate-spin shrink-0" />
                Excluindo...
              </>
            ) : (
              <>
                <Trash2 className="size-4 shrink-0" />
                {confirmLabel}
              </>
            )}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
