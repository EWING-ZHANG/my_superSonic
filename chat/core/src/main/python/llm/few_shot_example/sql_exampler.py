examplars= [  
    {
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长"]""",
        "question":"比较jerry和tom在内容库的访问次数",
        "analysis": """让我们一步一步地思考。在问题“比较jerry和tom在内容库的访问次数“中，我们被问：
“内容库的访问次数”，所以我们需要column=[访问次数]
”比较jerry和tom“，所以我们需要column=[用户名]
基于table和columns，可能的cell values 是 = ['jerry', 'tom']。""",
        "schema_links":"""["访问次数", "用户名", "'jerry'", "'tom'"]""",
        "sql":"""select 用户名, 访问次数 from 内容库产品 where 用户名 in ('jerry', 'tom')"""
    },
    {
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长"]""",
        "question":"内容库近12个月访问人数 按部门",
        "analysis": """让我们一步一步地思考。在问题“内容库近12个月访问人数 按部门“中，我们被问：
“内容库近12个月访问人数”，所以我们需要column=[访问人数]
”按部门“，所以我们需要column=[部门]
基于table和columns，可能的cell values 是 = []。""",
        "schema_links":"""["访问人数", "部门"]""",
        "sql":"""select 部门, sum(访问人数) from 内容库产品 where 部门 group by 部门"""
    },
    {
        "table_name":"内容库产品",
        "fields_list":"""["部门", "模块", "用户名", "访问次数", "访问人数", "访问时长"]""",
        "question":"内容库编辑部、美术部的访问时长",
        "analysis": """让我们一步一步地思考。在问题“内容库编辑部、美术部的访问时长“中，我们被问：
“访问时长”，所以我们需要column=[访问时长]
”内容库编辑部、美术部“，所以我们需要column=[部门]
基于table和columns，可能的cell values 是 = ['编辑部', '美术部']。""",
        "schema_links":"""["访问时长", "部门", "'编辑部'", "'美术部'"]""",
        "sql":"""select 部门, 访问时长 from 内容库产品 where 部门 in ('编辑部', '美术部')"""
    },
    {
        "table_name":"精选",
        "fields_list":"""['归属系', '付费模式', '结算播放份额', '付费用户结算播放份额']""",
        "question":"近3天飞天系结算播放份额",
        "analysis": """让我们一步一步地思考。在问题“近3天飞天系结算播放份额“中，我们被问：
“结算播放份额”，所以我们需要column=[结算播放份额]
”飞天系“，所以我们需要column=[归属系]
基于table和columns，可能的cell values 是 = ['飞天系']。""",
        "schema_links":"""["结算播放份额", "归属系", "'飞天系'"]""",
        "sql":"""select 归属系, 结算播放份额 from 精选 where 归属系 in ('飞天系')"""
    },
    {
        "table_name":"歌曲库",
        "fields_list":"""['歌曲ID', '歌曲MID', '歌曲名', '歌曲版本', '歌曲类型', '翻唱类型', '结算播放量', '运营播放量', '付费用户结算播放量', '历史累计结算播放量', '运营搜播量', '结算搜播量', '运营完播量', '运营推播量', '近7日复播率', '日均搜播量']""",
        "question":"对比近3天翻唱版和纯音乐的歌曲播放量",
        "analysis": """让我们一步一步地思考。在问题“对比近3天翻唱版和纯音乐的歌曲播放量“中，我们被问：
“歌曲播放量”，所以我们需要column=[结算播放量]
”翻唱版和纯音乐“，所以我们需要column=[歌曲类型]
基于table和columns，可能的cell values 是 = ['翻唱版', '纯音乐']。""",
        "schema_links":"""["结算播放量", "歌曲类型", "'翻唱版'", "'纯音乐'"]""",
        "sql":"""select 歌曲类型, 结算播放量 from 歌曲库 where 歌曲类型 in ('翻唱版', '纯音乐')"""
    },
    {
        "table_name":"艺人库",
        "fields_list":"""['上下架状态', '歌手名', '歌手等级', '歌手类型', '歌手来源', '活跃区域', '年龄', '歌手才能', '歌手风格', '粉丝数', '在架歌曲数', '有播放量歌曲数']""",
        "question":"对比一下流得滑、锅富程、章雪友的粉丝数",
        "analysis": """让我们一步一步地思考。在问题“对比一下流得滑、锅富程、章雪友的粉丝数“中，我们被问：
“粉丝数”，所以我们需要column=[粉丝数]
”流得滑、锅富程、章雪友“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['流得滑', '锅富程', '章雪友']。""",
        "schema_links":"""["粉丝数", "歌手名", "'流得滑'", "'锅富程'", "'章雪友'"]""",
        "sql":"""select 歌手名, 粉丝数 from 艺人库 where 歌手名 in ('流得滑', '锅富程', '章雪友')"""
    },
    {
        "table_name":"歌曲库",
        "fields_list":"""['歌曲ID', '歌曲MID', '歌曲名', '歌曲版本', '歌曲类型', '翻唱类型', '结算播放量', '运营播放量', '付费用户结算播放量', '历史累计结算播放量', '运营搜播量', '结算搜播量', '运营完播量', '运营推播量', '近7日复播率', '日均搜播量']""",
        "question":"播放量大于1万的歌曲有多少",
        "analysis": """让我们一步一步地思考。在问题“播放量大于1万的歌曲有多少“中，我们被问：
“歌曲有多少”，所以我们需要column=[歌曲名]
”播放量大于1万“，所以我们需要column=[结算播放量]
基于table和columns，可能的cell values 是 = [10000]。""",
        "schema_links":"""["歌曲名", "结算播放量", 10000]""",
        "sql":"""select 歌曲名 from 歌曲库 where 结算播放量 > 10000"""
    },
    {
        "table_name":"内容库产品",
        "fields_list":"""['用户名', '部门', '模块', '访问时长', '访问次数', '访问人数']""",
        "question":"内容库访问时长小于1小时，且来自美术部的用户是哪些",
        "analysis": """让我们一步一步地思考。在问题“内容库访问时长小于1小时，且来自美术部的用户是哪些“中，我们被问：
“用户是哪些”，所以我们需要column=[用户名]
”美术部的“，所以我们需要column=[部门]
”访问时长小于1小时“，所以我们需要column=[访问时长]
基于table和columns，可能的cell values 是 = ['美术部', 1]。""",
        "schema_links":"""["用户名", "部门", "访问时长", "'美术部'", 1]""",
        "sql":"""select 用户名 from 内容库产品 where 部门 = '美术部' and 访问时长 < 1"""
    },
    {
        "table_name":"内容库产品",
        "fields_list":"""['用户名', '部门', '模块', '访问次数', '访问人数', '访问时长']""",
        "question":"内容库pv最高的用户有哪些",
        "analysis": """让我们一步一步地思考。在问题“内容库pv最高的用户有哪些“中，我们被问：
“用户有哪些”，所以我们需要column=[用户名]
”pv最高的“，所以我们需要column=[访问次数]
基于table和columns，可能的cell values 是 = []。""",
        "schema_links":"""["用户名", "访问次数"]""",
        "sql":"""select 用户名 from 内容库产品 order by 访问次数 desc limit 10"""
    },
    {
        "table_name":"艺人库",
        "fields_list":"""['歌手名', '歌手等级', '歌手类型', '歌手来源', '结算播放量', '运营播放量', '历史累计结算播放量', '有播放量歌曲数', '历史累计运营播放量', '付费用户结算播放量', '结算播放量占比', '运营播放份额', '完播量']""",
        "question":"近90天袁呀味播放量平均值是多少",
        "analysis": """让我们一步一步地思考。在问题“近90天袁呀味播放量平均值是多少“中，我们被问：
“播放量平均值是多少”，所以我们需要column=[结算播放量]
”袁呀味“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['袁呀味']。""",
        "schema_links":"""["结算播放量", "歌手名", "'袁呀味'"]""",
        "sql":"""select avg(结算播放量) from 艺人库 where 歌手名 = '袁呀味'"""
    },
    {
        "table_name":"艺人库",
        "fields_list":"""['歌手名', '歌手等级', '歌手类型', '歌手来源', '结算播放量', '运营播放量', '历史累计结算播放量', '有播放量歌曲数', '历史累计运营播放量', '付费用户结算播放量', '结算播放量占比', '运营播放份额', '完播量']""",
        "question":"周浅近7天结算播放量总和是多少",
        "analysis": """让我们一步一步地思考。在问题“周浅近7天结算播放量总和是多少“中，我们被问：
“结算播放量总和是多少”，所以我们需要column=[结算播放量]
”周浅“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = ['周浅']。""",
        "schema_links":"""["结算播放量", "歌手名", "'周浅'"]""",
        "sql":"""select sum(结算播放量) from 艺人库 where 歌手名 = '周浅'"""
    },
    {
        "table_name":"内容库产品",
        "fields_list":"""['部门', '模块', '用户名', '访问次数', '访问人数', '访问时长']""",
        "question":"内容库访问次数大于1k的部门是哪些",
        "analysis": """让我们一步一步地思考。在问题“内容库访问次数大于1k的部门是哪些“中，我们被问：
“部门是哪些”，所以我们需要column=[部门]
”访问次数大于1k的“，所以我们需要column=[访问次数]
基于table和columns，可能的cell values 是 = [1000]。""",
        "schema_links":"""["部门", "访问次数", 1000]""",
        "sql":"""select 部门 from 内容库产品 where 访问次数 > 1000"""
    },
    {
        "table_name":"歌曲库",
        "fields_list":"""['歌曲ID', '歌曲MID', '歌曲名', '歌曲版本', '歌曲类型', '翻唱类型', '结算播放量', '运营播放量', '付费用户结算播放量', '历史累计结算播放量', '运营搜播量', '结算搜播量', '运营完播量', '运营推播量', '近7日复播率', '日均搜播量']""",
        "question":"陈奕迅唱的所有的播放量大于20k的雇佣者有哪些",
        "analysis": """让我们一步一步地思考。在问题“陈易迅唱的所有的播放量大于20k的雇佣者有哪些“中，我们被问：
“雇佣者有哪些”，所以我们需要column=[歌曲名]
”播放量大于20k的“，所以我们需要column=[结算播放量]
”陈易迅唱的“，所以我们需要column=[歌手名]
基于table和columns，可能的cell values 是 = [20000, '陈易迅']。""",
        "schema_links":"""["歌曲名", "结算播放量", "歌手名", 20000, "'陈易迅'"]""",
        "sql":"""select 歌曲名 from 歌曲库 where 结算播放量 > 20000 and 歌手名 = '陈易迅'"""
    }
]