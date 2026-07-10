import pg from 'pg';
const { Pool } = pg;

let pool = null;

export function initPostgres() {
  if (!process.env.DATABASE_URL) return null;
  pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: process.env.DATABASE_URL.includes('cockroachlabs') || process.env.DATABASE_URL.includes('neon.tech') ? { rejectUnauthorized: false } : false,
  });
  return pool;
}

export async function initTables() {
  if (!pool) return;
  const client = await pool.connect();
  try {
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        avatar TEXT,
        cover TEXT,
        bio TEXT DEFAULT '',
        location TEXT DEFAULT '',
        wenshu_coin INTEGER DEFAULT 0,
        is_vip BOOLEAN DEFAULT false,
        vip_level INTEGER DEFAULT 0,
        vip_exp INTEGER DEFAULT 0,
        vip_expires_at BIGINT,
        following_count INTEGER DEFAULT 0,
        followers_count INTEGER DEFAULT 0,
        likes_count INTEGER DEFAULT 0,
        register_rank INTEGER DEFAULT 0,
        is_signed_in_today BOOLEAN DEFAULT false,
        last_sign_in_date TEXT DEFAULT '',
        consecutive_sign_days INTEGER DEFAULT 0,
        created_at BIGINT,
        joined_qq_group BOOLEAN DEFAULT false
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS posts (
        id TEXT PRIMARY KEY,
        author_id TEXT NOT NULL,
        title TEXT DEFAULT '',
        content TEXT NOT NULL,
        images JSONB DEFAULT '[]'::jsonb,
        tags JSONB DEFAULT '[]'::jsonb,
        like_count INTEGER DEFAULT 0,
        comment_count INTEGER DEFAULT 0,
        collect_count INTEGER DEFAULT 0,
        created_at BIGINT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS comments (
        id TEXT PRIMARY KEY,
        post_id TEXT NOT NULL,
        author_id TEXT NOT NULL,
        content TEXT NOT NULL,
        like_count INTEGER DEFAULT 0,
        reply_to_id TEXT,
        created_at BIGINT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS likes (
        id TEXT PRIMARY KEY,
        post_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        created_at BIGINT,
        UNIQUE(post_id, user_id)
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS collects (
        id TEXT PRIMARY KEY,
        post_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        created_at BIGINT,
        UNIQUE(post_id, user_id)
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS follows (
        id TEXT PRIMARY KEY,
        follower_id TEXT NOT NULL,
        following_id TEXT NOT NULL,
        created_at BIGINT,
        UNIQUE(follower_id, following_id)
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS notifications (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        type TEXT NOT NULL,
        content TEXT NOT NULL,
        from_user_id TEXT,
        post_id TEXT,
        is_read BOOLEAN DEFAULT false,
        created_at BIGINT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS conversations (
        id TEXT PRIMARY KEY,
        type TEXT DEFAULT 'private',
        name TEXT,
        avatar TEXT,
        participant_ids JSONB DEFAULT '[]'::jsonb,
        last_message TEXT DEFAULT '',
        last_message_time BIGINT,
        unread_count INTEGER DEFAULT 0,
        is_system BOOLEAN DEFAULT false
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS messages (
        id TEXT PRIMARY KEY,
        conversation_id TEXT NOT NULL,
        sender_id TEXT NOT NULL,
        content TEXT NOT NULL,
        read BOOLEAN DEFAULT false,
        created_at BIGINT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS activities (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        cover TEXT,
        description TEXT,
        hashtag TEXT,
        reward_coins INTEGER DEFAULT 0,
        participant_count INTEGER DEFAULT 0,
        participants JSONB DEFAULT '[]'::jsonb,
        status TEXT DEFAULT 'active',
        start_date BIGINT,
        end_date BIGINT,
        rules JSONB DEFAULT '[]'::jsonb
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS redeem_codes (
        code TEXT PRIMARY KEY,
        coin_value INTEGER DEFAULT 0,
        reward_type TEXT DEFAULT 'coin',
        description TEXT,
        valid_until BIGINT,
        used_by JSONB DEFAULT '[]'::jsonb
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS redeem_records (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        code TEXT NOT NULL,
        coin_value INTEGER DEFAULT 0,
        reward_type TEXT DEFAULT 'coin',
        redeemed_at BIGINT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS meta (
        key TEXT PRIMARY KEY,
        value TEXT
      )
    `);

    await client.query(`
      CREATE TABLE IF NOT EXISTS blacklists (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        blocked_user_id TEXT NOT NULL,
        created_at BIGINT,
        UNIQUE(user_id, blocked_user_id)
      )
    `);

    await client.query(`CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_likes_post ON likes(post_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_likes_user ON likes(user_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_collects_post ON collects(post_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_collects_user ON collects(user_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_messages_conv ON messages(conversation_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_blacklists_user ON blacklists(user_id)`);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_blacklists_blocked ON blacklists(blocked_user_id)`);

    console.log('✅ PostgreSQL tables initialized');
  } finally {
    client.release();
  }
}

function rowToUser(row) {
  if (!row) return null;
  return {
    id: row.id,
    username: row.username,
    password: row.password,
    avatar: row.avatar,
    cover: row.cover,
    bio: row.bio || '',
    location: row.location || '',
    wenshuCoin: row.wenshu_coin || 0,
    isVip: row.is_vip || false,
    vipLevel: row.vip_level || 0,
    vipExp: row.vip_exp || 0,
    vipExpiresAt: row.vip_expires_at,
    followingCount: row.following_count || 0,
    followersCount: row.followers_count || 0,
    likesCount: row.likes_count || 0,
    registerRank: row.register_rank || 0,
    isSignedInToday: row.is_signed_in_today || false,
    lastSignInDate: row.last_sign_in_date || '',
    consecutiveSignDays: row.consecutive_sign_days || 0,
    createdAt: row.created_at,
    joinedQQGroup: row.joined_qq_group || false,
  };
}

function rowToPost(row) {
  if (!row) return null;
  return {
    id: row.id,
    authorId: row.author_id,
    title: row.title || '',
    content: row.content,
    images: row.images || [],
    tags: row.tags || [],
    likeCount: row.like_count || 0,
    commentCount: row.comment_count || 0,
    collectCount: row.collect_count || 0,
    createdAt: row.created_at,
  };
}

function rowToComment(row) {
  if (!row) return null;
  return {
    id: row.id,
    postId: row.post_id,
    authorId: row.author_id,
    content: row.content,
    likeCount: row.like_count || 0,
    replyToId: row.reply_to_id,
    createdAt: row.created_at,
  };
}

function rowToLike(row) {
  if (!row) return null;
  return { id: row.id, postId: row.post_id, userId: row.user_id, createdAt: row.created_at };
}

function rowToCollect(row) {
  if (!row) return null;
  return { id: row.id, postId: row.post_id, userId: row.user_id, createdAt: row.created_at };
}

function rowToFollow(row) {
  if (!row) return null;
  return { id: row.id, followerId: row.follower_id, followingId: row.following_id, createdAt: row.created_at };
}

function rowToNotification(row) {
  if (!row) return null;
  return {
    id: row.id,
    userId: row.user_id,
    type: row.type,
    content: row.content,
    fromUserId: row.from_user_id,
    postId: row.post_id,
    isRead: row.is_read,
    createdAt: row.created_at,
  };
}

function rowToConversation(row) {
  if (!row) return null;
  return {
    id: row.id,
    type: row.type,
    name: row.name,
    avatar: row.avatar,
    participantIds: row.participant_ids || [],
    lastMessage: row.last_message,
    lastMessageTime: row.last_message_time,
    unreadCount: row.unread_count,
    isSystem: row.is_system,
  };
}

function rowToMessage(row) {
  if (!row) return null;
  return {
    id: row.id,
    conversationId: row.conversation_id,
    senderId: row.sender_id,
    content: row.content,
    read: row.read,
    createdAt: row.created_at,
  };
}

function rowToActivity(row) {
  if (!row) return null;
  return {
    id: row.id,
    title: row.title,
    cover: row.cover,
    description: row.description,
    hashtag: row.hashtag,
    rewardCoins: row.reward_coins,
    participantCount: row.participant_count,
    participants: row.participants || [],
    status: row.status,
    startDate: row.start_date,
    endDate: row.end_date,
    rules: row.rules || [],
  };
}

function rowToRedeemCode(row) {
  if (!row) return null;
  return {
    code: row.code,
    coinValue: row.coin_value,
    rewardType: row.reward_type,
    description: row.description,
    validUntil: row.valid_until,
    usedBy: row.used_by || [],
  };
}

function rowToRedeemRecord(row) {
  if (!row) return null;
  return {
    id: row.id,
    userId: row.user_id,
    code: row.code,
    coinValue: row.coin_value,
    rewardType: row.reward_type,
    redeemedAt: row.redeemed_at,
  };
}

function rowToBlacklist(row) {
  if (!row) return null;
  return {
    id: row.id,
    userId: row.user_id,
    blockedUserId: row.blocked_user_id,
    createdAt: row.created_at,
  };
}

export async function pgGetUsers() {
  const res = await pool.query('SELECT * FROM users ORDER BY created_at ASC');
  return res.rows.map(rowToUser);
}

export async function pgSaveUser(user) {
  await pool.query(`
    INSERT INTO users (id, username, password, avatar, cover, bio, location, wenshu_coin, is_vip, vip_level, vip_exp, vip_expires_at, following_count, followers_count, likes_count, register_rank, is_signed_in_today, last_sign_in_date, consecutive_sign_days, created_at, joined_qq_group)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21)
    ON CONFLICT (id) DO UPDATE SET
      username = EXCLUDED.username, password = EXCLUDED.password, avatar = EXCLUDED.avatar, cover = EXCLUDED.cover,
      bio = EXCLUDED.bio, location = EXCLUDED.location, wenshu_coin = EXCLUDED.wenshu_coin, is_vip = EXCLUDED.is_vip,
      vip_level = EXCLUDED.vip_level, vip_exp = EXCLUDED.vip_exp, vip_expires_at = EXCLUDED.vip_expires_at,
      following_count = EXCLUDED.following_count, followers_count = EXCLUDED.followers_count, likes_count = EXCLUDED.likes_count,
      register_rank = EXCLUDED.register_rank, is_signed_in_today = EXCLUDED.is_signed_in_today,
      last_sign_in_date = EXCLUDED.last_sign_in_date, consecutive_sign_days = EXCLUDED.consecutive_sign_days,
      joined_qq_group = EXCLUDED.joined_qq_group
  `, [
    user.id, user.username, user.password, user.avatar, user.cover, user.bio || '', user.location || '',
    user.wenshuCoin || 0, user.isVip || false, user.vipLevel || 0, user.vipExp || 0, user.vipExpiresAt,
    user.followingCount || 0, user.followersCount || 0, user.likesCount || 0, user.registerRank || 0,
    user.isSignedInToday || false, user.lastSignInDate || '', user.consecutiveSignDays || 0,
    user.createdAt, user.joinedQQGroup || false
  ]);
}

export async function pgGetPosts() {
  const res = await pool.query('SELECT * FROM posts ORDER BY created_at DESC');
  return res.rows.map(rowToPost);
}

export async function pgSavePost(post) {
  await pool.query(`
    INSERT INTO posts (id, author_id, title, content, images, tags, like_count, comment_count, collect_count, created_at)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
    ON CONFLICT (id) DO UPDATE SET
      title = EXCLUDED.title, content = EXCLUDED.content, images = EXCLUDED.images, tags = EXCLUDED.tags,
      like_count = EXCLUDED.like_count, comment_count = EXCLUDED.comment_count, collect_count = EXCLUDED.collect_count
  `, [
    post.id, post.authorId, post.title || '', post.content,
    JSON.stringify(post.images || []), JSON.stringify(post.tags || []),
    post.likeCount || 0, post.commentCount || 0, post.collectCount || 0, post.createdAt
  ]);
}

export async function pgGetComments() {
  const res = await pool.query('SELECT * FROM comments ORDER BY created_at ASC');
  return res.rows.map(rowToComment);
}

export async function pgSaveComment(comment) {
  await pool.query(`
    INSERT INTO comments (id, post_id, author_id, content, like_count, reply_to_id, created_at)
    VALUES ($1,$2,$3,$4,$5,$6,$7)
    ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, like_count = EXCLUDED.like_count
  `, [comment.id, comment.postId, comment.authorId, comment.content, comment.likeCount || 0, comment.replyToId, comment.createdAt]);
}

export async function pgGetLikes() {
  const res = await pool.query('SELECT * FROM likes');
  return res.rows.map(rowToLike);
}

export async function pgSaveLike(like) {
  await pool.query(`
    INSERT INTO likes (id, post_id, user_id, created_at)
    VALUES ($1,$2,$3,$4)
    ON CONFLICT (post_id, user_id) DO NOTHING
  `, [like.id, like.postId, like.userId, like.createdAt]);
}

export async function pgDeleteLike(postId, userId) {
  await pool.query('DELETE FROM likes WHERE post_id = $1 AND user_id = $2', [postId, userId]);
}

export async function pgGetCollects() {
  const res = await pool.query('SELECT * FROM collects');
  return res.rows.map(rowToCollect);
}

export async function pgSaveCollect(collect) {
  await pool.query(`
    INSERT INTO collects (id, post_id, user_id, created_at)
    VALUES ($1,$2,$3,$4)
    ON CONFLICT (post_id, user_id) DO NOTHING
  `, [collect.id, collect.postId, collect.userId, collect.createdAt]);
}

export async function pgDeleteCollect(postId, userId) {
  await pool.query('DELETE FROM collects WHERE post_id = $1 AND user_id = $2', [postId, userId]);
}

export async function pgGetFollows() {
  const res = await pool.query('SELECT * FROM follows');
  return res.rows.map(rowToFollow);
}

export async function pgSaveFollow(follow) {
  await pool.query(`
    INSERT INTO follows (id, follower_id, following_id, created_at)
    VALUES ($1,$2,$3,$4)
    ON CONFLICT (follower_id, following_id) DO NOTHING
  `, [follow.id, follow.followerId, follow.followingId, follow.createdAt]);
}

export async function pgDeleteFollow(followerId, followingId) {
  await pool.query('DELETE FROM follows WHERE follower_id = $1 AND following_id = $2', [followerId, followingId]);
}

export async function pgGetNotifications() {
  const res = await pool.query('SELECT * FROM notifications ORDER BY created_at DESC');
  return res.rows.map(rowToNotification);
}

export async function pgSaveNotification(notif) {
  await pool.query(`
    INSERT INTO notifications (id, user_id, type, content, from_user_id, post_id, is_read, created_at)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8)
    ON CONFLICT (id) DO UPDATE SET is_read = EXCLUDED.is_read
  `, [notif.id, notif.userId, notif.type, notif.content, notif.fromUserId, notif.postId, notif.isRead, notif.createdAt]);
}

export async function pgGetConversations() {
  const res = await pool.query('SELECT * FROM conversations');
  return res.rows.map(rowToConversation);
}

export async function pgSaveConversation(conv) {
  await pool.query(`
    INSERT INTO conversations (id, type, name, avatar, participant_ids, last_message, last_message_time, unread_count, is_system)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    ON CONFLICT (id) DO UPDATE SET
      last_message = EXCLUDED.last_message, last_message_time = EXCLUDED.last_message_time, unread_count = EXCLUDED.unread_count
  `, [conv.id, conv.type, conv.name, conv.avatar, JSON.stringify(conv.participantIds || []),
       conv.lastMessage, conv.lastMessageTime, conv.unreadCount, conv.isSystem || false]);
}

export async function pgGetMessages() {
  const res = await pool.query('SELECT * FROM messages ORDER BY created_at ASC');
  return res.rows.map(rowToMessage);
}

export async function pgSaveMessage(msg) {
  await pool.query(`
    INSERT INTO messages (id, conversation_id, sender_id, content, read, created_at)
    VALUES ($1,$2,$3,$4,$5,$6)
    ON CONFLICT (id) DO UPDATE SET read = EXCLUDED.read
  `, [msg.id, msg.conversationId, msg.senderId, msg.content, msg.read || false, msg.createdAt]);
}

export async function pgMarkMessagesRead(conversationId, userId) {
  await pool.query(`
    UPDATE messages SET read = true
    WHERE conversation_id = $1 AND sender_id != $2
  `, [conversationId, userId]);
}

export async function pgGetActivities() {
  const res = await pool.query('SELECT * FROM activities');
  return res.rows.map(rowToActivity);
}

export async function pgSaveActivity(act) {
  await pool.query(`
    INSERT INTO activities (id, title, cover, description, hashtag, reward_coins, participant_count, participants, status, start_date, end_date, rules)
    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)
    ON CONFLICT (id) DO UPDATE SET
      participant_count = EXCLUDED.participant_count, participants = EXCLUDED.participants, status = EXCLUDED.status
  `, [act.id, act.title, act.cover, act.description, act.hashtag, act.rewardCoins,
       act.participantCount, JSON.stringify(act.participants || []), act.status,
       act.startDate, act.endDate, JSON.stringify(act.rules || [])]);
}

export async function pgGetRedeemCodes() {
  const res = await pool.query('SELECT * FROM redeem_codes');
  return res.rows.map(rowToRedeemCode);
}

export async function pgSaveRedeemCode(code) {
  await pool.query(`
    INSERT INTO redeem_codes (code, coin_value, reward_type, description, valid_until, used_by)
    VALUES ($1,$2,$3,$4,$5,$6)
    ON CONFLICT (code) DO UPDATE SET
      coin_value = EXCLUDED.coin_value, reward_type = EXCLUDED.reward_type,
      description = EXCLUDED.description, valid_until = EXCLUDED.valid_until, used_by = EXCLUDED.used_by
  `, [code.code, code.coinValue, code.rewardType, code.description, code.validUntil, JSON.stringify(code.usedBy || [])]);
}

export async function pgGetRedeemRecords() {
  const res = await pool.query('SELECT * FROM redeem_records ORDER BY redeemed_at DESC');
  return res.rows.map(rowToRedeemRecord);
}

export async function pgSaveRedeemRecord(rec) {
  await pool.query(`
    INSERT INTO redeem_records (id, user_id, code, coin_value, reward_type, redeemed_at)
    VALUES ($1,$2,$3,$4,$5,$6)
    ON CONFLICT (id) DO NOTHING
  `, [rec.id, rec.userId, rec.code, rec.coinValue, rec.rewardType, rec.redeemedAt]);
}

export async function pgGetRegisterCount() {
  const res = await pool.query("SELECT value FROM meta WHERE key = 'registerCount'");
  if (res.rows.length === 0) return 0;
  return parseInt(res.rows[0].value) || 0;
}

export async function pgIncrementRegisterCount() {
  const count = (await pgGetRegisterCount()) + 1;
  await pool.query(`
    INSERT INTO meta (key, value) VALUES ('registerCount', $1)
    ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value
  `, [count.toString()]);
  return count;
}

export async function pgGetBlacklists() {
  const res = await pool.query('SELECT * FROM blacklists');
  return res.rows.map(rowToBlacklist);
}

export async function pgSaveBlacklist(bl) {
  await pool.query(`
    INSERT INTO blacklists (id, user_id, blocked_user_id, created_at)
    VALUES ($1,$2,$3,$4)
    ON CONFLICT (user_id, blocked_user_id) DO NOTHING
  `, [bl.id, bl.userId, bl.blockedUserId, bl.createdAt]);
}

export async function pgDeleteBlacklist(userId, blockedUserId) {
  await pool.query('DELETE FROM blacklists WHERE user_id = $1 AND blocked_user_id = $2', [userId, blockedUserId]);
}

export { pool };
