# BTR 鐢熶骇閮ㄧ讲璁板綍

> 棣栨閮ㄧ讲锛?026-11-08
> 鏈嶅姟鍣細Ubuntu 22.04 / Java 21 / MySQL 8 / Nginx 1.18
> 鏃ч」鐩凡澶囦唤骞惰鐩栵紝鏃у簱宸插浠藉悗閲嶅缓銆?
## 璁块棶鍦板潃

| 鍏ュ彛 | 鍦板潃 |
|---|---|
| 涓诲叆鍙ｏ紙澶栭儴 20757 鈫?鍐呴儴 8080锛?| <http://YOUR_SERVER_IP:20757> |
| 澶囩敤鍏ュ彛锛堝閮?23699 鈫?鍐呴儴 80锛?| <http://YOUR_SERVER_IP:23699> |
| 鍚庣鍋ュ悍妫€鏌ワ紙浠呮湇鍔″櫒鍐咃級 | <http://127.0.0.1:8082/api/v1/health> |
| 璇佷功楠岀湡绀轰緥 | <http://YOUR_SERVER_IP:20757/verify/BTR-1-DEMO> |
| 绯荤粺璁剧疆 | <http://YOUR_SERVER_IP:20757/settings> |
| 鍟嗗煄 | <http://YOUR_SERVER_IP:20757/shop> |
| 鍟嗗搧绠＄悊 | <http://YOUR_SERVER_IP:20757/shop-admin> |

> 鐩存帴璁块棶 80 绔彛浼氳杩愯惀鍟嗗妗堥〉鎷︽埅锛涜浣跨敤 20757 鎴?23699銆?
## 鏈嶅姟鍣ㄨ矾寰?
| 鍐呭 | 璺緞 |
|---|---|
| 搴旂敤鐩綍 | `/root/BRT` |
| 鍚庣 jar | `/root/BRT/backend/btr-platform.jar` |
| 鐜鍙橀噺 | `/root/BRT/backend/.env`锛?00锛?|
| 搴旂敤鏃ュ織 | `/root/BRT/logs/app.log` |
| 涓婁紶鐩綍 | `/root/BRT/uploads` |
| 鍓嶇闈欐€佹枃浠?| `/var/www/btr-platform` |
| systemd 鏈嶅姟 | `/etc/systemd/system/btr-platform.service` |
| Nginx 閰嶇疆 | `/etc/nginx/sites-available/btr-platform` |
| Nginx 鍚敤閾炬帴 | `/etc/nginx/sites-enabled/btr-platform` |
| 鏃ч」鐩浠?| `/root/BRT-old-20261108-055551` |
| 鏃у簱澶囦唤 | `/root/backups/btr-pre-redeploy-20261108-055551.sql.gz` |

## 绔彛鏄犲皠

| 澶栭儴绔彛 | 鍐呴儴绔彛 | 鐢ㄩ€?|
|---|---|---|
| 20757 | 8080 | Nginx锛堝墠绔?+ `/api/` 鍙嶄唬 8082锛?|
| 23699 | 80 | Nginx锛堝鐢級 |
| YOUR_SSH_PORT | 22 | SSH |
| 鈥?| 8082 | Java 鍚庣锛堜粎鏈満锛?|
| 鈥?| 3306 / 6379 | MySQL / Redis锛堜粎鏈満锛?|

## 鏈嶅姟绠＄悊

```bash
# 鐘舵€?systemctl status btr-platform --no-pager

# 閲嶅惎
systemctl restart btr-platform

# 鏃ュ織
tail -f /root/BRT/logs/app.log

# Nginx
nginx -t
systemctl reload nginx
```

## 鏁版嵁搴?
- 搴撳悕锛歚btr`
- 鐢ㄦ埛锛歚btr`@127.0.0.1
- 杩佺Щ锛欶lyway 鑷姩鎵ц `V1__init.sql`銆乣V2__p1_core.sql`銆乣V3__settings_payments_risk.sql`銆乣V4__shop.sql`
- 褰撳墠琛ㄦ暟锛?5锛堝惈 `flyway_schema_history`锛?- 鏃у簱澶囦唤锛歚/root/backups/btr-pre-redeploy-20261108-055551.sql.gz`

