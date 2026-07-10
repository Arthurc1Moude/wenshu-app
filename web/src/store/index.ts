import { create } from 'zustand';
import { api, setToken, clearToken, API_BASE } from '@/utils/api';
import type { User, Post, Comment, Activity, AppNotification, Conversation, Message, TabType, Chat } from '@/types';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface AppState {
  currentUser: User | null;
  isLoggedIn: boolean;
  posts: Post[];
  hotPosts: Post[];
  activities: Activity[];
  notifications: AppNotification[];
  conversations: Conversation[];
  chats: Chat[];
  unreadCount: number;
  activeTab: TabType;
  toasts: Toast[];
  loading: boolean;

  setActiveTab: (tab: TabType) => void;
  showToast: (message: string, type?: 'success' | 'error' | 'info') => void;
  dismissToast: (id: string) => void;

  init: () => Promise<void>;
  login: (username: string, password: string) => Promise<boolean>;
  register: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  updateProfile: (data: Partial<User>) => Promise<void>;
  clearCache: () => void;

  loadPosts: (sort?: 'new' | 'hot') => Promise<void>;
  loadPostsByUser: (userId: string) => Promise<Post[]>;
  loadPostsByTag: (tag: string) => Promise<Post[]>;
  loadPost: (postId: string) => Promise<Post | null>;
  loadLikedPosts: () => Promise<Post[]>;
  loadSavedPosts: () => Promise<Post[]>;
  toggleLike: (postId: string) => Promise<void>;
  toggleCollect: (postId: string) => Promise<void>;
  createPost: (content: string, images: string[], tags: string[]) => Promise<Post | null>;
  searchPosts: (q: string) => Promise<Post[]>;
  uploadImage: (file: File) => Promise<string | null>;

  loadComments: (postId: string) => Promise<Comment[]>;
  addComment: (postId: string, content: string, replyToId?: string) => Promise<Comment | null>;

  loadActivities: (status?: string) => Promise<void>;
  loadActivity: (activityId: string) => Promise<Activity | null>;
  joinActivity: (activityId: string) => Promise<string | null>;

  signIn: () => Promise<{ coins: number; consecutiveDays: number } | null>;
  joinQQGroup: () => Promise<{ coins: number } | null>;
  purchaseVip: () => Promise<void>;
  redeemCode: (code: string) => Promise<{ coins: number; vipGranted?: boolean; vipExpiresAt?: number; description: string; totalCoins: number } | null>;
  loadRedeemRecords: () => Promise<any[]>;

  loadNotifications: () => Promise<void>;
  markNotificationsRead: () => Promise<void>;

  loadConversations: () => Promise<void>;
  loadChats: () => Promise<void>;
  loadMessages: (conversationId: string) => Promise<Message[]>;
  sendMessage: (conversationId: string, content: string) => Promise<Message | null>;
  createPrivateChat: (userId: string) => Promise<Conversation | null>;
  markChatRead: (chatId: string) => Promise<void>;

  seedPosts: () => Promise<void>;
}

