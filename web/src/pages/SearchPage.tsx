import { useState, useEffect } from 'react';
import { Search as SearchIcon, X, Flame, TrendingUp, Clock, Hash } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import PostCard from '@/components/post/PostCard';
import { useStore } from '@/store';
import TopBar from '@/components/layout/TopBar';
import type { Post } from '@/types';

const hotKeywords = ['文书会', '签到', '新人报到', '每日分享', '活动', '日常', '美食', '旅行'];
const recentKeywords = ['文书币', '兑换码'];

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [searched, setSearched] = useState(false);
  const [results, setResults] = useState<Post[]>([]);
  const { searchPosts } = useStore();
  const navigate = useNavigate();

  const handleSearch = async (q: string) => {
    if (!q.trim()) return;
    setQuery(q);
    setSearched(true);
    const res = await searchPosts(q);
    setResults(res);
  };

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="搜索" showBack />

      <div className="sticky top-14 z-20 bg-white px-4 py-2 border-b border-divider">
        <div className="flex items-center gap-2 bg-gray-100 rounded-full px-4 py-2">
          <SearchIcon className="w-4 h-4 text-text-tertiary" />
          <input
            type="text"
            value={query}
            onChange={e => { setQuery(e.target.value); if (!e.target.value) { setSearched(false); setResults([]); } }}
            onKeyDown={e => e.key === 'Enter' && handleSearch(query)}
            placeholder="搜索帖子、话题、用户"
            className="flex-1 bg-transparent outline-none text-sm"
            autoFocus
          />
          {query && <button onClick={() => { setQuery(''); setSearched(false); setResults([]); }}><X className="w-4 h-4 text-text-tertiary" /></button>}
        </div>
      </div>

      {!searched ? (
        <div className="p-4 space-y-6">
          <div>
            <h3 className="flex items-center gap-2 font-semibold text-sm mb-3">
              <Flame className="w-4 h-4 text-danger" />
              热门搜索
            </h3>
            <div className="flex gap-2 flex-wrap">
              {hotKeywords.map((k, i) => (
                <button
                  key={k}
                  onClick={() => handleSearch(k)}
                  className={`px-3 py-1.5 rounded-full text-sm ${
                    i < 3 ? 'bg-danger/5 text-danger' : 'bg-gray-100 text-text-secondary'
                  } transition-colors active:bg-gray-200`}
                >
                  <span className="inline-flex items-center gap-1">
                    {i < 3 && <TrendingUp className="w-3 h-3" />}
                    {k}
                  </span>
                </button>
              ))}
            </div>
          </div>

          <div>
            <h3 className="flex items-center gap-2 font-semibold text-sm mb-3">
              <Clock className="w-4 h-4" />
              最近搜索
            </h3>
            <div className="space-y-2">
              {recentKeywords.map(k => (
                <button key={k} onClick={() => handleSearch(k)} className="flex items-center gap-3 w-full py-2 text-sm text-text-secondary active:text-black">
                  <Clock className="w-4 h-4 text-text-hint" />
                  {k}
                </button>
              ))}
            </div>
          </div>

          <div>
            <h3 className="flex items-center gap-2 font-semibold text-sm mb-3">
              <Hash className="w-4 h-4" />
              热门话题
            </h3>
            <div className="space-y-3">
              {hotKeywords.slice(0, 5).map((k, i) => (
                <button key={k} onClick={() => handleSearch(k)} className="flex items-center gap-3 w-full text-left">
                  <span className={`w-6 h-6 rounded text-xs font-bold flex items-center justify-center ${
                    i < 3 ? 'bg-danger text-white' : 'bg-gray-100 text-text-tertiary'
                  }`}>{i + 1}</span>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-text-primary">#{k}</p>
                    <p className="text-xs text-text-tertiary">{Math.floor(Math.random() * 1000) + 100}万浏览</p>
                  </div>
                  <Flame className={`w-4 h-4 ${i < 3 ? 'text-danger' : 'text-text-hint'}`} />
                </button>
              ))}
            </div>
          </div>
        </div>
      ) : (
        <div>
          {results.length === 0 ? (
            <div className="text-center py-20 text-text-tertiary text-sm">
              没有找到 "{query}" 相关的内容
            </div>
          ) : (
            <div>
              <p className="px-4 py-2 text-xs text-text-tertiary">找到 {results.length} 条相关结果</p>
              {results.map((p, i) => <PostCard key={p.id} post={p} index={i} />)}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
