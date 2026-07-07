import { useEffect, useState } from 'react';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { useStore } from '@/store';
import { MessageCircle, Heart, Crown, UserPlus, AtSign, MessageSquare, ChevronRight } from 'lucide-react';
import { formatTime } from '@/utils/format';
import { useNavigate } from 'react-router-dom';
import type { AppNotification as Notification, Chat } from '@/types';

const notifIcons: Record<string, typeof Heart> = {
  like: Heart,
  comment: MessageCircle,
  follow: UserPlus,
  vip: Crown,
  mention: AtSign,
  system: MessageSquare,
};
const notifColors: Record<string, string> = {
  like: 'text-danger',
  comment: 'text-blue-500',
  follow: 'text-green-500',
  vip: 'text-gold',
  mention: 'text-purple-500',
  system: 'text-text-secondary',
};

export default function MessagesPage() {
  const { notifications, chats, loadNotifications, loadChats, markNotificationsRead, isLoggedIn } = useStore();
  const navigate = useNavigate();
  const [tab, setTab] = useState<'notifs' | 'chats'>('notifs');

  useEffect(() => {
    if (!isLoggedIn) { navigate('/login'); return; }
    loadNotifications();
    loadChats();
  }, []);

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="消息" showBack />

      <div className="flex border-b border-divider">
        {[
          { key: 'notifs', label: '通知' },
          { key: 'chats', label: '私信' },
        ].map(t => (
          <button
            key={t.key}
            onClick={() => { setTab(t.key as any); if (t.key === 'notifs') markNotificationsRead(); }}
            className={`flex-1 py-3 text-sm font-medium relative transition-colors ${
              tab === t.key ? 'text-black' : 'text-text-tertiary'
            }`}
          >
            {t.label}
            {tab === t.key && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-0.5 bg-black rounded-full" />}
          </button>
        ))}
      </div>

      {tab === 'notifs' ? (
        <div>
          {notifications.length === 0 ? (
            <div className="text-center py-20 text-text-tertiary">
              <MessageCircle className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p className="text-sm">暂无消息</p>
            </div>
          ) : (
            notifications.map(n => {
              const Icon = notifIcons[n.type] || MessageSquare;
              const color = notifColors[n.type] || 'text-text-secondary';
              return (
                <div key={n.id} className="flex items-start gap-3 px-4 py-3 active:bg-gray-50 border-b border-divider/60">
                  <div className={`mt-0.5 w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center ${color}`}>
                    <Icon className="w-5 h-5" fill={n.type === 'like' ? 'currentColor' : 'none'} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-text-primary leading-relaxed">{n.content}</p>
                    <p className="text-xs text-text-hint mt-1">{formatTime(n.createdAt)}</p>
                  </div>
                  {!n.isRead && <span className="w-2 h-2 bg-danger rounded-full mt-2" />}
                </div>
              );
            })
          )}
        </div>
      ) : (
        <div>
          {chats.length === 0 ? (
            <div className="text-center py-20 text-text-tertiary">
              <MessageCircle className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p className="text-sm">暂无聊天</p>
              <p className="text-xs mt-1">关注好友后即可开始聊天</p>
            </div>
          ) : (
            chats.map(c => (
              <div key={c.id} onClick={() => navigate(`/chat/${c.id}`)} className="flex items-center gap-3 px-4 py-3 active:bg-gray-50 border-b border-divider/60">
                <Avatar src={c.otherUser?.avatar} username={c.otherUser?.username} size="md" isVip={c.otherUser?.isVip} vipLevel={c.otherUser?.vipLevel} />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-sm text-text-primary truncate">{c.otherUser?.username}</span>
                    <span className="text-xs text-text-hint ml-2 whitespace-nowrap">{formatTime(c.lastMessageAt)}</span>
                  </div>
                  <p className="text-sm text-text-secondary truncate mt-0.5">{c.lastMessage || '开始聊天吧'}</p>
                </div>
                {c.unreadCount > 0 && <span className="bg-danger text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">{c.unreadCount}</span>}
                <ChevronRight className="w-4 h-4 text-text-hint -ml-2" />
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
