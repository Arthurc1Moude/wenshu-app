import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { BookOpen } from 'lucide-react';
import { useStore } from '@/store';

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
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: 'spring', duration: 0.6 }}
          className="w-20 h-20 bg-black rounded-3xl flex items-center justify-center mb-6 shadow-xl"
        >
          <BookOpen className="w-10 h-10 text-white" strokeWidth={2} />
        </motion.div>
        <motion.h1 initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.2 }} className="text-3xl font-bold text-black mb-2">文书</motion.h1>
        <motion.p initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.3 }} className="text-text-secondary text-sm mb-10">记录生活，分享美好</motion.p>

        <motion.form initial={{ y: 30, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.4 }} onSubmit={handleSubmit} className="w-full max-w-sm space-y-4">
          <div>
            <input
              type="text"
              placeholder="用户名"
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full px-4 py-3.5 bg-gray-50 rounded-xl text-text-primary placeholder:text-text-hint focus:bg-white focus:ring-2 focus:ring-black/10 transition-all"
            />
          </div>
          <div>
            <input
              type="password"
              placeholder="密码"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full px-4 py-3.5 bg-gray-50 rounded-xl text-text-primary placeholder:text-text-hint focus:bg-white focus:ring-2 focus:ring-black/10 transition-all"
            />
          </div>
          <button
            type="submit"
            disabled={loading || !username || !password}
            className="w-full btn-black py-3.5 text-base mt-2"
          >
            {loading ? '处理中...' : isLogin ? '登录' : '注册'}
          </button>
          <button
            type="button"
            onClick={() => setIsLogin(!isLogin)}
            className="w-full py-2 text-sm text-text-secondary hover:text-black transition-colors"
          >
            {isLogin ? '没有账号？去注册' : '已有账号？去登录'}
          </button>
        </motion.form>

        {!isLogin && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mt-6 text-xs text-text-tertiary text-center max-w-sm px-4 leading-relaxed">
            <p>🎁 首批注册用户福利：</p>
            <p>第1-5名：100000文书币</p>
            <p>第6-10名：50000文书币</p>
            <p>第11-15名：10000文书币</p>
          </motion.div>
        )}
      </div>
    </div>
  );
}
