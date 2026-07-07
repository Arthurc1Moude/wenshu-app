export interface User {
  id: string;
  username: string;
  avatar: string;
  cover?: string;
  bio: string;
  wenshuCoin: number;
  isVip: boolean;
  vipLevel: number;
  vipExp: number;
  vipExpiresAt?: number;
  followingCount: number;
  followersCount: number;
  likesCount: number;
  registerRank: number;
  isSignedInToday: boolean;
  lastSignInDate: string;
  consecutiveSignDays: number;
  joinedQQGroup?: boolean;
  createdAt: number;
}

export interface Post {
  id: string;
  authorId: string;
  author?: User;
  content: string;
  images: string[];
  tags: string[];
  likeCount: number;
  commentCount: number;
  collectCount: number;
  isLiked: boolean;
  isCollected: boolean;
  createdAt: number;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  author?: User;
  content: string;
  likeCount: number;
  isLiked: boolean;
  createdAt: number;
  replyToId?: string;
  replyToUser?: { id: string; username: string };
}

export interface Activity {
  id: string;
  title: string;
  cover: string;
  description: string;
  hashtag: string;
  rewardCoins: number;
  participantCount: number;
  participants?: string[];
  status: 'active' | 'ended' | 'upcoming';
  startDate: number;
  endDate: number;
  rules: string[];
}

export interface AppNotification {
  id: string;
  type: 'like' | 'comment' | 'follow' | 'system' | 'vip' | 'mention' | 'redeem_success';
  fromUserId?: string;
  fromUser?: User;
  postId?: string;
  content: string;
  isRead: boolean;
  createdAt: number;
}

export interface Conversation {
  id: string;
  type: 'group' | 'private';
  name: string;
  avatar?: string;
  participantIds: string[];
  lastMessage?: string;
  lastMessageAt: number;
  lastMessageTime?: number;
  unreadCount: number;
  otherUser?: User;
  isQQGroup?: boolean;
  qqGroupId?: string;
  isSystem?: boolean;
}

export type Chat = Conversation;

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderName?: string;
  senderAvatar?: string;
  sender?: User;
  content: string;
  createdAt: number;
  read?: boolean;
}

export interface RedeemCode {
  code: string;
  coinValue: number;
  rewardType?: 'coin' | 'vip';
  description: string;
  validUntil: number;
}

export interface RedeemRecord {
  id: string;
  userId: string;
  code: string;
  coinValue: number;
  rewardType?: 'coin' | 'vip';
  redeemedAt: number;
}

export type TabType = 'home' | 'activities' | 'vip' | 'profile';
