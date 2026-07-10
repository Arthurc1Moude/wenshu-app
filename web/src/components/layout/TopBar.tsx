import { Search, Bell, Settings, ChevronLeft } from 'lucide-react';
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
        <div className="flex items-center gap-3 w-24">
          {showBack && (
            <button onClick={() => navigate(-1)} className="p-1 -ml-1 active:opacity-50">
              <ChevronLeft className="w-6 h-6 text-ink" strokeWidth={2} />
            </button>
          )}
          {showSearch && (
            <button onClick={() => navigate('/search')} className="p-1 -ml-1 active:opacity-50">
              <Search className="w-5 h-5 text-ink" strokeWidth={1.5} />
            </button>
          )}
        </div>
        <h1 className="text-base font-serif text-ink font-medium tracking-widest flex-1 text-center">{title}</h1>
        <div className="flex items-center gap-3 w-24 justify-end">
          {rightElement}
          {showNotifications && (
            <button onClick={() => navigate('/messages')} className="p-1 -mr-1 relative active:opacity-50">
              <Bell className="w-5 h-5 text-ink" strokeWidth={1.5} />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-seal rounded-full" />
              )}
            </button>
          )}
          {showSettings && (
            <button onClick={() => navigate('/settings')} className="p-1 -mr-1 active:opacity-50">
              <Settings className="w-5 h-5 text-ink" strokeWidth={1.5} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
