import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { useStore } from '@/store';
import { Coins, Calendar, Edit3, Heart, Bookmark, FileText, Users, UserPlus, Check, ChevronRight } from 'lucide-react';
import PostCard from '@/components/post/PostCard';
import { formatNumber } from '@/utils/format';

export default function ProfilePage() {
  const { currentUser, isLoggedIn, signIn, loadPostsByUser, loadLikedPosts, loadSavedPosts, joinQQGroup } = useStore();
  const navigate = useNavigate();
  const [myPosts, setMyPosts] = useState<any[]>([]);
  const [likedPosts, setLikedPosts] = useState<any[]>([]);
  const [savedPosts, setSavedPosts] = useState<any[]>([]);
  const [tab, setTab] = useState<'posts' | 'liked' | 'saved'>('posts');
  const [signAnimating, setSignAnimating] = useState(false);

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }
    if (currentUser) {
      loadPostsByUser(currentUser.id).then(setMyPosts);
    }
  }, [isLoggedIn, currentUser, navigate]);

  useEffect(() => {
    if (!isLoggedIn) return;
    if (tab === 'liked' && likedPosts.length === 0) {
      loadLikedPosts().then(setLikedPosts);
    }
    if (tab === 'saved' && savedPosts.length === 0) {
      loadSavedPosts().then(setSavedPosts);
    }
  }, [tab, isLoggedIn]);

  if (!currentUser) return null;

  const handleSignIn = async () => {
    if (currentUser.isSignedInToday) return;
    setSignAnimating(true);
    await signIn();
    setTimeout(() => setSignAnimating(false), 1000);
  };

  const handleJoinQQ = async () => {
    await joinQQGroup();
  };

  const todayStr = new Date().toDateString();
  const isSignedToday = currentUser.lastSignInDate === new Date().toISOString().split('T')[0] || currentUser.isSignedInToday;

  return (
    <div className="pb-24">
      <TopBar title="我的" showSettings />

      <div className="relative h-40 bg-gradient-to-br from-gray-100 to-gray-200">
        <img src={currentUser.cover || `https://picsum.photos/seed/cover${currentUser.id}/800/320`} alt="cover" className="w-full h-full object-cover" />
      </div>

      <div className="px-4 -mt-10 relative">
        <div className="flex items-end justify-between">
          <div className="ring-4 ring-white rounded-full">
            <Avatar src={currentUser.avatar} username={currentUser.username} size="xl" isVip={currentUser.isVip} vipLevel={currentUser.vipLevel} />
          </div>
          <button
            onClick={() => navigate('/edit-profile')}
            className="btn-outline px-4 py-1.5 text-sm flex items-center gap-1"
          >
            <Edit3 className="w-3.5 h-3.5" />
            编辑资料
          </button>
        </div>

        <div className="mt-3">
          <div className="flex items-center gap-1.5">
            <h2 className="text-xl font-bold text-text-primary">{currentUser.username}</h2>
            {currentUser.isVip && <span className="text-xs bg-gold/10 text-gold px-2 py-0.5 rounded-full font-medium">文书会 Lv.{currentUser.vipLevel}</span>}
          </div>
          <p className="text-sm text-text-secondary mt-1">{currentUser.bio || '这个人很懒，什么都没写~'}</p>
        </div>

        <div className="mt-4 bg-gray-50 rounded-2xl p-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className={`w-10 h-10 rounded-full bg-gold/10 flex items-center justify-center ${signAnimating ? 'animate-coin-spin' : ''}`}>
              <Coins className="w-5 h-5 text-gold" />
            </div>
            <div>
              <p className="text-xs text-text-secondary">文书币</p>
              <p className="text-lg font-bold text-text-primary">{formatNumber(currentUser.wenshuCoin)}</p>
            </div>
          </div>
          <button
            onClick={handleSignIn}
            disabled={isSignedToday}
            className={`px-5 py-2 rounded-xl font-medium text-sm transition-all ${
              isSignedToday ? 'bg-gray-200 text-text-tertiary' : 'bg-black text-white active:scale-95'
            }`}
          >
            {isSignedToday ? <span className="flex items-center gap-1"><Check className="w-3.5 h-3.5" />已签到</span> : <span className="flex items-center gap-1"><Calendar className="w-3.5 h-3.5" />每日签到</span>}
          </button>
        </div>

        <div className="mt-4 flex divide-x divide-divider bg-white rounded-2xl py-3 border border-divider">
          {[
            { label: '关注', value: currentUser.followingCount, onClick: () => {} },
            { label: '粉丝', value: currentUser.followersCount, onClick: () => {} },
            { label: '获赞与收藏', value: currentUser.likesCount, onClick: () => {} },
          ].map((s, i) => (
            <button key={i} className="flex-1 text-center" onClick={s.onClick}>
              <p className="text-lg font-bold text-text-primary">{formatNumber(s.value)}</p>
              <p className="text-xs text-text-secondary mt-0.5">{s.label}</p>
            </button>
          ))}
        </div>

        {!currentUser.joinedQQGroup && (
          <button
            onClick={handleJoinQQ}
            className="mt-3 w-full py-3 rounded-xl bg-gold/10 text-gold font-medium text-sm flex items-center justify-center gap-2 active:bg-gold/20 transition-colors"
          >
            <UserPlus className="w-4 h-4" />
            加入QQ群(702404026)领200文书币
          </button>
        )}

        <div className="flex gap-4 mt-6 border-b border-divider">
          {([
            { key: 'posts', label: '帖子', icon: FileText },
            { key: 'liked', label: '点赞', icon: Heart },
            { key: 'saved', label: '收藏', icon: Bookmark },
          ] as const).map(t => {
            const Icon = t.icon;
            return (
              <button
                key={t.key}
                onClick={() => setTab(t.key)}
                className={`flex items-center gap-1 pb-3 relative text-sm transition-colors ${
                  tab === t.key ? 'text-black font-semibold' : 'text-text-tertiary'
                }`}
              >
                <Icon className="w-4 h-4" />
                {t.label}
                {tab === t.key && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-5 h-0.5 bg-black rounded-full" />}
              </button>
            );
          })}
        </div>
      </div>

      <div className="mt-2">
        {tab === 'posts' && (
          myPosts.length > 0 ? myPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm">还没有发布过帖子</div>
          )
        )}
        {tab === 'liked' && (
          likedPosts.length > 0 ? likedPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm">暂无点赞的帖子</div>
          )
        )}
        {tab === 'saved' && (
          savedPosts.length > 0 ? savedPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm">暂无收藏的帖子</div>
          )
        )}
      </div>
    </div>
  );
}
