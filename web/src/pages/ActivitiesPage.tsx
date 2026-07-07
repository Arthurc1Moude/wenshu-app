import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';
import { Calendar, Users, Coins, Clock, Flame } from 'lucide-react';
import { formatCountdown } from '@/utils/format';
import type { Activity } from '@/types';

function ActivityCard({ activity }: { activity: Activity }) {
  const navigate = useNavigate();
  const statusMap = {
    active: { label: '进行中', color: 'bg-black text-white' },
    upcoming: { label: '即将开始', color: 'bg-gray-100 text-text-secondary' },
    ended: { label: '已结束', color: 'bg-gray-200 text-text-tertiary' },
  };
  const st = statusMap[activity.status];

  return (
    <div onClick={() => navigate(`/activity/${activity.id}`)} className="bg-white rounded-2xl overflow-hidden shadow-sm active:scale-[0.98] transition-transform cursor-pointer">
      <div className="relative aspect-video bg-gray-100">
        <img src={activity.cover} alt={activity.title} className="w-full h-full object-cover" />
        <div className="absolute top-3 left-3">
          <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${st.color}`}>{st.label}</span>
        </div>
        {activity.status === 'active' && (
          <div className="absolute bottom-3 right-3 bg-black/70 text-white text-xs px-2 py-1 rounded-lg backdrop-blur-sm flex items-center gap-1">
            <Clock className="w-3 h-3" />
            {formatCountdown(activity.endDate)}
          </div>
        )}
      </div>
      <div className="p-4">
        <h3 className="font-bold text-base text-text-primary mb-2">{activity.title}</h3>
        <div className="flex items-center gap-1 mb-3">
          <Flame className="w-3.5 h-3.5 text-danger" />
          <span className="text-xs text-danger font-medium">#{activity.hashtag}</span>
        </div>
        <div className="flex items-center gap-4 text-xs text-text-secondary">
          <span className="flex items-center gap-1"><Users className="w-3.5 h-3.5" />{activity.participantCount}人参与</span>
          <span className="flex items-center gap-1"><Coins className="w-3.5 h-3.5 text-gold" />{activity.rewardCoins}币奖励</span>
        </div>
      </div>
    </div>
  );
}

export default function ActivitiesPage() {
  const { activities, loadActivities } = useStore();
  const [filter, setFilter] = useState<'all' | 'active' | 'ended' | 'upcoming'>('all');
  const navigate = useNavigate();

  useEffect(() => { loadActivities(filter === 'all' ? undefined : filter); }, [filter]);

  const filtered = activities;

  return (
    <div className="pb-24">
      <TopBar title="活动" />

      <div className="sticky top-14 z-20 bg-white border-b border-divider">
        <div className="flex gap-2 px-4 py-3 max-w-[480px] mx-auto overflow-x-auto scrollbar-hide">
          {([
            { key: 'all', label: '全部' },
            { key: 'active', label: '进行中' },
            { key: 'upcoming', label: '即将开始' },
            { key: 'ended', label: '已结束' },
          ] as const).map(f => (
            <button
              key={f.key}
              onClick={() => setFilter(f.key)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                filter === f.key ? 'bg-black text-white' : 'bg-gray-100 text-text-secondary'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      <div className="p-4 space-y-4">
        {filtered.length === 0 ? (
          <div className="text-center py-20 text-text-tertiary">
            <Calendar className="w-12 h-12 mx-auto mb-3 opacity-30" />
            <p className="text-sm">暂无活动</p>
          </div>
        ) : (
          filtered.map(a => <ActivityCard key={a.id} activity={a} />)
        )}
      </div>
    </div>
  );
}
