import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { Gift, Coins, Check, Crown } from 'lucide-react';
import { useStore } from '@/store';
import { motion } from 'framer-motion';

export default function RedeemPage() {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{ success: boolean; coins: number; vipGranted?: boolean } | null>(null);
  const { redeemCode } = useStore();
  const navigate = useNavigate();

  const handleSubmit = async () => {
    if (!code.trim()) return;
    setLoading(true);
    setResult(null);
    const r = await redeemCode(code.trim().toUpperCase());
    setLoading(false);
    if (r) setResult({ success: true, coins: r.coins, vipGranted: r.vipGranted });
    else setResult({ success: false, coins: 0 });
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-24">
      <TopBar title="兑换码" showBack />

      <div className="p-4">
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="bg-gradient-to-br from-black to-gray-800 rounded-3xl p-6 text-white text-center relative overflow-hidden"
        >
          <div className="absolute inset-0 bg-gradient-to-t from-gold/10 to-transparent" />
          <div className="relative">
            <div className="w-16 h-16 bg-gold/20 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Gift className="w-8 h-8 text-gold" />
            </div>
            <h2 className="text-xl font-bold mb-2">输入兑换码</h2>
            <p className="text-sm text-white/60">兑换码可在QQ群定时活动中获取</p>
          </div>
        </motion.div>

        <div className="mt-6 bg-white rounded-2xl p-4">
          <label className="text-xs text-text-secondary font-medium">兑换码</label>
          <input
            type="text"
            value={code}
            onChange={e => setCode(e.target.value.toUpperCase())}
            placeholder="请输入兑换码"
            className="w-full py-3 text-lg font-mono tracking-widest outline-none border-b-2 border-divider focus:border-black transition-colors text-center"
            maxLength={12}
          />

          <button
            onClick={handleSubmit}
            disabled={!code.trim() || loading}
            className="w-full mt-6 btn-black py-3.5 font-medium"
          >
            {loading ? '兑换中...' : '立即兑换'}
          </button>

          {result && (
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              className={`mt-4 p-4 rounded-xl text-center ${
                result.success ? 'bg-green-50' : 'bg-red-50'
              }`}
            >
              {result.success ? (
                result.vipGranted ? (
                  <div>
                    <div className="w-14 h-14 bg-gold/20 rounded-full flex items-center justify-center mx-auto mb-2">
                      <Crown className="w-7 h-7 text-gold" />
                    </div>
                    <p className="font-bold text-gold">🎉 兑换成功！文书会VIP已激活</p>
                    <p className="text-sm text-text-secondary mt-1">有效期一年，尽享会员特权</p>
                  </div>
                ) : (
                  <div>
                    <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-2">
                      <Check className="w-6 h-6 text-green-600" />
                    </div>
                    <p className="font-bold text-green-800">兑换成功！</p>
                    <p className="text-sm text-green-700 flex items-center justify-center gap-1 mt-1">
                      获得 <Coins className="w-4 h-4 text-gold" /> {result.coins} 文书币
                    </p>
                  </div>
                )
              ) : (
                <p className="text-red-600 font-medium">兑换码无效或已过期</p>
              )}
            </motion.div>
          )}
        </div>

        <div className="mt-4 text-xs text-text-tertiary space-y-1 px-2">
          <p>• 兑换码区分大小写，请正确输入</p>
          <p>• 每个兑换码仅可使用一次</p>
          <p>• 加入QQ群(702404026)参与定时抢码活动</p>
          <p>• 部分兑换码可免费开通文书会VIP</p>
        </div>
      </div>
    </div>
  );
}
