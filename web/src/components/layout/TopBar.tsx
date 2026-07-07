import { Search, Bell, ArrowLeft, Settings, ChevronLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useStore } from '@/store';

interface TopBarProps {
  title: string;
  showBack?: boolean;
  showSearch?: boolean;
  showNotifications?: boolean;
  showSettings?: boolean;
  rightElement?: React.ReactNode;
}

export default function TopBar({ title, showBack, showSearch, showNotifications, showSettings, rightElement }: TopBarProps) {
  const navigate = useNavigate();
  const { unreadCount } = useStore();

  return (
    <div className="sticky top-0 z-30 bg-white border-b border-divider">
      <div className="h-14 flex items-center justify-between px-4 max-w-[480px] mx-auto">
        <div className="flex items-center gap-2 w-20">
          {showBack && (
            <button onClick={() => navigate(-1)} className="p-1 -ml-1 active:opacity-60">
              <ChevronLeft className="w-6 h-6 text-black" strokeWidth={2.5} />
            </button>
          )}
          {showSearch && (
            <button onClick={() => navigate('/search')} className="p-1 -ml-1 active:opacity-60">
              <Search className="w-6 h-6 text-black" strokeWidth={2} />
            </button>
          )}
        </div>
        <h1 className="text-lg font-bold text-text-primary flex-1 text-center">{title}</h1>
        <div className="flex items-center gap-2 w-20 justify-end">
          {rightElement}
          {showNotifications && (
            <button onClick={() => navigate('/messages')} className="p-1 -mr-1 relative active:opacity-60">
              <Bell className="w-6 h-6 text-black" strokeWidth={2} />
              {unreadCount > 0 && (
                <span className="absolute top-0 right-0 w-2 h-2 bg-danger rounded-full" />
              )}
            </button>
          )}
          {showSettings && (
            <button onClick={() => navigate('/settings')} className="p-1 -mr-1 active:opacity-60">
              <Settings className="w-6 h-6 text-black" strokeWidth={2} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
