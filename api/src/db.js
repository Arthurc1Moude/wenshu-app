import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import {
  initPostgres, initTables, pool,
  pgGetUsers, pgSaveUser,
  pgGetPosts, pgSavePost,
  pgGetComments, pgSaveComment,
  pgGetLikes, pgSaveLike, pgDeleteLike,
  pgGetCollects, pgSaveCollect, pgDeleteCollect,
  pgGetFollows, pgSaveFollow, pgDeleteFollow,
  pgGetNotifications, pgSaveNotification,
  pgGetConversations, pgSaveConversation,
  pgGetMessages, pgSaveMessage, pgMarkMessagesRead,
  pgGetActivities, pgSaveActivity,
  pgGetRedeemCodes, pgSaveRedeemCode,
  pgGetRedeemRecords, pgSaveRedeemRecord,
  pgGetRegisterCount, pgIncrementRegisterCount,
  pgGetBlacklists, pgSaveBlacklist, pgDeleteBlacklist,
  pgSaveVerificationCode, pgFindValidVerificationCode, pgMarkVerificationCodeUsed,
  pgFindUserByPhone
} from './db-postgres.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DATA_DIR = path.join(__dirname, '..', 'data');

let usePostgres = !!process.env.DATABASE_URL;

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

export async function initDB() {
  if (usePostgres) {
    console.log('📦 Using PostgreSQL database');
    initPostgres();
    await initTables();
  } else {
    console.log('📁 Using JSON file storage (local dev mode)');
  }
}

let memCache = null;
function getCache() {
  if (usePostgres) return null;
  if (!memCache) {
    memCache = {
      users: readJSON('users.json', []),
      posts: readJSON('posts.json', []),
      comments: readJSON('comments.json', []),
      activities: readJSON('activities.json', []),
      notifications: readJSON('notifications.json', []),
      conversations: readJSON('conversations.json', []),
      messages: readJSON('messages.json', []),
      redeemCodes: readJSON('redeem_codes.json', []),
      redeemRecords: readJSON('redeem_records.json', []),
      likes: readJSON('likes.json', []),
      collects: readJSON('collects.json', []),
      follows: readJSON('follows.json', []),
      blacklists: readJSON('blacklists.json', []),
      verificationCodes: readJSON('verification_codes.json', []),
      meta: readJSON('meta.json', { registerCount: 0 }),
    };
  }
  return memCache;
}

function writeUsers(users) { writeJSON('users.json', users); }
function writePosts(posts) { writeJSON('posts.json', posts); }
function writeComments(comments) { writeJSON('comments.json', comments); }
function writeActivities(activities) { writeJSON('activities.json', activities); }
function writeNotifications(notifications) { writeJSON('notifications.json', notifications); }
function writeConversations(conversations) { writeJSON('conversations.json', conversations); }
function writeMessages(messages) { writeJSON('messages.json', messages); }
function writeRedeemCodes(codes) { writeJSON('redeem_codes.json', codes); }
function writeRedeemRecords(records) { writeJSON('redeem_records.json', records); }
function writeLikes(likes) { writeJSON('likes.json', likes); }
function writeCollects(collects) { writeJSON('collects.json', collects); }
function writeFollows(follows) { writeJSON('follows.json', follows); }
function writeBlacklists(blacklists) { writeJSON('blacklists.json', blacklists); }
function writeVerificationCodes(codes) { writeJSON('verification_codes.json', codes); }

export async function getUsers() {
  if (usePostgres) return pgGetUsers();
  return getCache().users;
}

export async function saveUsers(users) {
  if (usePostgres) {
    for (const u of users) await pgSaveUser(u);
    return;
  }
  getCache().users = users;
  writeUsers(users);
}

export async function saveUser(user) {
  if (usePostgres) return pgSaveUser(user);
  const cache = getCache();
  const idx = cache.users.findIndex(u => u.id === user.id);
  if (idx !== -1) cache.users[idx] = user;
  else cache.users.push(user);
  writeUsers(cache.users);
}

export async function getPosts() {
  if (usePostgres) return pgGetPosts();
  return getCache().posts;
}

export async function savePosts(posts) {
  if (usePostgres) {
    for (const p of posts) await pgSavePost(p);
    return;
  }
  getCache().posts = posts;
  writePosts(posts);
}

export async function savePost(post) {
  if (usePostgres) return pgSavePost(post);
  const cache = getCache();
  const idx = cache.posts.findIndex(p => p.id === post.id);
  if (idx !== -1) cache.posts[idx] = post;
  else cache.posts.unshift(post);
  writePosts(cache.posts);
}

