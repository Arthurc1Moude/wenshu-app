import { useState } from 'react';
import { Heart, MessageCircle, Bookmark, MoreHorizontal } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Avatar from '@/components/ui/Avatar';
import type { Post } from '@/types';
import { useStore } from '@/store';
import { formatTime } from '@/utils/format';
import { getApiUrl } from '@/utils/api';

interface PostCardProps {
  post: Post;
  index?: number;
}

function HeartIcon({ filled }: { filled: boolean }) {
  if (filled) {
    return (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="#C23A2B" stroke="#C23A2B" strokeWidth="1.5">
        <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
      </svg>
    );
  }
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
    </svg>
  );
}

function CommentIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z"/>
    </svg>
  );
}

function BookmarkIcon({ filled }: { filled: boolean }) {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill={filled ? '#1A1A1A' : 'none'} stroke="currentColor" strokeWidth="1.5">
      <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
    </svg>
  );
}

function MoreIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="1" fill="currentColor"/>
      <circle cx="19" cy="12" r="1" fill="currentColor"/>
      <circle cx="5" cy="12" r="1" fill="currentColor"/>
    </svg>
  );
}

export default function PostCard({ post, index = 0 }: PostCardProps) {
  const navigate = useNavigate();
  const { toggleLike, toggleCollect, isLoggedIn } = useStore();
  const [showBigHeart, setShowBigHeart] = useState(false);

  const author = post.author;

  const handleDoubleTap = () => {
    if (!isLoggedIn) { navigate('/login'); return; }
    if (!post.isLiked) toggleLike(post.id);
    setShowBigHeart(true);
    setTimeout(() => setShowBigHeart(false), 600);
  };

  const renderImages = () => {
    if (!post.images || post.images.length === 0) return null;
    const count = post.images.length;
    const gridClass = count === 1 ? 'grid-cols-1' : count === 2 ? 'grid-cols-2' : count === 4 ? 'grid-cols-2' : 'grid-cols-3';
    return (
      <div className={`grid ${gridClass} gap-px mt-3 bg-divider`}>
        {post.images.slice(0, 9).map((img, i) => (
          <div key={i} className={`relative overflow-hidden bg-paper ${count === 1 ? 'aspect-[4/3]' : 'aspect-square'}`}>
            <img src={getApiUrl(img)} alt="" className="w-full h-full object-cover" loading="lazy" />
            {i === 0 && post.tags && post.tags.length > 0 && post.tags[0] && (
              <div className="absolute top-2 left-2">
                {post.tags.slice(0, 1).map(tag => (
                  <span key={tag} className="text-[11px] bg-white/90 text-ink px-2 py-0.5 font-serif">#{tag}</span>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    );
  };

  return (
    <motion.article
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ delay: Math.min(index * 0.03, 0.3), duration: 0.3 }}
      className="bg-white px-4 py-4 active:bg-paper transition-colors border-b border-divider"
      onClick={() => navigate(`/post/${post.id}`)}
    >
      <div className="flex items-center gap-3">
        <div onClick={(e) => { e.stopPropagation(); navigate(`/profile`); }}>
          <Avatar src={author?.avatar} username={author?.username} size="md" isVip={author?.isVip} vipLevel={author?.vipLevel} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5">
            <span className="font-serif text-sm text-ink font-medium truncate tracking-wide">{author?.username}</span>
          </div>
          <span className="text-xs text-text-tertiary font-serif">{formatTime(post.createdAt)}</span>
        </div>
        <button onClick={(e) => e.stopPropagation()} className="p-1 -mr-1">
          <MoreIcon />
        </button>
      </div>

      <div className="mt-3 text-[15px] text-ink leading-[1.8] whitespace-pre-wrap break-words font-serif font-normal tracking-wide" onDoubleClick={(e) => { e.stopPropagation(); handleDoubleTap(); }}>
        {post.content}
      </div>

      {renderImages()}

      <div className="flex items-center gap-6 mt-3" onClick={(e) => e.stopPropagation()}>
        <button
          className="flex items-center gap-1.5 transition-opacity active:opacity-60"
          onDoubleClick={handleDoubleTap}
          onClick={() => {
            if (!isLoggedIn) { navigate('/login'); return; }
            toggleLike(post.id);
          }}
        >
          <HeartIcon filled={post.isLiked} />
          <span className={`text-xs font-serif ${post.isLiked ? 'text-seal' : 'text-text-secondary'}`}>{post.likeCount || ''}</span>
        </button>
        <button className="flex items-center gap-1.5 text-text-secondary transition-opacity active:opacity-60" onClick={() => navigate(`/post/${post.id}`)}>
          <CommentIcon />
          <span className="text-xs font-serif">{post.commentCount || ''}</span>
        </button>
        <button className="flex items-center gap-1.5 transition-opacity active:opacity-60" onClick={() => {
          if (!isLoggedIn) { navigate('/login'); return; }
          toggleCollect(post.id);
        }}>
          <BookmarkIcon filled={post.isCollected} />
          <span className={`text-xs font-serif ${post.isCollected ? 'text-ink' : 'text-text-secondary'}`}>{post.collectCount || ''}</span>
        </button>
      </div>

      <AnimatePresence>
        {showBigHeart && (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: [0, 1.3, 1], opacity: [0, 1, 1] }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.6 }}
            style={{ position: 'fixed', left: '50%', top: '40%', transform: 'translate(-50%,-50%)', zIndex: 50, pointerEvents: 'none' }}
          >
            <HeartIcon filled={true} />
            <div style={{ position: 'absolute', inset: 0, transform: 'scale(3)' }}>
              <HeartIcon filled={true} />
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.article>
  );
}