export const useStore = create<AppState>((set, get) => ({
  currentUser: null,
  isLoggedIn: false,
  posts: [],
  hotPosts: [],
  activities: [],
  notifications: [],
  conversations: [],
  chats: [],
  unreadCount: 0,
  activeTab: 'home',
  toasts: [],
  loading: false,

  setActiveTab: (tab) => set({ activeTab: tab }),

  showToast: (message, type = 'info') => {
    const id = Date.now().toString() + Math.random().toString(36).slice(2);
    set((state) => ({ toasts: [...state.toasts, { id, message, type }] }));
    setTimeout(() => get().dismissToast(id), 2500);
  },
  dismissToast: (id) => set((state) => ({ toasts: state.toasts.filter(t => t.id !== id) })),

  init: async () => {
    const token = localStorage.getItem('wenshu_token');
    if (!token) return;
    try {
      const user = await api.get<User>('/api/users/me');
      set({ currentUser: user, isLoggedIn: true });
    } catch {
      clearToken();
    }
  },

  login: async (username, password) => {
    try {
      const res = await api.post<{ user: User; token: string }>('/api/auth/login', { username, password });
      setToken(res.token);
      set({ currentUser: res.user, isLoggedIn: true });
      get().showToast('登录成功', 'success');
      await get().loadPosts('new');
      await get().loadActivities();
      await get().loadChats();
      await get().seedPosts();
      return true;
    } catch (e: any) {
      get().showToast(e.message || '登录失败', 'error');
      return false;
    }
  },

  register: async (username, password) => {
    try {
      const res = await api.post<{ user: User; token: string }>('/api/auth/register', { username, password });
      setToken(res.token);
      set({ currentUser: res.user, isLoggedIn: true });
      let bonusMsg = '注册成功';
      if (res.user.registerRank <= 5) bonusMsg = `恭喜作为第${res.user.registerRank}位注册用户，获得100000文书币！`;
      else if (res.user.registerRank <= 10) bonusMsg = `恭喜作为第${res.user.registerRank}位注册用户，获得50000文书币！`;
      else if (res.user.registerRank <= 15) bonusMsg = `恭喜作为第${res.user.registerRank}位注册用户，获得10000文书币！`;
      get().showToast(bonusMsg, 'success');
      await get().loadPosts('new');
      await get().loadActivities();
      await get().loadChats();
      await get().seedPosts();
      return true;
    } catch (e: any) {
      get().showToast(e.message || '注册失败', 'error');
      return false;
    }
  },

  logout: () => {
    clearToken();
    set({ currentUser: null, isLoggedIn: false, posts: [], notifications: [], conversations: [], chats: [] });
    get().showToast('已退出登录', 'info');
  },

  clearCache: () => {
    get().showToast('缓存已清除', 'success');
  },

  updateProfile: async (data) => {
    try {
      const user = await api.put<User>('/api/users/me', data);
      set({ currentUser: user });
      get().showToast('资料已更新', 'success');
    } catch (e: any) {
      get().showToast(e.message || '更新失败', 'error');
    }
  },

  loadPosts: async (sort = 'new') => {
    try {
      const posts = await api.get<Post[]>(`/api/posts?sort=${sort}`);
      if (sort === 'hot') set({ hotPosts: posts });
      else set({ posts });
    } catch (e) {
      console.error('加载帖子失败', e);
    }
  },

  loadPostsByUser: async (userId) => {
    try {
      return await api.get<Post[]>(`/api/posts?userId=${userId}`);
    } catch { return []; }
  },

  loadPostsByTag: async (tag) => {
    try {
      return await api.get<Post[]>(`/api/posts?tag=${encodeURIComponent(tag)}`);
    } catch { return []; }
  },

  loadPost: async (postId) => {
    try {
      return await api.get<Post>(`/api/posts/${postId}`);
    } catch { return null; }
  },

  loadLikedPosts: async () => {
    try {
      return await api.get<Post[]>('/api/posts/liked/mine');
    } catch { return []; }
  },

  loadSavedPosts: async () => {
    try {
      return await api.get<Post[]>('/api/posts/saved/mine');
    } catch { return []; }
  },

  toggleLike: async (postId) => {
    const { posts } = get();
    const updatePostLike = (p: Post) => {
      if (p.id !== postId) return p;
      return { ...p, isLiked: !p.isLiked, likeCount: p.isLiked ? p.likeCount - 1 : p.likeCount + 1 };
    };
    set({ posts: posts.map(updatePostLike), hotPosts: get().hotPosts.map(updatePostLike) });
    try {
      await api.post(`/api/posts/${postId}/like`);
    } catch (e: any) {
      set({ posts, hotPosts: get().hotPosts });
      get().showToast(e.message || '操作失败', 'error');
    }
  },

  toggleCollect: async (postId) => {
    const { posts } = get();
    const updatePostCollect = (p: Post) => {
      if (p.id !== postId) return p;
      return { ...p, isCollected: !p.isCollected, collectCount: p.isCollected ? p.collectCount - 1 : p.collectCount + 1 };
    };
    set({ posts: posts.map(updatePostCollect), hotPosts: get().hotPosts.map(updatePostCollect) });
    try {
      await api.post(`/api/posts/${postId}/collect`);
    } catch (e: any) {
      set({ posts, hotPosts: get().hotPosts });
      get().showToast(e.message || '操作失败', 'error');
    }
  },

  createPost: async (content, images, tags) => {
    try {
      const post = await api.post<Post>('/api/posts', { content, images, tags });
      set((state) => ({ posts: [post, ...state.posts] }));
      get().showToast('发布成功', 'success');
      return post;
    } catch (e: any) {
      get().showToast(e.message || '发布失败', 'error');
      return null;
    }
  },

  searchPosts: async (q) => {
    try {
      const res = await api.get<{ posts: Post[] }>(`/api/search?q=${encodeURIComponent(q)}`);
      return res.posts || [];
    } catch { return []; }
  },

  uploadImage: async (file) => {
    try {
      const formData = new FormData();
      formData.append('image', file);
      const token = localStorage.getItem('wenshu_token');
      const res = await fetch(`${API_BASE}/api/upload`, {
        method: 'POST',
        headers: token ? { 'Authorization': `Bearer ${token}` } : {},
        body: formData,
      });
      if (!res.ok) throw new Error('上传失败');
      const data = await res.json();
      return data.url;
    } catch (e: any) {
      get().showToast(e.message || '上传失败', 'error');
      return null;
    }
  },

  loadComments: async (postId) => {
    try {
      return await api.get<Comment[]>(`/api/posts/${postId}/comments`);
    } catch { return []; }
  },

  addComment: async (postId, content, replyToId) => {
    try {
      const comment = await api.post<Comment>(`/api/posts/${postId}/comments`, { content, replyToId });
      set((state) => ({
        posts: state.posts.map(p => p.id === postId ? { ...p, commentCount: p.commentCount + 1 } : p)
      }));
      return comment;
    } catch (e: any) {
      get().showToast(e.message || '评论失败', 'error');
      return null;
    }
  },

  loadActivities: async (status) => {
    try {
      const url = status ? `/api/activities?status=${status}` : '/api/activities';
      const activities = await api.get<Activity[]>(url);
      set({ activities });
    } catch (e) {
      console.error('加载活动失败', e);
    }
  },

  loadActivity: async (activityId) => {
    try {
      return await api.get<Activity>(`/api/activities/${activityId}`);
    } catch { return null; }
  },

  joinActivity: async (activityId) => {
    try {
      const res = await api.post<{ hashtag: string }>(`/api/activities/${activityId}/join`);
      get().showToast(`已加入活动，发布帖子带 #${res.hashtag} 即可参与`, 'success');
      return res.hashtag;
    } catch (e: any) {
      get().showToast(e.message || '加入失败', 'error');
      return null;
    }
  },

  signIn: async () => {
    try {
      const res = await api.post<{ coins: number; consecutiveDays: number; totalCoins: number }>('/api/coin/signin');
      if (get().currentUser) {
        set({ currentUser: { ...get().currentUser!, wenshuCoin: res.totalCoins, isSignedInToday: true } });
      }
      get().showToast(`签到成功！获得${res.coins}文书币，连续${res.consecutiveDays}天`, 'success');
      return res;
    } catch (e: any) {
      get().showToast(e.message || '签到失败', 'error');
      return null;
    }
  },

  joinQQGroup: async () => {
    try {
      const res = await api.post<{ coins: number; totalCoins: number }>('/api/coin/join-qq');
      if (get().currentUser) {
        set({ currentUser: { ...get().currentUser!, wenshuCoin: res.totalCoins, joinedQQGroup: true } });
      }
      get().showToast(`加入QQ群成功！获得${res.coins}文书币，群号：702404026`, 'success');
      return res;
    } catch (e: any) {
      get().showToast(e.message || '领取失败', 'error');
      return null;
    }
  },

  purchaseVip: async () => {
    try {
      const res = await api.post<{ user: User }>('/api/vip/purchase');
      set({ currentUser: res.user });
      get().showToast('🎉 欢迎加入文书会！', 'success');
    } catch (e: any) {
      get().showToast(e.message || '开通失败', 'error');
    }
  },

  redeemCode: async (code) => {
    try {
      const res = await api.post<{ coins: number; vipGranted?: boolean; vipExpiresAt?: number; description: string; totalCoins: number }>('/api/redeem', { code });
      if (get().currentUser) {
        const updatedUser = { ...get().currentUser!, wenshuCoin: res.totalCoins };
        if (res.vipGranted) {
          updatedUser.isVip = true;
          if (!updatedUser.vipLevel || updatedUser.vipLevel === 0) updatedUser.vipLevel = 1;
          updatedUser.vipExpiresAt = res.vipExpiresAt;
        }
        set({ currentUser: updatedUser });
      }
      if (res.vipGranted) {
        get().showToast('🎉 兑换成功！文书会VIP已激活！', 'success');
      } else {
        get().showToast(`兑换成功！获得${res.coins}文书币`, 'success');
      }
      return res;
    } catch (e: any) {
      get().showToast(e.message || '兑换失败', 'error');
      return null;
    }
  },

  loadRedeemRecords: async () => {
    try {
      return await api.get<any[]>('/api/redeem/records');
    } catch { return []; }
  },

  loadNotifications: async () => {
    try {
      const res = await api.get<{ notifications: AppNotification[]; unreadCount: number }>('/api/notifications');
      set({ notifications: res.notifications, unreadCount: res.unreadCount });
    } catch (e) {
      console.error('加载通知失败', e);
    }
  },

  markNotificationsRead: async () => {
    try {
      await api.post('/api/notifications/read');
      set((state) => ({
        notifications: state.notifications.map(n => ({ ...n, isRead: true })),
        unreadCount: 0
      }));
    } catch (e) { console.error(e); }
  },

  loadConversations: async () => {
    try {
      const conversations = await api.get<Conversation[]>('/api/conversations');
      set({ conversations, chats: conversations });
    } catch (e) { console.error(e); }
  },

  loadChats: async () => {
    try {
      const conversations = await api.get<Conversation[]>('/api/conversations');
      const chats: Chat[] = conversations.map(c => ({
        ...c,
        otherUser: c.participantIds.find((id: string) => id !== get().currentUser?.id) as any,
      }));
      for (const chat of chats) {
        const otherId = chat.participantIds.find((id: string) => id !== get().currentUser?.id);
        if (otherId) {
          try {
            const user = await api.get<User>(`/api/users/${otherId}`);
            chat.otherUser = user;
          } catch {}
        }
      }
      set({ conversations, chats });
    } catch (e) { console.error(e); }
  },

  loadMessages: async (conversationId) => {
    try {
      return await api.get<Message[]>(`/api/conversations/${conversationId}/messages`);
    } catch { return []; }
  },

  sendMessage: async (conversationId, content) => {
    try {
      const msg = await api.post<Message>(`/api/conversations/${conversationId}/messages`, { content });
      return msg;
    } catch (e: any) {
      get().showToast(e.message || '发送失败', 'error');
      return null;
    }
  },

  createPrivateChat: async (userId) => {
    try {
      return await api.post<Conversation>(`/api/conversations/private/${userId}`);
    } catch (e: any) {
      get().showToast(e.message || '创建聊天失败', 'error');
      return null;
    }
  },

  markChatRead: async (chatId) => {
    try {
      await api.post(`/api/conversations/${chatId}/read`);
      set((state) => ({
        chats: state.chats.map(c => c.id === chatId ? { ...c, unreadCount: 0 } : c),
      }));
    } catch {}
  },

  seedPosts: async () => {
    try {
      await api.post('/api/seed');
    } catch {}
  },
}));