鎭㈠鏃у簱锛?
```bash
gunzip -c /root/backups/btr-pre-redeploy-20261108-055551.sql.gz | mysql -uroot btr
```

## 鏇存柊鍙戝竷

```bash
# 鏈満鏋勫缓
cd btr-platform/backend && ./mvnw -DskipTests package
cd ../frontend && npm run build

# 涓婁紶
scp -P YOUR_SSH_PORT backend/target/btr-platform-0.1.0-SNAPSHOT.jar root@YOUR_SERVER_IP:/root/BRT/backend/btr-platform.jar
scp -P YOUR_SSH_PORT -r frontend/dist/* root@YOUR_SERVER_IP:/var/www/btr-platform/

# 鏈嶅姟鍣ㄩ噸鍚?ssh -p YOUR_SSH_PORT root@YOUR_SERVER_IP "systemctl restart btr-platform && systemctl reload nginx"
```

鍓嶇闈欐€佹枃浠跺睘涓伙細`www-data:www-data`銆?
## 鐜鍙橀噺

鐢熶骇鐜鍙橀噺鍦?`/root/BRT/backend/.env`锛岃嚦灏戝寘鍚細

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8082
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=btr
DB_USER=btr
DB_PASSWORD=***
JWT_SECRET=***
ADMIN_ACCOUNT=admin
ADMIN_PASSWORD=***
CORS_ORIGINS=http://YOUR_SERVER_IP:20757,http://YOUR_SERVER_IP:23699,http://YOUR_SERVER_IP
UPLOAD_DIR=/root/BRT/uploads
CONFIG_SECRET=***
CAPTCHA_ENABLED=true
TZ=Asia/Shanghai
```

淇敼鍚庯細

```bash
systemctl restart btr-platform
```

## 宸查獙璇?
- [x] MySQL Flyway 杩佺Щ鎴愬姛锛?7 琛級
- [x] 鍚庣鍋ュ悍妫€鏌?UP
- [x] 鍏綉 20757 棣栭〉 200
- [x] 鍏綉 23699 棣栭〉 200
- [x] 鍏綉 20757 / 23699 API 鍋ュ悍妫€鏌?- [x] 绠＄悊鍛樼櫥褰?- [x] 浠〃鐩樼粺璁★細1 璧涗簨 / 2 鎶ュ悕 / 2 浣滃搧 / 1 璇佷功
- [x] 鍏紑璧涗簨鍒楄〃銆佽鎯呫€佽禐鍔╀綅
- [x] 璇佷功 `BTR-1-DEMO` 楠岀湡
- [x] 娴忚鍣ㄦ帶鍒跺彴鏃犳姤閿?- [x] 绯荤粺璁剧疆锛氱珯鐐?/ 閭欢 / 涓婁紶 / 鎶ュ悕璐?/ 瀹夊叏绛栫暐 / 濂栭噾淇濋櫓 / 鏀粯璁㈠崟
- [x] 鏂囦欢涓婁紶涓庝笅杞斤紙`/api/v1/files`锛?- [x] 鎶ュ悕璐规祦绋嬶細鎶ュ悕 鈫?寰呮敮浠?鈫?绠＄悊鍛樼‘璁?鈫?鎶ュ悕鐢熸晥
- [x] 濂栭噾姹?/ 淇濋櫓 / 绾夸笅瀹夊叏鏂规璁板綍
- [x] 閫夋墜鎻愪氦浣滃搧椤碉紙鍚枃浠朵笂浼狅級
- [x] 鍟嗗搧鍟嗗煄锛氭柊寤哄晢鍝併€佸叕寮€鍟嗗煄銆佷笅鍗曘€佺‘璁ゆ敮浠樸€佸彂璐с€佸畬鎴愩€侀€€娆俱€佸簱瀛樻仮澶?- [x] 鎴愬憳绠＄悊锛氬垱寤鸿瘎濮?杩愯惀璐﹀彿銆佹敼瑙掕壊銆佸惎鍋?- [x] 绉熸埛寮€閫氭椂鍒涘缓鎵€鏈夎€呰处鍙?- [x] 鐧诲綍锛氶偖绠?璐﹀彿 + 瀵嗙爜 + 婊戝姩楠岃瘉
- [x] 娉ㄥ唽锛氶偖绠遍獙璇佺爜 + 璐﹀彿=閭
- [x] 鎵惧洖瀵嗙爜锛氶偖绠遍獙璇佺爜 + 鏂板瘑鐮?- [x] SMTP 宸查厤缃紙QQ 閭锛夛紝娴嬭瘯鍙戦€佹垚鍔?
## 鍚庡彴鍙厤缃」

鐧诲綍鍚庡彴 鈫?绯荤粺璁剧疆锛?
| Tab | 鍙厤缃?|
|---|---|
| 绔欑偣 | 绔欑偣鍚嶃€侀〉鑴氥€丩ogo銆佸叧浜庢枃妗?|
| 閭欢 | SMTP 涓绘満銆佺鍙ｃ€佽处鍙枫€佹巿鏉冪爜銆丼SL銆佸彂浠朵汉銆佹祴璇曞彂閫?|
| 涓婁紶 | 鍗曟枃浠朵笂闄愩€佸厑璁哥被鍨?|
| 鎶ュ悕璐?| 绾夸笅浜哄伐纭銆佹敮浠樿鏄?|
| 瀹夊叏绛栫暐 | 鐧诲綍澶辫触闃堝€笺€侀攣瀹氭椂闀匡紙璁板綍閰嶇疆锛岄檺娴侀€昏緫寰呮帴锛?|
| 濂栭噾 / 淇濋櫓 | 濂栭噾姹犮€佷繚鍗曘€佺嚎涓嬪畨鍏ㄦ柟妗?|
| 鏀粯璁㈠崟 | 鏌ョ湅鎶ュ悕璐硅鍗曘€佺‘璁ゆ敮浠樸€侀€€娆?|
| 鍟嗗搧绠＄悊 | 鏂板缓/缂栬緫鍟嗗搧銆佷笂涓嬫灦銆佽鍗曘€佺‘璁ゆ敮浠樸€佸彂璐с€侀€€娆?|

> 閭欢闇€瑕佺湡瀹?SMTP 鎺堟潈鐮侊紱鍦ㄧ嚎鏀粯闇€瑕佹寔鐗屾敮浠樻満鏋勶紱褰撳墠鎶ュ悕璐逛负鈥滅嚎涓嬫敮浠?+ 绠＄悊鍛樼‘璁も€濄€?
## 鐧诲綍涓庢敞鍐岃鍒?
| 椤?| 瑙勫垯 |
|---|---|
| 鐧诲綍 | 閭/璐﹀彿 + 瀵嗙爜 + 婊戝姩楠岃瘉 |
| 娉ㄥ唽 | 閭 + 閭楠岃瘉鐮?+ 鏄电О + 瀵嗙爜锛岃处鍙?閭 |
| 鎵惧洖瀵嗙爜 | 閭 + 閭楠岃瘉鐮?+ 鏂板瘑鐮?|
| 楠岃瘉鐮?| 6 浣嶆暟瀛楋紝10 鍒嗛挓鏈夋晥锛?0 绉掑彲閲嶅彂锛岄敊 5 娆′綔搴?|
| 婊戝姩楠岃瘉 | 鐧诲綍/鍙戦€侀獙璇佺爜鍓嶅繀椤诲畬鎴?|
| 閭欢鏈嶅姟 | 鍚庡彴銆岀郴缁熻缃?鈫?閭欢銆嶉厤缃?SMTP |

> 娉ㄦ剰锛氭敞鍐屽拰鎵惧洖瀵嗙爜渚濊禆 SMTP锛汼MTP 鏈厤缃椂浼氭彁绀衡€滈偖浠舵湇鍔℃湭閰嶇疆鈥濄€?
## 寰呭姙

- [ ] 缁戝畾姝ｅ紡鍩熷悕 + HTTPS锛堝綋鍓嶇敤 IP + 闈炴爣鍑嗙鍙ｏ級
- [ ] 淇敼榛樿绠＄悊鍛樺瘑鐮?- [ ] 澶囦唤绛栫暐鎺ュ叆瀹氭椂浠诲姟
- [ ] 瀵硅薄瀛樺偍 / 閭欢鏈嶅姟
- [ ] P1.5 鎶ュ悕璐广€丳2 鍟嗗搧銆丳2.5 绾夸笅瀹夊叏
