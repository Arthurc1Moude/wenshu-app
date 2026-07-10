import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';

function ChevronIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <polyline points="9 18 15 12 9 6"/>
    </svg>
  );
}

function TicketIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M2 9a3 3 0 010 6v2a2 2 0 002 2h16a2 2 0 002-2v-2a3 3 0 010-6V7a2 2 0 00-2-2H4a2 2 0 00-2 2z"/>
      <path d="M13 5v14" strokeDasharray="2 2"/>
    </svg>
  );
}

function CoinIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="9"/>
      <text x="12" y="16" textAnchor="middle" fontSize="10" fill="currentColor" stroke="none" fontFamily="serif">文</text>
    </svg>
  );
}

function BellIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M18 8a6 6 0 00-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
      <path d="M13.73 21a2 2 0 01-3.46 0"/>
    </svg>
  );
}

function InfoIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="9"/>
      <line x1="12" y1="16" x2="12" y2="12"/>
      <line x1="12" y1="8" x2="12.01" y2="8"/>
    </svg>
  );
}

function HelpIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="9"/>
      <path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3"/>
      <line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
  );
}

function ShieldIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
    </svg>
  );
}

function LogoutIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
      <polyline points="16 17 21 12 16 7"/>
      <line x1="21" y1="12" x2="9" y2="12"/>
    </svg>
  );
}

const SettingItem = ({ icon, label, value, onClick, danger }: {
  icon: React.ReactNode; label: string; value?: string; onClick?: () => void; danger?: boolean;
}) => (
  <button onClick={onClick} className={`w-full flex items-center gap-4 px-6 py-4 active:bg-paper transition-colors text-left border-b border-divider ${danger ? 'text-seal' : 'text-ink'}`}>
    <span className={danger ? 'text-seal' : 'text-ink'}>{icon}</span>
    <span className="flex-1 font-serif tracking-wide">{label}</span>
    {value && <span className="text-sm font-serif text-text-tertiary">{value}</span>}
    {!danger && <ChevronIcon />}
  </button>
);

export default function SettingsPage() {
  const navigate = useNavigate();
  const { currentUser, logout, clearCache } = useStore();

  const handleLogout = () => {
    if (confirm('确定要退出登录吗？')) {
      logout();
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="设 置" showBack />

      <div className="pt-2 max-w-[480px] mx-auto">
        <div className="mx-4 mb-1">
          <p className="text-xs font-serif text-text-tertiary tracking-widest px-2 py-2">账 户</p>
        </div>
        <div className="border-t border-divider">
          <SettingItem icon={<TicketIcon />} label="兑换码" onClick={() => navigate('/redeem')} />
          <SettingItem icon={<CoinIcon />} label="文书币" value={currentUser ? `${currentUser.wenshuCoin}` : '0'} onClick={() => {}} />
        </div>

        <div className="mx-4 mb-1 mt-4">
          <p className="text-xs font-serif text-text-tertiary tracking-widest px-2 py-2">通 用</p>
        </div>
        <div className="border-t border-divider">
          <SettingItem icon={<BellIcon />} label="消息通知" onClick={() => {}} />
          <SettingItem icon={<ShieldIcon />} label="隐私与安全" onClick={() => {}} />
          <SettingItem icon={<HelpIcon />} label="帮助与反馈" onClick={() => {}} />
          <SettingItem icon={<InfoIcon />} label="关于文书" value="v1.0" onClick={() => {}} />
        </div>

        <div className="px-6 pt-8">
          <button
            onClick={handleLogout}
            className="w-full py-4 bg-white border border-seal text-seal font-serif tracking-[0.3em] active:bg-red-50 transition-colors"
            style={{ borderRadius: '2px' }}
          >
            <span className="flex items-center justify-center gap-2">
              <LogoutIcon />
              退 出 登 录
            </span>
          </button>
        </div>
      </div>
    </div>
  );
}
