package cn.linguaverse.seed;

import java.util.List;

import static cn.linguaverse.seed.ContentSpecs.*;

/** 英语 L1–L3 语料，共 3 级 × 2 单元 × 2 课时。 */
public final class EnContent {
    private EnContent() {}

    public static List<Level> levels() {
        return List.of(
            new Level(1, "入门", List.of(
                new Unit("第一次开口", "First Words", List.of(greetings(), numbers())),
                new Unit("我的世界", "My World", List.of(family(), objects()))
            )),
            new Level(2, "进阶", List.of(
                new Unit("城市生活", "City Life", List.of(coffeeShop(), onTheStreet())),
                new Unit("安排与沟通", "Making Plans", List.of(weekendPlans(), onThePhone()))
            )),
            new Level(3, "中阶", List.of(
                new Unit("观点表达", "Expressing Views", List.of(movieReview(), travelBlog())),
                new Unit("职场与新闻", "Work & News", List.of(jobInterview(), newsSnippet()))
            ))
        );
    }

    // ---------------- L1 ----------------

    static Lesson greetings() {
        return new Lesson("l1-u1-l1", "见面问候", "Greetings", "机场到达大厅，留学生 Emma 第一次与室友见面",
            List.of(
                v("hello", "/həˈləʊ/", "你好", "Hello, nice to meet you.", "你好，很高兴见到你。"),
                v("goodbye", "/ˌɡʊdˈbaɪ/", "再见", "Goodbye, see you tomorrow.", "再见，明天见。"),
                v("thanks", "/θæŋks/", "谢谢", "Thanks for your help.", "谢谢你的帮助。"),
                v("sorry", "/ˈsɒri/", "对不起", "Sorry, I'm late.", "抱歉，我迟到了。"),
                v("please", "/pliːz/", "请", "Please sit down.", "请坐。"),
                v("yes", "/jes/", "是", "Yes, that's right.", "是的，没错。"),
                v("no", "/nəʊ/", "不", "No, thank you.", "不用了，谢谢。"),
                v("name", "/neɪm/", "名字", "My name is Emma.", "我叫 Emma。")
            ),
            "be 动词：I am / You are",
            "am 只和 I 搭配，are 与 you/we/they 搭配。I'm 是 I am 的缩写，What's = What is。",
            List.of(
                q("___ a new student.", List.of("I are", "I am", "I is", "I be"), 1, "主语 I 固定搭配 am。"),
                q("—How are you? —___ fine, thanks.", List.of("I'm", "You are", "Is", "Am"), 0, "回答自己的状态用 I'm。"),
                q("What ___ your name?", List.of("are", "am", "is", "be"), 2, "your name 是第三人称单数，用 is。"),
                q("Nice ___ meet you.", List.of("to", "for", "at", "of"), 0, "固定句型 Nice to meet you。")
            ),
            new Listening("初次见面", List.of(
                l("Emma", "Hi, I'm Emma. Are you Tom?", "你好，我是 Emma。你是 Tom 吗？"),
                l("Tom", "Yes, I am! Nice to meet you.", "是的！很高兴见到你。"),
                l("Emma", "Nice to meet you too. Sorry I'm late.", "我也很高兴。抱歉我迟到了。"),
                l("Tom", "No problem. Let's go in.", "没关系。我们进去吧。")
            ), "Why does Emma say sorry?",
                List.of("She lost her bag.", "She is late.", "She forgot Tom's name.", "She is sick."), 1,
                "Emma 说 Sorry I'm late，因为迟到而道歉。"),
            List.of(
                s("Hi, I'm Emma. Nice to meet you.", "嗨，我是 Emma，很高兴见到你。"),
                s("How are you today?", "你今天好吗？"),
                s("I'm fine, thanks. And you?", "我很好，谢谢。你呢？"),
                s("Sorry, I'm late.", "抱歉，我迟到了。"),
                s("Goodbye, see you tomorrow.", "再见，明天见。")
            ));
    }

