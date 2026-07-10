import { useEffect } from 'react';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const privileges = [
  { title: '每日添笔', desc: '签到额外获赠十文书币' },
  { title: '朱印标识', desc: '昵称旁专属印章标记' },
  { title: '文会徽章', desc: '个人主页展示会员徽章' },
  { title: '入会赠礼', desc: '开通即赠五百文书币' },
  { title: '雅集优先', desc: '限定活动优先参与权' },
  { title: '进学加速', desc: '学识经验积累加速' },
];

function SealVip() {
  return (
    <div className="inline-flex items-center justify-center w-16 h-16 border-[3px] border-seal text-seal font-serif font-bold text-2xl transform -rotate-3 tracking-widest">
      文会
    </div>
  );
}

export default function VipPage() {
  const { currentUser, purchaseVip, isLoggedIn } = useStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) navigate('/login');
  }, [isLoggedIn, navigate]);

  if (!currentUser) return null;

  const isVip = currentUser.isVip;
  const level = currentUser.vipLevel || 0;
  const exp = currentUser.vipExp || 0;
  const expNeeded = level * 100 || 100;
  const expProgress = level > 0 ? Math.min((exp / expNeeded) * 100, 100) : 0;

  const handlePurchase = () => {
    if (confirm('确认花费 ¥0.99 加入文书会？（模拟支付）')) {
      purchaseVip();
    }
  };

  return (
    <div className="pb-24 bg-white min-h-screen">
      <TopBar title="文 书 会" />

      <div className="p-6 max-w-[480px] mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="border border-divider-heavy p-8 bg-white relative"
        >
          <div className="absolute top-4 right-4">
            <SealVip />
          </div>
          <div className="mb-6">
            <p className="text-text-secondary font-serif text-sm tracking-widest mb-1">WENSHU SOCIETY</p>
            <h2 className="text-3xl font-serif font-bold text-ink tracking-[0.2em] mb-2">文书会</h2>
            <div className="w-12 h-px bg-ink mb-4" />
            {isVip ? (
              <p className="font-serif text-text-secondary tracking-wide">以文会友，以书载道</p>
            ) : (
              <p className="font-serif text-text-secondary tracking-wide">入会享六重雅礼</p>
            )}
          </div>

          {isVip ? (
            <div>
              <div className="border-t border-divider pt-4">
                <div className="flex items-baseline gap-2 mb-1">
                  <span className="text-4xl font-serif font-bold text-ink">{level}</span>
                  <span className="font-serif text-text-secondary text-sm tracking-wider">第 {level} 阶</span>
                </div>
                <p className="font-serif text-text-secondary text-sm mb-4 tracking-wide">会员尊享中</p>
                <div>
                  <div className="flex justify-between text-xs mb-2 font-serif">
                    <span className="text-text-tertiary">学识</span>
                    <span className="text-text-secondary">{exp} / {expNeeded}</span>
                  </div>
                  <div className="h-1 bg-divider w-full">
                    <motion.div
                      initial={{ width: 0 }}
                      animate={{ width: `${expProgress}%` }}
                      transition={{ duration: 0.8, delay: 0.3 }}
                      className="h-full bg-ink"
                    />
                  </div>
                  {level < 100 && <p className="text-xs text-text-tertiary mt-2 font-serif">进阶至第 {level + 1} 阶，尚需 {expNeeded - exp} 学识</p>}
                  {level >= 100 && <p className="text-xs text-seal mt-2 font-serif">已至大成之境</p>}
                </div>
              </div>
            </div>
          ) : (
            <div className="border-t border-divider pt-6">
              <p className="font-serif text-sm text-text-secondary mb-4 leading-relaxed">
                文书会乃文人雅士相聚之所，入会者可享专属礼遇，与同道中人共论诗文。
              </p>
              <button onClick={handlePurchase} className="w-full bg-ink text-white font-serif py-4 text-base tracking-[0.3em] transition-colors active:bg-ink-light">
                九 毛 九 分 · 入 会
              </button>
              <p className="text-xs text-text-tertiary text-center mt-3 font-serif">入会即赠五百文书币</p>
            </div>
          )}
        </motion.div>

        <h3 className="font-serif text-base mt-8 mb-4 text-ink font-medium tracking-widest flex items-center gap-3">
          <span className="w-6 h-px bg-divider-heavy" />
          会 员 雅 礼
          <span className="w-6 h-px bg-divider-heavy" />
        </h3>
        <div className="space-y-0 border-t border-divider">
          {privileges.map((p, i) => (
            <motion.div
              key={p.title}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.05 }}
              className="flex items-start gap-4 py-4 border-b border-divider"
            >
              <div className="w-8 h-8 border border-ink flex items-center justify-center shrink-0 font-serif text-ink text-sm font-bold mt-0.5" style={{ borderRadius: '1px' }}>
                {i + 1}
              </div>
              <div>
                <p className="font-serif text-ink font-medium tracking-wide">{p.title}</p>
                <p className="text-sm font-serif text-text-secondary mt-0.5">{p.desc}</p>
              </div>
              {isVip && (
                <div className="ml-auto seal-stamp text-[10px] px-2 shrink-0">已享</div>
              )}
            </motion.div>
          ))}
        </div>

        {isVip && (
          <div className="mt-8 border border-divider p-4 bg-paper">
            <h3 className="font-serif text-sm mb-3 text-ink font-medium tracking-wider">进阶礼单</h3>
            <div className="space-y-2 text-sm font-serif text-text-secondary">
              <p>第十阶 · 专属头像纹饰</p>
              <p>第三十阶 · 评论朱字题名</p>
              <p>第五十阶 · 自定主页封面</p>
              <p>第一百阶 · 大成勋章</p>
            </div>
          </div>
        )}

        {!isVip && (
          <div className="mt-8 text-center space-y-1">
            <p className="text-xs text-text-tertiary font-serif tracking-wide">会期一年，自入会之日起算</p>
            <p className="text-xs text-text-tertiary font-serif tracking-wide">本页为模拟支付，仅供体验</p>
          </div>
        )}
      </div>
    </div>
  );
}
