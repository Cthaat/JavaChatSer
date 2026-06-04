from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path
from textwrap import wrap
from zipfile import ZIP_DEFLATED, ZipFile

import win32com.client
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
SCREENSHOTS = DOCS / "screenshots"
DIAGRAMS = DOCS / "diagrams"
REPORT = DOCS / "JavaChatSer大作业报告.docx"
MEMBER_A_REPORT = DOCS / "JavaChatSer大作业报告-成员A.docx"
COVER = DOCS / "大作业报告封面.docx"
TASK_BOOK = DOCS / "《企业级应用开发 》课程大作业任务书.doc"
FORMAT_RULES = DOCS / "大作业格式要求.doc"
SUBMIT_ZIP = ROOT / "JavaChatSer-source-submit.zip"

PROJECT_TITLE = "JavaChatSer 在线即时聊天系统"
COURSE_NAME = "企业级应用开发"
MAJOR = "软件工程"
CLASS_PLACEHOLDER = "2023级 软件工程（班级待填写）"
TEACHER_PLACEHOLDER = "任课教师待填写"
DATE_RANGE = "2026.03.02-2026.06.20"
FINISH_DATE = "2026年06月20日"

WD_ALIGN_LEFT = 0
WD_ALIGN_CENTER = 1
WD_ALIGN_JUSTIFY = 3
WD_BREAK_PAGE = 7
WD_COLLAPSE_END = 0
WD_STORY = 6
WD_FORMAT_DOC = 0
WD_FORMAT_DOCX = 16


@dataclass(frozen=True)
class UseCase:
    code: str
    title: str
    owner: str
    flow: str
    acceptance: str


MEMBERS = [
    ("成员A（姓名/学号待填写）", "后端用户与好友模块"),
    ("成员B（姓名/学号待填写）", "私聊、消息状态与缓存模块"),
    ("成员C（姓名/学号待填写）", "公共聊天室、WebSocket 与部署模块"),
    ("成员D（姓名/学号待填写）", "前端工作台、上传、统计与文档测试"),
]

