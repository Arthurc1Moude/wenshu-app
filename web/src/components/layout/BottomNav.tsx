import { Home, Calendar, Crown, User, Plus } from 'lucide-react';
import { useStore } from '@/store';
import { useLocation, useNavigate } from 'react-router-dom';
import type { TabType } from '@/types';

function HomeIcon({ active }: { active: boolean }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2 : 1.5}>
      <path d="M3 10l9-7 9 7v10a1 1 0 01-1 1h-5v-6H9v6H4a1 1 0 01-1-1V10z" strokeLinejoin="round"/>
    </svg>
  );
}

function ActivityIcon({ active }: { active: boolean }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2 : 1.5}>
      <rect x="3" y="4" width="18" height="18" rx="1"/>
      <path d="M3 9h18M8 2v4M16 2v4M8 14l2 2 4-4"/>
    </svg>
  );
}

function VipIcon({ active }: { active: boolean }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2 : 1.5}>
      <path d="M3 7l4 10h10l4-10-5 3-4-5-4 5-5-3z" strokeLinejoin="round"/>
    </svg>
  );
}

function ProfileIcon({ active }: { active: boolean }) {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2 : 1.5}>
      <circle cx="12" cy="8" r="4"/>
      <path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
    </svg>
  );
}

const tabs: { key: TabType; label: string; Icon: React.FC<{active: boolean}> }[] = [
  { key: 'home', label: '首页', Icon: HomeIcon },
  { key: 'activities', label: '活动', Icon: ActivityIcon },
  { key: 'vip', label: '会员', Icon: VipIcon },
  { key: 'profile', label: '我的', Icon: ProfileIcon },
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
    location.pathname.startsWith('/activity/') ||
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
        <div className="flex items-center justify-around h-14 px-2">
          {tabs.slice(0, 2).map(tab => {
            const Icon = tab.Icon;
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => handleTab(tab.key)}
                className="flex flex-col items-center gap-0 flex-1 py-1 transition-colors"
              >
                <Icon active={isActive} />
                <span className={`text-[11px] font-serif ${isActive ? 'text-ink font-medium' : 'text-text-tertiary'}`}>{tab.label}</span>
              </button>
            );
          })}
          <div className="w-12" />
          {tabs.slice(2).map(tab => {
            const Icon = tab.Icon;
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => handleTab(tab.key)}
                className="flex flex-col items-center gap-0 flex-1 py-1 transition-colors"
              >
                <Icon active={isActive} />
                <span className={`text-[11px] font-serif ${isActive ? 'text-ink font-medium' : 'text-text-tertiary'}`}>{tab.label}</span>
              </button>
            );
          })}
        </div>
        <button
          onClick={handlePublish}
          className="absolute left-1/2 -translate-x-1/2 -top-5 w-12 h-12 bg-ink flex items-center justify-center transition-transform active:scale-90"
          style={{ borderRadius: '2px' }}
        >
          <Plus className="w-6 h-6 text-white" strokeWidth={2} />
        </button>
      </div>
    </div>
  );
}
