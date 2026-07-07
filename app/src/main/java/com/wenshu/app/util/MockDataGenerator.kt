package com.wenshu.app.util

import com.wenshu.app.data.model.Comment
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.data.model.NotificationType
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.User
import kotlin.random.Random

object MockDataGenerator {

    private val usernames = listOf(
        "小文同学", "旅行的鱼", "美食探店家", "穿搭日记", "美妆博主Lily",
        "学习打卡", "职场新人", "生活记录者", "摄影爱好者", "读书分享",
        "健身达人", "甜品制作", "家居改造", "数码测评", "电影推荐",
        "音乐现场", "艺术展览", "手工DIY", "宠物日常", "植物养护",
        "咖啡时光", "烘焙小屋", "穿搭学院", "护肤笔记", "旅行日记",
        "减脂餐单", "考研上岸", "副业赚钱", "租房改造", "办公室好物"
    )

    private val postTitles = listOf(
        "超详细的周末探店攻略｜这家咖啡店真的绝了",
        "2024年必买的10件单品｜学生党平价好物",
        "一个人住30平小屋｜改造前后对比",
        "自学编程6个月｜我的学习路线分享",
        "减脂期一周三餐记录｜已瘦15斤",
        "重庆3天2夜旅行攻略｜本地人推荐",
        "敏感肌护肤心得｜这些年踩过的坑",
        "极简穿搭公式｜5件衣服穿一个月",
        "月薪5k如何存钱｜我的理财方法",
        "新手化妆教程｜日常伪素颜妆",
        "租房必买的20件神器｜不踩雷",
        "这家面馆我吃了10年｜真心推荐",
        "考研英语85分｜我的备考经验",
        "猫咪饲养指南｜新手铲屎官必看",
        "在家也能做的米其林甜品",
        "夏日穿搭合集｜清爽又好看",
        "办公室久坐族必看｜缓解腰痛",
        "日本东京7天自由行攻略",
        "读完这5本书｜我彻底改变了",
        "小户型显大秘诀｜收纳技巧分享",
        "秋冬必备口红色号｜黄皮友好",
        "毕业3年｜我是如何攒到20万的",
        "自制奶茶教程｜比外面卖的还好喝",
        "北京胡同探店｜隐藏的宝藏小店",
        "第一次做vlog｜设备推荐",
        "自律给我自由｜我的晨间routine",
        "学生党化妆包大公开｜百元内好物",
        "30天健身变化｜从胖子到马甲线",
        "上海周末去哪儿｜小众打卡地",
        "零失败烤箱料理｜新手也能做",
        "护肤品空瓶记｜哪些值得回购",
        "租房改造｜500元打造ins风卧室",
        "面试常见问题｜HR不会告诉你的技巧",
        "宠物猫咪日常｜我的主子太可爱了",
        "读书笔记｜《原子习惯》改变人生",
        "旅行拍照技巧｜手机也能出大片",
        "一日三餐吃什么｜快手菜谱合集",
        "平价替代｜大牌口红平替推荐",
        "居家办公好物｜提升幸福感",
        "广州美食攻略｜本地人常去的店",
        "考研政治70+｜背诵技巧分享",
        "手账入门｜新手需要知道的事",
        "100元过一周｜省钱挑战",
        "短发打理教程｜3分钟出门造型",
        "家庭咖啡角｜设备选购指南"
    )

    private val postContents = listOf(
        "今天给大家分享一下我的探店体验。这家店藏在老巷子里，不太好找但是真的值得！\n\n环境：装修很有格调，工业风加一点点复古感，拍照超级出片\n\n服务：店员都很热情，会详细介绍每一款产品\n\n推荐菜品：\n1. 招牌拿铁 - 奶泡细腻，咖啡香气浓郁\n2. 提拉米苏 - 不会太甜，马斯卡彭芝士味很正\n3. 牛油果吐司 - 早餐首选，健康又好吃\n\n人均大概60左右，周末需要排队，建议工作日去~",
        "姐妹们！这些东西真的太好用了，价格还便宜，学生党必入！\n\n1. 某某品牌的防晒霜，SPF50+，不搓泥不泛白\n2. 多功能收纳盒，桌面瞬间整洁\n3. 便携充电宝，轻薄容量大\n4. 静音鼠标，图书馆必备\n5. 蒸汽眼罩，熬夜党救星\n\n都是我用了很久的东西，真心推荐给大家~",
        "从决定改造到完工花了一个月的时间，总花费不到3000元，但是效果真的太满意了！\n\n改造重点：\n1. 墙面刷成了米白色，空间瞬间变大\n2. 添置了一些绿植，增添生气\n3. 窗帘换成了纱帘加遮光帘双层\n4. 买了很多收纳盒，把杂物都藏起来\n5. 灯光很重要，暖色调的灯让家里更温馨",
        "很多人问我是怎么自学编程的，今天就来分享一下我的学习路线。\n\n第一阶段（1-2个月）：\n- Python基础语法\n- 数据结构与算法入门\n\n第二阶段（2-3个月）：\n- Web开发基础（HTML/CSS/JavaScript）\n- 选择一个后端框架深入学习\n\n第三阶段（3-6个月）：\n- 做项目！做项目！做项目！\n- 学习Git、数据库等工具\n\n最重要的是坚持，每天至少写2小时代码。"
    )