    static Lesson numbers() {
        return new Lesson("l1-u1-l2", "数字与时间", "Numbers & Time", "在便利店看价格、问营业时间",
            List.of(
                v("one", "/wʌn/", "一", "One coffee, please.", "请来一杯咖啡。"),
                v("two", "/tuː/", "二", "I have two tickets.", "我有两张票。"),
                v("ten", "/ten/", "十", "It costs ten dollars.", "这个十美元。"),
                v("morning", "/ˈmɔːnɪŋ/", "早上", "Good morning, everyone.", "大家早上好。"),
                v("night", "/naɪt/", "夜晚", "Good night, sleep well.", "晚安，睡个好觉。"),
                v("today", "/təˈdeɪ/", "今天", "Today is Monday.", "今天是星期一。"),
                v("tomorrow", "/təˈmɒrəʊ/", "明天", "See you tomorrow.", "明天见。"),
                v("time", "/taɪm/", "时间", "What time is it?", "现在几点？")
            ),
            "What time 疑问句",
            "询问时间用 What time is it? 回答用 It's + 数字（+o'clock）。数字 13–19 以 -teen 结尾，整十以 -ty 结尾。",
            List.of(
                q("—What time is it? —___ nine o'clock.", List.of("It", "It's", "Its", "Is"), 1, "表示“现在是…”用 It's。"),
                q("Three plus four equals ___.", List.of("six", "seven", "eight", "nine"), 1, "3+4=7。"),
                q("We open ___ 8 in the morning.", List.of("on", "in", "at", "to"), 2, "具体钟点前用 at。"),
                q("见面对方说 Good morning，你应回答：", List.of("Good night.", "Good morning.", "Goodbye.", "Good luck."), 1, "问候语原样回应 Good morning。")
            ),
            new Listening("便利店结账", List.of(
                l("Emma", "Excuse me, what time do you close?", "请问你们几点关门？"),
                l("Clerk", "We close at ten o'clock at night.", "我们晚上十点关门。"),
                l("Emma", "OK. Two bottles of water, please.", "好的，请来两瓶水。"),
                l("Clerk", "That's four dollars.", "一共四美元。")
            ), "How much is the water in total?",
                List.of("Two dollars.", "Four dollars.", "Ten dollars.", "Eight dollars."), 1,
                "店员说 That's four dollars，总价四美元。"),
            List.of(
                s("What time is it now?", "现在几点？"),
                s("It's nine o'clock in the morning.", "现在是早上九点。"),
                s("We close at ten at night.", "我们晚上十点关门。"),
                s("Two coffees, please.", "请来两杯咖啡。"),
                s("See you tomorrow morning.", "明天早上见。")
            ));
    }

    static Lesson family() {
        return new Lesson("l1-u2-l1", "我的家人", "My Family", "Emma 在宿舍向室友展示全家福",
            List.of(
                v("mother", "/ˈmʌðə/", "母亲", "My mother is a teacher.", "我妈妈是老师。"),
                v("father", "/ˈfɑːðə/", "父亲", "Her father works in a hospital.", "她父亲在医院工作。"),
                v("sister", "/ˈsɪstə/", "姐妹", "I have one sister.", "我有一个姐姐。"),
                v("brother", "/ˈbrʌðə/", "兄弟", "My brother is taller than me.", "我哥哥比我高。"),
                v("friend", "/frend/", "朋友", "She is my best friend.", "她是我最好的朋友。"),
                v("family", "/ˈfæməli/", "家庭", "I love my family.", "我爱我的家人。"),
                v("this", "/ðɪs/", "这个", "This is my mom.", "这是我妈妈。"),
                v("who", "/huː/", "谁", "Who is that boy?", "那个男孩是谁？")
            ),
            "This is... 与 Who 疑问句",
            "介绍近处的人用 This is...；询问“是谁”用 Who is...，回答用 He's / She's。",
            List.of(
                q("___ is my sister, Lily.", List.of("These", "This", "They", "Those"), 1, "介绍单个人用 This is。"),
                q("—Who is he? —___ my brother.", List.of("His", "She's", "He's", "Her"), 2, "男性用 He's（=He is）。"),
                q("My mother and father are my ___.", List.of("friends", "parents", "sisters", "brothers"), 1, "mother 和 father 合称 parents。"),
                q("—Is this your friend? —Yes, ___.", List.of("it is", "she isn't", "I am", "you are"), 0, "Is this...? 肯定回答 Yes, it is。")
            ),
            new Listening("看全家福", List.of(
                l("Tom", "Who is this in the photo?", "照片里这位是谁？"),
                l("Emma", "This is my mother, and that's my father.", "这是我妈妈，那是我爸爸。"),
                l("Tom", "Is the girl your sister?", "这个女孩是你姐姐吗？"),
                l("Emma", "Yes, she is. Her name is Anna.", "是的，她叫 Anna。")
            ), "How many people does Emma point out?",
                List.of("Two.", "Three.", "Four.", "Five."), 1,
                "Emma 指了 mother、father 和 sister，共三人。"),
            List.of(
                s("This is my family.", "这是我的家人。"),
                s("Who is that woman?", "那位女士是谁？"),
                s("She is my mother.", "她是我妈妈。"),
                s("I have one brother and one sister.", "我有一个哥哥和一个姐姐。"),
                s("We are a happy family.", "我们是幸福的一家人。")
            ));
    }

