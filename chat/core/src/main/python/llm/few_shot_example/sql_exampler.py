examplars= [  
    {   "current_date":"2020-12-01",
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长", "数据日期"]""",
        "question":"比较jerryjzhang和lexluo在内容库的访问次数",
        "prior_schema_links":"""['jerryjzhang'->用户名, 'lexluo'->用户名]""",
        "analysis": """让我们一步一步地思考。在问题“比较jerryjzhang和lexluo在内容库的访问次数“中，我们被问：
“比较jerryjzhang和lexluo”，所以我们需要column=[用户名]
”内容库的访问次数“，所以我们需要column=[访问次数]
基于table和columns，可能的cell values 是 = ['jerryjzhang', 'lexluo']。""",
        "schema_links":"""["用户名", "访问次数", "'jerryjzhang'", "'lexluo'"]""",
        "sql":"""select 用户名, 访问次数 from 内容库产品 where 用户名 in ('jerryjzhang', 'lexluo') and 数据日期 = '2020-12-01' """ 
    },
    {   "current_date":"2022-11-06",
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长", "数据日期"]""",
        "question":"内容库近12个月访问人数 按部门",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“内容库近12个月访问人数 按部门“中，我们被问：
”内容库近12个月“，所以我们需要column=[数据日期]
“访问人数”，所以我们需要column=[访问人数]
”按部门“，所以我们需要column=[部门]
基于table和columns，可能的cell values 是 = [12]。""",
        "schema_links":"""["访问人数", "部门", "数据日期", 12]""",
        "sql":"""select 部门, 数据日期, 访问人数 from 内容库产品 where datediff('month', 数据日期, '2022-11-06') <= 12 """
    },
    {   "current_date":"2023-04-21",
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长", "数据日期"]""",
        "question":"内容库内容合作部、生态业务部的访问时长",
        "prior_schema_links":"""['内容合作部'->部门, '生态业务部'->部门]""",
        "analysis": """让我们一步一步地思考。在问题“内容库内容合作部、生态业务部的访问时长“中，我们被问：
“访问时长”，所以我们需要column=[访问时长]
”内容库内容合作部、生态业务部“，所以我们需要column=[部门]
基于table和columns，可能的cell values 是 = ['内容合作部', '生态业务部']。""",
        "schema_links":"""["访问时长", "部门", "'内容合作部'", "'生态业务部'"]""",
        "sql":"""select 部门, 访问时长 from 内容库产品 where 部门 in ('内容合作部', '生态业务部') and 数据日期 = '2023-04-21' """
    },
    {   "current_date":"2023-08-21",
        "table_name":"优选",
        "fields_list":"""["优选版权归属系", "付费模式", "结算播放份额", "付费用户结算播放份额", "数据日期"]""",
        "question":"近3天阔景系TME结算播放份额",
        "prior_schema_links":"""['阔景系'->优选版权归属系]""",
        "analysis": """让我们一步一步地思考。在问题“近3天阔景系TME结算播放份额“中，我们被问：
“TME结算播放份额”，所以我们需要column=[结算播放份额]
”阔景系“，所以我们需要column=[优选版权归属系]
”近3天“，所以我们需要column=[数据日期]
基于table和columns，可能的cell values 是 = ['阔景系', 3]。""",
        "schema_links":"""["结算播放份额", "优选版权归属系", "数据日期", "'阔景系'", 3]""",
        "sql":"""select 优选版权归属系, 结算播放份额 from 优选 where 优选版权归属系 = '阔景系' and datediff('day', 数据日期, '2023-08-21') <= 3 """
    },
    {   "current_date":"2023-05-22",
        "table_name":"歌曲库",
        "fields_list":"""["是否音乐人歌曲", "Q音歌曲ID", "Q音歌曲MID", "歌曲名", "歌曲版本", "语种", "歌曲类型", "翻唱类型", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "结算播放量", "运营播放量", "付费用户结算播放量", "历史累计结算播放量", "运营搜播量", "结算搜播量", "运营完播量", "运营推播量", "近7日复播率", "日均搜播量", "数据日期"]""",
        "question":"对比近7天翻唱版和纯音乐的歌曲播放量",
        "prior_schema_links":"""['纯音乐'->语种, '翻唱版'->歌曲版本]""",
        "analysis": """让我们一步一步地思考。在问题“对比近3天翻唱版和纯音乐的歌曲播放量“中，我们被问：
“歌曲播放量”，所以我们需要column=[结算播放量]
”翻唱版“，所以我们需要column=[歌曲版本]
”和纯音乐的歌曲“，所以我们需要column=[语种]
”近7天“，所以我们需要column=[数据日期]
基于table和columns，可能的cell values 是 = ['翻唱版', '纯音乐', 7]。""",
        "schema_links":"""["结算播放量", "歌曲版本", "语种", "数据日期", "'翻唱版'", "'纯音乐'", 7]""",
        "sql":"""select 歌曲版本, 语种, 结算播放量 from 歌曲库 where 歌曲版本 = '翻唱版' and 语种 = '纯音乐' and datediff('day', 数据日期, '2023-05-22') <= 7 """
    },
    {   "current_date":"2023-05-31",
        "table_name":"艺人库",
        "fields_list":"""["上下架状态", "歌手名", "歌手等级", "歌手类型", "歌手来源", "TME音乐人等级", "活跃区域", "年龄", "歌手才能", "歌手风格", "粉丝数", "抖音粉丝数", "网易粉丝数", "微博粉丝数", "网易歌曲数", "在架歌曲数", "网易分享数", "独占歌曲数", "网易在架歌曲评论数", "有播放量歌曲数", "数据日期"]""",
        "question":"对比一下陈卓璇、孟美岐、赖美云的粉丝数",
        "prior_schema_links":"""['1527896'->TME歌手ID, '1565463'->TME歌手ID, '2141459'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“对比一下陈卓璇、孟美岐、赖美云的粉丝数“中，我们被问：
“粉丝数”，所以我们需要column=[粉丝数]
”陈卓璇、孟美岐、赖美云“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['陈卓璇', '孟美岐', '赖美云']。""",
        "schema_links":"""["粉丝数", "歌手名", "'陈卓璇'", "'孟美岐'", "'赖美云'"]""",
        "sql":"""select 歌手名, 粉丝数 from 艺人库 where 歌手名 in ('陈卓璇', '孟美岐', '赖美云') and 数据日期 = '2023-05-31' """
    },
    {   "current_date":"2023-07-31",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌曲类型", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"播放量大于1万的歌曲有多少",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“播放量大于1万的歌曲有多少“中，我们被问：
“歌曲有多少”，所以我们需要column=[歌曲名]
”播放量大于1万的“，所以我们需要column=[结算播放量]
基于table和columns，可能的cell values 是 = [10000]。""",
        "schema_links":"""["歌曲名", "结算播放量", 10000]""",
        "sql":"""select 歌曲名 from 歌曲库 where 结算播放量 > 10000 and 数据日期 = '2023-07-31' """
    },
    {   "current_date":"2023-07-31",
        "table_name":"内容库产品",
        "fields_list":"""["用户名", "部门", "模块", "访问时长", "访问次数", "访问人数", "数据日期"]""",
        "question":"内容库访问时长小于1小时，且来自内容合作部的用户是哪些",
        "prior_schema_links":"""['内容合作部'->部门]""",
        "analysis": """让我们一步一步地思考。在问题“内容库访问时长小于1小时，且来自内容合作部的用户是哪些“中，我们被问：
“用户是哪些”，所以我们需要column=[用户名]
”内容合作部的“，所以我们需要column=[部门]
”访问时长小于1小时“，所以我们需要column=[访问时长]
基于table和columns，可能的cell values 是 = ['内容合作部', 1]。""",
        "schema_links":"""["用户名", "部门", "访问时长", "'内容合作部'", 1]""",
        "sql":"""select 用户名 from 内容库产品 where 部门 = '内容合作部' and 访问时长 < 1 and 数据日期 = '2023-07-31' """
    },
    {   "current_date":"2023-08-31",
        "table_name":"内容库产品",
        "fields_list":"""["用户名", "部门", "模块", "访问时长", "访问次数", "访问人数", "数据日期"]""",
        "question":"内容库pv最高的用户有哪些",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“内容库pv最高的用户有哪些“中，我们被问：
“用户有哪些”，所以我们需要column=[用户名]
”pv最高的“，所以我们需要column=[访问次数]
基于table和columns，可能的cell values 是 = []。""",
        "schema_links":"""["用户名", "访问次数"]""",
        "sql":"""select 用户名 from 内容库产品 where 数据日期 = '2023-08-31' order by 访问次数 desc limit 10 """
    },
    {   "current_date":"2023-08-31",
        "table_name":"艺人库",
        "fields_list":"""["播放量层级", "播放量单调性", "播放量方差", "播放量突增类型", "播放量集中度", "歌手名", "歌手等级", "歌手类型", "歌手来源", "TME音乐人等级", "结算播放量", "运营播放量", "历史累计结算播放量", "有播放量歌曲数", "历史累计运营播放量", "付费用户结算播放量", "结算播放量占比", "运营播放份额", "免费用户结算播放占比", "完播量", "数据日期"]""",
        "question":"近90天袁娅维播放量平均值是多少",
        "prior_schema_links":"""['152789226'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“近90天袁娅维播放量平均值是多少“中，我们被问：
“播放量平均值是多少”，所以我们需要column=[结算播放量]
”袁娅维“，所以我们需要column=[歌手名]
”近90天“，所以我们需要column=[数据日期]
基于table和columns，可能的cell values 是 = ['袁娅维', 90]。""",
        "schema_links":"""["结算播放量", "歌手名", "数据日期", "'袁娅维'", 90]""",
        "sql":"""select avg(结算播放量) from 艺人库 where 歌手名 = '袁娅维' and datediff('day', 数据日期, '2023-08-31') <= 90 """
    },
    {   "current_date":"2023-08-31",
        "table_name":"艺人库",
        "fields_list":"""["播放量层级", "播放量单调性", "播放量方差", "播放量突增类型", "播放量集中度", "歌手名", "歌手等级", "歌手类型", "歌手来源", "TME音乐人等级", "结算播放量", "运营播放量", "历史累计结算播放量", "有播放量歌曲数", "历史累计运营播放量", "付费用户结算播放量", "结算播放量占比", "运营播放份额", "免费用户结算播放占比", "完播量", "数据日期"]""",
        "question":"周深近7天结算播放量总和是多少",
        "prior_schema_links":"""['199509'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“周深近7天结算播放量总和是多少“中，我们被问：
“结算播放量总和是多少”，所以我们需要column=[结算播放量]
”周深“，所以我们需要column=[歌手名]
”近7天“，所以我们需要column=[数据日期]
基于table和columns，可能的cell values 是 = ['周深', 7]。""",
        "schema_links":"""["结算播放量", "歌手名", "数据日期", "'周深'", 7]""",
        "sql":"""select sum(结算播放量) from 艺人库 where 歌手名 = '周深' and datediff('day', 数据日期, '2023-08-31') <= 7 """
    },
    {   "current_date":"2023-09-14",
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长", "数据日期"]""",
        "question":"内容库访问次数大于1k的部门是哪些",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“内容库访问次数大于1k的部门是哪些“中，我们被问：
“部门是哪些”，所以我们需要column=[部门]
”访问次数大于1k的“，所以我们需要column=[访问次数]
基于table和columns，可能的cell values 是 = [1000]。""",
        "schema_links":"""["部门", "访问次数", 1000]""",
        "sql":"""select 部门 from 内容库产品 where 访问次数 > 1000 and 数据日期 = '2023-09-14' """
    },
    {   "current_date":"2023-09-18",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "TME歌手ID", "歌曲版本", "歌曲类型", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"陈奕迅唱的所有的播放量大于20k的孤勇者有哪些",
        "prior_schema_links":"""['199509'->TME歌手ID, '1527123'->TME歌曲ID]""",
        "analysis": """让我们一步一步地思考。在问题“陈奕迅唱的所有的播放量大于20k的孤勇者有哪些“中，我们被问：
“孤勇者有哪些”，所以我们需要column=[歌曲名]
”播放量大于20k的“，所以我们需要column=[结算播放量]
”陈奕迅唱的“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = [20000, '陈奕迅', '孤勇者']。""",
        "schema_links":"""["歌曲名", "结算播放量", "歌手名", 20000, "'陈奕迅'", "'孤勇者'"]""",
        "sql":"""select 歌曲名 from 歌曲库 where 结算播放量 > 20000 and 歌手名 = '陈奕迅' and 歌曲名 = '孤勇者' and 数据日期 = '2023-09-18' """
    },
    {   "current_date":"2023-09-18",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"周杰伦去年发布的歌曲有哪些",
        "prior_schema_links":"""['23109'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“周杰伦去年发布的歌曲有哪些“中，我们被问：
“歌曲有哪些”，所以我们需要column=[歌曲名]
”去年发布的“，所以我们需要column=[发布时间]
”周杰伦“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['周杰伦', 1]。""",
        "schema_links":"""["歌曲名", "发布时间", "歌手名", 1, "'周杰伦'"]""",
        "sql":"""select 歌曲名 from 歌曲库 where datediff('year', 发布时间, '2023-09-18') <= 1 and 歌手名 = '周杰伦' and 数据日期 = '2023-09-18' """
    },
    {   "current_date":"2023-09-11",
        "table_name":"艺人库",
        "fields_list":"""["播放量层级", "播放量单调性", "播放量方差", "播放量突增类型", "播放量集中度", "歌手名", "歌手等级", "歌手类型", "歌手来源", "签约日期", "TME音乐人等级", "结算播放量", "运营播放量", "历史累计结算播放量", "有播放量歌曲数", "历史累计运营播放量", "付费用户结算播放量", "结算播放量占比", "运营播放份额", "免费用户结算播放占比", "完播量", "数据日期"]""",
        "question":"我想要近半年签约的播放量前十的歌手有哪些",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“我想要近半年签约的播放量前十的歌手“中，我们被问：
“歌手有哪些”，所以我们需要column=[歌手名]
”播放量前十的“，所以我们需要column=[结算播放量]
”近半年签约的“，所以我们需要column=[签约日期]
基于table和columns，可能的cell values 是 = [0.5, 10]。""",
        "schema_links":"""["歌手名", "结算播放量", "签约日期", 0.5, 10]""",
        "sql":"""select 歌手名 from 艺人库 where datediff('year', 签约日期, '2023-09-11') <= 0.5 and 数据日期 = '2023-09-11' order by 结算播放量 desc limit 10"""
    },
    {   "current_date":"2023-08-12",
        "table_name":"歌曲库",
        "fields_list": """["发行日期", "歌曲语言", "歌曲来源", "歌曲流派", "歌曲名", "歌曲版本", "歌曲类型", "发行时间", "数据日期"]""",
        "question":"最近一年发行的歌曲中，有哪些在近7天播放超过一千万的",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“最近一年发行的歌曲中，有哪些在近7天播放超过一千万的“中，我们被问：
“发行的歌曲中，有哪些”，所以我们需要column=[歌曲名]
”最近一年发行的“，所以我们需要column=[发行日期]
”在近7天播放超过一千万的“，所以我们需要column=[数据日期, 结算播放量]
基于table和columns，可能的cell values 是 = [1, 10000000]""",
        "schema_links":"""["歌曲名", "发行日期", "数据日期", "结算播放量", 1, 10000000]""",
        "sql":"""select 歌曲名 from 歌曲库 where datediff('year', 发行日期, '2023-08-12') <= 1 and datediff('day', 数据日期, '2023-08-12') <= 7 and 结算播放量 > 10000000"""
    },
    {   "current_date":"2023-08-12",
        "table_name":"歌曲库",
        "fields_list": """["发行日期", "歌曲语言", "歌曲来源", "歌曲流派", "歌曲名", "歌曲版本", "歌曲类型", "发行时间", "数据日期"]""",
        "question":"今年以来发行的歌曲中，有哪些在近7天播放超过一千万的",
        "prior_schema_links":"""[]""",
        "analysis": """让我们一步一步地思考。在问题“今年以来发行的歌曲中，有哪些在近7天播放超过一千万的“中，我们被问：
“发行的歌曲中，有哪些”，所以我们需要column=[歌曲名]
”今年以来发行的“，所以我们需要column=[发行日期]
”在近7天播放超过一千万的“，所以我们需要column=[数据日期, 结算播放量]
基于table和columns，可能的cell values 是 = [0, 7, 10000000]""",
        "schema_links":"""["歌曲名", "发行日期", "数据日期", "结算播放量", 0, 7, 10000000]""",
        "sql":"""select 歌曲名 from 歌曲库 where datediff('year', 发行日期, '2023-08-12') <= 0 and datediff('day', 数据日期, '2023-08-12') <= 7 and 结算播放量 > 10000000"""
    },
    {   "current_date":"2023-08-12",
        "table_name":"歌曲库",
        "fields_list": """["发行日期", "歌曲语言", "歌曲来源", "歌曲流派", "歌曲名", "歌曲版本", "歌曲类型", "发行时间", "数据日期"]""",
        "question":"2023年以来发行的歌曲中，有哪些在近7天播放超过一千万的",
        "prior_schema_links":"""['514129144'->TME歌曲ID]""",
        "analysis": """让我们一步一步地思考。在问题“2023年以来发行的歌曲中，有哪些在近7天播放超过一千万的“中，我们被问：
“发行的歌曲中，有哪些”，所以我们需要column=[歌曲名]
”2023年以来发行的“，所以我们需要column=[发行日期]
”在近7天播放超过一千万的“，所以我们需要column=[数据日期, 结算播放量]
基于table和columns，可能的cell values 是 = [2023, 7, 10000000]""",
        "schema_links":"""["歌曲名", "发行日期", "数据日期", "结算播放量", 2023, 7, 10000000]""",   
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(发行日期) >= 2023 and datediff('day', 数据日期, '2023-08-12') <= 7 and 结算播放量 > 10000000"""
    },
    {   "current_date":"2023-08-01",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"周杰伦2023年6月之后发布的歌曲有哪些",
        "prior_schema_links":"""['23109'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“周杰伦2023年6月之后发布的歌曲有哪些“中，我们被问：
“歌曲有哪些”，所以我们需要column=[歌曲名]
”2023年6月之后发布的“，所以我们需要column=[发布时间]
”周杰伦“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['周杰伦', 2023, 6]。""",
        "schema_links":"""["歌曲名", "发布时间", "歌手名", "周杰伦", 2023, 6]""",
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(发布时间) >= 2023 and MONTH(发布时间) >= 6 and 歌手名 = '周杰伦' and 数据日期 = '2023-08-01' """
    },
    {   "current_date":"2023-08-01",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"邓紫棋在2023年1月5日之后发布的歌曲中，有哪些播放量大于500W的？",
        "prior_schema_links":"""['2312311'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“邓紫棋在2023年1月5日之后发布的歌曲中，有哪些播放量大于500W的？“中，我们被问：
“播放量大于500W的”，所以我们需要column=[结算播放量]
”邓紫棋在2023年1月5日之后发布的“，所以我们需要column=[发布时间]
”邓紫棋“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['邓紫棋', 2023, 1, 5, 5000000]。""",
        "schema_links":"""["结算播放量", "发布时间", "歌手名", "邓紫棋", 2023, 1, 5, 5000000]""",
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(发布时间) >= 2023 and MONTH(发布时间) >= 1 and DAY(发布时间) >= 5 and 歌手名 = '邓紫棋' and 结算播放量 > 5000000 and 数据日期 = '2023-08-01'"""
    },
    {   "current_date":"2023-09-17",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"2023年6月以后，张靓颖播放量大于200万的歌曲有哪些？",
        "prior_schema_links":"""['45453'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“2023年6月以后，张靓颖播放量大于200万的歌曲有哪些？“中，我们被问：
“播放量大于200万的”，所以我们需要column=[结算播放量]
”2023年6月以后，张靓颖“，所以我们需要column=[数据日期, 歌手名]
”歌曲有哪些“，所以我们需要column=[歌曲名]
基于table和columns，可能的cell values 是 = ['张靓颖', 2023, 6, 2000000]。""",
        "schema_links":"""["结算播放量", "数据日期", "歌手名", "张靓颖", 2023, 6, 2000000]""",
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(数据日期) >= 2023 and MONTH(数据日期) >= 6 and 歌手名 = '张靓颖' and 结算播放量 > 2000000 """
    },
    {   "current_date":"2023-08-16",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"2021年6月以后发布的李宇春的播放量大于20万的歌曲有哪些",
        "prior_schema_links":"""['23109'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“2021年6月以后发布的李宇春的播放量大于20万的歌曲有哪些“中，我们被问：
“播放量大于20万的”，所以我们需要column=[结算播放量]
”2021年6月以后发布的“，所以我们需要column=[发布时间]
”李宇春“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['李宇春', 2021, 6, 200000]。""",
        "schema_links":"""["结算播放量", "发布时间", "歌手名", "李宇春", 2021, 6, 200000]""",
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(发布时间) >= 2021 and MONTH(发布时间) >= 6 and 歌手名 = '李宇春' and 结算播放量 > 200000 and 数据日期 = '2023-08-16'"""
    },
    {   "current_date":"2023-08-16",
        "table_name":"歌曲库",
        "fields_list":"""["歌曲名", "歌曲版本", "歌手名", "歌曲类型", "发布时间", "TME歌曲ID", "是否优选窄口径歌曲", "是否优选宽口径歌曲", "是否音乐人歌曲", "网易歌曲ID", "Q音歌曲ID", "Q音歌曲MID", "结算播放量", "运营播放量", "分享量", "收藏量", "运营搜播量", "结算搜播量", "拉新用户数", "拉活用户数", "分享率", "结算播放份额", "数据日期"]""",
        "question":"刘德华在1992年4月2日到2020年5月2日之间发布的播放量大于20万的歌曲有哪些",
        "prior_schema_links":"""['4234234'->TME歌手ID]""",
        "analysis": """让我们一步一步地思考。在问题“刘德华在1992年4月2日到2020年5月2日之间发布的播放量大于20万的歌曲有哪些“中，我们被问：
“播放量大于20万的”，所以我们需要column=[结算播放量]
”1992年4月2日到2020年5月2日之间发布的“，所以我们需要column=[发布时间]
”刘德华“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['刘德华', 1992, 4, 2, 2020, 5, 2, 200000]。""",
        "schema_links":"""["结算播放量", "发布时间", "歌手名", "刘德华", 1992, 4, 2, 2020, 5, 2, 200000]""",    
        "sql":"""select 歌曲名 from 歌曲库 where YEAR(发布时间) >= 1992 and MONTH(发布时间) >= 4 and DAY(发布时间) >= 2 and YEAR(发布时间) <= 2020 and MONTH(发布时间) <= 5 and DAY(发布时间) <= 2 and 歌手名 = '刘德华' and 结算播放量 > 200000 and 数据日期 = '2023-08-16'"""
    }
]