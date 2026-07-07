import type { AppNotification } from '@/types';

const now = Date.now();
const minute = 60 * 1000;
const hour = 60 * minute;

function minsAgo(n: number): number {
  return now - n * minute;
}

function hoursAgoNotif(n: number): number {
  return now - n * hour;
}

export const notifications: AppNotification[] = [
  {
    id: 'notif_1',
    type: 'system',
    content: '欢迎加入文书APP！新人礼包已发放至您的账户，快来领取500文书币吧！',
    isRead: false,
    createdAt: minsAgo(5),
  },
  {
    id: 'notif_2',
    type: 'like',
    fromUserId: 'user_qingfeng',
    postId: 'post_1',
    content: '赞了你的帖子',
    isRead: false,
    createdAt: minsAgo(23),
  },
  {
    id: 'notif_3',
    type: 'follow',
    fromUserId: 'user_moxiang',
    content: '关注了你',
    isRead: false,
    createdAt: minsAgo(45),
  },
  {
    id: 'notif_4',
    type: 'comment',
    fromUserId: 'user_chihuo',
    postId: 'post_3',
    content: '评论了你的帖子：看起来好好吃！求教程啊！',
    isRead: false,
    createdAt: hoursAgoNotif(1),
  },
  {
    id: 'notif_5',
    type: 'like',
    fromUserId: 'user_xiaoxiao',
    postId: 'post_2',
    content: '赞了你的帖子',
    isRead: true,
    createdAt: hoursAgoNotif(2),
  },
  {
    id: 'notif_6',
    type: 'like',
    fromUserId: 'user_shiheyuanfang',
    postId: 'post_5',
    content: '赞了你的帖子',
    isRead: true,
    createdAt: hoursAgoNotif(3),
  },
  {
    id: 'notif_7',
    type: 'comment',
    fromUserId: 'user_xueba',
    postId: 'post_4',
    content: '评论了你的帖子：一起加油！考研人冲冲冲！',
    isRead: true,
    createdAt: hoursAgoNotif(5),
  },
  {
    id: 'notif_8',
    type: 'follow',
    fromUserId: 'user_yese',
    content: '关注了你',
    isRead: true,
    createdAt: hoursAgoNotif(8),
  },
  {
    id: 'notif_9',
    type: 'system',
    content: '您参与的「夏日摄影大赛」活动正在火热进行中，快去发帖参赛赢取5000文书币大奖吧！',
    isRead: true,
    createdAt: hoursAgoNotif(12),
  },
  {
    id: 'notif_10',
    type: 'like',
    fromUserId: 'user_yangguang',
    postId: 'post_6',
    content: '赞了你的帖子',
    isRead: true,
    createdAt: hoursAgoNotif(18),
  },
  {
    id: 'notif_11',
    type: 'comment',
    fromUserId: 'user_moxiang',
    postId: 'post_2',
    content: '评论了你的帖子：写得真好！百年孤独我也很喜欢，每年都会重读一遍',
    isRead: true,
    createdAt: hoursAgoNotif(24),
  },
  {
    id: 'notif_12',
    type: 'system',
    content: '签到提醒：今天还没有签到哦，连续签到可以获得更多文书币奖励～',
    isRead: true,
    createdAt: hoursAgoNotif(36),
  },
  {
    id: 'notif_13',
    type: 'follow',
    fromUserId: 'user_chihuo',
    content: '关注了你',
    isRead: true,
    createdAt: hoursAgoNotif(48),
  },
  {
    id: 'notif_14',
    type: 'redeem_success',
    content: '兑换成功！您已成功使用兑换码，获得200文书币奖励。',
    isRead: true,
    createdAt: hoursAgoNotif(60),
  },
];