    static Lesson objects() {
        return new Lesson("l1-u2-l2", "日常物品", "Everyday Things", "在教室里询问文具和物品归属",
            List.of(
                v("book", "/bʊk/", "书", "This book is interesting.", "这本书很有趣。"),
                v("pen", "/pen/", "钢笔", "Can I borrow your pen?", "能借一下你的笔吗？"),
                v("bag", "/bæɡ/", "包", "My bag is under the desk.", "我的包在桌子下面。"),
                v("phone", "/fəʊn/", "手机", "Your phone is ringing.", "你的手机在响。"),
                v("key", "/kiː/", "钥匙", "Where are my keys?", "我的钥匙在哪儿？"),
                v("water", "/ˈwɔːtə/", "水", "I need a glass of water.", "我需要一杯水。"),
                v("desk", "/desk/", "书桌", "The book is on the desk.", "书在书桌上。"),
                v("what", "/wɒt/", "什么", "What is this?", "这是什么？")
            ),
            "What's this? 与 a/an",
            "询问物品用 What's this? 回答 It's a/an...；辅音音素前用 a，元音音素前用 an。",
            List.of(
                q("—What's this? —It's ___ umbrella.", List.of("a", "an", "the", "不填"), 1, "umbrella 以元音音素开头，用 an。"),
                q("I have ___ pen and two books.", List.of("an", "a", "the", "不填"), 1, "pen 以辅音音素 /p/ 开头，用 a。"),
                q("—Is this your book? —No, it's ___.", List.of("not", "no", "don't", "am not"), 0, "Is this...? 否定简答为 No, it isn't / it's not。"),
                q("Where ___ my keys?", List.of("is", "am", "are", "be"), 2, "keys 是复数，用 are。")
            ),
            new Listening("找东西", List.of(
                l("Emma", "I can't find my keys.", "我找不到钥匙了。"),
                l("Tom", "Are they in your bag?", "它们在你包里吗？"),
                l("Emma", "No... wait, they're on my desk.", "不在……等等，在我书桌上。"),
                l("Tom", "And your phone is in your hand!", "而你的手机就在你手里！")
            ), "Where are the keys?",
                List.of("In the bag.", "On the desk.", "In the hand.", "Under the book."), 1,
                "Emma 说 they're on my desk。"),
            List.of(
                s("What's this in English?", "这个用英语怎么说？"),
                s("It's a book.", "这是一本书。"),
                s("Is this your pen?", "这是你的笔吗？"),
                s("Where is my bag?", "我的包在哪里？"),
                s("Your phone is on the desk.", "你的手机在书桌上。")
            ));
    }

    // ---------------- L2 ----------------

    static Lesson coffeeShop() {
        return new Lesson("l2-u1-l3", "咖啡店点单", "At the Coffee Shop", "上班前在咖啡店点咖啡和点心",
            List.of(
                v("order", "/ˈɔːdə/", "点餐；订单", "Can I order now?", "我可以点餐了吗？"),
                v("coffee", "/ˈkɒfi/", "咖啡", "A cup of coffee, please.", "请来一杯咖啡。"),
                v("sugar", "/ˈʃʊɡə/", "糖", "No sugar, thanks.", "不加糖，谢谢。"),
                v("milk", "/mɪlk/", "牛奶", "Would you like milk?", "要加牛奶吗？"),
                v("hot", "/hɒt/", "热的", "Be careful, it's hot.", "小心，很烫。"),
                v("cold", "/kəʊld/", "冷的", "I prefer cold drinks.", "我更喜欢冷饮。"),
                v("here", "/hɪə/", "这里", "For here or to go?", "堂食还是外带？"),
                v("change", "/tʃeɪndʒ/", "找零", "Keep the change.", "不用找零了。")
            ),
            "情态动词 can / would like 礼貌请求",
            "Can I have...? 用于点单或请求；Would you like...? 是店员礼貌询问，回答 Yes, please / No, thanks。",
            List.of(
                q("Can I ___ a latte, please?", List.of("have", "has", "having", "had"), 0, "can 后接动词原形。"),
                q("—Would you like sugar? —___", List.of("Yes, I like.", "No, thanks.", "No, I wouldn't like.", "Yes, I would."), 1, "礼貌拒绝标准答语 No, thanks。"),
                q("For here ___ to go?", List.of("and", "or", "but", "so"), 1, "二选一疑问句用 or。"),
                q("—How ___ is it? —Four dollars.", List.of("many", "much", "cost", "money"), 1, "问价格用 How much。")
            ),
            new Listening("点早餐", List.of(
                l("Barista", "Hi, what can I get for you?", "您好，想喝点什么？"),
                l("Emma", "Can I have a hot latte, please?", "请给我一杯热拿铁。"),
                l("Barista", "Sure. Would you like sugar or milk?", "好的，加糖还是牛奶？"),
                l("Emma", "Just milk, no sugar. For here.", "只加牛奶，不加糖。堂食。")
            ), "What does Emma order?",
                List.of("An iced coffee with sugar.", "A hot latte with milk.", "A cold tea to go.", "A black coffee."), 1,
                "Emma 点了 hot latte，只要牛奶。"),
            List.of(
                s("Can I have a latte, please?", "请给我一杯拿铁。"),
                s("Would you like anything else?", "还要别的吗？"),
                s("No, thank you. That's all.", "不用了，谢谢，就这些。"),
                s("For here or to go?", "堂食还是外带？"),
                s("How much is it together?", "一共多少钱？")
            ));
    }

