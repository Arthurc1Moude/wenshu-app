import { useState, useEffect } from 'react';
import TopBar from '@/components/layout/TopBar';
import PostCard from '@/components/post/PostCard';
import { useStore } from '@/store';

export default function HomePage() {
  const { posts, loadPosts, isLoggedIn } = useStore();
  const [tab, setTab] = useState<'new' | 'hot'>('new');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      await loadPosts(tab);
      setLoading(false);
    })();
  }, [tab]);

  return (
    <div className="pb-20">
      <TopBar title="文 书" showSearch showNotifications={isLoggedIn} />

      <div className="sticky top-14 z-20 bg-white border-b border-divider">
        <div className="flex max-w-[480px] mx-auto">
          {(['new', 'hot'] as const).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`flex-1 py-3.5 text-center font-serif tracking-[0.2em] transition-colors relative ${tab === t ? 'text-ink font-medium' : 'text-text-tertiary'}`}
            >
              {t === 'new' ? '新 帖' : '热 帖'}
              {tab === t && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-8 h-px bg-ink" />}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="p-4 space-y-6">
          {[1,2,3].map(i => (
            <div key={i} className="animate-pulse">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-divider" style={{ borderRadius: '2px' }} />
                <div className="flex-1 space-y-2">
                  <div className="h-3 bg-divider w-24" />
                  <div className="h-2 bg-surface w-16" />
                </div>
              </div>
              <div className="mt-3 space-y-2">
                <div className="h-3 bg-surface w-full" />
                <div className="h-3 bg-surface w-4/5" />
              </div>
            </div>
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-text-tertiary">
          <p className="text-sm font-serif tracking-widest">尚无帖子</p>
          <p className="text-xs mt-2 font-serif">点下方「＋」发布第一篇</p>
        </div>
      ) : (
        <div>
          {posts.map((post, i) => <PostCard key={post.id} post={post} index={i} />)}
        </div>
      )}
    </div>
  );
}
