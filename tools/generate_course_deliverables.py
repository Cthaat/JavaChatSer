from __future__ import annotations

import argparse
import os
from pathlib import Path
from textwrap import wrap

import win32com.client
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
SCREENSHOTS = DOCS / "screenshots"
DIAGRAMS = DOCS / "diagrams"
REPORT = DOCS / "JavaChatSer大作业报告.docx"
COVER = DOCS / "大作业报告封面.docx"


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        Path(r"C:\Windows\Fonts\msyh.ttc"),
        Path(r"C:\Windows\Fonts\simsun.ttc"),
        Path(r"C:\Windows\Fonts\simhei.ttf"),
        Path(r"C:\Windows\Fonts\arial.ttf"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def multiline(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], text: str, size: int = 26) -> None:
    x1, y1, x2, y2 = box
    f = font(size)
    lines: list[str] = []
    for part in text.split("\n"):
        width = max(8, (x2 - x1) // max(size, 1))
        lines.extend(wrap(part, width=width) or [""])
    heights = [draw.textbbox((0, 0), line, font=f)[3] for line in lines]
    total = sum(heights) + max(0, len(lines) - 1) * 8
    y = y1 + ((y2 - y1) - total) // 2
    for line in lines:
        bbox = draw.textbbox((0, 0), line, font=f)
        x = x1 + ((x2 - x1) - (bbox[2] - bbox[0])) // 2
        draw.text((x, y), line, fill="#1f2937", font=f)
        y += (bbox[3] - bbox[1]) + 8


def rounded_box(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    fill: str,
    outline: str = "#2563eb",
    size: int = 26,
) -> None:
    draw.rounded_rectangle(box, radius=22, fill=fill, outline=outline, width=3)
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
    img = Image.new("RGB", (1600, 1000), "#f8fafc")
    draw = ImageDraw.Draw(img)
    draw.text((56, 42), title, fill="#0f172a", font=font(42, bold=True))
    draw.line([(56, 104), (1544, 104)], fill="#cbd5e1", width=3)
    return img, draw


def generate_diagrams() -> None:
    DIAGRAMS.mkdir(parents=True, exist_ok=True)

    img, draw = canvas("JavaChatSer 用例图")
    draw.ellipse((80, 210, 150, 280), outline="#0f172a", width=4)
    draw.line((115, 280, 115, 400), fill="#0f172a", width=4)
    draw.line((60, 330, 170, 330), fill="#0f172a", width=4)
    draw.line((115, 400, 70, 500), fill="#0f172a", width=4)
    draw.line((115, 400, 160, 500), fill="#0f172a", width=4)
    draw.text((76, 525), "普通用户", fill="#0f172a", font=font(26))
    draw.ellipse((1350, 210, 1420, 280), outline="#0f172a", width=4)
    draw.line((1385, 280, 1385, 400), fill="#0f172a", width=4)
    draw.line((1330, 330, 1440, 330), fill="#0f172a", width=4)
    draw.line((1385, 400, 1340, 500), fill="#0f172a", width=4)
    draw.line((1385, 400, 1430, 500), fill="#0f172a", width=4)
    draw.text((1330, 525), "管理员", fill="#0f172a", font=font(26))
    cases = [
        ((330, 170, 620, 250), "注册/登录\n获取 JWT"),
        ((690, 170, 980, 250), "维护个人资料\n上传头像"),
        ((1050, 170, 1340, 250), "查看系统概览"),
        ((330, 340, 620, 420), "搜索用户\n发送好友申请"),
        ((690, 340, 980, 420), "接受/拒绝申请\n删除好友"),
        ((1050, 340, 1340, 420), "查看在线状态\n未读消息"),
        ((330, 510, 620, 590), "一对一私聊\n历史分页"),
        ((690, 510, 980, 590), "公共聊天室\n实时群聊"),
        ((1050, 510, 1340, 590), "图片消息\n消息撤回"),
        ((690, 680, 980, 760), "WebSocket 心跳\n实时通知"),
    ]
    for box, text in cases:
        rounded_box(draw, box, text, "#dbeafe")
    for x, y in [(330, 210), (330, 380), (330, 550), (690, 380), (690, 550), (690, 720)]:
        arrow(draw, (190, 360), (x, y), "#64748b")
    for x, y in [(1340, 210), (1340, 550), (980, 210)]:
        arrow(draw, (1320, 360), (x, y), "#64748b")
    img.save(DIAGRAMS / "uml-use-cases.png", quality=95)

    img, draw = canvas("后端类/模块关系图")
    columns = [
        ("Controller", ["AuthController", "UserController", "FriendController", "ChatController", "StatsController"]),
        ("Service", ["UserService", "FriendService", "ChatService", "StatsService", "MediaStorageService"]),
        ("Repository", ["UserRepository", "FriendRepository", "PrivateMessageRepository", "PublicMessageRepository"]),
        ("Domain / DTO", ["User", "FriendRelation", "PrivateMessage", "PublicMessage", "ApiResponse"]),
    ]
    x = 120
    for title, items in columns:
        rounded_box(draw, (x, 170, x + 300, 250), title, "#e0f2fe", "#0284c7", 30)
        y = 300
        for item in items:
            rounded_box(draw, (x, y, x + 300, y + 64), item, "#ffffff", "#94a3b8", 22)
            y += 92
        x += 370
    for x in [420, 790, 1160]:
        arrow(draw, (x, 520), (x + 70, 520), "#64748b")
    rounded_box(draw, (420, 830, 1180, 910), "Security / WebSocket / Redis Cache 横向支撑认证、实时推送、在线状态和未读数", "#dcfce7", "#16a34a", 24)
    img.save(DIAGRAMS / "uml-module-classes.png", quality=95)

    img, draw = canvas("系统组件图")
    boxes = {
        "Browser\nVue 3 SPA": (90, 250, 330, 360),
        "Nginx\n静态资源与代理": (470, 250, 760, 360),
        "Spring Boot\nREST + WebSocket": (900, 220, 1260, 390),
        "MySQL 8\n业务持久化": (920, 560, 1180, 670),
        "Redis 7\n缓存/在线/未读": (1240, 560, 1500, 670),
        "Uploads\n头像/图片": (560, 560, 800, 670),
    }
    for text, box in boxes.items():
        rounded_box(draw, box, text, "#eef2ff", "#4f46e5", 28)
    arrow(draw, (330, 305), (470, 305))
    draw.text((350, 260), "HTTP / WS", fill="#475569", font=font(22))
    arrow(draw, (760, 305), (900, 305))
    draw.text((780, 260), "/api /ws /uploads", fill="#475569", font=font(22))
    arrow(draw, (1080, 390), (1050, 560))
    arrow(draw, (1140, 390), (1370, 560))
    arrow(draw, (900, 390), (730, 560))
    img.save(DIAGRAMS / "uml-components.png", quality=95)

    img, draw = canvas("Docker Compose 部署图")
    rounded_box(draw, (80, 170, 1520, 840), "宿主机 Docker Compose 项目 javachatser", "#ffffff", "#64748b", 30)
    nodes = [
        ((160, 300, 450, 460), "frontend\nNginx + Vue\n宿主端口 5173"),
        ((540, 300, 850, 460), "backend\nSpring Boot 3\n宿主端口 8080"),
        ((940, 300, 1230, 460), "mysql\nMySQL 8\n内部 3306"),
        ((940, 570, 1230, 730), "redis\nRedis 7\n内部 6379"),
        ((540, 570, 850, 730), "backend-uploads\n持久化卷"),
    ]
    for box, text in nodes:
        rounded_box(draw, box, text, "#fef3c7", "#d97706", 26)
    arrow(draw, (450, 380), (540, 380))
    arrow(draw, (850, 380), (940, 380))
    arrow(draw, (850, 420), (940, 620))
    arrow(draw, (695, 460), (695, 570))
    draw.text((168, 245), "浏览器访问 http://localhost:5173", fill="#334155", font=font(24))
    img.save(DIAGRAMS / "uml-deployment.png", quality=95)


def word_app():
    app = win32com.client.Dispatch("Word.Application")
    app.Visible = False
    app.DisplayAlerts = 0
    return app


def cm(value: float) -> float:
    return value * 28.3464567


def line_multiple(size: float = 12, multiple: float = 1.35) -> float:
    return size * multiple


def set_font(selection, name: str = "宋体", size: float = 12, bold: bool = False) -> None:
    selection.Font.Name = name
    selection.Font.NameFarEast = name
    selection.Font.Size = size
    selection.Font.Bold = -1 if bold else 0


def set_para(selection, word, align: int = 3, space_after: float = 6) -> None:
    selection.ParagraphFormat.Alignment = align
    selection.ParagraphFormat.LineSpacingRule = 5
    selection.ParagraphFormat.LineSpacing = line_multiple()
    selection.ParagraphFormat.SpaceAfter = space_after


def type_para(selection, word, text: str = "", size: float = 12, bold: bool = False, align: int = 3) -> None:
    set_font(selection, size=size, bold=bold)
    set_para(selection, word, align=align)
    selection.TypeText(text)
    selection.TypeParagraph()


def heading(selection, word, text: str, level: int) -> None:
    try:
        selection.Style = selection.Document.Styles(-1 - level)
    except Exception:
        pass
    type_para(selection, word, text, size=14 if level == 1 else 12, bold=True, align=0)
    try:
        selection.Style = selection.Document.Styles(0)
    except Exception:
        pass


def add_table(selection, word, headers: list[str], rows: list[list[str]], widths: list[float] | None = None) -> None:
    doc = selection.Document
    table = doc.Tables.Add(selection.Range, len(rows) + 1, len(headers))
    table.Borders.Enable = True
    table.Range.Font.Name = "宋体"
    table.Range.Font.NameFarEast = "宋体"
    table.Range.Font.Size = 10.5
    table.Range.ParagraphFormat.LineSpacingRule = 5
    table.Range.ParagraphFormat.LineSpacing = line_multiple(10.5, 1.2)
    for i, header in enumerate(headers, start=1):
        cell = table.Cell(1, i)
        cell.Range.Text = header
        cell.Range.Font.Bold = -1
        cell.Shading.BackgroundPatternColor = 15724527
    for r, row in enumerate(rows, start=2):
        for c, value in enumerate(row, start=1):
            table.Cell(r, c).Range.Text = value
    if widths:
        for i, width in enumerate(widths, start=1):
            table.Columns(i).Width = cm(width)
    table.Rows.Alignment = 0
    table.Range.Select()
    selection.Collapse(0)
    selection.TypeParagraph()


def add_picture(selection, word, image_path: Path, caption: str, max_width_cm: float = 14.0) -> None:
    if not image_path.exists():
        type_para(selection, word, f"{caption}（截图或图片待补充）", size=10.5, align=1)
        return
    inline = selection.InlineShapes.AddPicture(str(image_path), False, True)
    inline.LockAspectRatio = True
    max_width = cm(max_width_cm)
    if inline.Width > max_width:
        inline.Width = max_width
    selection.TypeParagraph()
    type_para(selection, word, caption, size=10.5, align=1)


def generate_report() -> None:
    generate_diagrams()
    if REPORT.exists():
        REPORT.unlink()

    app = word_app()
    doc = None
    try:
        if COVER.exists():
            doc = app.Documents.Open(str(COVER))
            doc.SaveAs(str(REPORT))
        else:
            doc = app.Documents.Add()
            doc.SaveAs(str(REPORT))

        setup = doc.PageSetup
        setup.TopMargin = cm(2.54)
        setup.BottomMargin = cm(2.54)
        setup.LeftMargin = cm(3.67)
        setup.RightMargin = cm(2.67)
        setup.HeaderDistance = cm(1.5)
        setup.FooterDistance = cm(1.75)

        normal = doc.Styles(-1)
        normal.Font.Name = "宋体"
        normal.Font.NameFarEast = "宋体"
        normal.Font.Size = 12
        normal.ParagraphFormat.LineSpacingRule = 5
        normal.ParagraphFormat.LineSpacing = line_multiple()

        find = doc.Content.Find
        find.Text = "题 目:"
        find.Replacement.Text = "题 目: JavaChatSer 在线即时聊天系统"
        find.Execute(Replace=2)

        selection = app.Selection
        selection.EndKey(6)
        selection.InsertBreak(7)

        type_para(selection, app, "JavaChatSer 在线即时聊天系统大作业报告", size=18, bold=True, align=1)
        type_para(selection, app, "题目：JavaChatSer 在线即时聊天系统", size=12, align=1)
        type_para(selection, app, "姓名、学号、班级、任课教师、组员与分工：请在封面及 1.3 节占位处补充。", size=12, align=1)

        heading(selection, app, "1 引言", 1)
        heading(selection, app, "1.1 项目背景", 2)
        type_para(selection, app, "JavaChatSer 原项目是一个传统 Servlet/JSP 聊天原型，包含用户、好友、私聊、公共聊天室、Redis 缓存和原生 Socket 在线转发等基础能力。课程任务要求使用 Vue 3.0 与 Spring Boot 3.0 开发具备一定规模和复杂度的企业级 Web 应用，因此本项目在保留原有聊天业务的基础上，升级为前后端分离的在线即时聊天系统。")
        type_para(selection, app, "新版系统采用 Spring Boot 3、Spring Security、JWT、Spring Data JPA、Spring Data Redis、Spring WebSocket、Vue 3、Vite、Pinia、Axios 和 Docker Compose，实现了认证、好友关系、实时私聊、公共聊天室、图片上传、消息撤回、在线状态、未读消息和系统概览等功能。")
        heading(selection, app, "1.2 开发目标", 2)
        add_table(selection, app, ["目标类别", "具体目标"], [
            ["功能目标", "完成用户认证、好友管理、私聊、公共聊天室、文件上传、统计概览和实时通知。"],
            ["技术目标", "使用 Spring Boot 3 + Vue 3 前后端分离架构，使用 MySQL 持久化，使用 Redis 缓存和在线状态。"],
            ["工程目标", "提供统一接口响应、分层后端结构、路由化前端页面、Docker Compose 一键部署和可答辩文档。"],
            ["课程目标", "形成可运行、可展示、可说明的大作业成果，并在报告中包含需求、设计、实现、截图和总结。"],
        ], [3.0, 11.0])
        heading(selection, app, "1.3 开发过程与分工", 2)
        type_para(selection, app, "本项目按“旧系统分析、后端重构、前端实现、部署验证、文档整理”的顺序推进。旧版代码保留在 src/ 目录作为业务参考，新版后端位于 backend/，新版前端位于 frontend/，课程文档和报告材料位于 docs/。")
        add_table(selection, app, ["成员", "主要分工", "完成情况"], [
            ["姓名/学号（待填写）", "后端认证、好友、聊天、WebSocket、Redis 缓存", "已完成"],
            ["姓名/学号（待填写）", "Vue 前端页面、状态管理、接口联调、截图整理", "已完成"],
            ["姓名/学号（待填写）", "数据库设计、Docker 部署、报告与答辩材料", "已完成"],
        ], [3.0, 8.0, 3.0])

        heading(selection, app, "2 需求分析", 1)
        heading(selection, app, "2.1 用户角色", 2)
        add_table(selection, app, ["角色", "权限与职责"], [
            ["普通用户", "注册登录、维护资料、搜索用户、发送和处理好友申请、私聊、公共聊天、上传图片、查看个人统计。"],
            ["管理员", "拥有普通用户功能，并可查看全局统计、撤回任意用户发送的私聊或公共消息。"],
        ], [3.0, 11.0])
        heading(selection, app, "2.2 用例需求", 2)
        add_picture(selection, app, DIAGRAMS / "uml-use-cases.png", "图 2-1 系统用例图")
        add_table(selection, app, ["编号", "用例", "主要流程", "验收标准"], [
            ["UC-01", "搜索并添加好友", "用户输入关键字搜索用户，向目标用户发送好友申请。", "不能添加自己，重复申请返回冲突，申请进入待处理列表。"],
            ["UC-02", "处理好友申请", "接收者查看待处理申请，选择接受或拒绝。", "接受后建立双向好友关系，拒绝后不会出现在好友列表。"],
            ["UC-03", "一对一私聊", "好友之间发送文本或图片消息，双方在线时实时收到。", "消息先写入 MySQL，在线用户收到 WebSocket 推送。"],
            ["UC-04", "公共聊天室", "用户进入公共聊天室发送消息，所有在线用户接收。", "公共消息写入数据库并广播，刷新后可加载历史。"],
            ["UC-05", "消息已读与未读", "接收者进入私聊会话后将消息标记已读。", "好友列表未读数归零，数据库 read_at 被更新。"],
            ["UC-06", "消息撤回", "发送者撤回自己消息，管理员可撤回任意消息。", "撤回后内容置空并推送 MESSAGE_RECALLED。"],
            ["UC-07", "资料与头像", "用户查看资料并上传头像。", "图片保存到 uploads，用户 avatar_url 更新。"],
            ["UC-08", "系统概览", "用户查看个人统计，管理员查看全局统计。", "返回用户数、在线数、消息数、好友申请和好友关系统计。"],
        ], [1.6, 3.0, 6.0, 4.2])
        heading(selection, app, "2.3 非功能需求", 2)
        add_table(selection, app, ["类别", "要求"], [
            ["安全性", "使用 JWT 保护除注册、登录、健康检查和静态资源外的接口；密码使用 BCrypt 哈希保存。"],
            ["可靠性", "消息以 MySQL 为最终事实来源，Redis 只保存可重建缓存，Redis 短暂不可用不应导致核心消息丢失。"],
            ["可维护性", "后端按 Controller、Service、Repository、DTO、Entity 分层，前端按 views、stores、api、components 组织。"],
            ["可部署性", "通过 Docker Compose 启动 MySQL、Redis、backend、frontend，前端 Nginx 代理 /api、/ws、/uploads。"],
        ], [3.0, 11.0])

        heading(selection, app, "3 软件设计", 1)
        heading(selection, app, "3.1 总体架构", 2)
        type_para(selection, app, "系统采用前后端分离架构。浏览器访问 Vue 3 单页应用，前端通过 Axios 调用 REST API，通过原生 WebSocket 连接后端实时通道；后端使用 Spring Boot 3 暴露接口和 WebSocket 服务；MySQL 保存用户、好友和消息数据；Redis 保存好友列表缓存、最近消息缓存、在线状态和未读数。")
        add_picture(selection, app, DIAGRAMS / "uml-components.png", "图 3-1 系统组件图")
        heading(selection, app, "3.2 后端模块设计", 2)
        add_picture(selection, app, DIAGRAMS / "uml-module-classes.png", "图 3-2 后端类/模块关系图")
        add_table(selection, app, ["模块", "主要职责"], [
            ["common", "统一响应 ApiResponse、分页 PageResponse、业务异常和全局异常处理。"],
            ["security", "JWT 生成校验、认证过滤器、安全配置和登录用户上下文。"],
            ["user", "注册、登录、当前用户、搜索用户和头像更新。"],
            ["friend", "好友申请、接受、拒绝、删除、好友列表、在线状态和未读数聚合。"],
            ["chat", "私聊、公共聊天、消息分页、已读、撤回、图片消息和缓存更新。"],
            ["websocket", "WebSocket 握手认证、会话管理、实时消息、好友上下线和通知推送。"],
            ["stats/media", "系统统计、个人统计、头像和聊天图片存储。"],
        ], [3.0, 11.0])
        heading(selection, app, "3.3 数据库与 Redis 设计", 2)
        add_table(selection, app, ["数据表", "用途", "关键字段"], [
            ["chat_user", "用户账号表", "username、password_hash、nickname、avatar_url、bio、role、enabled"],
            ["friend_relation", "好友关系和申请表", "user_id、friend_id、status、created_at、updated_at"],
            ["private_message", "私聊消息表", "sender_id、receiver_id、content、message_type、read_at、recalled_at"],
            ["public_message", "公共聊天室消息表", "sender_id、content、message_type、recalled_at、created_at"],
        ], [3.0, 4.2, 6.8])
        add_table(selection, app, ["Redis Key", "用途", "过期策略"], [
            ["online:user:{userId}", "保存在线状态和 sessionId", "2 分钟心跳续期"],
            ["friend:list:{userId}", "缓存好友基础资料列表", "5 分钟"],
            ["chat:private:{minUserId}:{maxUserId}", "缓存最近私聊消息", "1 天"],
            ["chat:public:recent", "缓存最近公共消息", "1 天"],
            ["unread:{userId}:{friendId}", "保存某好友未读消息数", "无固定 TTL"],
        ], [5.0, 6.0, 3.0])
        heading(selection, app, "3.4 部署设计", 2)
        add_picture(selection, app, DIAGRAMS / "uml-deployment.png", "图 3-3 Docker Compose 部署图")

        heading(selection, app, "4 软件实现", 1)
        heading(selection, app, "4.1 后端关键实现", 2)
        type_para(selection, app, "后端入口为 backend/src/main/java/com/example/javachat/JavaChatApplication.java。接口层只负责参数接收和统一响应，业务规则集中在 Service 层，数据库访问交由 Repository。全局异常处理把参数错误、未登录、无权限、冲突和服务端错误转换成统一 code/message/data 结构。")
        add_table(selection, app, ["功能", "接口或类", "实现说明"], [
            ["认证", "AuthController、UserService、JwtTokenProvider", "登录校验 BCrypt 密码，成功后返回 JWT；后续请求由 JwtAuthenticationFilter 写入 LoginUser。"],
            ["好友", "FriendController、FriendService", "申请为 PENDING，接受时生成双向 ACCEPTED 关系，删除时双向置为 DELETED。"],
            ["私聊", "ChatController、ChatService", "发送前检查好友关系，写入 private_message，更新 Redis 最近消息和未读数。"],
            ["公共聊天室", "ChatWebSocketHandler、ChatRealtimeNotifier", "REST 或 WebSocket 发送后写库、缓存并广播给在线连接。"],
            ["上传", "MediaStorageService、UploadResourceConfig", "限制图片文件，保存到 uploads/avatars 或 uploads/images，并暴露静态访问路径。"],
        ], [3.0, 5.0, 6.0])
        heading(selection, app, "4.2 前端关键实现", 2)
        type_para(selection, app, "前端入口为 frontend/src/main.ts，路由位于 frontend/src/router/index.ts。认证状态保存在 Pinia 的 auth store 中，Axios 请求拦截器自动附带 Authorization；chat store 负责 WebSocket 连接、心跳、自动重连、消息收发、图片发送和撤回同步；friends store 负责好友列表、申请列表、搜索结果、在线状态和未读数。")
        add_table(selection, app, ["页面", "路径", "功能"], [
            ["登录页", "/login", "用户名密码登录，保存 Token 后进入聊天页面。"],
            ["注册页", "/register", "注册账号并直接登录。"],
            ["聊天页", "/chat", "公共聊天室、好友私聊、在线状态、未读数、图片消息和撤回。"],
            ["好友页", "/friends", "搜索用户、发送申请、处理申请、删除好友。"],
            ["个人资料", "/profile", "查看账号资料、上传头像、退出登录。"],
            ["系统概览", "/dashboard", "展示用户、在线、消息、好友申请和好友关系统计。"],
        ], [3.0, 3.0, 8.0])
        heading(selection, app, "4.3 接口与 WebSocket 实现", 2)
        add_table(selection, app, ["类型", "路径或消息", "说明"], [
            ["REST", "POST /api/auth/login", "登录并返回 JWT 和用户资料。"],
            ["REST", "GET /api/friends", "返回好友资料、在线状态和未读消息数。"],
            ["REST", "POST /api/chats/private/{friendId}/messages", "发送私聊消息。"],
            ["REST", "GET /api/chats/public/messages", "分页查询公共聊天室历史。"],
            ["WebSocket", "PRIVATE_MESSAGE", "发送私聊并推送给发送者和接收者。"],
            ["WebSocket", "PUBLIC_MESSAGE", "发送公共消息并广播给所有在线用户。"],
            ["WebSocket", "FRIEND_STATUS / FRIEND_REQUEST / MESSAGE_RECALLED", "推送好友上下线、申请和撤回事件。"],
        ], [3.0, 5.0, 6.0])
        heading(selection, app, "4.4 运行效果", 2)
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
            add_picture(selection, app, SCREENSHOTS / file_name, caption, max_width_cm=13.5)

        heading(selection, app, "5 结论", 1)
        type_para(selection, app, "本项目完成了从传统 Java Web 聊天原型到 Spring Boot + Vue 前后端分离系统的升级。系统已经具备用户认证、好友申请、实时私聊、公共聊天室、图片消息、消息撤回、在线状态、未读消息、统计概览和 Docker Compose 部署能力，符合课程对企业级应用开发技术栈、规模和复杂度的要求。")
        type_para(selection, app, "项目中仍有可以继续完善的方向，例如接入更细粒度的消息搜索、群组聊天室、文件消息、操作审计、移动端适配和更完整的自动化端到端测试。这些扩展不影响当前大作业提交，但可以作为后续迭代方向。")

        heading(selection, app, "6 体会与建议", 1)
        type_para(selection, app, "通过本次大作业，项目从早期 Servlet、JSP、原生 Socket 和手写数据库访问逐步演进为分层清晰、可部署、可测试、可展示的现代 Web 应用。实践中体会最深的是：企业级应用不仅要能实现功能，还要关注安全、分层、缓存一致性、接口规范、错误处理、部署方式和文档可维护性。")
        type_para(selection, app, "建议后续开发继续保持“先设计接口和数据结构，再实现业务，再补充验证和文档”的节奏。对于聊天类系统，应始终坚持消息先持久化再推送，避免把 Redis 或 WebSocket 当作最终数据来源。")

        doc.SaveAs(str(REPORT))
    finally:
        if doc is not None:
            try:
                doc.Close(False)
            except Exception:
                pass
        app.Quit()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--diagrams-only", action="store_true")
    args = parser.parse_args()
    generate_diagrams()
    if not args.diagrams_only:
        generate_report()


if __name__ == "__main__":
    main()
