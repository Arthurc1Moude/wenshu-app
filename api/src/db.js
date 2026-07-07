import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DATA_DIR = path.join(__dirname, '..', 'data');

function ensureDataDir() {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
}

function readJSON(filename, defaultValue) {
  ensureDataDir();
  const filePath = path.join(DATA_DIR, filename);
  try {
    if (!fs.existsSync(filePath)) {
      fs.writeFileSync(filePath, JSON.stringify(defaultValue, null, 2));
      return defaultValue;
    }
    const content = fs.readFileSync(filePath, 'utf-8');
    return JSON.parse(content);
  } catch (e) {
    console.error(`Error reading ${filename}:`, e);
    return defaultValue;
  }
}

function writeJSON(filename, data) {
  ensureDataDir();
  const filePath = path.join(DATA_DIR, filename);
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
}

export function getUsers() { return readJSON('users.json', []); }
export function saveUsers(users) { writeJSON('users.json', users); }

export function getPosts() { return readJSON('posts.json', []); }
export function savePosts(posts) { writeJSON('posts.json', posts); }

export function getComments() { return readJSON('comments.json', []); }
export function saveComments(comments) { writeJSON('comments.json', comments); }

export function getActivities() { return readJSON('activities.json', []); }
export function saveActivities(activities) { writeJSON('activities.json', activities); }

export function getNotifications() { return readJSON('notifications.json', []); }
export function saveNotifications(notifications) { writeJSON('notifications.json', notifications); }

export function getConversations() { return readJSON('conversations.json', []); }
export function saveConversations(conversations) { writeJSON('conversations.json', conversations); }

export function getMessages() { return readJSON('messages.json', []); }
export function saveMessages(messages) { writeJSON('messages.json', messages); }

export function getRedeemCodes() { return readJSON('redeem_codes.json', []); }
export function saveRedeemCodes(codes) { writeJSON('redeem_codes.json', codes); }

export function getRedeemRecords() { return readJSON('redeem_records.json', []); }
export function saveRedeemRecords(records) { writeJSON('redeem_records.json', records); }

export function getLikes() { return readJSON('likes.json', []); }
export function saveLikes(likes) { writeJSON('likes.json', likes); }

export function getCollects() { return readJSON('collects.json', []); }
export function saveCollects(collects) { writeJSON('collects.json', collects); }

export function getFollows() { return readJSON('follows.json', []); }
export function saveFollows(follows) { writeJSON('follows.json', follows); }

export function getRegisterCount() {
  const meta = readJSON('meta.json', { registerCount: 0 });
  return meta.registerCount || 0;
}
export function incrementRegisterCount() {
  const count = getRegisterCount() + 1;
  writeJSON('meta.json', { registerCount: count });
  return count;
}