export async function getComments() {
  if (usePostgres) return pgGetComments();
  return getCache().comments;
}

export async function saveComments(comments) {
  if (usePostgres) {
    for (const c of comments) await pgSaveComment(c);
    return;
  }
  getCache().comments = comments;
  writeComments(comments);
}

export async function saveComment(comment) {
  if (usePostgres) return pgSaveComment(comment);
  const cache = getCache();
  const idx = cache.comments.findIndex(c => c.id === comment.id);
  if (idx !== -1) cache.comments[idx] = comment;
  else cache.comments.push(comment);
  writeComments(cache.comments);
}

export async function getLikes() {
  if (usePostgres) return pgGetLikes();
  return getCache().likes;
}

export async function saveLikes(likes) {
  if (usePostgres) {
    for (const l of likes) await pgSaveLike(l);
    return;
  }
  getCache().likes = likes;
  writeLikes(likes);
}

export async function addLike(like) {
  if (usePostgres) return pgSaveLike(like);
  const cache = getCache();
  cache.likes.push(like);
  writeLikes(cache.likes);
}

export async function removeLike(postId, userId) {
  if (usePostgres) return pgDeleteLike(postId, userId);
  const cache = getCache();
  cache.likes = cache.likes.filter(l => !(l.postId === postId && l.userId === userId));
  writeLikes(cache.likes);
}

export async function getCollects() {
  if (usePostgres) return pgGetCollects();
  return getCache().collects;
}

export async function saveCollects(collects) {
  if (usePostgres) {
    for (const c of collects) await pgSaveCollect(c);
    return;
  }
  getCache().collects = collects;
  writeCollects(collects);
}

export async function addCollect(collect) {
  if (usePostgres) return pgSaveCollect(collect);
  const cache = getCache();
  cache.collects.push(collect);
  writeCollects(cache.collects);
}

export async function removeCollect(postId, userId) {
  if (usePostgres) return pgDeleteCollect(postId, userId);
  const cache = getCache();
  cache.collects = cache.collects.filter(c => !(c.postId === postId && c.userId === userId));
  writeCollects(cache.collects);
}

export async function getFollows() {
  if (usePostgres) return pgGetFollows();
  return getCache().follows;
}

export async function saveFollows(follows) {
  if (usePostgres) {
    for (const f of follows) await pgSaveFollow(f);
    return;
  }
  getCache().follows = follows;
  writeFollows(follows);
}

export async function addFollow(follow) {
  if (usePostgres) return pgSaveFollow(follow);
  const cache = getCache();
  cache.follows.push(follow);
  writeFollows(cache.follows);
}

export async function removeFollow(followerId, followingId) {
  if (usePostgres) return pgDeleteFollow(followerId, followingId);
  const cache = getCache();
  cache.follows = cache.follows.filter(f => !(f.followerId === followerId && f.followingId === followingId));
  writeFollows(cache.follows);
}

export async function getBlacklists() {
  if (usePostgres) return pgGetBlacklists();
  return getCache().blacklists;
}

export async function addBlacklist(bl) {
  if (usePostgres) return pgSaveBlacklist(bl);
  const cache = getCache();
  cache.blacklists.push(bl);
  writeBlacklists(cache.blacklists);
}

export async function removeBlacklist(userId, blockedUserId) {
  if (usePostgres) return pgDeleteBlacklist(userId, blockedUserId);
  const cache = getCache();
  cache.blacklists = cache.blacklists.filter(b => !(b.userId === userId && b.blockedUserId === blockedUserId));
  writeBlacklists(cache.blacklists);
}

export async function getActivities() {
  if (usePostgres) return pgGetActivities();
  return getCache().activities;
}

export async function saveActivities(activities) {
  if (usePostgres) {
    for (const a of activities) await pgSaveActivity(a);
    return;
  }
  getCache().activities = activities;
  writeActivities(activities);
}

export async function saveActivity(act) {
  if (usePostgres) return pgSaveActivity(act);
  const cache = getCache();
  const idx = cache.activities.findIndex(a => a.id === act.id);
  if (idx !== -1) cache.activities[idx] = act;
  else cache.activities.push(act);
  writeActivities(cache.activities);
}

export async function getNotifications() {
  if (usePostgres) return pgGetNotifications();
  return getCache().notifications;
}

export async function saveNotifications(notifications) {
  if (usePostgres) {
    for (const n of notifications) await pgSaveNotification(n);
    return;
  }
  getCache().notifications = notifications;
  writeNotifications(notifications);
}

