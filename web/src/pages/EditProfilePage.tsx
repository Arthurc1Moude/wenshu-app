import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { Camera, Edit3, User, FileText, Save } from 'lucide-react';
import { useStore } from '@/store';
import { getApiUrl } from '@/utils/api';

export default function EditProfilePage() {
  const { currentUser, updateProfile, isLoggedIn } = useStore();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [bio, setBio] = useState('');
  const [avatar, setAvatar] = useState('');
  const [cover, setCover] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isLoggedIn) { navigate('/login'); return; }
    if (currentUser) {
      setUsername(currentUser.username);
      setBio(currentUser.bio || '');
      setAvatar(currentUser.avatar || '');
      setCover(currentUser.cover || '');
    }
  }, [currentUser, isLoggedIn, navigate]);

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => setAvatar(ev.target?.result as string);
      reader.readAsDataURL(file);
    }
  };

  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => setCover(ev.target?.result as string);
      reader.readAsDataURL(file);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    await updateProfile({ username, bio, avatar, cover });
    setSaving(false);
    navigate(-1);
  };

  if (!currentUser) return null;

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="编辑资料" showBack rightElement={
        <button onClick={handleSave} disabled={saving} className="text-sm font-medium text-black active:opacity-60">
          {saving ? '保存中...' : '保存'}
        </button>
      } />

      <div className="relative h-32 bg-gray-200">
        <img src={getApiUrl(cover) || `https://picsum.photos/seed/cover${currentUser.id}/800/320`} alt="" className="w-full h-full object-cover" />
        <label className="absolute inset-0 bg-black/30 flex items-center justify-center cursor-pointer active:bg-black/40 transition-colors">
          <Camera className="w-6 h-6 text-white" />
          <input type="file" accept="image/*" className="hidden" onChange={handleCoverChange} />
        </label>
      </div>

      <div className="px-4 -mt-10 relative z-10">
        <div className="flex justify-center">
          <label className="relative cursor-pointer">
            <div className="ring-4 ring-white rounded-full">
              <Avatar src={avatar} username={username} size="xl" isVip={currentUser.isVip} vipLevel={currentUser.vipLevel} />
            </div>
            <div className="absolute bottom-0 right-0 w-8 h-8 bg-black rounded-full flex items-center justify-center ring-2 ring-white">
              <Camera className="w-4 h-4 text-white" />
            </div>
            <input type="file" accept="image/*" className="hidden" onChange={handleAvatarChange} />
          </label>
        </div>

        <div className="mt-6 space-y-5">
          <div>
            <label className="text-xs text-text-secondary font-medium flex items-center gap-1 mb-2">
              <User className="w-3.5 h-3.5" />
              用户名
            </label>
            <input
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full py-2.5 border-b border-divider outline-none focus:border-black text-sm"
              placeholder="请输入用户名"
              maxLength={20}
            />
          </div>

          <div>
            <label className="text-xs text-text-secondary font-medium flex items-center gap-1 mb-2">
              <FileText className="w-3.5 h-3.5" />
              个人简介
            </label>
            <textarea
              value={bio}
              onChange={e => setBio(e.target.value)}
              className="w-full py-2.5 border-b border-divider outline-none focus:border-black text-sm resize-none"
              placeholder="介绍一下自己吧~"
              rows={3}
              maxLength={100}
            />
            <p className="text-right text-xs text-text-hint mt-1">{bio.length}/100</p>
          </div>

          <button onClick={handleSave} disabled={saving} className="w-full btn-black py-3.5 mt-4 flex items-center justify-center gap-2">
            <Save className="w-4 h-4" />
            {saving ? '保存中...' : '保存资料'}
          </button>
        </div>
      </div>
    </div>
  );
}
