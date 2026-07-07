import { useEffect } from 'react';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';
import { Crown, Coins, Star, Zap, Gift, BadgeCheck, Sparkles, Check } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const privileges = [
  { icon: Coins, title: '每日额外文书币', desc: '签到额外+10币' },
  { icon: Crown, title: '金色皇冠标识', desc: '昵称旁专属皇冠' },
  { icon: BadgeCheck, title: 'VIP专属徽章', desc: '个人主页展示' },
  { icon: Gift, title: '开通奖励500币', desc: '立即到账' },
  { icon: Star, title: '活动优先参与', desc: '限定活动优先权' },
  { icon: Zap, title: '经验加成', desc: '升级速度加快' },
];

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
  const expNeeded = level * 100;
  const expProgress = level > 0 ? Math.min((exp / expNeeded) * 100, 100) : 0;

  const handlePurchase = () => {
    if (confirm('确认花费 ¥0.99 开通文书会VIP？（模拟支付）')) {
      purchaseVip();
    }
  };

  return (
    <div className="pb-24">
      <TopBar title="文书会" />

      <div className="p-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="vip-card-bg rounded-3xl p-6 text-white relative overflow-hidden"
        >
          <div className="absolute -top-10 -right-10 w-40 h-40 bg-gold/20 rounded-full blur-3xl" />
          <div className="absolute -bottom-10 -left-10 w-32 h-32 bg-gold/10 rounded-full blur-3xl" />
          <div className="relative">
            <div className="flex items-center gap-2 mb-4">
              <Crown className="w-7 h-7 text-gold" fill="#D4AF37" />
              <span className="text-lg font-bold text-gold">文书会</span>
              {isVip && <span className="text-xs bg-gold text-black px-2 py-0.5 rounded-full font-bold">Lv.{level}</span>}
            </div>

            {isVip ? (
              <div>
                <h2 className="text-2xl font-bold mb-1">文书会 Lv.{level}</h2>
                <p className="text-white/70 text-sm mb-4">尊享会员特权中</p>
                <div className="bg-white/10 rounded-xl p-3 backdrop-blur-sm">
                  <div className="flex justify-between text-xs mb-2">
                    <span className="text-white/70">经验值</span>
                    <span>{exp} / {expNeeded}</span>
                  </div>
                  <div className="h-2 bg-white/20 rounded-full overflow-hidden">
                    <motion.div
                      initial={{ width: 0 }}
                      animate={{ width: `${expProgress}%` }}
                      transition={{ duration: 0.8, delay: 0.3 }}
                      className="h-full gold-gradient rounded-full"
                    />
                  </div>
                  {level < 100 && <p className="text-xs text-white/60 mt-2">距离 Lv.{level + 1} 还需 {expNeeded - exp} 经验</p>}
                  {level >= 100 && <p className="text-xs text-gold mt-2">🎉 已满级！</p>}
                </div>
              </div>
            ) : (
              <div>
                <h2 className="text-2xl font-bold mb-1">开通文书会</h2>
                <p className="text-white/70 text-sm mb-4">尊享6大会员特权</p>
                <button onClick={handlePurchase} className="gold-gradient text-black font-bold py-3 px-6 rounded-xl shadow-lg active:scale-95 transition-transform w-full">
                  ¥0.99 立即开通
                </button>
                <p className="text-xs text-white/50 text-center mt-2">开通即送500文书币</p>
              </div>
            )}
          </div>
        </motion.div>

        <h3 className="font-bold text-base mt-6 mb-3 flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-gold" />
          会员特权
        </h3>
        <div className="grid grid-cols-2 gap-3">
          {privileges.map((p, i) => {
            const Icon = p.icon;
            return (
              <motion.div
                key={p.title}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className={`p-4 rounded-2xl border ${isVip ? 'border-gold/30 bg-gold/5' : 'border-divider bg-white'}`}
              >
                <Icon className={`w-6 h-6 mb-2 ${isVip ? 'text-gold' : 'text-black'}`} />
                <p className="font-semibold text-sm text-text-primary">{p.title}</p>
                <p className="text-xs text-text-secondary mt-0.5">{p.desc}</p>
                {isVip && <Check className="w-4 h-4 text-gold absolute" />}
              </motion.div>
            );
          })}
        </div>

        {isVip && (
          <div className="mt-6 bg-gradient-to-r from-gold/10 to-gold/5 rounded-2xl p-4 border border-gold/20">
            <h3 className="font-bold text-sm mb-2 flex items-center gap-2"><Gift className="w-4 h-4 text-gold" /> 升级福利预览</h3>
            <div className="space-y-2 text-xs text-text-secondary">
              <p>Lv.10：专属头像框</p>
              <p>Lv.30：评论区红色ID</p>
              <p>Lv.50：自定义个人主页封面</p>
              <p>Lv.100：终身成就勋章</p>
            </div>
          </div>
        )}

        {!isVip && (
          <div className="mt-6 text-center">
            <p className="text-xs text-text-tertiary">会员有效期：自开通之日起365天</p>
            <p className="text-xs text-text-tertiary">自动续费可随时关闭（模拟支付）</p>
          </div>
        )}
      </div>
    </div>
  );
}
