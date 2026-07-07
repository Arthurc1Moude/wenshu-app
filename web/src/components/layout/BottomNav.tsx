import { Home, Calendar, Crown, User, Plus } from 'lucide-react';
import { useStore } from '@/store';
import { useLocation, useNavigate } from 'react-router-dom';
import type { TabType } from '@/types';

const tabs: { key: TabType; label: string; icon: typeof Home }[] = [
  { key: 'home', label: '首页', icon: Home },
  { key: 'activities', label: '活动', icon: Calendar },
  { key: 'vip', label: '会员', icon: Crown },
  { key: 'profile', label: '我的', icon: User },
];

export default function BottomNav() {
  const { activeTab, setActiveTab, isLoggedIn } = useStore();
  const navigate = useNavigate();
  const location = useLocation();

  const isDetailPage = location.pathname.startsWith('/post/') || 
    location.pathname.startsWith('/chat') || 
    location.pathname === '/settings' || 
    location.pathname === '/redeem' ||
    location.pathname === '/edit-profile' ||
    location.pathname === '/publish' ||
    location.pathname === '/activity/' ||
    location.pathname === '/search';

  if (isDetailPage) return null;

  const handleTab = (tab: TabType) => {
    setActiveTab(tab);
    const routes: Record<TabType, string> = {
      home: '/',
      activities: '/activities',
      vip: '/vip',
      profile: isLoggedIn ? '/profile' : '/login',
    };
    navigate(routes[tab]);
  };

  const handlePublish = () => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }
    navigate('/publish');
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 z-40">
      <div className="max-w-[480px] mx-auto relative bg-white border-t border-divider pb-safe">
        <div className="flex items-center justify-around h-16 px-2">
          {tabs.slice(0, 2).map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => handleTab(tab.key)}
                className="flex flex-col items-center gap-0.5 flex-1 py-1 transition-colors"
              >
                <Icon className={`w-6 h-6 ${isActive ? 'text-black' : 'text-text-tertiary'}`} strokeWidth={isActive ? 2.5 : 2} fill={isActive ? 'black' : 'none'} />
                <span className={`text-[10px] font-medium ${isActive ? 'text-black' : 'text-text-tertiary'}`}>{tab.label}</span>
              </button>
            );
          })}
          <div className="w-14" />
          {tabs.slice(2).map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => handleTab(tab.key)}
                className="flex flex-col items-center gap-0.5 flex-1 py-1 transition-colors"
              >
                <Icon className={`w-6 h-6 ${isActive ? 'text-black' : 'text-text-tertiary'}`} strokeWidth={isActive ? 2.5 : 2} fill={isActive ? 'black' : 'none'} />
                <span className={`text-[10px] font-medium ${isActive ? 'text-black' : 'text-text-tertiary'}`}>{tab.label}</span>
              </button>
            );
          })}
        </div>
        <button
          onClick={handlePublish}
          className="absolute left-1/2 -translate-x-1/2 -top-6 w-14 h-14 bg-black rounded-full flex items-center justify-center shadow-lg active:scale-90 transition-transform"
        >
          <Plus className="w-7 h-7 text-white" strokeWidth={2.5} />
        </button>
      </div>
    </div>
  );
}