export function seedInitialData() {
  const activities = getActivities();
  if (activities.length === 0) {
    const now = Date.now();
    const seedActivities = [
      {
        id: 'act_1',
        title: '夏日生活记录',
        cover: 'https://picsum.photos/seed/act1/800/450',
        description: '分享你的夏日生活瞬间，带话题#夏日生活 发布帖子即可参与！',
        hashtag: '夏日生活',
        rewardCoins: 500,
        participantCount: 1234,
        participants: [],
        status: 'active',
        startDate: now - 86400000 * 3,
        endDate: now + 86400000 * 7,
        rules: [
          '带话题#夏日生活 发布原创帖子',
          '内容需与夏日生活相关',
          '活动结束后评选10名优质内容，每人获得500文书币'
        ]
      },
      {
        id: 'act_2',
        title: '日常打卡挑战',
        cover: 'https://picsum.photos/seed/act2/800/450',
        description: '连续7天发布日常帖子，坚持打卡赢取文书币奖励！',
        hashtag: '日常打卡',
        rewardCoins: 200,
        participantCount: 5678,
        participants: [],
        status: 'active',
        startDate: now - 86400000,
        endDate: now + 86400000 * 14,
        rules: [
          '带话题#日常打卡 连续7天发帖',
          '每天至少发布1篇帖子',
          '完成打卡即可获得200文书币'
        ]
      },
      {
        id: 'act_3',
        title: '读书分享月',
        cover: 'https://picsum.photos/seed/act3/800/450',
        description: '分享你最近在读的好书，带话题#读书分享 参与活动。',
        hashtag: '读书分享',
        rewardCoins: 300,
        participantCount: 892,
        participants: [],
        status: 'upcoming',
        startDate: now + 86400000 * 2,
        endDate: now + 86400000 * 30,
        rules: [
          '带话题#读书分享 发布读书笔记',
          '包含书籍封面照片更佳',
          '优质分享将获得300文书币奖励'
        ]
      },
      {
        id: 'act_4',
        title: '春季摄影大赛',
        cover: 'https://picsum.photos/seed/act4/800/450',
        description: '记录春天的美好瞬间，摄影作品征集活动已圆满结束。',
        hashtag: '春季摄影',
        rewardCoins: 800,
        participantCount: 3421,
        participants: [],
        status: 'ended',
        startDate: now - 86400000 * 30,
        endDate: now - 86400000 * 5,
        rules: [
          '带话题#春季摄影 发布摄影作品',
          '作品需为原创',
          '已评选出获奖名单'
        ]
      }
    ];
    saveActivities(seedActivities);
  }

  const redeemCodes = getRedeemCodes();
  const now = Date.now();
  const allCodes = [
    { code: 'WENSHU2024', coinValue: 200, rewardType: 'coin', description: '新用户欢迎礼包', validUntil: now + 86400000 * 365 },
    { code: 'QQGROUP702', coinValue: 300, rewardType: 'coin', description: '加入QQ群专属奖励', validUntil: now + 86400000 * 365 },
    { code: 'DAILYSIGN', coinValue: 50, rewardType: 'coin', description: '每日签到额外奖励', validUntil: now + 86400000 * 30 },
    { code: 'VIPWELCOME', coinValue: 500, rewardType: 'coin', description: '开通文书会奖励', validUntil: now + 86400000 * 365 },
    { code: 'SUMMER2024', coinValue: 100, rewardType: 'coin', description: '夏日活动兑换码', validUntil: now + 86400000 * 60 },
    { code: 'WELCOME100', coinValue: 100, rewardType: 'coin', description: '欢迎使用文书APP', validUntil: now + 86400000 * 365 },
    { code: 'A3k320', coinValue: 2000, rewardType: 'coin', description: '限时兑换码-2000文书币', validUntil: now + 86400000 * 90 },
    { code: '56gh70', coinValue: 1000, rewardType: 'coin', description: '限时兑换码-1000文书币', validUntil: now + 86400000 * 90 },
    { code: 'Kp9m2X', coinValue: 0, rewardType: 'vip', description: '免费开通文书会VIP', validUntil: now + 86400000 * 90 },
    { code: 'Qr4n7V', coinValue: 0, rewardType: 'vip', description: '免费开通文书会VIP', validUntil: now + 86400000 * 90 },
    { code: 'Zt8w3Y', coinValue: 0, rewardType: 'vip', description: '免费开通文书会VIP', validUntil: now + 86400000 * 90 },
  ];
  let codesUpdated = false;
  allCodes.forEach(c => {
    const existing = redeemCodes.find(r => r.code.toUpperCase() === c.code.toUpperCase());
    if (!existing) {
      redeemCodes.push({ ...c, code: c.code, usedBy: [] });
      codesUpdated = true;
    } else {
      existing.coinValue = c.coinValue;
      existing.rewardType = c.rewardType;
      existing.description = c.description;
      codesUpdated = true;
    }
  });
  if (codesUpdated) {
    saveRedeemCodes(redeemCodes);
  }

  const conversations = getConversations();
  if (conversations.length === 0) {
    saveConversations([]);
  }
}