    static Lesson onTheStreet() {
        return new Lesson("l2-u1-l4", "街头问路", "On the Street", "向路人询问地铁站怎么走",
            List.of(
                v("street", "/striːt/", "街道", "The bank is on this street.", "银行就在这条街上。"),
                v("turn", "/tɜːn/", "转弯", "Turn left at the corner.", "在拐角处左转。"),
                v("left", "/left/", "左边", "The shop is on the left.", "商店在左边。"),
                v("right", "/raɪt/", "右边", "Take the second right.", "在第二个路口右转。"),
                v("near", "/nɪə/", "在附近", "The station is near here.", "车站就在附近。"),
                v("far", "/fɑː/", "远的", "Is it far from here?", "离这儿远吗？"),
                v("map", "/mæp/", "地图", "Let me check the map.", "我看一下地图。"),
                v("station", "/ˈsteɪʃn/", "车站", "Where is the subway station?", "地铁站在哪里？")
            ),
            "祈使句指路",
            "指路常用动词原形开头：Turn left/right, Go straight, Cross the street。礼貌问路用 Could you tell me where... is?",
            List.of(
                q("___ straight for two blocks.", List.of("Going", "Goes", "Go", "Went"), 2, "祈使句用动词原形。"),
                q("The station is ___ the left.", List.of("in", "on", "at", "to"), 1, "在左/右边用 on the left/right。"),
                q("Could you tell me ___ the bank is?", List.of("where", "what", "who", "when"), 0, "问路地点用 where 引导宾语从句。"),
                q("—Is it far? —No, it's just five ___ walk.", List.of("minute", "minutes", "minutes'", "minute's"), 2, "“五分钟的步行”用复数所有格 minutes'。")
            ),
            new Listening("去地铁站", List.of(
                l("Emma", "Excuse me, where is the subway station?", "请问地铁站怎么走？"),
                l("Man", "Go straight and turn left at the bank.", "直走，到银行左转。"),
                l("Emma", "Is it far from here?", "离这儿远吗？"),
                l("Man", "No, it's about a three-minute walk. It's on your right.", "不远，走三分钟左右，在你右边。")
            ), "Where is the station?",
                List.of("Next to the bank on the left.", "After turning left, on the right.", "Across the street.", "Behind the coffee shop."), 1,
                "左转后三分钟路程，在右侧。"),
            List.of(
                s("Excuse me, how can I get to the station?", "请问去车站怎么走？"),
                s("Go straight and turn left.", "直走然后左转。"),
                s("It's on your right.", "它在你的右边。"),
                s("Is it within walking distance?", "走路能到吗？"),
                s("Thank you for your help.", "谢谢你的帮助。")
            ));
    }

