# Reader

電子書閱讀平臺，支援 EPUB、TXT、PDF、MOBI。

## 功能

- 註冊、登入
- 上傳書籍，可選擇是否私密。公開書籍所有登入使用者可閱讀，私密書籍僅擁有者可見
- 擁有者可切換公開狀態、刪除書籍
- EPUB、TXT、MOBI：解析成章節後重新排版，分頁閱讀
  - 主題、字型、字級、行距、邊距、寬螢幕雙欄
  - 目錄、全書進度
  - 鍵盤、點擊左右兩側、觸控滑動翻頁
- PDF：連續捲動、縮放、跳頁、大綱目錄、文字選取
- 每位使用者各自保存閱讀進度

## 技術棧

| 層級 | 技術 |
| --- | --- |
| 後端 | Java 25、Spring Boot 4.1、Spring Security、Spring Data JPA、Flyway |
| 解析 | jsoup（EPUB、HTML 過濾）、PDFBox、juniversalchardet、自製 MOBI 解析器 |
| 前端 | Vue 3、Vue Router、Vite、pdf.js |
| 資料庫 | MySQL 8.4 |

## 本機執行

需要 JDK 25、Maven、Node.js 22.13 以上、Docker。

```bash
# 資料庫，對外埠 3307
docker compose up -d

# 後端 http://localhost:8080
cd backend
mvn spring-boot:run

# 前端 http://localhost:5173
cd frontend
npm install
npm run dev
```

資料表由 Flyway 於後端啟動時自動建立。

## 測試

```bash
cd backend && mvn test
```

測試使用 H2 記憶體資料庫，不需啟動 MySQL。

## 環境變數

| 名稱 | 預設值 | 說明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3307/reader?...` | 資料庫連線字串 |
| `DB_USERNAME` | `reader` | 資料庫帳號 |
| `DB_PASSWORD` | `reader` | 資料庫密碼 |
| `DB_ROOT_PASSWORD` | `root` | 僅 Docker Compose 使用 |
| `STORAGE_ROOT` | `../data` | 書籍檔案目錄，相對於啟動位置 |

預設密碼僅供本機開發，正式環境務必更換。

## 部署

```bash
cd backend && mvn package
cd frontend && npm ci && npm run build
```

- 執行 `java -jar backend/target/reader-backend-1.0.0.jar`
- 以 Nginx 等伺服器提供 `frontend/dist`，並將 `/api` 反向代理至後端
- 未匹配的路徑需回傳 `index.html`
- `.mjs` 檔案需以 `text/javascript` 回傳
- 上傳上限為 100 MB，反向代理需同步放寬（Nginx：`client_max_body_size 101m;`）
- 使用 HTTPS，並設定 `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`
- 登入狀態存於記憶體，後端重啟後需重新登入

## 專案結構

```
backend/src/main/java/com/reader/
  common/    例外處理
  config/    安全設定
  user/      註冊與登入
  book/      上傳、書架、權限、檔案下載
  content/   章節資料
  reading/   章節內容與閱讀進度
  storage/   檔案儲存
  parser/    解析框架與內容過濾
    txt/ epub/ mobi/ pdf/

frontend/src/
  api/          後端接口
  stores/       登入狀態
  views/        頁面
  components/   shelf（書架）、reader（閱讀器）
  composables/  分頁、手勢、進度、設定、PDF 捲動
  utils/        共用函式
```

## 已知限制

- 不支援 DRM 加密書籍與加密 PDF
- 僅含 KF8 格式的 MOBI／AZW3 內容，依檔案內原始順序呈現，複雜排版的書籍可能順序有誤
- HUFF/CDIC 壓縮僅通過合成資料測試，尚未以實際書籍驗證
- 書內超連結（註腳、內文跳轉）不可點擊，書籍自帶樣式不套用
- 掃描版 PDF 沒有文字層，無法選取文字
- 尚未提供登入嘗試次數限制，公開部署前建議於反向代理加上

## 分支規範

遵循 Git Flow：`main` 為發布版本，`develop` 為開發主線，功能使用 `feature/*`，發布使用 `release/*`，緊急修復使用 `hotfix/*`。

提交訊息格式：`<type>(<scope>): <subject>`
