import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useStore } from '@/store';

function SealIcon() {
  return (
    <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="8" y="8" width="48" height="48" rx="2" stroke="#C23A2B" strokeWidth="3"/>
      <text x="32" y="42" textAnchor="middle" fill="#C23A2B" fontSize="32" fontWeight="700" fontFamily="Noto Serif SC, serif">文</text>
    </svg>
  );
}

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;
    setLoading(true);
    const ok = isLogin ? await login(username.trim(), password) : await register(username.trim(), password);
    setLoading(false);
    if (ok) navigate('/');
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <div className="flex-1 flex flex-col items-center justify-center px-8 -mt-10">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-8"
        >
          <SealIcon />
        </motion.div>
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1, duration: 0.5 }}
          className="text-4xl font-serif font-bold text-ink mb-2 tracking-widest"
        >
          文书
        </motion.h1>
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-text-secondary text-sm font-serif mb-12 tracking-widest"
        >
          以文会友，以书载道
        </motion.p>

        <motion.form
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3, duration: 0.4 }}
          onSubmit={handleSubmit}
          className="w-full max-w-sm space-y-0"
        >
          <div className="border border-divider-heavy border-b-0">
            <input
              type="text"
              placeholder="用户名"
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full px-4 py-4 bg-white text-ink font-serif placeholder:text-text-hint focus:bg-paper transition-colors border-0 outline-none"
            />
          </div>
          <div className="border border-divider-heavy">
            <input
              type="password"
              placeholder="密码"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full px-4 py-4 bg-white text-ink font-serif placeholder:text-text-hint focus:bg-paper transition-colors border-0 outline-none"
            />
          </div>
          <button
            type="submit"
            disabled={loading || !username || !password}
            className="w-full bg-ink text-white font-serif py-4 text-base tracking-[0.3em] mt-6 transition-colors active:bg-ink-light disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {loading ? '处理中...' : isLogin ? '登 录' : '注 册'}
          </button>
          <button
            type="button"
            onClick={() => setIsLogin(!isLogin)}
            className="w-full py-4 text-sm text-text-secondary font-serif tracking-wide hover:text-ink transition-colors"
          >
            {isLogin ? '没有账号？注册新账号' : '已有账号？立即登录'}
          </button>
        </motion.form>

        {!isLogin && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mt-8 text-xs text-text-tertiary text-center max-w-sm px-4 leading-loose font-serif tracking-wide">
            <div className="flex items-center gap-2 justify-center mb-2">
              <span className="w-8 h-px bg-divider-heavy"></span>
              <span className="seal-stamp text-[10px] px-2">新友福利</span>
              <span className="w-8 h-px bg-divider-heavy"></span>
            </div>
            <p>首批注册用户获赠文书币</p>
            <p>第一至五名 · 十万文书币</p>
            <p>第六至十名 · 五万文书币</p>
            <p>第十一至十五名 · 一万文书币</p>
          </motion.div>
        )}
      </div>
    </div>
  );
}