    static Lesson weekendPlans() {
        return new Lesson("l2-u2-l5", "周末计划", "Weekend Plans", "和朋友讨论周末打算做什么",
            List.of(
                v("plan", "/plæn/", "计划", "What's your plan for Saturday?", "你周六有什么计划？"),
                v("movie", "/ˈmuːvi/", "电影", "Let's watch a movie.", "我们去看电影吧。"),
                v("park", "/pɑːk/", "公园", "We can have a picnic in the park.", "我们可以去公园野餐。"),
                v("meet", "/miːt/", "见面", "Let's meet at noon.", "我们中午见。"),
                v("busy", "/ˈbɪzi/", "忙碌的", "I'm busy this weekend.", "这个周末我很忙。"),
                v("free", "/friː/", "有空的", "Are you free on Sunday?", "你周日有空吗？"),
                v("maybe", "/ˈmeɪbi/", "也许", "Maybe next time.", "也许下次吧。"),
                v("together", "/təˈɡeðə/", "一起", "Let's cook together.", "我们一起做饭吧。")
            ),
            "be going to 表将来",
            "主语 + am/is/are + going to + 动词原形，表示计划好的打算。疑问句把 be 动词提前。",
            List.of(
                q("I ___ going to visit my parents.", List.of("is", "am", "are", "be"), 1, "主语 I 搭配 am。"),
                q("What ___ you going to do on Sunday?", List.of("are", "is", "do", "am"), 0, "主语 you 搭配 are。"),
                q("We ___ going to watch a movie tonight.", List.of("is", "am", "are", "be"), 2, "主语 we 搭配 are。"),
                q("—Are you free this weekend? —Sorry, I'm ___.", List.of("free", "busy", "going", "sure"), 1, "拒绝时说明自己 busy（忙碌）。")
            ),
            new Listening("约时间", List.of(
                l("Tom", "Are you free on Saturday? Let's see a movie.", "你周六有空吗？去看电影吧。"),
                l("Emma", "Sorry, I'm busy on Saturday.", "抱歉，我周六很忙。"),
                l("Tom", "What about Sunday then?", "那周日呢？"),
                l("Emma", "Sunday is fine. I'm going to cook in the evening, but we can meet at noon.",
                    "周日可以。我晚上要做饭，但我们可以中午见。")
            ), "When do they decide to meet?",
                List.of("Saturday evening.", "Sunday at noon.", "Sunday night.", "Saturday morning."), 1,
                "Emma 周六忙，提议周日中午见面。"),
            List.of(
                s("What are you going to do this weekend?", "这个周末你打算做什么？"),
                s("I'm going to study for the exam.", "我打算复习备考。"),
                s("Are you free on Sunday afternoon?", "你周日下午有空吗？"),
                s("Let's meet in front of the cinema.", "我们在电影院门口见吧。"),
                s("Maybe another time.", "下次吧。")
            ));
    }

    static Lesson onThePhone() {
        return new Lesson("l2-u2-l6", "电话沟通", "On the Phone", "接到预约确认电话并留言",
            List.of(
                v("call", "/kɔːl/", "打电话", "I'll call you later.", "我稍后打给你。"),
                v("message", "/ˈmesɪdʒ/", "留言", "Can I leave a message?", "我可以留言吗？"),
                v("hold", "/həʊld/", "稍等；握住", "Please hold on.", "请稍等。"),
                v("speak", "/spiːk/", "说话", "May I speak to Tom?", "请找 Tom 接电话。"),
                v("repeat", "/rɪˈpiːt/", "重复", "Could you repeat that?", "您能重复一遍吗？"),
                v("number", "/ˈnʌmbə/", "号码", "What's your phone number?", "你的电话号码是多少？"),
                v("confirm", "/kənˈfɜːm/", "确认", "I'm calling to confirm your booking.", "我来电确认您的预约。"),
                v("moment", "/ˈməʊmənt/", "片刻", "Wait a moment, please.", "请稍等片刻。")
            ),
            "现在进行时表示当下",
            "am/is/are + 动词-ing 表示此刻正在发生，电话中常用 I'm calling to...（我打电话是为了……）说明来意。",
            List.of(
                q("Hello, this is Emma ___.", List.of("speak", "speaking", "speaks", "spoke"), 1, "电话自我介绍 This is... speaking。"),
                q("May I ___ to Mr. Lee?", List.of("say", "tell", "speak", "talk to is wrong"), 2, "电话找人用 speak to。"),
                q("I'm ___ to confirm your reservation.", List.of("call", "called", "calling", "calls"), 2, "此刻正在来电用现在进行时。"),
                q("Could you please ___ that more slowly?", List.of("repeat", "repeating", "repeated", "repeats"), 0, "could 后接动词原形。")
            ),
            new Listening("预约确认", List.of(
                l("Receptionist", "Hello, this is Green Clinic. I'm calling to confirm your appointment tomorrow at 10 a.m.",
                    "您好，这里是 Green 诊所，来电确认您明天上午十点的预约。"),
                l("Emma", "Tomorrow at 10? Yes, that's correct.", "明天十点？对，没错。"),
                l("Receptionist", "Great. Please bring your ID card.", "好的，请带好身份证。"),
                l("Emma", "Sure, thank you. See you tomorrow.", "好的，谢谢，明天见。")
            ), "Why does the clinic call?",
                List.of("To change the time.", "To confirm an appointment.", "To ask for an ID number.", "To cancel the visit."), 1,
                "诊所来电是为了 confirm your appointment。"),
            List.of(
                s("Hello, this is Emma speaking.", "您好，我是 Emma。"),
                s("May I speak to Tom, please?", "请找 Tom 接电话好吗？"),
                s("Please hold on a moment.", "请稍等一下。"),
                s("Can I leave a message?", "我可以留言吗？"),
                s("Let me repeat your phone number.", "我重复一下您的电话号码。")
            ));
    }