USE_CASES = [
    UseCase("UC-01", "搜索用户", "成员A", "输入用户名或昵称关键字，系统分页返回可添加用户。", "排除当前用户，空关键字和分页参数按接口规则处理。"),
    UseCase("UC-02", "发送好友申请", "成员A", "选择搜索结果中的用户并提交好友申请。", "不能添加自己，重复申请或已有好友关系返回冲突。"),
    UseCase("UC-03", "查看收到的好友申请", "成员A", "进入好友页面加载待处理申请列表。", "按申请时间倒序展示申请人信息和处理入口。"),
    UseCase("UC-04", "接受或拒绝好友申请", "成员A", "对待处理申请选择接受或拒绝。", "接受后建立双向好友关系，拒绝后不会出现在好友列表。"),
    UseCase("UC-05", "删除好友", "成员A", "在好友列表中删除已建立的好友关系。", "双方关系同步置为删除状态，好友缓存同步失效。"),
    UseCase("UC-06", "发送文本私聊", "成员B", "在好友会话中输入文本并发送。", "仅好友可发送，消息先写入 MySQL，再更新 Redis 和前端列表。"),
    UseCase("UC-07", "发送图片私聊", "成员B", "上传图片后以图片消息形式发送给好友。", "图片地址来自上传接口，非法图片地址返回参数错误。"),
    UseCase("UC-08", "分页查看私聊历史", "成员B", "进入好友会话时分页加载历史消息。", "消息按时间升序展示，撤回消息不显示原始内容。"),
    UseCase("UC-09", "标记私聊已读", "成员B", "进入会话后将该好友发来的未读消息标记为已读。", "数据库 read_at 更新，Redis 未读数清零。"),
    UseCase("UC-10", "撤回私聊消息", "成员B", "发送者撤回自己消息，管理员可撤回任意私聊消息。", "撤回后返回 MESSAGE_RECALLED，前端同步置空内容。"),
    UseCase("UC-11", "发送公共聊天室消息", "成员C", "用户在公共聊天室输入文本或图片消息。", "消息写入 public_message 并广播给在线用户。"),
    UseCase("UC-12", "分页查看公共消息历史", "成员C", "进入公共聊天室时加载历史消息。", "历史消息按时间升序分页返回。"),
    UseCase("UC-13", "撤回公共消息", "成员C", "发送者或管理员撤回公共聊天室消息。", "撤回事件广播给所有在线会话。"),
    UseCase("UC-14", "建立 WebSocket 实时连接", "成员C", "前端携带 JWT 连接 /ws/chat。", "握手阶段校验 JWT，无效 Token 不能建立连接。"),
    UseCase("UC-15", "好友在线状态通知", "成员C", "用户上线、心跳或离线时更新在线状态。", "Redis 在线状态设置 TTL，并向好友推送状态变化。"),
    UseCase("UC-16", "好友申请实时通知", "成员D", "申请创建后向在线接收者推送通知。", "好友页面待处理数量实时变化。"),
    UseCase("UC-17", "上传头像并更新资料", "成员D", "用户选择图片作为头像上传。", "图片保存到 uploads/avatars，当前用户 avatar_url 更新。"),
    UseCase("UC-18", "上传聊天图片", "成员D", "用户上传聊天图片并获得可访问 URL。", "只接受图片文件，返回 url、contentType 和 size。"),
    UseCase("UC-19", "查看统计概览", "成员D", "进入 dashboard 查看统计数据。", "管理员看到全局统计，普通用户看到个人统计。"),
    UseCase("UC-20", "切换深色模式", "成员D", "用户在侧边栏切换浅色/深色主题。", "主题写入 localStorage，刷新页面后保持。"),
]


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        Path(r"C:\Windows\Fonts\msyh.ttc"),
        Path(r"C:\Windows\Fonts\simhei.ttf"),
        Path(r"C:\Windows\Fonts\simsun.ttc"),
        Path(r"C:\Windows\Fonts\arial.ttf"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def multiline(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], text: str, size: int = 26) -> None:
    x1, y1, x2, y2 = box
    selected_font = font(size)
    lines: list[str] = []
    for part in text.split("\n"):
        width = max(8, (x2 - x1) // max(size, 1))
        lines.extend(wrap(part, width=width) or [""])
    heights = [draw.textbbox((0, 0), line, font=selected_font)[3] for line in lines]
    total_height = sum(heights) + max(0, len(lines) - 1) * 8
    y = y1 + ((y2 - y1) - total_height) // 2
    for line in lines:
        bbox = draw.textbbox((0, 0), line, font=selected_font)
        x = x1 + ((x2 - x1) - (bbox[2] - bbox[0])) // 2
        draw.text((x, y), line, fill="#1f2937", font=selected_font)
        y += (bbox[3] - bbox[1]) + 8


def rounded_box(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    fill: str,
    outline: str = "#2563eb",
    size: int = 26,
) -> None:
    draw.rounded_rectangle(box, radius=18, fill=fill, outline=outline, width=3)
    multiline(draw, box, text, size=size)


def arrow(draw: ImageDraw.ImageDraw, start: tuple[int, int], end: tuple[int, int], color: str = "#4b5563") -> None:
    draw.line([start, end], fill=color, width=4)
    sx, sy = start
    ex, ey = end
    if abs(ex - sx) >= abs(ey - sy):
        sign = 1 if ex > sx else -1
        points = [(ex, ey), (ex - sign * 18, ey - 10), (ex - sign * 18, ey + 10)]
    else:
        sign = 1 if ey > sy else -1
        points = [(ex, ey), (ex - 10, ey - sign * 18), (ex + 10, ey - sign * 18)]
    draw.polygon(points, fill=color)


def canvas(title: str) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGB", (1800, 1100), "#f8fafc")
    draw = ImageDraw.Draw(image)
    draw.text((56, 42), title, fill="#0f172a", font=font(44, bold=True))
    draw.line([(56, 112), (1744, 112)], fill="#cbd5e1", width=3)
    return image, draw


def generate_diagrams() -> None:
    DIAGRAMS.mkdir(parents=True, exist_ok=True)

    image, draw = canvas("JavaChatSer 用例图")
    member_x = [270, 650, 1030, 1410]
    member_titles = ["成员A\n好友管理", "成员B\n私聊消息", "成员C\n公共/实时", "成员D\n前端/统计"]
    for x, title in zip(member_x, member_titles):
        rounded_box(draw, (x - 135, 155, x + 135, 235), title, "#dbeafe", "#2563eb", 22)
    for index, use_case in enumerate(USE_CASES):
        col = index // 5
        row = index % 5
        x = member_x[col]
        y = 290 + row * 125
        rounded_box(
            draw,
            (x - 145, y, x + 145, y + 76),
            f"{use_case.code}\n{use_case.title}",
            "#ffffff",
            "#94a3b8",
            20,
        )
        arrow(draw, (x, 235), (x, y), "#64748b")
    rounded_box(draw, (70, 390, 210, 510), "普通用户", "#fef3c7", "#d97706", 24)
    rounded_box(draw, (1590, 390, 1730, 510), "管理员", "#fef3c7", "#d97706", 24)
    for x in member_x:
        arrow(draw, (210, 450), (x - 145, 450), "#94a3b8")
    arrow(draw, (1590, 450), (1320, 450), "#94a3b8")
    draw.text((56, 1025), "说明：登录、注册、密码修改等基础用例未计入 20 个课程用例。", fill="#475569", font=font(24))
    image.save(DIAGRAMS / "uml-use-cases.png", quality=95)

    image, draw = canvas("后端类/模块关系图")
    columns = [
        ("Controller", ["AuthController", "UserController", "FriendController", "ChatController", "StatsController"]),
        ("Service", ["UserService", "FriendService", "ChatService", "StatsService", "MediaStorageService"]),
        ("Repository", ["UserRepository", "FriendRepository", "PrivateMessageRepository", "PublicMessageRepository"]),
        ("Domain / DTO", ["User", "FriendRelation", "PrivateMessage", "PublicMessage", "ApiResponse"]),
    ]
    x = 120
    for title, items in columns:
        rounded_box(draw, (x, 170, x + 330, 250), title, "#e0f2fe", "#0284c7", 30)
        y = 300
        for item in items:
            rounded_box(draw, (x, y, x + 330, y + 64), item, "#ffffff", "#94a3b8", 22)
            y += 92
        x += 410
    for x in [450, 860, 1270]:
        arrow(draw, (x, 520), (x + 80, 520), "#64748b")
    rounded_box(
        draw,
        (400, 850, 1400, 930),
        "Security / WebSocket / Redis Cache 横向支撑认证、实时推送、在线状态和未读数",
        "#dcfce7",
        "#16a34a",
        24,
    )
    image.save(DIAGRAMS / "uml-module-classes.png", quality=95)

    image, draw = canvas("系统组件图")
    boxes = {
        "Browser\nVue 3 SPA": (90, 280, 330, 390),
        "Nginx\n静态资源与代理": (500, 280, 800, 390),
        "Spring Boot 3\nREST + WebSocket": (960, 250, 1320, 420),
        "MySQL 8\n业务持久化": (960, 620, 1200, 730),
        "Redis 7\n缓存/在线/未读": (1300, 620, 1580, 730),
        "Uploads\n头像/图片": (550, 620, 820, 730),
    }
    for text, box in boxes.items():
        rounded_box(draw, box, text, "#eef2ff", "#4f46e5", 28)
    arrow(draw, (330, 335), (500, 335))
    draw.text((360, 292), "HTTP / WS", fill="#475569", font=font(22))
    arrow(draw, (800, 335), (960, 335))
    draw.text((820, 292), "/api /ws /uploads", fill="#475569", font=font(22))
    arrow(draw, (1110, 420), (1080, 620))
    arrow(draw, (1190, 420), (1440, 620))
    arrow(draw, (960, 420), (710, 620))
    image.save(DIAGRAMS / "uml-components.png", quality=95)

    image, draw = canvas("Docker Compose 部署图")
    rounded_box(draw, (80, 170, 1720, 900), "宿主机 Docker Compose 项目 javachatser", "#ffffff", "#64748b", 30)
    nodes = [
        ((180, 320, 480, 480), "frontend\nNginx + Vue\n宿主端口 5173"),
        ((620, 320, 940, 480), "backend\nSpring Boot 3\n宿主端口 8080"),
        ((1080, 320, 1380, 480), "mysql\nMySQL 8\n内部 3306"),
        ((1080, 640, 1380, 800), "redis\nRedis 7\n内部 6379"),
        ((620, 640, 940, 800), "backend-uploads\n持久化卷"),
    ]
    for box, text in nodes:
        rounded_box(draw, box, text, "#fef3c7", "#d97706", 26)
    arrow(draw, (480, 400), (620, 400))
    arrow(draw, (940, 400), (1080, 400))
    arrow(draw, (940, 450), (1080, 690))
    arrow(draw, (780, 480), (780, 640))
    draw.text((188, 265), "浏览器访问 http://localhost:5173", fill="#334155", font=font(24))
    image.save(DIAGRAMS / "uml-deployment.png", quality=95)


def word_app():
    app = win32com.client.Dispatch("Word.Application")
    app.Visible = False
    app.DisplayAlerts = 0
    return app


def cm(value: float) -> float:
    return value * 28.3464567


def line_multiple(size: float = 12, multiple: float = 1.35) -> float:
    return size * multiple


def set_page(doc) -> None:
    setup = doc.PageSetup
    setup.TopMargin = cm(2.54)
    setup.BottomMargin = cm(2.54)
    setup.LeftMargin = cm(3.67)
    setup.RightMargin = cm(2.67)
    setup.HeaderDistance = cm(1.5)
    setup.FooterDistance = cm(1.75)


def set_normal_style(doc) -> None:
    normal = doc.Styles(-1)
    normal.Font.Name = "宋体"
    normal.Font.NameFarEast = "宋体"
    normal.Font.Size = 12
    normal.ParagraphFormat.LineSpacingRule = 5
    normal.ParagraphFormat.LineSpacing = line_multiple()
    normal.ParagraphFormat.SpaceAfter = 6


def set_font(selection, name: str = "宋体", size: float = 12, bold: bool = False) -> None:
    selection.Font.Name = name
    try:
        selection.Font.NameFarEast = name
    except Exception:
        selection.Font.NameFarEast = "宋体"
    selection.Font.Size = size
    selection.Font.Bold = -1 if bold else 0


def set_para(selection, align: int = WD_ALIGN_JUSTIFY, space_after: float = 6, multiple: float = 1.35) -> None:
    selection.ParagraphFormat.Alignment = align
    selection.ParagraphFormat.LineSpacingRule = 5
    selection.ParagraphFormat.LineSpacing = line_multiple(selection.Font.Size or 12, multiple)
    selection.ParagraphFormat.SpaceAfter = space_after


def type_para(selection, text: str = "", size: float = 12, bold: bool = False, align: int = WD_ALIGN_JUSTIFY) -> None:
    set_font(selection, size=size, bold=bold)
    set_para(selection, align=align)
    selection.TypeText(text)
    selection.TypeParagraph()


def heading(selection, text: str, level: int) -> None:
    try:
        selection.Style = selection.Document.Styles(-1 - level)
    except Exception:
        pass
    type_para(selection, text, size=14 if level == 1 else 12, bold=True, align=WD_ALIGN_LEFT)
    try:
        selection.Style = selection.Document.Styles(-1)
    except Exception:
        pass


def add_table(selection, headers: list[str], rows: list[list[str]], widths: list[float] | None = None) -> None:
    doc = selection.Document
    table = doc.Tables.Add(selection.Range, len(rows) + 1, len(headers))
    table.Borders.Enable = True
    table.AllowAutoFit = True
    table.TopPadding = 4
    table.BottomPadding = 4
    table.LeftPadding = 5
    table.RightPadding = 5
    table.Range.Font.Name = "宋体"
    table.Range.Font.NameFarEast = "宋体"
    table.Range.Font.Size = 10.5
    table.Range.ParagraphFormat.LineSpacingRule = 5
    table.Range.ParagraphFormat.LineSpacing = line_multiple(10.5, 1.2)
    table.Range.ParagraphFormat.SpaceAfter = 3
    for i, header in enumerate(headers, start=1):
        cell = table.Cell(1, i)
        cell.Range.Text = header
        cell.Range.Font.Bold = -1
        cell.Shading.BackgroundPatternColor = 15724527
        cell.VerticalAlignment = 1
    for r, row in enumerate(rows, start=2):
        for c, value in enumerate(row, start=1):
            cell = table.Cell(r, c)
            cell.Range.Text = value
            cell.VerticalAlignment = 1
    if widths:
        for i, width in enumerate(widths, start=1):
            table.Columns(i).Width = cm(width)
    table.Rows.Alignment = WD_ALIGN_LEFT
    table.Range.Select()
    selection.Collapse(WD_COLLAPSE_END)
    selection.TypeParagraph()


def add_picture(selection, image_path: Path, caption: str, max_width_cm: float = 14.0) -> None:
    if not image_path.exists():
        type_para(selection, f"{caption}（截图或图片待补充）", size=10.5, align=WD_ALIGN_CENTER)
        return
    inline = selection.InlineShapes.AddPicture(str(image_path), False, True)
    inline.LockAspectRatio = True
    max_width = cm(max_width_cm)
    if inline.Width > max_width:
        inline.Width = max_width
    selection.TypeParagraph()
    type_para(selection, caption, size=10.5, align=WD_ALIGN_CENTER)


def add_code_block(selection, title: str, code: str) -> None:
    type_para(selection, title, size=10.5, bold=True, align=WD_ALIGN_LEFT)
    set_font(selection, "Consolas", 9, False)
    selection.Font.NameFarEast = "等线"
    set_para(selection, align=WD_ALIGN_LEFT, space_after=0, multiple=1.0)
    for line in code.strip().splitlines():
        selection.TypeText(line.rstrip())
        selection.TypeParagraph()
    type_para(selection, "", size=6, align=WD_ALIGN_LEFT)


def generate_cover(app) -> None:
    if COVER.exists():
        COVER.unlink()
    doc = app.Documents.Add()
    try:
        set_page(doc)
        set_normal_style(doc)
        selection = app.Selection
        type_para(selection, "NANCHANG UNIVERSITY", size=16, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, f"《{COURSE_NAME}》大作业报告", size=20, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, "", size=12, align=WD_ALIGN_CENTER)
        cover_rows = [
            ["题    目", PROJECT_TITLE],
            ["专    业", MAJOR],
            ["班    级", CLASS_PLACEHOLDER],
            ["学    号", "待填写（每人提交时替换为本人学号）"],
            ["姓    名", "待填写（每人提交时替换为本人姓名）"],
            ["组    员", "成员A、成员B、成员C、成员D（姓名/学号待填写）"],
            ["任课教师", TEACHER_PLACEHOLDER],
            ["完成时间", FINISH_DATE],
        ]
        add_table(selection, ["项目", "内容"], cover_rows, [3.4, 10.2])
        type_para(selection, "说明：本封面保留个人信息占位，四名成员提交个人报告时分别替换本人姓名、学号、班级和教师信息。", size=10.5)
        doc.SaveAs2(str(COVER), FileFormat=WD_FORMAT_DOCX)
    finally:
        doc.Close(False)


def generate_task_book(app) -> None:
    if TASK_BOOK.exists():
        TASK_BOOK.unlink()
    doc = app.Documents.Add()
    try:
        set_page(doc)
        set_normal_style(doc)
        selection = app.Selection
        type_para(selection, "软件学院大作业任务书", size=18, bold=True, align=WD_ALIGN_CENTER)
        add_table(selection, ["项目", "内容"], [
            ["课程名称", COURSE_NAME],
            ["题    目", PROJECT_TITLE],
            ["专    业", MAJOR],
            ["班    级", CLASS_PLACEHOLDER],
            ["完成人数", "4 人"],
            ["起讫日期", DATE_RANGE],
            ["任课教师", TEACHER_PLACEHOLDER],
            ["系 主 任", "魏勍颋"],
            ["完成时间", "2026.06.20"],
        ], [3.2, 10.4])
        heading(selection, "说明", 1)
        type_para(selection, "本任务书由任课教师填写后下达到学生。任务完成后，学生按要求提交个人大作业报告和源代码压缩包。")
        heading(selection, "大作业的要求和内容", 1)
        heading(selection, "训练目的", 2)
        type_para(selection, "针对自选题目的需求，使用 Vue 3.0 + Spring Boot 3.0 技术，开发一个有一定规模和复杂度的具备企业级应用功能的 Web 应用程序，从而真正掌握 Java Web 应用开发相关的企业级应用开发技术，并具备应用这些技术高效开发企业应用软件的能力。")
        heading(selection, "训练内容和要求", 2)
        add_table(selection, ["序号", "要求", "本组落实情况"], [
            ["1", "每 3 到 4 位同学 1 组，每组 1 题，各组之间选题尽量不同。", "本组 4 人，题目为 JavaChatSer 在线即时聊天系统。"],
            ["2", "撰写适当的软件需求规格说明和软件设计规格说明，建议采用用例驱动的面向对象分析与设计方法。", "报告包含角色分析、20 个非基础用例、UML 用例图、模块/类关系图、组件图和部署图。"],
            ["3", "选用 Vue 3.0 + Spring Boot 3.0，建议使用 Spring Web、Spring、MyBatisPlus、Redis 等技术。", "项目使用 Vue 3、TypeScript、Pinia、Axios、Spring Boot 3、Spring Web、Spring Security、JWT、Spring Data JPA、Flyway、Redis、WebSocket、MySQL 和 Docker Compose。"],
            ["4", "完成大作业报告，封面参见模板，格式参见大作业格式要求。", "已生成正式报告、封面、任务书、UML 图、运行截图和源码压缩包。"],
        ], [1.4, 6.2, 7.0])
        heading(selection, "注意事项", 2)
        type_para(selection, "报告引言部分介绍项目背景和开发过程，包括 4 名成员分工。需求分析和软件设计部分包含 UML 图表和文字说明。功能实现部分包含关键代码、实现说明和运行效果图。结论部分总结开发结果、存在问题和开发体会。")
        type_para(selection, "系统功能按成员拆分，每位成员负责 5 个以上非基础用例；登录、注册、密码修改等基础用例不计入成员最低用例数量。")
        heading(selection, "递交时间", 1)
        type_para(selection, "在 2026 年 6 月 20 日前提交大作业报告，每人一份。提交内容除大作业报告外，还需提交源代码压缩包。")
        heading(selection, "考核方法", 1)
        type_para(selection, "课程考核建议结合项目功能完成度、技术栈使用情况、工程结构、报告规范性、测试验证结果、运行截图、源码质量和现场答辩演示进行综合评价。")
        heading(selection, "教师小结", 1)
        add_table(selection, ["项目", "内容"], [
            ["成绩", "待填写"],
            ["教师签名", "待填写"],
            ["教研部负责人", "待填写"],
            ["学生姓名", "待填写"],
        ], [3.2, 10.4])
        doc.SaveAs2(str(TASK_BOOK), FileFormat=WD_FORMAT_DOC)
    finally:
        doc.Close(False)


def generate_report(app) -> None:
    generate_diagrams()
    if REPORT.exists():
        REPORT.unlink()

    doc = app.Documents.Open(str(COVER))
    try:
        doc.SaveAs2(str(REPORT), FileFormat=WD_FORMAT_DOCX)
        set_page(doc)
        set_normal_style(doc)
        selection = app.Selection
        selection.EndKey(WD_STORY)
        selection.InsertBreak(WD_BREAK_PAGE)

        type_para(selection, "JavaChatSer 在线即时聊天系统大作业报告", size=18, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, "题目：JavaChatSer 在线即时聊天系统", size=12, align=WD_ALIGN_CENTER)
        type_para(selection, "姓名、学号、班级、任课教师：保留占位，每位成员提交时替换为本人信息。", size=12, align=WD_ALIGN_CENTER)

        heading(selection, "1 引言", 1)
        heading(selection, "1.1 项目背景", 2)
        type_para(selection, "JavaChatSer 原项目是一个传统 Servlet/JSP 聊天原型，包含用户、好友、私聊、公共聊天室、Redis 缓存和原生 Socket 在线转发等基础能力。课程任务要求使用 Vue 3.0 与 Spring Boot 3.0 开发具备一定规模和复杂度的企业级 Web 应用，因此本项目在保留原有聊天业务的基础上，升级为前后端分离的在线即时聊天系统。")
        type_para(selection, "新版系统采用 Spring Boot 3、Spring Web、Spring Security、JWT、Spring Data JPA、Flyway、Spring Data Redis、Spring WebSocket、Vue 3、TypeScript、Vite、Pinia、Axios、Nginx 和 Docker Compose，实现了认证、好友关系、实时私聊、公共聊天室、图片上传、消息撤回、在线状态、未读消息和统计概览等功能。")
        heading(selection, "1.2 开发目标", 2)
        add_table(selection, ["目标类别", "具体目标"], [
            ["功能目标", "完成好友管理、私聊、公共聊天室、文件上传、统计概览、实时通知和主题切换等企业级应用功能。"],
            ["技术目标", "使用 Spring Boot 3 + Vue 3 前后端分离架构，使用 MySQL 持久化，使用 Redis 缓存和在线状态。"],
            ["工程目标", "提供统一接口响应、分层后端结构、路由化前端页面、自动化测试、Docker Compose 一键部署和可答辩文档。"],
            ["课程目标", "形成可运行、可展示、可说明的大作业成果，并在报告中包含需求、设计、实现、截图、测试和总结。"],
        ], [3.0, 11.0])
        heading(selection, "1.3 开发过程与分工", 2)
        type_para(selection, "本项目按“旧系统分析、后端重构、前端实现、部署验证、文档整理”的顺序推进。旧版代码保留在 src/ 目录作为业务参考，新版后端位于 backend/，新版前端位于 frontend/，课程文档和报告材料位于 docs/。")
        add_table(selection, ["成员", "主要分工", "负责用例", "完成情况"], [
            [MEMBERS[0][0], MEMBERS[0][1], "UC-01 到 UC-05", "已完成"],
            [MEMBERS[1][0], MEMBERS[1][1], "UC-06 到 UC-10", "已完成"],
            [MEMBERS[2][0], MEMBERS[2][1], "UC-11 到 UC-15", "已完成"],
            [MEMBERS[3][0], MEMBERS[3][1], "UC-16 到 UC-20", "已完成"],
        ], [3.0, 5.0, 3.2, 2.0])
        type_para(selection, "说明：登录、注册、退出登录等基础功能已实现，但不计入课程要求的“每位成员 5 个以上用例”。上表每位成员均负责 5 个非基础业务用例。")

        heading(selection, "2 需求分析", 1)
        heading(selection, "2.1 用户角色", 2)
        add_table(selection, ["角色", "权限与职责"], [
            ["普通用户", "维护资料、搜索用户、发送和处理好友申请、删除好友、私聊、公共聊天、上传图片、查看个人统计和切换主题。"],
            ["管理员", "拥有普通用户功能，并可查看全局统计、撤回任意用户发送的私聊或公共消息。"],
        ], [3.0, 11.0])
        heading(selection, "2.2 用例需求", 2)
        add_picture(selection, DIAGRAMS / "uml-use-cases.png", "图 2-1 系统用例图", 14.0)
        add_table(
            selection,
            ["编号", "用例", "成员", "主要流程", "验收标准"],
            [[case.code, case.title, case.owner, case.flow, case.acceptance] for case in USE_CASES],
            [1.4, 2.5, 1.6, 5.2, 5.0],
        )
        heading(selection, "2.3 非功能需求", 2)
        add_table(selection, ["类别", "要求"], [
            ["安全性", "使用 JWT 保护除注册、登录、健康检查和静态资源外的接口；密码使用 BCrypt 哈希保存；WebSocket 握手阶段校验 Token。"],
            ["可靠性", "消息以 MySQL 为最终事实来源，Redis 只保存可重建缓存，Redis 短暂不可用不应导致核心消息丢失。"],
            ["可维护性", "后端按 Controller、Service、Repository、DTO、Entity 分层，前端按 views、stores、api、components 组织。"],
            ["可部署性", "通过 Docker Compose 启动 MySQL、Redis、backend、frontend，前端 Nginx 代理 /api、/ws、/uploads。"],
            ["可测试性", "后端提供 JUnit 5 + Spring Boot Test 自动化测试，前端提供 TypeScript 类型检查和生产构建验证。"],
        ], [3.0, 11.0])

        heading(selection, "3 软件设计", 1)
        heading(selection, "3.1 总体架构", 2)
        type_para(selection, "系统采用前后端分离架构。浏览器访问 Vue 3 单页应用，前端通过 Axios 调用 REST API，通过原生 WebSocket 连接后端实时通道；后端使用 Spring Boot 3 暴露接口和 WebSocket 服务；MySQL 保存用户、好友和消息数据；Redis 保存好友列表缓存、最近消息缓存、在线状态和未读数。")
        add_picture(selection, DIAGRAMS / "uml-components.png", "图 3-1 系统组件图", 14.0)
        heading(selection, "3.2 后端模块设计", 2)
        add_picture(selection, DIAGRAMS / "uml-module-classes.png", "图 3-2 后端类/模块关系图", 14.0)
        add_table(selection, ["模块", "主要职责"], [
            ["common", "统一响应 ApiResponse、分页 PageResponse、业务异常和全局异常处理。"],
            ["security", "JWT 生成校验、认证过滤器、安全配置和登录用户上下文。"],
            ["user", "注册、登录、当前用户、搜索用户和头像更新。"],
            ["friend", "好友申请、接受、拒绝、删除、好友列表、在线状态和未读数聚合。"],
            ["chat", "私聊、公共聊天、消息分页、已读、撤回、图片消息和缓存更新。"],
            ["websocket", "WebSocket 握手认证、会话管理、实时消息、好友上下线和通知推送。"],
            ["stats/media", "系统统计、个人统计、头像和聊天图片存储。"],
        ], [3.0, 11.0])
        heading(selection, "3.3 数据库与 Redis 设计", 2)
        add_table(selection, ["数据表", "用途", "关键字段"], [
            ["chat_user", "用户账号表", "username、password_hash、nickname、avatar_url、bio、role、enabled"],
            ["friend_relation", "好友关系和申请表", "user_id、friend_id、status、created_at、updated_at"],
            ["private_message", "私聊消息表", "sender_id、receiver_id、content、message_type、read_at、recalled_at"],
            ["public_message", "公共聊天室消息表", "sender_id、content、message_type、recalled_at、created_at"],
        ], [3.0, 4.2, 6.8])
        add_table(selection, ["Redis Key", "用途", "过期策略"], [
            ["online:user:{userId}", "保存在线状态和 sessionId", "2 分钟心跳续期"],
            ["friend:list:{userId}", "缓存好友基础资料列表", "5 分钟"],
            ["chat:private:{minUserId}:{maxUserId}", "缓存最近私聊消息", "1 天"],
            ["chat:public:recent", "缓存最近公共消息", "1 天"],
            ["unread:{userId}:{friendId}", "保存某好友未读消息数", "无固定 TTL"],
        ], [5.0, 6.0, 3.0])
        heading(selection, "3.4 部署设计", 2)
        add_picture(selection, DIAGRAMS / "uml-deployment.png", "图 3-3 Docker Compose 部署图", 14.0)
        type_para(selection, "Docker Compose 编排 mysql、redis、backend 和 frontend。前端容器只暴露 5173 端口，Nginx 将 /api、/ws 和 /uploads 代理到后端容器；后端容器依赖 MySQL 和 Redis 健康检查后启动。")

        heading(selection, "4 软件实现", 1)
        heading(selection, "4.1 后端关键实现", 2)
        type_para(selection, "后端入口为 backend/src/main/java/com/example/javachat/JavaChatApplication.java。接口层只负责参数接收和统一响应，业务规则集中在 Service 层，数据库访问交由 Repository。全局异常处理把参数错误、未登录、无权限、冲突和服务端错误转换成统一 code/message/data 结构。")
        add_table(selection, ["功能", "接口或类", "实现说明"], [
            ["认证", "AuthController、UserService、JwtTokenProvider", "登录校验 BCrypt 密码，成功后返回 JWT；后续请求由 JwtAuthenticationFilter 写入 LoginUser。"],
            ["好友", "FriendController、FriendService", "申请为 PENDING，接受时生成双向 ACCEPTED 关系，删除时双向置为 DELETED。"],
            ["私聊", "ChatController、ChatService", "发送前检查好友关系，写入 private_message，更新 Redis 最近消息和未读数。"],
            ["公共聊天室", "ChatWebSocketHandler、ChatRealtimeNotifier", "REST 或 WebSocket 发送后写库、缓存并广播给在线连接。"],
            ["上传", "MediaStorageService、UploadResourceConfig", "限制图片文件，保存到 uploads/avatars 或 uploads/images，并暴露静态访问路径。"],
        ], [3.0, 5.0, 6.0])
        add_code_block(selection, "代码清单 4-1 JWT 生成关键代码", """
public String createToken(User user) {
    Instant now = Instant.now(clock);
    Instant expiresAt = now.plusMillis(properties.expiration());
    return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey())
            .compact();
}
""")
        type_para(selection, "该代码把用户主键、用户名、角色和过期时间写入 JWT，并使用配置中的密钥签名。后续 REST 请求和 WebSocket 握手都会基于该 Token 恢复 LoginUser。")
        add_code_block(selection, "代码清单 4-2 私聊发送关键代码", """
ensurePrivateChatAllowed(senderId, receiverId);
MessageType messageType = parseMessageType(request.messageType());
validateMessageContent(request.content(), messageType);
PrivateMessage message = new PrivateMessage(senderId, receiverId, request.content().trim(), messageType);
PrivateMessage savedMessage = privateMessageRepository.saveAndFlush(message);
privateChatCacheService.cacheRecentMessage(senderId, receiverId, response);
privateChatCacheService.setUnreadCount(receiverId, senderId, unreadCount);
""")
        type_para(selection, "私聊发送先校验好友关系和消息类型，再持久化到 MySQL，最后更新 Redis 最近消息与未读数。这样即使实时推送失败，消息仍可从数据库历史中恢复。")
        add_code_block(selection, "代码清单 4-3 WebSocket 私聊处理关键代码", """
PrivateMessageResponse response = chatService.sendPrivateMessage(
        loginUser.id(),
        receiverId,
        new PrivateMessageSendRequest(content, optionalText(payload, "messageType"))
);
WebSocketEnvelope<PrivateMessageResponse> envelope = WebSocketEnvelope.of(
        WebSocketMessageType.PRIVATE_MESSAGE,
        response
);
sessionManager.sendToUser(loginUser.id(), envelope);
sessionManager.sendToUser(receiverId, envelope);
""")
        type_para(selection, "WebSocket 层不重复实现业务规则，而是复用 ChatService。发送成功后同时推送给发送者和接收者，保证两个窗口的消息状态一致。")

        heading(selection, "4.2 前端关键实现", 2)
        type_para(selection, "前端入口为 frontend/src/main.ts，路由位于 frontend/src/router/index.ts。认证状态保存在 Pinia 的 auth store 中，Axios 请求拦截器自动附带 Authorization；chat store 负责 WebSocket 连接、心跳、自动重连、消息收发、图片发送和撤回同步；friends store 负责好友列表、申请列表、搜索结果、在线状态和未读数。")
        add_table(selection, ["页面", "路径", "功能"], [
            ["登录页", "/login", "用户名密码登录，保存 Token 后进入聊天页面。"],
            ["注册页", "/register", "注册账号并直接登录。"],
            ["聊天页", "/chat", "公共聊天室、好友私聊、在线状态、未读数、图片消息和撤回。"],
            ["好友页", "/friends", "搜索用户、发送申请、处理申请、删除好友。"],
            ["个人资料", "/profile", "查看账号资料、上传头像、退出登录。"],
            ["系统概览", "/dashboard", "展示用户、在线、消息、好友申请和好友关系统计。"],
        ], [3.0, 3.0, 8.0])
        add_code_block(selection, "代码清单 4-4 前端 WebSocket 连接关键代码", """
this.socket = new WebSocket(`${WS_BASE_URL}/ws/chat?token=${encodeURIComponent(token)}`)
this.socket.onopen = () => {
  this.socketStatus = 'open'
  this.heartbeatTimer = setInterval(() => {
    if (this.isSocketOpen) {
      this.socket?.send(JSON.stringify({ type: 'PING' }))
    }
  }, 30000)
}
this.socket.onmessage = (event) => this.handleSocketMessage(event.data)
""")
        type_para(selection, "前端在连接地址中携带 JWT，连接成功后每 30 秒发送 PING 心跳。收到服务端消息后统一进入 handleSocketMessage，再按消息类型更新好友列表、聊天记录、未读数和撤回状态。")

        heading(selection, "4.3 接口与 WebSocket 实现", 2)
        add_table(selection, ["类型", "路径或消息", "说明"], [
            ["REST", "POST /api/auth/login", "登录并返回 JWT 和用户资料。"],
            ["REST", "GET /api/friends", "返回好友资料、在线状态和未读消息数。"],
            ["REST", "POST /api/chats/private/{friendId}/messages", "发送私聊消息。"],
            ["REST", "GET /api/chats/public/messages", "分页查询公共聊天室历史。"],
            ["REST", "GET /api/stats/overview", "管理员返回全局统计，普通用户返回个人统计。"],
            ["WebSocket", "PRIVATE_MESSAGE", "发送私聊并推送给发送者和接收者。"],
            ["WebSocket", "PUBLIC_MESSAGE", "发送公共消息并广播给所有在线用户。"],
            ["WebSocket", "FRIEND_STATUS / FRIEND_REQUEST / MESSAGE_RECALLED", "推送好友上下线、申请和撤回事件。"],
        ], [3.0, 5.0, 6.0])

        heading(selection, "4.4 运行效果", 2)
        screenshots = [
            ("01-login.png", "图 4-1 登录页面"),
            ("02-register.png", "图 4-2 注册页面"),
            ("03-friends.png", "图 4-3 好友管理页面"),
            ("04-public-chat.png", "图 4-4 公共聊天室页面"),
            ("05-private-chat.png", "图 4-5 私聊页面"),
            ("06-profile.png", "图 4-6 个人资料页面"),
            ("07-dashboard.png", "图 4-7 系统概览页面"),
        ]
        for file_name, caption in screenshots:
            add_picture(selection, SCREENSHOTS / file_name, caption, max_width_cm=13.2)

        heading(selection, "4.5 测试与验收", 2)
        type_para(selection, "本项目采用自动化测试、前端构建验证、文档结构检查和源码压缩包检查共同验收。后端测试覆盖认证、用户、好友、聊天、统计、WebSocket 会话管理、在线状态和公共聊天缓存等场景；前端构建验证 TypeScript 类型和 Vue 生产构建。")
        add_table(selection, ["验证项", "命令或文件", "验收结果"], [
            ["后端自动化测试", "cd backend; mvn test", "31 个测试执行通过，Failures=0，Errors=0，Skipped=0。"],
            ["前端类型检查与构建", "cd frontend; npm run build", "vue-tsc 与 vite build 执行通过，生成 dist 生产资源。"],
            ["运行截图", "docs/screenshots/01-login.png 到 07-dashboard.png", "覆盖登录、注册、好友、公共聊天、私聊、资料和概览页面。"],
            ["UML 与部署图", "docs/diagrams/*.png", "覆盖用例图、模块/类关系图、组件图和 Docker Compose 部署图。"],
            ["源码提交包", "JavaChatSer-source-submit.zip", "包含源码、测试、文档、截图和 Word，排除 .git、node_modules、target、dist、out。"],
        ], [3.4, 5.8, 5.8])

        heading(selection, "5 结论", 1)
        type_para(selection, "本项目完成了从传统 Java Web 聊天原型到 Spring Boot 3 + Vue 3 前后端分离系统的升级。系统已经具备好友申请、实时私聊、公共聊天室、图片消息、消息撤回、在线状态、未读消息、统计概览和 Docker Compose 部署能力，符合课程对企业级应用开发技术栈、规模和复杂度的要求。")
        type_para(selection, "项目中仍有可以继续完善的方向，例如接入更细粒度的消息搜索、群组聊天室、文件消息、操作审计、移动端适配和更完整的自动化端到端测试。这些扩展不影响当前大作业提交，但可以作为后续迭代方向。")

        heading(selection, "6 体会与建议", 1)
        type_para(selection, "通过本次大作业，项目从早期 Servlet、JSP、原生 Socket 和手写数据库访问逐步演进为分层清晰、可部署、可测试、可展示的现代 Web 应用。实践中体会最深的是：企业级应用不仅要能实现功能，还要关注安全、分层、缓存一致性、接口规范、错误处理、部署方式和文档可维护性。")
        type_para(selection, "建议后续开发继续保持“先设计接口和数据结构，再实现业务，再补充验证和文档”的节奏。对于聊天类系统，应始终坚持消息先持久化再推送，避免把 Redis 或 WebSocket 当作最终数据来源。")
        doc.SaveAs2(str(REPORT), FileFormat=WD_FORMAT_DOCX)
    finally:
        doc.Close(False)


def generate_member_a_report(app) -> None:
    generate_diagrams()
    if MEMBER_A_REPORT.exists():
        MEMBER_A_REPORT.unlink()

    member_a_cases = [case for case in USE_CASES if case.owner == "成员A"]
    doc = app.Documents.Add()
    try:
        set_page(doc)
        set_normal_style(doc)
        selection = app.Selection

        type_para(selection, "NANCHANG UNIVERSITY", size=16, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, f"《{COURSE_NAME}》大作业个人报告", size=20, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, "", size=12, align=WD_ALIGN_CENTER)
        add_table(selection, ["项目", "内容"], [
            ["题    目", PROJECT_TITLE],
            ["专    业", MAJOR],
            ["班    级", CLASS_PLACEHOLDER],
            ["学    号", "待填写（成员A本人学号）"],
            ["姓    名", "待填写（成员A本人姓名）"],
            ["组内身份", "成员A"],
            ["主要分工", "后端用户与好友模块；负责用户搜索、好友申请、申请列表、申请处理、删除好友 5 个非基础用例。"],
            ["任课教师", TEACHER_PLACEHOLDER],
            ["完成时间", FINISH_DATE],
        ], [3.2, 10.4])
        selection.InsertBreak(WD_BREAK_PAGE)

        type_para(selection, "JavaChatSer 在线即时聊天系统个人大作业报告", size=18, bold=True, align=WD_ALIGN_CENTER)
        type_para(selection, "成员身份：成员A（后端用户与好友模块）", size=12, align=WD_ALIGN_CENTER)
        type_para(selection, "姓名、学号、班级、任课教师：提交前替换为本人真实信息。", size=12, align=WD_ALIGN_CENTER)

        heading(selection, "1 引言", 1)
        heading(selection, "1.1 项目背景", 2)
        type_para(selection, "JavaChatSer 是一个从传统 Servlet/JSP 聊天原型升级而来的 Spring Boot 3 + Vue 3 前后端分离聊天系统。课程要求每组使用 Vue 3.0 与 Spring Boot 3.0 开发具有一定规模和复杂度的企业级 Web 应用，因此本项目围绕在线即时聊天场景，重构并扩展了用户、好友、私聊、公共聊天室、实时通知、上传、统计和部署等功能。")
        type_para(selection, "本人作为成员A，主要负责后端用户与好友管理模块。该模块是聊天系统的关系入口：用户需要先搜索目标用户、发送好友申请、等待对方处理并建立关系，之后才能进入私聊。好友模块还需要支持删除关系、缓存失效、在线状态和未读数聚合，为后续聊天功能提供稳定的数据基础。")
        heading(selection, "1.2 个人开发目标", 2)
        add_table(selection, ["目标类别", "个人目标"], [
            ["需求目标", "完成 5 个非基础业务用例：搜索用户、发送好友申请、查看收到的申请、接受或拒绝申请、删除好友。"],
            ["设计目标", "将好友相关逻辑放在 friend 包中，按 Controller、Service、Repository、DTO 分层，避免接口层直接处理业务规则。"],
            ["实现目标", "实现好友申请状态流转、双向好友关系、重复关系校验、缓存失效和实时好友申请通知。"],
            ["验证目标", "通过 FriendControllerTest 等自动化测试验证好友申请、接受、拒绝、删除和异常分支。"],
        ], [3.0, 11.0])
        heading(selection, "1.3 组内分工与本人职责", 2)
        add_table(selection, ["成员", "主要分工", "负责用例", "说明"], [
            [MEMBERS[0][0], MEMBERS[0][1], "UC-01 到 UC-05", "本人负责，聚焦用户与好友关系后端实现。"],
            [MEMBERS[1][0], MEMBERS[1][1], "UC-06 到 UC-10", "负责私聊、图片消息、已读和撤回。"],
            [MEMBERS[2][0], MEMBERS[2][1], "UC-11 到 UC-15", "负责公共聊天室、WebSocket 和部署。"],
            [MEMBERS[3][0], MEMBERS[3][1], "UC-16 到 UC-20", "负责前端通知、上传、统计、主题和文档测试。"],
        ], [3.0, 4.7, 3.0, 3.3])
        type_para(selection, "登录、注册、退出登录等基础功能已实现，但不计入课程要求的“每位成员 5 个以上用例”。本报告重点说明成员A负责的 5 个非基础用例。")

        heading(selection, "2 需求分析", 1)
        heading(selection, "2.1 用户角色与业务边界", 2)
        type_para(selection, "成员A模块面向普通用户和管理员两类角色。普通用户可以搜索其他用户、发送好友申请、查看收到的申请、接受或拒绝申请、删除好友；管理员在好友模块中暂不额外增加特殊权限，主要通过后续统计与消息管理模块体现管理员能力。")
        add_picture(selection, DIAGRAMS / "uml-use-cases.png", "图 2-1 系统用例图（成员A负责 UC-01 到 UC-05）", 14.0)
        heading(selection, "2.2 成员A负责用例", 2)
        add_table(
            selection,
            ["编号", "用例", "主要流程", "验收标准"],
            [[case.code, case.title, case.flow, case.acceptance] for case in member_a_cases],
            [1.6, 3.0, 5.7, 5.0],
        )
        heading(selection, "2.3 成员A模块非功能需求", 2)
        add_table(selection, ["类别", "要求"], [
            ["安全性", "搜索用户、好友申请、处理申请和删除好友接口均需要 JWT 登录态；接口不能允许用户添加自己或绕过好友状态。"],
            ["一致性", "接受好友申请时要建立双向 ACCEPTED 关系；删除好友时双方关系都应置为 DELETED，并清理双方好友缓存。"],
            ["可维护性", "好友业务规则集中在 FriendService，Controller 只负责接收参数和返回统一响应。"],
            ["可测试性", "使用 MockMvc 和 H2 内存数据库覆盖正常流程、重复申请、自我添加、非本人申请处理等场景。"],
        ], [3.0, 11.0])

        heading(selection, "3 软件设计", 1)
        heading(selection, "3.1 总体架构", 2)
        type_para(selection, "成员A模块位于整体 Spring Boot 后端中。前端好友页面通过 Axios 调用 /api/users/search、/api/friends、/api/friends/requests 等 REST 接口；后端 FriendController 接收请求后调用 FriendService；FriendService 使用 UserRepository 和 FriendRepository 访问 MySQL，并通过 FriendCacheService、PrivateChatCacheService 聚合在线状态和未读数。")
        add_picture(selection, DIAGRAMS / "uml-components.png", "图 3-1 系统组件图", 14.0)
        heading(selection, "3.2 成员A后端模块设计", 2)
        add_picture(selection, DIAGRAMS / "uml-module-classes.png", "图 3-2 后端模块关系图", 14.0)
        add_table(selection, ["类或模块", "职责"], [
            ["UserController / UserService", "提供当前用户资料、用户搜索和头像更新等用户相关能力，其中成员A关注用户搜索。"],
            ["FriendController", "暴露好友列表、发送申请、查看申请、接受申请、拒绝申请和删除好友 REST 接口。"],
            ["FriendService", "集中处理好友状态流转、自我添加校验、重复关系校验、双向关系建立和缓存失效。"],
            ["FriendRepository", "查询和保存 friend_relation 表，支持按用户、好友、状态和申请 ID 检索关系。"],
            ["FriendCacheService", "缓存好友基础资料列表，聚合 Redis 在线状态，好友关系变化后清理缓存。"],
            ["Friend DTO", "定义 FriendRequestCreateRequest、FriendRequestResponse、FriendResponse 和 FriendUserResponse 等接口数据结构。"],
        ], [4.0, 10.0])
        heading(selection, "3.3 数据库设计", 2)
        add_table(selection, ["数据表", "成员A关注字段", "说明"], [
            ["chat_user", "id、username、nickname、avatar_url、enabled", "用户搜索和好友展示依赖该表；enabled=false 的用户不能被添加。"],
            ["friend_relation", "user_id、friend_id、status、created_at、updated_at", "保存好友申请和好友关系，status 包括 PENDING、ACCEPTED、REJECTED、DELETED。"],
        ], [3.0, 5.0, 6.0])
        type_para(selection, "好友接受后系统保存双向关系：A -> B 与 B -> A 都为 ACCEPTED。这样读取某个用户的好友列表时，只需按 user_id 和 ACCEPTED 查询即可，减少额外方向判断。")

        heading(selection, "4 软件实现", 1)
        heading(selection, "4.1 用户搜索实现", 2)
        type_para(selection, "用户搜索接口用于在添加好友前定位目标用户。接口会根据用户名或昵称分页查询，并排除当前登录用户，避免用户把自己加入好友列表。搜索结果只返回必要展示字段，避免向前端暴露密码哈希等敏感信息。")
        add_table(selection, ["接口", "路径", "说明"], [
            ["搜索用户", "GET /api/users/search?keyword=&page=0&size=10", "按用户名或昵称搜索，排除当前用户。"],
            ["好友列表", "GET /api/friends", "返回好友基础资料、在线状态和未读数。"],
        ], [3.0, 6.0, 5.0])
        heading(selection, "4.2 好友申请实现", 2)
        type_para(selection, "发送好友申请时，系统先校验不能添加自己，再确认申请人和目标用户均存在且启用。随后同时检查正向和反向关系，只要存在 PENDING 或 ACCEPTED 状态，就认为好友关系或申请已存在，返回冲突错误。")
        add_code_block(selection, "代码清单 4-1 发送好友申请关键代码", """
if (requesterId.equals(friendId)) {
    throw new BusinessException(ErrorCode.BAD_REQUEST, "不能添加自己为好友");
}
FriendRelation existingForward = friendRepository.findByUserIdAndFriendId(requesterId, friendId)
        .orElse(null);
FriendRelation existingReverse = friendRepository.findByUserIdAndFriendId(friendId, requesterId)
        .orElse(null);
if (isActiveRelation(existingForward) || isActiveRelation(existingReverse)) {
    throw new BusinessException(ErrorCode.CONFLICT, "好友关系或申请已存在");
}
FriendRelation savedRelation = friendRepository.save(relation);
chatRealtimeNotifier.notifyFriendRequest(targetUser.getId(), response);
""")
        type_para(selection, "该实现保证了同一对用户之间不会出现重复申请，也不会在对方已经发起申请时再次创建冲突数据。保存申请后还会通知在线接收者，使好友页能够实时更新待处理数量。")
        heading(selection, "4.3 好友申请处理实现", 2)
        type_para(selection, "接收者可以查看收到的 PENDING 申请，并选择接受或拒绝。接受申请时，系统会把原始申请置为 ACCEPTED，再创建或更新反向关系为 ACCEPTED，从而形成双向好友关系。拒绝申请时，只修改当前申请状态，不创建好友关系。")
        add_code_block(selection, "代码清单 4-2 接受好友申请关键代码", """
FriendRelation request = findPendingRequestForUser(userId, requestId);
request.setStatus(FriendStatus.ACCEPTED);
FriendRelation savedRequest = friendRepository.save(request);

FriendRelation reverseRelation = friendRepository.findByUserIdAndFriendId(userId, request.getUserId())
        .orElseGet(() -> new FriendRelation(userId, request.getUserId(), FriendStatus.ACCEPTED));
reverseRelation.setStatus(FriendStatus.ACCEPTED);
friendRepository.save(reverseRelation);

evictFriendCaches(userId, request.getUserId());
""")
        type_para(selection, "findPendingRequestForUser 会校验申请是否属于当前用户，避免用户处理不属于自己的申请。接受后清理双方好友缓存，保证下一次读取好友列表能得到最新关系。")
        heading(selection, "4.4 删除好友实现", 2)
        type_para(selection, "删除好友并不直接物理删除记录，而是将双方 ACCEPTED 关系置为 DELETED。这样既保留历史状态，也避免聊天历史因好友关系删除而丢失。删除完成后同步清理双方 friend:list 缓存。")
        add_code_block(selection, "代码清单 4-3 删除好友关键代码", """
FriendRelation relation = friendRepository.findByUserIdAndFriendId(userId, friendId)
        .filter(item -> item.getStatus() == FriendStatus.ACCEPTED)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "好友关系不存在"));
FriendRelation reverseRelation = friendRepository.findByUserIdAndFriendId(friendId, userId)
        .filter(item -> item.getStatus() == FriendStatus.ACCEPTED)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "好友关系不存在"));

relation.setStatus(FriendStatus.DELETED);
reverseRelation.setStatus(FriendStatus.DELETED);
friendRepository.save(relation);
friendRepository.save(reverseRelation);
evictFriendCaches(userId, friendId);
""")
        heading(selection, "4.5 运行效果", 2)
        add_picture(selection, SCREENSHOTS / "03-friends.png", "图 4-1 好友管理页面：搜索用户、发送申请、处理申请和删除好友", 13.2)
        heading(selection, "4.6 测试与验收", 2)
        type_para(selection, "成员A相关测试主要位于 backend/src/test/java/com/example/javachat/friend/FriendControllerTest.java，同时用户搜索和头像等用户接口由 UserControllerTest 覆盖。后端整体测试使用 H2 内存数据库、MockMvc 和 Spring Boot Test，验证接口响应、业务状态和异常分支。")
        add_table(selection, ["验证项", "命令或测试文件", "验收重点"], [
            ["好友接口测试", "FriendControllerTest", "发送申请、重复申请、接受申请、拒绝申请、好友列表、删除好友。"],
            ["用户接口测试", "UserControllerTest", "用户搜索、当前用户资料和头像相关接口。"],
            ["后端回归测试", "cd backend; mvn test", "31 个测试通过，Failures=0，Errors=0，Skipped=0。"],
            ["前端构建验证", "cd frontend; npm run build", "vue-tsc 类型检查与 vite build 生产构建通过。"],
        ], [3.0, 5.0, 6.0])

        heading(selection, "5 结论", 1)
        type_para(selection, "成员A负责的后端用户与好友模块已经实现搜索用户、发送好友申请、查看收到的申请、接受或拒绝申请、删除好友 5 个非基础业务用例。模块采用 Spring Boot 3 分层结构，业务规则集中在 FriendService，数据通过 JPA Repository 持久化到 MySQL，并结合 Redis 缓存好友列表、在线状态和未读数。")
        type_para(selection, "从项目整体看，好友模块为私聊功能提供了必要前置条件：非好友不能发送私聊，好友删除后关系状态会变化，好友列表可以显示在线状态和未读数。该模块与聊天、WebSocket、前端好友页之间形成了清晰协作关系。")

        heading(selection, "6 体会与建议", 1)
        type_para(selection, "通过负责成员A模块，我体会到企业级 Web 应用不能只关注单个接口是否能返回数据，还必须关注状态流转、权限边界、重复请求、缓存一致性和测试覆盖。好友关系看似简单，但如果不处理正反向关系、重复申请、自我添加和缓存失效，后续聊天功能就会出现数据不一致。")
        type_para(selection, "后续如果继续完善，可以为好友模块增加申请备注、黑名单、好友分组、操作审计和更完整的端到端测试。现阶段实现已经满足课程对每位成员至少 5 个非基础用例、Spring Boot 3 + Vue 3 技术栈、文档说明和测试验证的要求。")

        doc.SaveAs2(str(MEMBER_A_REPORT), FileFormat=WD_FORMAT_DOCX)
    finally:
        doc.Close(False)


def should_package(path: Path) -> bool:
    rel = path.relative_to(ROOT)
    parts = set(rel.parts)
    excluded_dirs = {
        ".git",
        ".idea",
        ".vscode",
        ".worktrees",
        "__pycache__",
        "node_modules",
        "target",
        "dist",
        "out",
    }
    if parts & excluded_dirs:
        return False
    if path.name in {".env", SUBMIT_ZIP.name}:
        return False
    if path.suffix in {".pyc", ".class"}:
        return False
    return path.is_file()


def generate_source_zip() -> None:
    if SUBMIT_ZIP.exists():
        SUBMIT_ZIP.unlink()
    with ZipFile(SUBMIT_ZIP, "w", compression=ZIP_DEFLATED, compresslevel=9) as archive:
        for path in sorted(ROOT.rglob("*")):
            if should_package(path):
                archive.write(path, path.relative_to(ROOT).as_posix())


def generate_all(no_zip: bool = False) -> None:
    DOCS.mkdir(parents=True, exist_ok=True)
    app = word_app()
    try:
        generate_cover(app)
        generate_task_book(app)
        generate_report(app)
    finally:
        app.Quit()
    if not no_zip:
        generate_source_zip()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--diagrams-only", action="store_true", help="Only regenerate UML and deployment images.")
    parser.add_argument("--zip-only", action="store_true", help="Only rebuild JavaChatSer-source-submit.zip.")
    parser.add_argument("--member-a-report-only", action="store_true", help="Only regenerate member A personal report.")
    parser.add_argument("--no-zip", action="store_true", help="Regenerate Word files without rebuilding source zip.")
    args = parser.parse_args()

    if args.diagrams_only:
        generate_diagrams()
        return
    if args.zip_only:
        generate_source_zip()
        return
    if args.member_a_report_only:
        app = word_app()
        try:
            generate_member_a_report(app)
        finally:
            app.Quit()
        return
    generate_all(no_zip=args.no_zip)


if __name__ == "__main__":
    main()