export async function addNotification(notif) {
  if (usePostgres) return pgSaveNotification(notif);
  const cache = getCache();
  cache.notifications.unshift(notif);
  writeNotifications(cache.notifications);
}

export async function markNotificationsRead(userId) {
  if (usePostgres) {
    const notifs = await pgGetNotifications();
    for (const n of notifs) {
      if (n.userId === userId && !n.isRead) {
        n.isRead = true;
        await pgSaveNotification(n);
      }
    }
    return;
  }
  const cache = getCache();
  cache.notifications.forEach(n => { if (n.userId === userId) n.isRead = true; });
  saveNotifications(cache.notifications);
}

export async function getConversations() {
  if (usePostgres) return pgGetConversations();
  return getCache().conversations;
}

export async function saveConversations(conversations) {
  if (usePostgres) {
    for (const c of conversations) await pgSaveConversation(c);
    return;
  }
  getCache().conversations = conversations;
  writeConversations(conversations);
}

export async function saveConversation(conv) {
  if (usePostgres) return pgSaveConversation(conv);
  const cache = getCache();
  const idx = cache.conversations.findIndex(c => c.id === conv.id);
  if (idx !== -1) cache.conversations[idx] = conv;
  else cache.conversations.push(conv);
  writeConversations(cache.conversations);
}

export async function getMessages() {
  if (usePostgres) return pgGetMessages();
  return getCache().messages;
}

export async function saveMessages(messages) {
  if (usePostgres) {
    for (const m of messages) await pgSaveMessage(m);
    return;
  }
  getCache().messages = messages;
  writeMessages(messages);
}

export async function saveMessage(msg) {
  if (usePostgres) return pgSaveMessage(msg);
  const cache = getCache();
  const idx = cache.messages.findIndex(m => m.id === msg.id);
  if (idx !== -1) cache.messages[idx] = msg;
  else cache.messages.push(msg);
  writeMessages(cache.messages);
}

export async function markMessagesRead(conversationId, userId) {
  if (usePostgres) return pgMarkMessagesRead(conversationId, userId);
  const cache = getCache();
  cache.messages.forEach(m => {
    if (m.conversationId === conversationId && m.senderId !== userId) m.read = true;
  });
  writeMessages(cache.messages);
}

export async function getRedeemCodes() {
  if (usePostgres) return pgGetRedeemCodes();
  return getCache().redeemCodes;
}

export async function saveRedeemCodes(codes) {
  if (usePostgres) {
    for (const c of codes) await pgSaveRedeemCode(c);
    return;
  }
  getCache().redeemCodes = codes;
  writeRedeemCodes(codes);
}

export async function saveRedeemCode(code) {
  if (usePostgres) return pgSaveRedeemCode(code);
  const cache = getCache();
  const idx = cache.redeemCodes.findIndex(c => c.code === code.code);
  if (idx !== -1) cache.redeemCodes[idx] = code;
  else cache.redeemCodes.push(code);
  writeRedeemCodes(cache.redeemCodes);
}

export async function getRedeemRecords() {
  if (usePostgres) return pgGetRedeemRecords();
  return getCache().redeemRecords;
}

export async function saveRedeemRecords(records) {
  if (usePostgres) {
    for (const r of records) await pgSaveRedeemRecord(r);
    return;
  }
  getCache().redeemRecords = records;
  writeRedeemRecords(records);
}

export async function addRedeemRecord(rec) {
  if (usePostgres) return pgSaveRedeemRecord(rec);
  const cache = getCache();
  cache.redeemRecords.unshift(rec);
  writeRedeemRecords(cache.redeemRecords);
}

export async function getRegisterCount() {
  if (usePostgres) return pgGetRegisterCount();
  const meta = readJSON('meta.json', { registerCount: 0 });
  return meta.registerCount || 0;
}

export async function incrementRegisterCount() {
  if (usePostgres) return pgIncrementRegisterCount();
  const count = (await getRegisterCount()) + 1;
  writeJSON('meta.json', { registerCount: count });
  if (memCache) memCache.meta.registerCount = count;
  return count;
}

