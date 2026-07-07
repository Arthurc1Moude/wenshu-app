import { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import Avatar from '@/components/ui/Avatar';
import { Send } from 'lucide-react';
import { useStore } from '@/store';
import type { Message, User } from '@/types';
import { formatTime } from '@/utils/format';

interface ChatMessage extends Message {
  sender?: User | null;
  senderName?: string;
  senderAvatar?: string;
}

export default function ChatPage() {
  const { chatId } = useParams<{ chatId: string }>();
  const { loadMessages, sendMessage, currentUser, markChatRead, chats } = useStore();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [text, setText] = useState('');
  const scrollRef = useRef<HTMLDivElement>(null);
  const chat = chats.find(c => c.id === chatId);

  useEffect(() => {
    if (!chatId) return;
    loadMessages(chatId).then(msgs => {
      setMessages(msgs as ChatMessage[]);
    });
    markChatRead(chatId);
  }, [chatId]);

  useEffect(() => {
    setTimeout(() => {
      scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
    }, 50);
  }, [messages]);

  const handleSend = async () => {
    if (!text.trim() || !chatId) return;
    const msg = await sendMessage(chatId, text.trim());
    if (msg) {
      setMessages([...messages, msg as ChatMessage]);
      setText('');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col" style={{ height: '100vh' }}>
      <TopBar title={chat?.otherUser?.username || '聊天'} showBack />

      <div ref={scrollRef} className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
        {messages.length === 0 && (
          <div className="text-center text-text-tertiary text-sm py-20">暂无消息，发送第一条消息吧~</div>
        )}
        {messages.map(m => {
          const isMe = m.senderId === currentUser?.id;
          const isSystem = m.senderId === 'system_assistant';
          let avatarUser: any = chat?.otherUser;
          if (isMe) avatarUser = currentUser;
          else if (m.sender) avatarUser = m.sender;
          else if (isSystem) avatarUser = { id: 'system_assistant', username: '文书小助手', avatar: null, isVip: false, vipLevel: 0 };

          return (
            <div key={m.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'} gap-2`}>
              {!isMe && avatarUser && (
                <Avatar src={avatarUser.avatar} username={avatarUser.username} size="sm" isVip={avatarUser.isVip} vipLevel={avatarUser.vipLevel} />
              )}
              <div className={`max-w-[70%] flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                <div className={`px-4 py-2.5 rounded-2xl text-sm ${
                  isMe ? 'bg-black text-white rounded-br-md' :
                  isSystem ? 'bg-gold/10 text-text-primary rounded-bl-md border border-gold/20' :
                  'bg-white text-text-primary rounded-bl-md shadow-sm'
                }`}>
                  {m.content}
                </div>
                <p className={`text-[10px] text-text-hint mt-1 ${isMe ? 'text-right' : ''}`}>{formatTime(m.createdAt)}</p>
              </div>
            </div>
          );
        })}
      </div>

      <div className="bg-white border-t border-divider px-4 py-2">
        <div className="flex items-center gap-2 max-w-[480px] mx-auto">
          <input
            type="text"
            value={text}
            onChange={e => setText(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSend()}
            placeholder="说点什么..."
            className="flex-1 bg-gray-100 rounded-full px-4 py-2.5 text-sm outline-none focus:bg-white focus:ring-2 focus:ring-black/5"
          />
          <button
            onClick={handleSend}
            disabled={!text.trim()}
            className="w-10 h-10 bg-black rounded-full flex items-center justify-center text-white disabled:opacity-40 active:scale-90 transition-transform"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
