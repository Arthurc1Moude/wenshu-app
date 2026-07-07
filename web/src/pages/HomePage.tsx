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
    <div className="pb-24">
      <TopBar title="首页" showSearch showNotifications={isLoggedIn} />

      <div className="sticky top-14 z-20 bg-white border-b border-divider">
        <div className="flex gap-6 px-4 max-w-[480px] mx-auto">
          {(['new', 'hot'] as const).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`py-3 relative transition-colors ${tab === t ? 'text-black font-semibold' : 'text-text-tertiary'}`}
            >
              {t === 'new' ? '最新' : '最热'}
              {tab === t && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-0.5 bg-black rounded-full" />}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="p-4 space-y-4">
          {[1,2,3,4].map(i => (
            <div key={i} className="animate-pulse">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-200 rounded-full" />
                <div className="flex-1 space-y-2">
                  <div className="h-3 bg-gray-200 rounded w-24" />
                  <div className="h-2 bg-gray-100 rounded w-16" />
                </div>
              </div>
              <div className="mt-3 space-y-2">
                <div className="h-3 bg-gray-100 rounded w-full" />
                <div className="h-3 bg-gray-100 rounded w-4/5" />
              </div>
              <div className="grid grid-cols-3 gap-1 mt-3">
                <div className="aspect-square bg-gray-200 rounded-lg" />
                <div className="aspect-square bg-gray-200 rounded-lg" />
                <div className="aspect-square bg-gray-200 rounded-lg" />
              </div>
            </div>
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-text-tertiary">
          <p className="text-sm">还没有帖子</p>
          <p className="text-xs mt-1">点击下方 + 发布第一篇帖子吧</p>
        </div>
      ) : (
        <div>
          {posts.map((post, i) => <PostCard key={post.id} post={post} index={i} />)}
        </div>
      )}
    </div>
  );
}