    private val tags = listOf(
        "日常分享", "探店打卡", "好物推荐", "穿搭分享", "护肤心得",
        "学习打卡", "职场干货", "美食教程", "旅行攻略", "健身日记",
        "家居改造", "彩妆教程", "平价好物", "减脂餐", "读书分享"
    )

    private val locations = listOf(
        "北京·朝阳区", "上海·静安区", "广州·天河区", "深圳·南山区",
        "杭州·西湖区", "成都·锦江区", "重庆·渝中区", "南京·鼓楼区",
        "武汉·武昌区", "西安·雁塔区", null, null, null, null
    )

    private val avatarColors = listOf(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
    )

    private val imageColors = listOf(
        "#F5E6D3", "#E8D5C4", "#D4E6F1", "#D5F5E3", "#FCF3CF",
        "#FADBD8", "#E8DAEF", "#D6EAF8", "#D1F2EB", "#FDEBD0",
        "#F9E79F", "#ABEBC6", "#F5B7B1", "#D2B4DE", "#AED6F1",
        "#2C3E50", "#34495E", "#1ABC9C", "#E74C3C", "#3498DB",
        "#F39C12", "#9B59B6", "#16A085", "#C0392B", "#2980B9"
    )

    private val commentContents = listOf(
        "太好看了吧！请问是在哪里买的呀？",
        "收藏了！周末就去试试",
        "博主写得好详细，感动😭",
        "同款爱好者举手！",
        "请问这个多少钱呀？",
        "已经去打卡了！真的很棒",
        "拍照太好看了，求滤镜参数",
        "马住！下次一定去",
        "姐妹你也太会搭了吧",
        "这个真的超好用！我也在用",
        "终于找到详细的攻略了！谢谢博主",
        "看起来好好吃的样子🤤",
        "已经收藏了！准备开始执行",
        "姐妹你皮肤好好啊",
        "这个店具体位置在哪里呀？"
    )

    private val systemMessages = listOf(
        "欢迎来到文书APP！开始记录你的美好生活吧~",
        "你发布的笔记《周末探店》获得了100个赞！",
        "恭喜你升级了，继续加油哦~",
        "你的笔记被精选推荐到首页啦！"
    )

    fun generateUsers(count: Int = 30): List<User> {
        return (1..count).map { i ->
            val nickname = usernames[(i - 1) % usernames.size]
            val colorIndex = (i - 1) % avatarColors.size
            User(
                id = "user_$i",
                username = "user$i",
                nickname = if (i <= usernames.size) nickname else "${nickname}$i",
                avatarUrl = "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(nickname, "UTF-8")}&background=${avatarColors[colorIndex].replace("#", "")}&color=fff&size=200",
                bio = "热爱生活，分享美好 ✨",
                followersCount = Random.nextInt(10, 100000),
                followingCount = Random.nextInt(10, 1000),
                postsCount = Random.nextInt(5, 200),
                totalLikesCount = Random.nextInt(100, 50000),
                isVerified = Random.nextBoolean() && i <= 10,
                isFollowing = Random.nextBoolean()
            )
        }
    }

    fun generatePosts(users: List<User>, count: Int = 50): List<Post> {
        return (1..count).map { i ->
            val author = users[Random.nextInt(users.size)]
            val title = postTitles[(i - 1) % postTitles.size]
            val contentIndex = (i - 1) % postContents.size
            val colorIndex = (i - 1) % imageColors.size
            val isVideo = Random.nextBoolean() && i % 5 == 0
            val width = 400
            val height = if (i % 3 == 0) 500 else if (i % 3 == 1) 600 else 400

            val tagCount = Random.nextInt(1, 4)
            val postTags = tags.shuffled().take(tagCount)

            Post(
                id = "post_$i",
                author = author,
                title = if (i > postTitles.size) "${title}（${i / postTitles.size + 1}）" else title,
                content = postContents[contentIndex],
                coverImageUrl = "https://picsum.photos/seed/wenshu$i/$width/$height",
                imageUrls = listOf("https://picsum.photos/seed/wenshu${i}_1/800/600"),
                videoUrl = if (isVideo) "https://example.com/video_$i.mp4" else null,
                videoDuration = if (isVideo) Random.nextLong(10, 300) else null,
                tags = postTags,
                location = locations[Random.nextInt(locations.size)],
                likeCount = Random.nextInt(10, 50000),
                commentCount = Random.nextInt(0, 500),
                collectCount = Random.nextInt(0, 10000),
                shareCount = Random.nextInt(0, 1000),
                isLiked = Random.nextBoolean(),
                isCollected = Random.nextBoolean(),
                isFollowed = author.isFollowing,
                category = listOf("recommend", "follow", "food", "travel", "fashion", "life", "study")[Random.nextInt(7)],
                coverWidth = width,
                coverHeight = height,
                createdAt = System.currentTimeMillis() - Random.nextLong(0, 30L * 24 * 60 * 60 * 1000)
            )
        }
    }

