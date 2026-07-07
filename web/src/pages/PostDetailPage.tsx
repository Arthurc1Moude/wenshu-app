import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { Heart, MessageCircle, Bookmark, Share2, Send, Crown } from 'lucide-react';
import { useStore } from '@/store';
import { formatTime } from '@/utils/format';
import { motion, AnimatePresence } from 'framer-motion';
import type { Post, Comment } from '@/types';

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { loadPost, loadComments, toggleLike, toggleCollect, addComment, isLoggedIn } = useStore();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentText, setCommentText] = useState('');
  const [showBigHeart, setShowBigHeart] = useState(false);
  const [replyTo, setReplyTo] = useState<Comment | null>(null);
  const lastTapRef = useRef(0);

  useEffect(() => {
    if (!id) return;
    loadPost(id).then(setPost);
    loadComments(id).then(setComments);
  }, [id]);

  const handleDoubleTap = () => {
    if (!post || post.isLiked) {
      setShowBigHeart(true);
      setTimeout(() => setShowBigHeart(false), 600);
      return;
    }
    toggleLike(post.id);
    setShowBigHeart(true);
    setTimeout(() => setShowBigHeart(false), 600);
    if (post) setPost({ ...post, isLiked: true, likeCount: post.likeCount + 1 });
  };

  const handleLike = () => {
    if (!isLoggedIn) { navigate('/login'); return; }
    if (!post) return;
    toggleLike(post.id);
    setPost({ ...post, isLiked: !post.isLiked, likeCount: post.isLiked ? post.likeCount - 1 : post.likeCount + 1 });
  };

  const handleCollect = () => {
    if (!isLoggedIn) { navigate('/login'); return; }
    if (!post) return;
    toggleCollect(post.id);
    setPost({ ...post, isCollected: !post.isCollected, collectCount: post.isCollected ? post.collectCount - 1 : post.collectCount + 1 });
  };

  const handleSendComment = async () => {
    if (!commentText.trim() || !post) return;
    if (!isLoggedIn) { navigate('/login'); return; }
    const c = await addComment(post.id, commentText.trim(), replyTo?.id);
    if (c) {
      setComments([...comments, c]);
      setPost({ ...post, commentCount: post.commentCount + 1 });
      setCommentText('');
      setReplyTo(null);
    }
  };

  if (!post) return (
    <div className="min-h-screen bg-white">
      <TopBar title="帖子" showBack />
      <div className="flex items-center justify-center py-20"><div className="animate-pulse text-text-tertiary">加载中...</div></div>
    </div>
  );

  return (
    <div className="min-h-screen bg-white pb-20">
      <TopBar title="帖子详情" showBack />

      <div className="px-4 py-4">
        <div className="flex items-center gap-3">
          <Avatar src={post.author?.avatar} username={post.author?.username} size="md" isVip={post.author?.isVip} vipLevel={post.author?.vipLevel} />
          <div className="flex-1">
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-sm">{post.author?.username}</span>
              {post.author?.isVip && <Crown className="w-3.5 h-3.5 text-gold" fill="#D4AF37" />}
            </div>
            <span className="text-xs text-text-tertiary">{formatTime(post.createdAt)}</span>
          </div>
          <button className="btn-outline px-4 py-1.5 text-xs">关注</button>
        </div>

        <div
          className="mt-4 text-[15px] leading-relaxed whitespace-pre-wrap"
          onDoubleClick={handleDoubleTap}
          onTouchStart={(e) => {
            const now = Date.now();
            if (now - lastTapRef.current < 300) handleDoubleTap();
            lastTapRef.current = now;
          }}
        >
          {post.content}
        </div>

        {post.tags.length > 0 && (
          <div className="flex gap-2 flex-wrap mt-3">
            {post.tags.map(t => (
              <span key={t} className="text-sm text-blue-600">#{t}</span>
            ))}
          </div>
        )}

        {post.images.length > 0 && (
          <div className="mt-4 space-y-2">
            {post.images.map((img, i) => (
              <img key={i} src={img} alt="" className="w-full rounded-xl object-cover max-h-[500px]" onDoubleClick={handleDoubleTap} />
            ))}
          </div>
        )}

        <div className="flex items-center gap-6 mt-5 pt-4 border-t border-divider">
          <button className="flex items-center gap-1.5 active:scale-90 transition-transform" onClick={handleLike}>
            <Heart className={`w-6 h-6 ${post.isLiked ? 'text-danger fill-danger' : 'text-text-secondary'}`} />
            <span className={`text-sm ${post.isLiked ? 'text-danger' : 'text-text-secondary'}`}>{post.likeCount}</span>
          </button>
          <button className="flex items-center gap-1.5 active:scale-90 transition-transform">
            <MessageCircle className="w-6 h-6 text-text-secondary" />
            <span className="text-sm text-text-secondary">{post.commentCount}</span>
          </button>
          <button className="flex items-center gap-1.5 active:scale-90 transition-transform" onClick={handleCollect}>
            <Bookmark className={`w-6 h-6 ${post.isCollected ? 'text-black fill-black' : 'text-text-secondary'}`} />
            <span className={`text-sm ${post.isCollected ? 'text-black' : 'text-text-secondary'}`}>{post.collectCount}</span>
          </button>
          <div className="flex-1" />
          <button className="active:scale-90 transition-transform"><Share2 className="w-6 h-6 text-text-secondary" /></button>
        </div>
      </div>

      <div className="border-t-8 border-gray-50"></div>

      <div className="px-4 py-3">
        <h3 className="font-semibold text-sm mb-3">评论 {post.commentCount}</h3>
        {comments.length === 0 ? (
          <p className="text-center text-text-tertiary text-sm py-8">暂无评论，快来抢沙发吧~</p>
        ) : (
          <div className="space-y-4">
            {comments.map(c => (
              <div key={c.id} className="flex gap-3">
                <Avatar src={c.author?.avatar} username={c.author?.username} size="sm" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5">
                    <span className="font-medium text-xs text-text-secondary">{c.author?.username}</span>
                    {c.author?.isVip && <Crown className="w-3 h-3 text-gold" fill="#D4AF37" />}
                    <span className="text-xs text-text-hint ml-auto">{formatTime(c.createdAt)}</span>
                  </div>
                  <p className="text-sm text-text-primary mt-0.5">
                    {c.replyToUser && <span className="text-text-tertiary">回复 @{c.replyToUser.username}：</span>}
                    {c.content}
                  </p>
                  <button
                    onClick={() => setReplyTo(c)}
                    className="text-xs text-text-tertiary mt-1 active:text-black"
                  >
                    回复
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <AnimatePresence>
        {showBigHeart && (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: [0, 1.3, 1], opacity: [0, 1, 1] }}
            exit={{ scale: 0.8, opacity: 0 }}
            transition={{ duration: 0.6 }}
            className="fixed inset-0 flex items-center justify-center pointer-events-none z-50"
          >
            <Heart className="w-28 h-28 text-danger fill-danger drop-shadow-2xl" />
          </motion.div>
        )}
      </AnimatePresence>

      <div className="fixed bottom-16 left-0 right-0 bg-white border-t border-divider px-4 py-2 max-w-[480px] mx-auto">
        {replyTo && (
          <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
            <span>回复 @{replyTo.author?.username}</span>
            <button onClick={() => setReplyTo(null)} className="text-text-tertiary">×</button>
          </div>
        )}
        <div className="flex items-center gap-2">
          <input
            type="text"
            placeholder={replyTo ? `回复 @${replyTo.author?.username}` : '说点什么...'}
            value={commentText}
            onChange={e => setCommentText(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSendComment()}
            className="flex-1 bg-gray-100 rounded-full px-4 py-2.5 text-sm outline-none focus:bg-white focus:ring-2 focus:ring-black/5"
          />
          <button
            onClick={handleSendComment}
            disabled={!commentText.trim()}
            className="w-9 h-9 bg-black rounded-full flex items-center justify-center text-white disabled:opacity-40 active:scale-90 transition-transform"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