    // ---------------- L3 ----------------

    static Lesson movieReview() {
        return new Lesson("l3-u1-l7", "影评怎么说", "A Movie Review", "听完播客影评后表达自己的看法",
            List.of(
                v("review", "/rɪˈvjuː/", "评论", "Her review of the film is online.", "她对这部电影的评论在网上。"),
                v("director", "/dəˈrektə/", "导演", "The director won an award.", "这位导演获了奖。"),
                v("actor", "/ˈæktə/", "演员", "He is my favorite actor.", "他是我最喜欢的演员。"),
                v("scene", "/siːn/", "场景", "The final scene is unforgettable.", "最后一幕令人难忘。"),
                v("boring", "/ˈbɔːrɪŋ/", "无聊的", "I found the story boring.", "我觉得剧情很无聊。"),
                v("exciting", "/ɪkˈsaɪtɪŋ/", "激动人心的", "The match was really exciting.", "比赛真的很精彩。"),
                v("recommend", "/ˌrekəˈmend/", "推荐", "I recommend this movie.", "我推荐这部电影。"),
                v("opinion", "/əˈpɪnjən/", "观点", "In my opinion, it's the best this year.", "在我看来，这是今年最佳。")
            ),
            "一般过去时叙事",
            "规则动词过去式加 -ed（watched, recommended），不规则动词需记忆（go→went, see→saw, is→was）。评论常用 In my opinion 引出观点。",
            List.of(
                q("I ___ the film last night and really enjoyed it.", List.of("watch", "watched", "watching", "watches"), 1, "last night 是过去时间，用过去式 watched。"),
                q("The story ___ exciting, but the ending was weak.", List.of("is", "was", "were", "are"), 1, "主语 story 单数，过去时用 was。"),
                q("In my ___, the actor did a great job.", List.of("idea", "thought", "opinion", "mind"), 2, "固定短语 in my opinion。"),
                q("We ___ the movie at the cinema yesterday.", List.of("see", "saw", "seen", "seeing"), 1, "see 的过去式为 saw。")
            ),
            new Listening("播客影评", List.of(
                l("Host", "Tonight we're reviewing 'The Long Road'. I saw it twice.",
                    "今晚我们评论《长路》，我看了两遍。"),
                l("Guest", "Me too. The director took a simple story and made it exciting.",
                    "我也是。导演把一个简单的故事拍得很动人。"),
                l("Host", "And the final scene — I was in tears.", "还有最后一幕，我看哭了。"),
                l("Guest", "I recommend it to everyone who likes drama.", "我向所有喜欢剧情片的人推荐。")
            ), "What do the speakers agree on?",
                List.of("The film was boring.", "The film is worth recommending.", "The ending was bad.", "They disliked the actor."), 1,
                "两人都称赞影片，嘉宾明确 recommend。"),
            List.of(
                s("I watched a great movie last night.", "我昨晚看了一部很棒的电影。"),
                s("In my opinion, the acting was excellent.", "在我看来，演技非常出色。"),
                s("The final scene was unforgettable.", "最后一幕令人难忘。"),
                s("I would highly recommend it.", "我强烈推荐它。"),
                s("I found the story a bit slow.", "我觉得故事节奏有点慢。")
            ));
    }

