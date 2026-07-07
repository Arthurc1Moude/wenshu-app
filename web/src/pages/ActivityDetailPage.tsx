import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { useStore } from '@/store';
import { Calendar, Users, Coins, Clock, Flame, Award, Plus, Check } from 'lucide-react';
import { formatCountdown } from '@/utils/format';
import PostCard from '@/components/post/PostCard';

export default function ActivityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { loadActivity, loadPostsByTag, currentUser, isLoggedIn } = useStore();
  const [activity, setActivity] = useState<any>(null);
  const [posts, setPosts] = useState<any[]>([]);

  useEffect(() => {
    if (!id) return;
    loadActivity(id).then(a => {
      setActivity(a);
      if (a) loadPostsByTag(a.hashtag).then(setPosts);
    });
  }, [id]);

  if (!activity) return (
    <div className="min-h-screen bg-white">
      <TopBar title="活动详情" showBack />
      <div className="flex items-center justify-center py-20 text-text-tertiary">加载中...</div>
    </div>
  );

  const hasJoined = activity.participants?.includes(currentUser?.id);

  return (
    <div className="min-h-screen bg-white pb-24">
      <TopBar title="活动详情" showBack />

      <div className="relative aspect-video bg-gray-100">
        <img src={activity.cover} alt="" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-4 left-4 right-4 text-white">
          <span className="inline-block px-2.5 py-1 bg-black/60 backdrop-blur-sm rounded-full text-xs font-medium mb-2">
            {activity.status === 'active' ? '进行中' : activity.status === 'upcoming' ? '即将开始' : '已结束'}
          </span>
          <h1 className="text-xl font-bold">{activity.title}</h1>
        </div>
      </div>

      <div className="p-4">
        <div className="flex items-center gap-4 text-sm text-text-secondary">
          <span className="flex items-center gap-1"><Users className="w-4 h-4" />{activity.participantCount}人参与</span>
          <span className="flex items-center gap-1"><Coins className="w-4 h-4 text-gold" />{activity.rewardCoins}币</span>
          {activity.status === 'active' && (
            <span className="flex items-center gap-1 text-danger"><Clock className="w-4 h-4" />{formatCountdown(activity.endDate)}</span>
          )}
        </div>

        <div className="mt-4 flex items-center gap-2 bg-gold/5 rounded-xl p-3 border border-gold/20">
          <Flame className="w-5 h-5 text-danger" />
          <span className="text-sm font-medium">参与方式：发布帖子带话题 </span>
          <span className="text-sm font-bold text-danger">#{activity.hashtag}</span>
        </div>

        {activity.status === 'active' && (
          <button
            onClick={() => {
              if (!isLoggedIn) { navigate('/login'); return; }
              navigate('/publish', { state: { tag: activity.hashtag } });
            }}
            className={`w-full mt-4 py-3.5 rounded-xl font-medium text-sm flex items-center justify-center gap-2 transition-all ${
              hasJoined ? 'bg-gray-100 text-text-secondary' : 'bg-black text-white active:scale-95'
            }`}
          >
            {hasJoined ? <><Check className="w-4 h-4" />已参与</> : <><Plus className="w-4 h-4" />发布参与帖子</>}
          </button>
        )}

        <div className="mt-6">
          <h2 className="font-bold text-base mb-3">活动规则</h2>
          <div className="text-sm text-text-secondary leading-relaxed whitespace-pre-wrap bg-gray-50 rounded-xl p-4">
            {activity.description}
          </div>
        </div>

        {activity.rewardCoins > 0 && (
          <div className="mt-4 bg-gradient-to-r from-gold/10 to-gold/5 rounded-xl p-4 border border-gold/20">
            <h3 className="font-bold text-sm flex items-center gap-2 mb-2"><Award className="w-4 h-4 text-gold" />活动奖励</h3>
            <p className="text-xs text-text-secondary">
              优质内容创作者将获得{activity.rewardCoins}文书币奖励，还有机会获得文书会体验资格！
            </p>
          </div>
        )}
      </div>

      <div className="border-t-8 border-gray-50"></div>

      <div className="px-4 pt-4">
        <h3 className="font-bold text-base mb-3 flex items-center gap-2">
          <Flame className="w-4 h-4 text-danger" />
          #{activity.hashtag} 相关帖子
        </h3>
        {posts.length === 0 ? (
          <p className="text-center py-10 text-text-tertiary text-sm">暂无相关帖子，快来参与吧！</p>
        ) : (
          posts.map((p, i) => <PostCard key={p.id} post={p} index={i} />)
        )}
      </div>
    </div>
  );
}
