import type { RedeemCode } from '@/types';

const now = Date.now();
const year = 365 * 24 * 60 * 60 * 1000;

export const redeemCodes: RedeemCode[] = [
  {
    code: 'WENSHU2024',
    coinValue: 500,
    description: '新用户福利',
    validUntil: now + year,
  },
  {
    code: 'QQGROUP702',
    coinValue: 200,
    description: 'QQ群专属福利',
    validUntil: now + year,
  },
  {
    code: 'VIP099',
    coinValue: 100,
    description: '会员专享',
    validUntil: now + year,
  },
  {
    code: 'DAILY100',
    coinValue: 100,
    description: '每日兑换',
    validUntil: now + year,
  },
  {
    code: 'FIRST500',
    coinValue: 500,
    description: '首发兑换码',
    validUntil: now + year,
  },
  {
    code: 'ACTIVITY1',
    coinValue: 300,
    description: '活动奖励',
    validUntil: now + year,
  },
];