    fun generateComments(users: List<User>, postId: String, count: Int = 10): List<Comment> {
        return (1..count).map { i ->
            val author = users[Random.nextInt(users.size)]
            val hasReplies = Random.nextBoolean() && i % 3 == 0
            val replies = if (hasReplies) {
                (1..Random.nextInt(1, 4)).map { r ->
                    val replyUser = users[Random.nextInt(users.size)]
                    val replyToIdx = Random.nextInt(users.size)
                    Comment(
                        id = "comment_${postId}_${i}_$r",
                        postId = postId,
                        author = replyUser,
                        content = commentContents[Random.nextInt(commentContents.size)],
                        likeCount = Random.nextInt(0, 100),
                        isLiked = Random.nextBoolean(),
                        parentId = "comment_${postId}_$i",
                        replyToUser = users[replyToIdx].nickname,
                        createdAt = System.currentTimeMillis() - Random.nextLong(0, 7L * 24 * 60 * 60 * 1000)
                    )
                }
            } else emptyList()

            Comment(
                id = "comment_${postId}_$i",
                postId = postId,
                author = author,
                content = commentContents[Random.nextInt(commentContents.size)],
                likeCount = Random.nextInt(0, 500),
                replyCount = replies.size,
                isLiked = Random.nextBoolean(),
                replies = replies,
                createdAt = System.currentTimeMillis() - Random.nextLong(0, 30L * 24 * 60 * 60 * 1000)
            )
        }
    }

    fun generateNotifications(users: List<User>, count: Int = 30): List<NotificationItem> {
        return (1..count).map { i ->
            val type = when {
                i % 6 == 0 -> NotificationType.SYSTEM
                i % 5 == 0 -> NotificationType.FOLLOW
                i % 4 == 0 -> NotificationType.COLLECT
                i % 3 == 0 -> NotificationType.COMMENT
                else -> NotificationType.LIKE
            }
            val user = if (type != NotificationType.SYSTEM) users[Random.nextInt(users.size)] else null
            val postIdx = Random.nextInt(1, 50)

            NotificationItem(
                id = "notif_$i",
                type = type,
                user = user,
                postId = if (type != NotificationType.SYSTEM && type != NotificationType.FOLLOW) "post_$postIdx" else null,
                postCoverUrl = if (type != NotificationType.SYSTEM && type != NotificationType.FOLLOW)
                    "https://picsum.photos/seed/wenshu$postIdx/200/200" else null,
                content = when (type) {
                    NotificationType.LIKE -> "${user?.nickname} 赞了你的笔记"
                    NotificationType.COMMENT -> "${user?.nickname} 评论了你的笔记：${commentContents[Random.nextInt(commentContents.size)]}"
                    NotificationType.COLLECT -> "${user?.nickname} 收藏了你的笔记"
                    NotificationType.FOLLOW -> "${user?.nickname} 关注了你"
                    NotificationType.MENTION -> "${user?.nickname} 在评论中@了你"
                    NotificationType.SYSTEM -> systemMessages[Random.nextInt(systemMessages.size)]
                },
                commentContent = if (type == NotificationType.COMMENT) commentContents[Random.nextInt(commentContents.size)] else null,
                isRead = i > 10,
                createdAt = System.currentTimeMillis() - Random.nextLong(0, 7L * 24 * 60 * 60 * 1000)
            )
        }
    }

    fun getCurrentUser(): User {
        return User(
            id = "current_user",
            username = "wenshu_user",
            nickname = "文书用户",
            avatarUrl = "https://ui-avatars.com/api/?name=%E6%96%87%E4%B9%A6&background=000000&color=fff&size=200",
            bio = "记录生活，分享美好 | 文书APP创作者",
            followersCount = 128,
            followingCount = 256,
            postsCount = 12,
            totalLikesCount = 1892,
            isVerified = false,
            isFollowing = false
        )
    }

    fun getCategories(): List<Triple<String, String, String>> {
        return listOf(
            Triple("recommend", "推荐", "category_recommend"),
            Triple("follow", "关注", "category_follow"),
            Triple("nearby", "附近", "category_nearby"),
            Triple("food", "美食", "category_food"),
            Triple("travel", "旅行", "category_travel"),
            Triple("fashion", "穿搭", "category_fashion"),
            Triple("beauty", "美妆", "category_beauty"),
            Triple("life", "生活", "category_life"),
            Triple("study", "学习", "category_study"),
            Triple("work", "职场", "category_work")
        )
    }

    fun getHotSearches(): List<String> {
        return listOf(
            "夏日穿搭", "减脂餐食谱", "租房改造", "考研经验",
            "咖啡推荐", "平价好物", "旅行攻略", "化妆教程",
            "副业推荐", "读书笔记", "健身计划", "甜品配方",
            "护肤心得", "数码测评", "周末好去处"
        )
    }
}