    static Lesson travelBlog() {
        return new Lesson("l3-u1-l8", "旅行博客", "Travel Blog", "阅读一篇京都旅行博客并复述经历",
            List.of(
                v("travel", "/ˈtrævl/", "旅行", "I love to travel by train.", "我喜欢坐火车旅行。"),
                v("abroad", "/əˈbrɔːd/", "在国外", "She is studying abroad.", "她在国外留学。"),
                v("flight", "/flaɪt/", "航班", "Our flight was delayed.", "我们的航班延误了。"),
                v("luggage", "/ˈlʌɡɪdʒ/", "行李", "My luggage is too heavy.", "我的行李太重了。"),
                v("hotel", "/həʊˈtel/", "酒店", "We stayed at a small hotel.", "我们住在一家小酒店。"),
                v("sight", "/saɪt/", "景点", "We visited all the famous sights.", "我们游览了所有著名景点。"),
                v("experience", "/ɪkˈspɪəriəns/", "经历；体验", "It was an amazing experience.", "那是一次很棒的经历。"),
                v("describe", "/dɪˈskraɪb/", "描述", "How would you describe the trip?", "你会怎么描述这次旅行？")
            ),
            "现在完成时讲经历",
            "have/has + 过去分词表示到目前为止的经历，常与 ever/never/before 连用：I have visited Kyoto twice. 注意 have been to（去过）与 have gone to（去了未回）的区别。",
            List.of(
                q("I have ___ to Japan twice.", List.of("been", "gone", "be", "went"), 0, "“去过已回”用 have been to。"),
                q("Have you ___ tried sushi?", List.of("ever", "never", "yet", "since"), 0, "疑问句中“曾经”用 ever。"),
                q("She has ___ visited a temple before.", List.of("ever", "never", "yet", "already? grammar odd"), 1, "“从未”用 never。"),
                q("We have stayed in this hotel ___ Monday.", List.of("for", "since", "in", "at"), 1, "since + 时间点。")
            ),
            new Listening("京都博客片段", List.of(
                l("Blogger", "I've just finished my third day in Kyoto.", "我刚结束在京都的第三天。"),
                l("Blogger", "I have visited Fushimi Inari and walked through thousands of red gates.",
                    "我去了伏见稻荷，穿过了成千上万座红色鸟居。"),
                l("Blogger", "I've never seen anything like it before.", "我以前从未见过那样的景象。"),
                l("Blogger", "Tomorrow I'm taking an early train to Nara.", "明天我要坐早班火车去奈良。")
            ), "What is true about the blogger?",
                List.of("This is her first day.", "She has visited Fushimi Inari.", "She dislikes red gates.", "She is leaving Japan today."), 1,
                "博主明确说 I have visited Fushimi Inari。"),
            List.of(
                s("I have just come back from Kyoto.", "我刚从京都回来。"),
                s("Have you ever been abroad?", "你出过国吗？"),
                s("I've never seen such a beautiful temple.", "我从未见过这么美的寺庙。"),
                s("Our flight was delayed for two hours.", "我们的航班延误了两个小时。"),
                s("It was an unforgettable experience.", "那是一次难忘的经历。")
            ));
    }

    static Lesson jobInterview() {
        return new Lesson("l3-u2-l9", "求职面试", "Job Interview", "模拟一场英文岗位面试",
            List.of(
                v("interview", "/ˈɪntəvjuː/", "面试", "I have a job interview tomorrow.", "我明天有一场求职面试。"),
                v("resume", "/ˈrezjumeɪ/", "简历", "Please send me your resume.", "请把简历发给我。"),
                v("position", "/pəˈzɪʃn/", "职位", "I'm applying for the marketing position.", "我在申请市场部职位。"),
                v("experience_n", "/ɪkˈspɪəriəns/", "经验", "Do you have any work experience?", "你有工作经验吗？"),
                v("strength", "/streŋθ/", "优势", "What is your greatest strength?", "你最大的优点是什么？"),
                v("challenge", "/ˈtʃælɪndʒ/", "挑战", "I enjoy a good challenge.", "我喜欢有挑战性的事。"),
                v("improve", "/ɪmˈpruːv/", "改进", "I want to improve my English.", "我想提高英语水平。"),
                v("hire", "/ˈhaɪə/", "雇用", "They decided to hire her.", "他们决定录用她。")
            ),
            "used to 与 used for 表达经历",
            "used to + 动词原形表示过去的习惯或状态（I used to work in retail）。面试中描述职责常用 responsible for + 名词/动名词。",
            List.of(
                q("I ___ to work in a small shop during college.", List.of("use", "used", "using", "uses"), 1, "过去的经历用 used to。"),
                q("I was responsible ___ customer service.", List.of("to", "for", "of", "with"), 1, "responsible for 是固定搭配。"),
                q("Tell me a little about ___.", List.of("you", "your", "yourself", "yours"), 2, "面试固定开场 about yourself。"),
                q("I'm good ___ solving problems under pressure.", List.of("at", "in", "on", "for"), 0, "be good at 表示擅长。")
            ),
            new Listening("面试对话", List.of(
                l("Interviewer", "Tell me about yourself and your experience.", "介绍一下你自己和你的经历。"),
                l("Emma", "I used to work in sales, and I was responsible for key accounts.",
                    "我以前做销售，负责重点客户。"),
                l("Interviewer", "Why do you want this position?", "你为什么想要这个职位？"),
                l("Emma", "I enjoy challenges and I want to improve my marketing skills.",
                    "我喜欢挑战，也想提升营销能力。")
            ), "What did Emma use to do?",
                List.of("She taught marketing.", "She worked in sales.", "She ran a café.", "She was an interviewer."), 1,
                "Emma 说 I used to work in sales。"),
            List.of(
                s("Thank you for giving me this interview.", "感谢给我这次面试机会。"),
                s("I used to work in customer service.", "我以前从事客户服务工作。"),
                s("I was responsible for team training.", "我曾负责团队培训。"),
                s("My greatest strength is communication.", "我最大的优势是沟通能力。"),
                s("I'm looking forward to hearing from you.", "期待您的回复。")
            ));
    }

