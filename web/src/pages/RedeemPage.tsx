import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
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
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="兑 换 码" showBack />

      <div className="p-6 max-w-[480px] mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="border border-divider-heavy p-8 bg-white text-center"
        >
          <div className="seal-stamp text-sm mx-auto mb-4 px-3 py-1">兑</div>
          <h2 className="text-xl font-serif font-bold text-ink tracking-widest mb-2">兑换雅礼</h2>
          <p className="text-sm font-serif text-text-secondary tracking-wide mb-6">凭码兑换文书币或文会资格</p>
          <div className="w-8 h-px bg-divider-heavy mx-auto" />
        </motion.div>

        <div className="mt-6 border border-divider p-6 bg-white">
          <label className="label-text block mb-2 tracking-widest">兑换码</label>
          <input
            type="text"
            value={code}
            onChange={e => setCode(e.target.value.toUpperCase())}
            placeholder="请输入兑换码"
            className="w-full py-3 text-lg font-serif tracking-[0.3em] outline-none border border-divider-heavy bg-paper text-center focus:border-ink transition-colors"
            style={{ borderRadius: '2px' }}
            maxLength={12}
          />

          <button
            onClick={handleSubmit}
            disabled={!code.trim() || loading}
            className="w-full mt-6 bg-ink text-white font-serif py-3.5 tracking-[0.3em] transition-colors active:bg-ink-light disabled:opacity-40"
          >
            {loading ? '兑换中...' : '兑 换'}
          </button>

          {result && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className={`mt-6 p-5 border text-center ${
                result.success ? 'border-ink bg-paper' : 'border-seal bg-red-50/30'
              }`}
              style={{ borderRadius: '2px' }}
            >
              {result.success ? (
                result.vipGranted ? (
                  <div>
                    <div className="seal-stamp text-sm mx-auto mb-3 px-3 py-1">文会</div>
                    <p className="font-serif font-bold text-seal tracking-wide">兑换成功 · 文书会已激活</p>
                    <p className="text-sm font-serif text-text-secondary mt-2 tracking-wide">有效期一年，尽享会员雅礼</p>
                  </div>
                ) : (
                  <div>
                    <div className="w-10 h-10 border border-ink flex items-center justify-center mx-auto mb-3 font-serif text-ink font-bold">礼</div>
                    <p className="font-serif font-bold text-ink tracking-wide">兑换成功</p>
                    <p className="text-sm font-serif text-text-secondary mt-2 tracking-wide">
                      获赠 <span className="text-ink font-medium">{result.coins}</span> 文书币
                    </p>
                  </div>
                )
              ) : (
                <p className="font-serif text-seal tracking-wide">兑换码无效或已过期</p>
              )}
            </motion.div>
          )}
        </div>

        <div className="mt-6 text-xs text-text-tertiary space-y-1.5 px-1 font-serif tracking-wide leading-relaxed">
          <p>· 兑换码不区分大小写，请正确输入</p>
          <p>· 每码每人仅可使用一次</p>
          <p>· 加入QQ群（702404026）参与定时发放活动</p>
          <p>· 部分兑换码可免费获赠文书会资格</p>
        </div>
      </div>
    </div>
  );
}
