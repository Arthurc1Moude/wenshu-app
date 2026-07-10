import { useStore } from '@/store';
import { AnimatePresence, motion } from 'framer-motion';

export default function ToastContainer() {
  const { toasts, dismissToast } = useStore();
  return (
    <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[100] flex flex-col gap-2 pointer-events-none">
      <AnimatePresence>
        {toasts.map(toast => (
          <motion.div
            key={toast.id}
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            onClick={() => dismissToast(toast.id)}
            className={`pointer-events-auto px-5 py-3 border text-sm font-serif tracking-wide cursor-pointer min-w-[200px] text-center ${
              toast.type === 'success' ? 'bg-white text-ink border-ink' :
              toast.type === 'error' ? 'bg-white text-seal border-seal' :
              'bg-white text-ink border-ink'
            }`}
          >
            {toast.message}
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
}
