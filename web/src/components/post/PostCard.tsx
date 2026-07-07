import { useState } from 'react';
import { Heart, MessageCircle, Bookmark, MoreHorizontal, Crown, Play } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Avatar from '@/components/ui/Avatar';
import type { Post } from '@/types';
import { useStore } from '@/store';
import { formatTime } from '@/utils/format';

interface PostCardProps {
  post: Post;
  index?: number;
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
      <div className={`grid ${gridClass} gap-1 mt-3`}>
        {post.images.slice(0, 9).map((img, i) => (
          <div key={i} className={`relative overflow-hidden bg-gray-100 ${count === 1 ? 'aspect-[4/3] rounded-xl' : 'aspect-square rounded-lg'}`}>
            <img src={img} alt="" className="w-full h-full object-cover" loading="lazy" />
            {i === 0 && post.tags && post.tags.length > 0 && post.tags[0] && (
              <div className="absolute top-2 left-2 flex items-center gap-1">
                {post.tags.slice(0, 1).map(tag => (
                  <span key={tag} className="text-[10px] bg-black/50 text-white px-1.5 py-0.5 rounded-full backdrop-blur-sm">#{tag}</span>
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
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05, duration: 0.3 }}
      className="bg-white px-4 py-3 active:bg-gray-50 transition-colors"
      onClick={() => navigate(`/post/${post.id}`)}
    >
      <div className="flex items-center gap-3">
        <div onClick={(e) => { e.stopPropagation(); navigate(`/profile`); }}>
          <Avatar src={author?.avatar} username={author?.username} size="md" isVip={author?.isVip} vipLevel={author?.vipLevel} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-sm text-text-primary truncate">{author?.username}</span>
            {author?.isVip && <Crown className="w-3.5 h-3.5 text-gold" fill="#D4AF37" />}
          </div>
          <span className="text-xs text-text-tertiary">{formatTime(post.createdAt)}</span>
        </div>
        <button onClick={(e) => e.stopPropagation()} className="p-1 -mr-1">
          <MoreHorizontal className="w-5 h-5 text-text-tertiary" />
        </button>
      </div>

      <div className="mt-2 text-[15px] text-text-primary leading-relaxed whitespace-pre-wrap break-words" onDoubleClick={(e) => { e.stopPropagation(); handleDoubleTap(); }}>
        {post.content}
      </div>

      {renderImages()}

      <div className="flex items-center gap-5 mt-3" onClick={(e) => e.stopPropagation()}>
        <button
          className="flex items-center gap-1 active:scale-90 transition-transform"
          onDoubleClick={handleDoubleTap}
          onClick={() => {
            if (!isLoggedIn) { navigate('/login'); return; }
            toggleLike(post.id);
          }}
        >
          <Heart className={`w-5 h-5 transition-colors ${post.isLiked ? 'text-danger fill-danger' : 'text-text-secondary'}`} />
          <span className={`text-xs ${post.isLiked ? 'text-danger' : 'text-text-secondary'}`}>{post.likeCount || ''}</span>
        </button>
        <button className="flex items-center gap-1 active:scale-90 transition-transform" onClick={() => navigate(`/post/${post.id}`)}>
          <MessageCircle className="w-5 h-5 text-text-secondary" />
          <span className="text-xs text-text-secondary">{post.commentCount || ''}</span>
        </button>
        <button className="flex items-center gap-1 active:scale-90 transition-transform" onClick={() => {
          if (!isLoggedIn) { navigate('/login'); return; }
          toggleCollect(post.id);
        }}>
          <Bookmark className={`w-5 h-5 transition-colors ${post.isCollected ? 'text-black fill-black' : 'text-text-secondary'}`} />
          <span className={`text-xs ${post.isCollected ? 'text-black' : 'text-text-secondary'}`}>{post.collectCount || ''}</span>
        </button>
      </div>

      <AnimatePresence>
        {showBigHeart && (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: [0, 1.3, 1], opacity: [0, 1, 1] }}
            exit={{ scale: 0.8, opacity: 0 }}
            transition={{ duration: 0.6 }}
            className="absolute inset-0 flex items-center justify-center pointer-events-none"
            style={{ position: 'fixed', left: '50%', top: '40%', transform: 'translate(-50%,-50%)', zIndex: 50 }}
          >
            <Heart className="w-24 h-24 text-danger fill-danger drop-shadow-2xl" />
          </motion.div>
        )}
      </AnimatePresence>

      <div className="divider mt-3" />
    </motion.article>
  );
}
