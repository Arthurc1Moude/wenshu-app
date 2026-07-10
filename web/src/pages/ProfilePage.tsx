import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { useStore } from '@/store';
import PostCard from '@/components/post/PostCard';
import { formatNumber } from '@/utils/format';
import { getApiUrl } from '@/utils/api';

function EditIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
    </svg>
  );
}

function CoinIcon({ spin }: { spin?: boolean }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#C23A2B" strokeWidth="1.5" className={spin ? 'animate-spin' : ''}>
      <circle cx="12" cy="12" r="9"/>
      <text x="12" y="16" textAnchor="middle" fontSize="10" fill="#C23A2B" stroke="none" fontFamily="serif" fontWeight="700">文</text>
    </svg>
  );
}

function CalendarIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <rect x="3" y="4" width="18" height="18" rx="0" ry="0"/>
      <line x1="16" y1="2" x2="16" y2="6"/>
      <line x1="8" y1="2" x2="8" y2="6"/>
      <line x1="3" y1="10" x2="21" y2="10"/>
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <polyline points="20 6 9 17 4 12"/>
    </svg>
  );
}

function PostsTabIcon() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="16" y1="13" x2="8" y2="13"/>
      <line x1="16" y1="17" x2="8" y2="17"/>
    </svg>
  );
}

function HeartTabIcon({ filled }: { filled?: boolean }) {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill={filled ? '#C23A2B' : 'none'} stroke={filled ? '#C23A2B' : 'currentColor'} strokeWidth="1.5">
      <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
    </svg>
  );
}

function BookmarkTabIcon({ filled }: { filled?: boolean }) {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill={filled ? '#1A1A1A' : 'none'} stroke="currentColor" strokeWidth="1.5">
      <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
    </svg>
  );
}

export default function ProfilePage() {
  const { currentUser, isLoggedIn, signIn, loadPostsByUser, loadLikedPosts, loadSavedPosts } = useStore();
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

  const isSignedToday = currentUser.lastSignInDate === new Date().toISOString().split('T')[0] || currentUser.isSignedInToday;

  return (
    <div className="pb-24 bg-white min-h-screen">
      <TopBar title="我 的" showSettings />

      <div className="relative h-36 bg-paper">
        <img src={getApiUrl(currentUser.cover) || `https://picsum.photos/seed/cover${currentUser.id}/800/320`} alt="cover" className="w-full h-full object-cover opacity-70" />
        <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, transparent 40%, #FFFFFF 100%)' }} />
      </div>

      <div className="px-4 -mt-12 relative max-w-[480px] mx-auto">
        <div className="flex items-end justify-between">
          <div className="border-2 border-white">
            <Avatar src={currentUser.avatar} username={currentUser.username} size="xl" isVip={currentUser.isVip} vipLevel={currentUser.vipLevel} />
          </div>
          <button
            onClick={() => navigate('/edit-profile')}
            className="border border-ink text-ink px-4 py-1.5 text-sm font-serif tracking-wider flex items-center gap-1 active:bg-paper transition-colors"
            style={{ borderRadius: '2px' }}
          >
            <EditIcon />
            编辑资料
          </button>
        </div>

        <div className="mt-3">
          <div className="flex items-center gap-2 flex-wrap">
            <h2 className="text-xl font-serif text-ink font-medium tracking-wide">{currentUser.username}</h2>
            {currentUser.isVip && (
              <span className="seal-stamp text-xs">
                文 书 会 Lv.{currentUser.vipLevel}
              </span>
            )}
          </div>
          <p className="text-sm text-text-secondary mt-1 font-serif leading-relaxed">{currentUser.bio || '此人无言，自有风骨'}</p>
        </div>

        <div className="mt-4 bg-paper p-4 flex items-center justify-between border border-divider">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 border border-seal/30 flex items-center justify-center bg-white">
              <CoinIcon spin={signAnimating} />
            </div>
            <div>
              <p className="text-xs text-text-secondary font-serif tracking-wide">文书币</p>
              <p className="text-lg font-serif text-ink font-medium">{formatNumber(currentUser.wenshuCoin)}</p>
            </div>
          </div>
          <button
            onClick={handleSignIn}
            disabled={isSignedToday}
            className={`px-5 py-2 font-serif text-sm tracking-wider transition-colors ${
              isSignedToday ? 'bg-paper border border-divider text-text-tertiary' : 'bg-ink text-white active:bg-ink-light'
            }`}
            style={{ borderRadius: '2px' }}
          >
            {isSignedToday ? <span className="flex items-center gap-1"><CheckIcon />已签到</span> : <span className="flex items-center gap-1"><CalendarIcon />每日签到</span>}
          </button>
        </div>

        <div className="mt-4 flex border-t border-b border-divider bg-white">
          {[
            { label: '关注', value: currentUser.followingCount },
            { label: '粉丝', value: currentUser.followersCount },
            { label: '获赞与收藏', value: currentUser.likesCount },
          ].map((s, i) => (
            <div key={i} className={`flex-1 text-center py-3 ${i > 0 ? 'border-l border-divider' : ''}`}>
              <p className="text-lg font-serif text-ink font-medium">{formatNumber(s.value)}</p>
              <p className="text-xs text-text-secondary mt-0.5 font-serif tracking-wide">{s.label}</p>
            </div>
          ))}
        </div>

        <div className="flex gap-6 mt-6 border-b border-divider">
          {([
            { key: 'posts', label: '帖 子' },
            { key: 'liked', label: '点 赞' },
            { key: 'saved', label: '收 藏' },
          ] as const).map(t => {
            const active = tab === t.key;
            return (
              <button
                key={t.key}
                onClick={() => setTab(t.key)}
                className={`flex items-center gap-1.5 pb-3 relative text-sm font-serif tracking-wider transition-colors ${
                  active ? 'text-ink font-medium' : 'text-text-tertiary'
                }`}
              >
                {t.key === 'posts' && <PostsTabIcon />}
                {t.key === 'liked' && <HeartTabIcon filled={active} />}
                {t.key === 'saved' && <BookmarkTabIcon filled={active} />}
                {t.label}
                {active && <span className="absolute bottom-0 left-0 w-full h-px bg-ink" />}
              </button>
            );
          })}
        </div>
      </div>

      <div className="mt-2 max-w-[480px] mx-auto">
        {tab === 'posts' && (
          myPosts.length > 0 ? myPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm font-serif">尚未执笔，敬请期待</div>
          )
        )}
        {tab === 'liked' && (
          likedPosts.length > 0 ? likedPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm font-serif">尚无点赞</div>
          )
        )}
        {tab === 'saved' && (
          savedPosts.length > 0 ? savedPosts.map((p, i) => <PostCard key={p.id} post={p} index={i} />) : (
            <div className="text-center py-16 text-text-tertiary text-sm font-serif">尚无收藏</div>
          )
        )}
      </div>
    </div>
  );
}