    static Lesson newsSnippet() {
        return new Lesson("l3-u2-l10", "新闻片段", "News Snippet", "听懂一段简短英语科技新闻",
            List.of(
                v("report", "/rɪˈpɔːt/", "报道", "The report came out this morning.", "报道今晨发布。"),
                v("government", "/ˈɡʌvənmənt/", "政府", "The government announced a new plan.", "政府公布了一项新计划。"),
                v("increase", "/ɪnˈkriːs/", "增加", "Prices continue to increase.", "价格持续上涨。"),
                v("research", "/rɪˈsɜːtʃ/", "研究", "New research suggests better sleep helps memory.", "新研究表明良好睡眠有助记忆。"),
                v("technology", "/tekˈnɒlədʒi/", "科技", "Technology changes our daily life.", "科技改变日常生活。"),
                v("environment", "/ɪnˈvaɪrənmənt/", "环境", "We should protect the environment.", "我们应该保护环境。"),
                v("according", "/əˈkɔːdɪŋ/", "根据", "According to the report, sales rose 8%.", "据报道，销售额增长了 8%。"),
                v("announce", "/əˈnaʊns/", "宣布", "The company announced its results.", "公司公布了业绩。")
            ),
            "被动语态",
            "be + 过去分词构成被动：The plan was announced（计划被公布）。新闻常用被动突出事件本身；according to 表示信息来源。",
            List.of(
                q("A new policy ___ announced yesterday.", List.of("is", "was", "were", "are"), 1, "yesterday + 单数主语，用 was announced。"),
                q("___ to the report, traffic is getting worse.", List.of("According", "Based", "Thanks", "Due"), 0, "according to 为固定表达。"),
                q("The new phones ___ sold in 30 countries.", List.of("is", "was", "are", "be"), 2, "主语 phones 为复数，用 are sold。"),
                q("Researchers say the technology ___ widely used soon.", List.of("is", "was", "will be", "were"), 2, "将来时被动 will be used。")
            ),
            new Listening("早间新闻", List.of(
                l("Anchor", "Good morning. A new study was released yesterday on sleep and memory.",
                    "早上好。昨天发布了一项关于睡眠与记忆的研究。"),
                l("Anchor", "According to the researchers, students who slept eight hours remembered 20% more words.",
                    "研究人员称，睡满八小时的学生多记住 20% 的单词。"),
                l("Anchor", "The study was carried out at City University.", "这项研究由城市大学完成。"),
                l("Anchor", "The government announced it will support further research.", "政府宣布将支持进一步研究。")
            ), "What did the study find?",
                List.of("Sleep has no effect.", "Eight hours of sleep helped memory.", "Students need less sleep.", "Memory gets worse with sleep."), 1,
                "研究发现睡八小时的学生多记住 20% 单词。"),
            List.of(
                s("According to the report, the economy is improving.", "据报道，经济正在好转。"),
                s("The new policy was announced on Monday.", "新政策于周一公布。"),
                s("The research was carried out last year.", "这项研究是去年进行的。"),
                s("Prices have increased by five percent.", "物价上涨了 5%。"),
                s("More details will be released soon.", "更多细节将很快公布。")
            ));
    }

    // ---------------- 定级题（由易到难） ----------------

    public static List<PlacementQ> placement() {
        return List.of(
            new PlacementQ("Hello! ___ your name?", List.of("What", "What's", "How", "Where"), 1),
            new PlacementQ("I ___ a student.", List.of("is", "are", "am", "be"), 2),
            new PlacementQ("There ___ two books on the desk.", List.of("is", "are", "be", "am"), 1),
            new PlacementQ("She ___ TV every evening.", List.of("watch", "watches", "watching", "watched"), 1),
            new PlacementQ("—___ you like some tea? —Yes, please.", List.of("Do", "Would", "Are", "Can"), 1),
            new PlacementQ("Look! The children ___ in the park.", List.of("play", "plays", "are playing", "played"), 2),
            new PlacementQ("I ___ the movie yesterday.", List.of("see", "saw", "seen", "seeing"), 1),
            new PlacementQ("She has ___ lived in Paris.", List.of("never", "ever", "yet", "ago"), 0),
            new PlacementQ("The bridge ___ in 1990.", List.of("built", "was built", "is built", "builds"), 1),
            new PlacementQ("If I ___ more time, I would learn another language.",
                List.of("have", "had", "will have", "having"), 1)
        );
    }
}
