import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { useStore } from '@/store';
import { formatTime } from '@/utils/format';
import { motion, AnimatePresence } from 'framer-motion';
import type { Post, Comment } from '@/types';
import { getApiUrl } from '@/utils/api';

function SendIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <line x1="22" y1="2" x2="11" y2="13"/>
      <polygon points="22 2 15 22 11 13 2 9 22 2"/>
    </svg>
  );
}

function HeartIcon({ filled }: { filled: boolean }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill={filled ? '#C23A2B' : 'none'} stroke={filled ? '#C23A2B' : 'currentColor'} strokeWidth="1.5">
      <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
    </svg>
  );
}

function CommentDetailIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z"/>
    </svg>
  );
}

function BookmarkDetailIcon({ filled }: { filled: boolean }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill={filled ? '#1A1A1A' : 'none'} stroke="currentColor" strokeWidth="1.5">
      <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/>
    </svg>
  );
}

function ShareIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="18" cy="5" r="3"/>
      <circle cx="6" cy="12" r="3"/>
      <circle cx="18" cy="19" r="3"/>
      <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
      <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
    </svg>
  );
}

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
    if (!post) return;
    if (!post.isLiked) {
      toggleLike(post.id);
      setPost({ ...post, isLiked: true, likeCount: post.likeCount + 1 });
    }
    setShowBigHeart(true);
    setTimeout(() => setShowBigHeart(false), 600);
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
      <TopBar title="帖 子" showBack />
      <div className="flex items-center justify-center py-20"><div className="text-text-tertiary font-serif">加载中...</div></div>
    </div>
  );

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="帖 子" showBack />

      <div className="px-4 py-4 max-w-[480px] mx-auto">
        <div className="flex items-center gap-3">
          <Avatar src={post.author?.avatar} username={post.author?.username} size="md" isVip={post.author?.isVip} vipLevel={post.author?.vipLevel} />
          <div className="flex-1">
            <div className="flex items-center gap-1.5">
              <span className="font-serif text-sm text-ink font-medium tracking-wide">{post.author?.username}</span>
            </div>
            <span className="text-xs text-text-tertiary font-serif">{formatTime(post.createdAt)}</span>
          </div>
          <button className="border border-ink text-ink px-4 py-1.5 text-xs font-serif tracking-wider active:bg-paper transition-colors" style={{ borderRadius: '2px' }}>关注</button>
        </div>

        <div
          className="mt-4 text-[15px] text-ink leading-[1.9] whitespace-pre-wrap font-serif tracking-wide"
          onDoubleClick={handleDoubleTap}
          onTouchStart={(e) => {
            const now = Date.now();
            if (now - lastTapRef.current < 300) handleDoubleTap();
            lastTapRef.current = now;
          }}
        >
          {post.content}
        </div>

        {post.tags && post.tags.length > 0 && (
          <div className="flex gap-2 flex-wrap mt-3">
            {post.tags.map(t => (
              <span key={t} className="text-sm text-text-secondary font-serif">#{t}</span>
            ))}
          </div>
        )}

        {post.images && post.images.length > 0 && (
          <div className="mt-4 space-y-1 bg-divider">
            {post.images.map((img, i) => (
              <img key={i} src={getApiUrl(img)} alt="" className="w-full object-cover max-h-[500px]" onDoubleClick={handleDoubleTap} />
            ))}
          </div>
        )}

        <div className="flex items-center gap-6 mt-5 pt-4 border-t border-divider">
          <button className="flex items-center gap-1.5 transition-opacity active:opacity-60" onClick={handleLike}>
            <HeartIcon filled={post.isLiked} />
            <span className={`text-sm font-serif ${post.isLiked ? 'text-seal' : 'text-text-secondary'}`}>{post.likeCount}</span>
          </button>
          <button className="flex items-center gap-1.5 text-text-secondary transition-opacity active:opacity-60">
            <CommentDetailIcon />
            <span className="text-sm font-serif">{post.commentCount}</span>
          </button>
          <button className="flex items-center gap-1.5 transition-opacity active:opacity-60" onClick={handleCollect}>
            <BookmarkDetailIcon filled={post.isCollected} />
            <span className={`text-sm font-serif ${post.isCollected ? 'text-ink' : 'text-text-secondary'}`}>{post.collectCount}</span>
          </button>
          <div className="flex-1" />
          <button className="text-text-secondary transition-opacity active:opacity-60"><ShareIcon /></button>
        </div>
      </div>

      <div className="h-2 bg-paper" />

      <div className="px-4 py-4 max-w-[480px] mx-auto">
        <h3 className="font-serif text-sm text-ink font-medium mb-4 tracking-wider">评 论 {post.commentCount}</h3>
        {comments.length === 0 ? (
          <p className="text-center text-text-tertiary text-sm font-serif py-8 tracking-wide">尚无评论，快来开篇吧</p>
        ) : (
          <div className="space-y-4">
            {comments.map(c => (
              <div key={c.id} className="flex gap-3">
                <Avatar src={c.author?.avatar} username={c.author?.username} size="sm" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5">
                    <span className="font-serif text-xs text-text-secondary">{c.author?.username}</span>
                    <span className="text-xs text-text-hint ml-auto font-serif">{formatTime(c.createdAt)}</span>
                  </div>
                  <p className="text-sm text-ink mt-1 font-serif leading-relaxed">
                    {c.replyToUser && <span className="text-text-tertiary">回复 @{c.replyToUser.username}：</span>}
                    {c.content}
                  </p>
                  <button
                    onClick={() => setReplyTo(c)}
                    className="text-xs text-text-tertiary mt-1 font-serif active:text-ink"
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
            exit={{ opacity: 0 }}
            transition={{ duration: 0.6 }}
            className="fixed inset-0 flex items-center justify-center pointer-events-none z-50"
          >
            <div style={{ transform: 'scale(3)' }}><HeartIcon filled={true} /></div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="fixed bottom-16 left-0 right-0 bg-white border-t border-divider px-4 py-2 max-w-[480px] mx-auto">
        {replyTo && (
          <div className="flex items-center justify-between text-xs text-text-secondary mb-1 font-serif">
            <span>回复 @{replyTo.author?.username}</span>
            <button onClick={() => setReplyTo(null)} className="text-text-tertiary text-lg leading-none">×</button>
          </div>
        )}
        <div className="flex items-center gap-2">
          <input
            type="text"
            placeholder={replyTo ? `回复 @${replyTo.author?.username}` : '写下你的评论...'}
            value={commentText}
            onChange={e => setCommentText(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSendComment()}
            className="flex-1 bg-paper border border-divider px-4 py-2.5 text-sm font-serif outline-none focus:border-ink transition-colors"
            style={{ borderRadius: '2px' }}
          />
          <button
            onClick={handleSendComment}
            disabled={!commentText.trim()}
            className="w-10 h-10 bg-ink flex items-center justify-center text-white disabled:opacity-40 transition-opacity active:opacity-80"
            style={{ borderRadius: '2px' }}
          >
            <SendIcon />
          </button>
        </div>
      </div>
    </div>
  );
}
