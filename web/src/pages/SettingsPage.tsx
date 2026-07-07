import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';
import { ChevronRight, Ticket, Bell, Moon, Shield, HelpCircle, Info, LogOut, Coins, QrCode, Users, Trash2, Download } from 'lucide-react';

const SettingGroup = ({ children }: { children: React.ReactNode }) => (
  <div className="bg-white rounded-2xl overflow-hidden mx-4 mb-3 divide-y divide-divider">{children}</div>
);

const SettingItem = ({ icon, label, value, onClick, danger, right }: {
  icon: React.ReactNode; label: string; value?: string; onClick?: () => void; danger?: boolean; right?: React.ReactNode;
}) => (
  <button onClick={onClick} className={`w-full flex items-center gap-3 px-4 py-3.5 active:bg-gray-50 transition-colors text-left ${danger ? 'text-danger' : 'text-text-primary'}`}>
    <span className={danger ? 'text-danger' : 'text-black'}>{icon}</span>
    <span className="flex-1 text-sm font-medium">{label}</span>
    {value && <span className="text-xs text-text-tertiary">{value}</span>}
    {right || <ChevronRight className="w-4 h-4 text-text-hint" />}
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
    <div className="min-h-screen bg-gray-50 pb-24">
      <TopBar title="设置" showBack />

      <div className="pt-4">
        <SettingGroup>
          <SettingItem
            icon={<Ticket className="w-5 h-5" />}
            label="兑换码"
            onClick={() => navigate('/redeem')}
          />
          <SettingItem
            icon={<Coins className="w-5 h-5" />}
            label="文书币"
            value={currentUser ? `${currentUser.wenshuCoin}` : '0'}
            onClick={() => {}}
          />
        </SettingGroup>

        <SettingGroup>
          <SettingItem icon={<Bell className="w-5 h-5" />} label="消息通知" onClick={() => {}} />
          <SettingItem icon={<Moon className="w-5 h-5" />} label="深色模式" right={
            <span className="text-xs text-text-hint">跟随系统</span>
          } onClick={() => {}} />
          <SettingItem icon={<Download className="w-5 h-5" />} label="自动下载图片" right={
            <span className="text-xs text-text-hint">WiFi下</span>
          } onClick={() => {}} />
          <SettingItem icon={<Trash2 className="w-5 h-5" />} label="清除缓存" onClick={clearCache} />
        </SettingGroup>

        <SettingGroup>
          <SettingItem icon={<Shield className="w-5 h-5" />} label="隐私与安全" onClick={() => {}} />
          <SettingItem icon={<Users className="w-5 h-5" />} label="关于文书会" onClick={() => {}} />
          <SettingItem icon={<HelpCircle className="w-5 h-5" />} label="帮助与反馈" onClick={() => {}} />
          <SettingItem icon={<Info className="w-5 h-5" />} label="关于文书APP" value="v1.0.0" onClick={() => {}} />
        </SettingGroup>

        <div className="px-4 pt-4">
          <button
            onClick={handleLogout}
            className="w-full py-3.5 bg-white rounded-2xl text-danger font-medium text-sm active:bg-gray-50 transition-colors"
          >
            <span className="flex items-center justify-center gap-2">
              <LogOut className="w-4 h-4" />
              退出登录
            </span>
          </button>
        </div>
      </div>
    </div>
  );
}