export async function seedInitialData() {
  const activities = await getActivities();
  if (activities.length === 0) {
    const now = Date.now();
    const seedActivities = [
      {
        id: 'act_1', title: '夏日生活记录', cover: 'https://picsum.photos/seed/act1/800/450',
        description: '分享你的夏日生活瞬间，带话题#夏日生活 发布帖子即可参与！',
        hashtag: '夏日生活', rewardCoins: 500, participantCount: 1234, participants: [],
        status: 'active', startDate: now - 86400000 * 3, endDate: now + 86400000 * 7,
        rules: ['带话题#夏日生活 发布原创帖子', '内容需与夏日生活相关', '活动结束后评选10名优质内容，每人获得500文书币']
      },
      {
        id: 'act_2', title: '日常打卡挑战', cover: 'https://picsum.photos/seed/act2/800/450',
        description: '连续7天发布日常帖子，坚持打卡赢取文书币奖励！',
        hashtag: '日常打卡', rewardCoins: 200, participantCount: 5678, participants: [],
        status: 'active', startDate: now - 86400000, endDate: now + 86400000 * 14,
        rules: ['带话题#日常打卡 连续7天发帖', '每天至少发布1篇帖子', '完成打卡即可获得200文书币']
      },
      {
        id: 'act_3', title: '读书分享月', cover: 'https://picsum.photos/seed/act3/800/450',
        description: '分享你最近在读的好书，带话题#读书分享 参与活动。',
        hashtag: '读书分享', rewardCoins: 300, participantCount: 892, participants: [],
        status: 'upcoming', startDate: now + 86400000 * 2, endDate: now + 86400000 * 30,
        rules: ['带话题#读书分享 发布读书笔记', '包含书籍封面照片更佳', '优质分享将获得300文书币奖励']
      },
      {
        id: 'act_4', title: '春季摄影大赛', cover: 'https://picsum.photos/seed/act4/800/450',
        description: '记录春天的美好瞬间，摄影作品征集活动已圆满结束。',
        hashtag: '春季摄影', rewardCoins: 800, participantCount: 3421, participants: [],
        status: 'ended', startDate: now - 86400000 * 30, endDate: now - 86400000 * 5,
        rules: ['带话题#春季摄影 发布摄影作品', '作品需为原创', '已评选出获奖名单']
      }
    ];
    await saveActivities(seedActivities);
  }

  const redeemCodes = await getRedeemCodes();
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
  for (const c of allCodes) {
    const existing = redeemCodes.find(r => r.code.toUpperCase() === c.code.toUpperCase());
    if (!existing) {
      redeemCodes.push({ ...c, usedBy: [] });
      codesUpdated = true;
    } else {
      existing.coinValue = c.coinValue;
      existing.rewardType = c.rewardType;
      existing.description = c.description;
      codesUpdated = true;
    }
  }
  if (codesUpdated) {
    await saveRedeemCodes(redeemCodes);
  }

  const conversations = await getConversations();
  if (conversations.length === 0) {
    await saveConversations([]);
  }

  const users = await getUsers();
  const posts = await getPosts();
  if (users.length === 0 && posts.length === 0) {
    const genId = (prefix) => `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
    const botNames = ['文书小助手', '生活记录者', '读书爱好者', '咖啡控', '摄影小白', '跑步达人', '美食猎人'];
    const botUsers = [];
    botNames.forEach((name, i) => {
      const color = ['000000', '333333', '555555', '1a1a1a', '2d2d2d', '4a4a4a', '666666'][i];
      const bot = {
        id: genId('bot'), username: name, password: 'bot123456',
        avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=${color}&color=fff&size=200&bold=true`,
        cover: `https://picsum.photos/seed/bot${i}/800/320`,
        bio: '我是机器人，分享美好生活~', wenshuCoin: Math.floor(Math.random() * 5000),
        isVip: name === '文书小助手', vipLevel: name === '文书小助手' ? 10 : 0,
        vipExp: 0, vipExpiresAt: null,
        followingCount: Math.floor(Math.random() * 100), followersCount: Math.floor(Math.random() * 2000),
        likesCount: Math.floor(Math.random() * 5000), registerRank: 0,
        isSignedInToday: false, lastSignInDate: '', consecutiveSignDays: Math.floor(Math.random() * 30),
        createdAt: Date.now() - Math.floor(Math.random() * 86400000 * 30), joinedQQGroup: Math.random() > 0.5
      };
      botUsers.push(bot);
    });
    for (const u of botUsers) await saveUser(u);

    const seedPosts = [
      { content: '今天天气真好，出门散步看到了很美的夕阳🌇 生活中的小确幸就是这样不期而遇～ #日常 #生活记录', images: ['https://picsum.photos/seed/p1/600/600'], tags: ['日常', '生活记录'] },
      { content: '分享一本最近在读的书《被讨厌的勇气》，非常推荐！里面的观点颠覆了我很多固有的思维方式。#读书分享 #推荐', images: ['https://picsum.photos/seed/p2/600/800', 'https://picsum.photos/seed/p2b/600/600'], tags: ['读书分享', '推荐'] },
      { content: '自己做的拿铁拉花，虽然不是很完美但是进步了！☕ 继续加油练习～ #咖啡 #日常打卡', images: ['https://picsum.photos/seed/p3/600/600'], tags: ['咖啡', '日常打卡'] },
      { content: '周末去了美术馆，看到了很多喜欢的作品。艺术真的能让人静下心来。#夏日生活 #艺术', images: ['https://picsum.photos/seed/p4/800/600', 'https://picsum.photos/seed/p4b/600/600', 'https://picsum.photos/seed/p4c/600/600'], tags: ['夏日生活', '艺术'] },
      { content: '新学会的一道菜——番茄炒蛋！看起来简单，做好吃还是需要技巧的😋 #美食 #日常', images: ['https://picsum.photos/seed/p5/600/600'], tags: ['美食', '日常'] },
      { content: '跑步第30天打卡！从一开始跑1公里都喘到现在能轻松跑5公里，坚持真的有意义💪 #日常打卡 #运动', images: [], tags: ['日常打卡', '运动'] },
      { content: '今天的云好美啊，像棉花糖一样☁️ 拍了好多张照片，每一张都像壁纸。#夏日生活 #摄影', images: ['https://picsum.photos/seed/p7/600/400', 'https://picsum.photos/seed/p7b/600/800'], tags: ['夏日生活', '摄影'] },
      { content: '深夜emo时间...有时候觉得努力了很久的事情却看不到结果，但还是要相信一切都是最好的安排吧。#日常 #心情', images: [], tags: ['日常', '心情'] },
      { content: '新买的文具到了！开箱的快乐谁懂啊✨ 这个本子的纸质超级好，写字好顺滑。#好物分享 #文具', images: ['https://picsum.photos/seed/p9/600/600', 'https://picsum.photos/seed/p9b/600/600'], tags: ['好物分享', '文具'] },
      { content: '城市夜景永远拍不腻🌃 站在天桥上看车水马龙，感觉自己很渺小但又充满可能性。#摄影 #城市', images: ['https://picsum.photos/seed/p10/800/600'], tags: ['摄影', '城市'] },
      { content: '夏天就是要吃西瓜🍉 冰镇西瓜配上空调，这才是夏天该有的样子！#夏日生活 #美食', images: ['https://picsum.photos/seed/p11/600/600'], tags: ['夏日生活', '美食'] },
      { content: '今天尝试了新的妆容，感觉自己美美哒💄 女孩子要学会爱自己~ #日常 #美妆', images: ['https://picsum.photos/seed/p12/600/700'], tags: ['日常', '美妆'] },
    ];

    for (let i = 0; i < seedPosts.length; i++) {
      const seed = seedPosts[i];
      const author = botUsers[Math.floor(Math.random() * botUsers.length)];
      const likeCount = Math.floor(Math.random() * 200) + 10;
      const commentCount = Math.floor(Math.random() * 30);
      const collectCount = Math.floor(Math.random() * 50);
      await savePost({
        id: genId('post'), authorId: author.id, content: seed.content,
        images: seed.images, tags: seed.tags, likeCount, commentCount, collectCount,
        createdAt: Date.now() - i * 3600000 * (Math.random() * 4 + 1)
      });
    }
    console.log('🌱 初始种子数据已创建：', botUsers.length, '个机器人用户，', seedPosts.length, '条帖子');
  }
}

export async function saveVerificationCode(vc) {
  if (usePostgres) return pgSaveVerificationCode(vc);
  const cache = getCache();
  cache.verificationCodes = cache.verificationCodes.filter(c => c.phone !== vc.phone || c.purpose !== vc.purpose);
  cache.verificationCodes.push(vc);
  writeVerificationCodes(cache.verificationCodes);
}

export async function findValidVerificationCode(phone, code, purpose) {
  if (usePostgres) return pgFindValidVerificationCode(phone, code, purpose);
  const cache = getCache();
  const now = Date.now();
  return cache.verificationCodes.find(
    c => c.phone === phone && c.code === code && c.purpose === purpose && !c.used && c.expiresAt > now
  );
}

export async function markVerificationCodeUsed(id) {
  if (usePostgres) return pgMarkVerificationCodeUsed(id);
  const cache = getCache();
  const vc = cache.verificationCodes.find(c => c.id === id);
  if (vc) {
    vc.used = true;
    writeVerificationCodes(cache.verificationCodes);
  }
}

export async function findUserByPhone(phone) {
  if (usePostgres) return pgFindUserByPhone(phone);
  const users = await getUsers();
  return users.find(u => u.phone === phone) || null;
}

export { usePostgres, pool };
