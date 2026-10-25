# 🩸 RaktSetu - Blood Donor Directory

Full-stack web app: **Spring Boot (backend) + HTML/CSS/JavaScript (frontend)**, light theme.
Frontend Spring Boot ke `static` folder me hai, isliye **ek hi server, ek hi deploy** - alag se frontend host karne ki zaroorat nahi (chaho to kar sakte ho, neeche Option B).

## Features

- Donor registration (validation + duplicate phone check + consent checkbox)
- Search & filter: naam, blood group, city, sirf available donors, pagination
- Blood group chips with live donor count
- Donor card: Available / Rest period / Unavailable badge, Call + WhatsApp button
- Blood requests: post karo, urgency (Normal/Urgent/Critical), Open/Fulfilled filter
- Admin mode: donor edit/delete, request fulfil/delete (admin key se)
- Blood compatibility chart, donate karne ki shartein, FAQ
- Mobile responsive, light colour theme

## Folder structure

```
blood-donor-directory/
├── pom.xml                         # Maven dependencies
├── Dockerfile                      # deployment ke liye
├── .dockerignore / .gitignore
├── README.md                       # ye guide
└── src/main/
    ├── java/com/blooddonor/
    │   ├── BloodDonorApplication.java      # app yahin se start
    │   ├── model/                          # Donor, BloodRequest (tables) + Stats, PageResponse
    │   ├── repository/                     # database queries (Spring Data JPA)
    │   ├── service/                        # business logic (search, duplicate check, stats)
    │   ├── controller/                     # REST APIs (Donor, BloodRequest, Admin)
    │   ├── exception/                      # saare errors ek format me
    │   └── config/                         # CORS, AdminGuard, DataSeeder (demo data)
    └── resources/
        ├── application.properties          # local settings (H2 database)
        ├── application-prod.properties     # production settings (PostgreSQL)
        └── static/                         # FRONTEND
            ├── index.html
            ├── css/style.css
            └── js/ config.js, app.js
```

Request ka flow: `Browser (app.js) → Controller → Service → Repository → Database`

---

## 1) Local pe chalana (pehle yahi karo)

**Chahiye:** Java 17+ aur Maven (ya IntelliJ / Eclipse / VS Code with Java extension).

```bash
cd blood-donor-directory
mvn spring-boot:run
```

Browser me kholo: **http://localhost:8080**

- Pehli baar demo donors/requests apne aap ban jayenge (fake numbers `9000000xxx`).
- Database file `./data/blooddb...` me save hota hai (H2), restart pe data bachega.
- **Admin login:** key `admin123` (sirf local ke liye; production me badalna hai).

IDE se chalana ho to `BloodDonorApplication.java` me `main` ko Run karo.

Jar bana ke chalana:

```bash
mvn clean package
java -jar target/app.jar
```

## 2) API list

| Method | URL | Kaun | Kaam |
|---|---|---|---|
| GET | `/api/donors?bloodGroup=A%2B&city=meerut&q=aarav&available=true&page=0&size=9` | public | search + filter |
| GET | `/api/donors/stats` | public | counters |
| GET | `/api/donors/{id}` | public | ek donor |
| POST | `/api/donors` | public | register |
| PUT | `/api/donors/{id}` | admin | edit |
| DELETE | `/api/donors/{id}` | admin | delete |
| GET | `/api/requests?status=Open` | public | requests list |
| POST | `/api/requests` | public | nayi request |
| PATCH | `/api/requests/{id}/fulfil` | admin | fulfilled mark |
| DELETE | `/api/requests/{id}` | admin | delete |
| GET | `/api/admin/verify` | admin | key check |

Admin calls me header bhejna hota hai: `X-Admin-Key: <tumhari key>`.
Dhyan: blood group URL me `+` ko `%2B` likhte hain (`A%2B`), warna space ban jata hai.

## 3) Settings (environment variables)

| Variable | Matlab | Default |
|---|---|---|
| `PORT` | server port | 8080 |
| `ADMIN_KEY` | admin password | `admin123` (prod me **zaroori**) |
| `SEED_DEMO` | demo data daalna | local: true, prod: false |
| `CORS_ORIGINS` | frontend alag host ho to uska URL | `*` |
| `SPRING_PROFILES_ACTIVE` | `prod` = PostgreSQL mode | - |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL details (sirf prod) | - |

---

## 4) Deploy karna (Option A: poora app ek saath, Docker + PostgreSQL)

Free hosts pe disk **temporary** hota hai, isliye production me H2 file nahi, **PostgreSQL** use karo.

### Step 1 - Code GitHub pe daalo

```bash
cd blood-donor-directory
git init
git add .
git commit -m "Blood donor directory"
# GitHub pe naya empty repo banao, phir:
git branch -M main
git remote add origin https://github.com/<tumhara-username>/blood-donor-directory.git
git push -u origin main
```

### Step 2 - Free PostgreSQL banao

Koi bhi managed Postgres chalega (Neon, Supabase, Render Postgres, Railway...). Free plan ki shartein badalti rehti hain, signup se pehle unka pricing page check kar lena.

Database ban jaye to ye 4 cheezein note karo: **host, database name, user, password**. Phir:

```
DB_URL      = jdbc:postgresql://<HOST>:5432/<DBNAME>?sslmode=require
DB_USER     = <user>
DB_PASSWORD = <password>
```

(Dhyan: `postgres://...` wala URL seedha nahi chalega, Spring ko `jdbc:postgresql://` format chahiye.)

### Step 3 - Web service banao (Render ka example)

1. render.com pe login → **New → Web Service** → apna GitHub repo connect karo.
2. **Runtime / Language: Docker** chuno (repo me `Dockerfile` hai, wo khud pakad lega).
3. **Environment Variables** me ye daalo:

   | Key | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | Step 2 wala |
   | `DB_USER` | Step 2 wala |
   | `DB_PASSWORD` | Step 2 wala |
   | `ADMIN_KEY` | koi lamba strong password |
   | `SEED_DEMO` | `false` (ya `true` agar sirf demo dikhana hai) |

4. **Create Web Service** dabao. Pehli build me kuch minute lagte hain.
5. Deploy hone ke baad Render ka URL (`https://...onrender.com`) kholo. Wahi tumhari live website hai.

> Free instance kuch der idle rehne par so jaata hai, to pehla page load slow ho sakta hai. Ye normal hai.

### Step 4 - Check karo

- Website khulti hai?
- Register karke dekho, donor list me aaya?
- Navbar me **Admin Login** → apni `ADMIN_KEY` daalo → Edit/Delete buttons dikhne chahiye.

Doosre hosts (Railway, Koyeb, Fly.io) pe bhi yahi Dockerfile + yahi environment variables chalte hain.

---

## 5) Option B: Frontend alag deploy (Netlify / Vercel / GitHub Pages)

Jab frontend alag chahiye:

1. Backend upar wale Option A se deploy karo.
2. `src/main/resources/static/js/config.js` me backend URL daalo:
   ```js
   window.APP_CONFIG = { API_BASE: "https://tumhara-backend.onrender.com" };
   ```
3. Poora `static` folder (index.html, css, js) Netlify/Vercel pe upload/deploy karo.
4. Backend me env variable set karo (CORS ke liye):
   `CORS_ORIGINS = https://tumhari-site.netlify.app`
5. Backend redeploy karo.

## 6) Option C: Apne server / VPS pe

```bash
mvn clean package
SPRING_PROFILES_ACTIVE=prod DB_URL=... DB_USER=... DB_PASSWORD=... ADMIN_KEY=... \
  java -jar target/app.jar
```
Saath me Nginx + HTTPS (Let's Encrypt) lagana best rehta hai.

---

## 7) Aam errors aur solution

| Problem | Solution |
|---|---|
| `Could not resolve placeholder 'DB_URL'` | prod me env variables set nahi hain (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_KEY`) |
| `Port 8080 already in use` | `PORT=9090` set karo ya purani app band karo |
| Page khulta hai par donors nahi aate | Browser Console (F12) dekho; alag frontend ho to `API_BASE` aur `CORS_ORIGINS` check karo |
| `Connection refused` / DB error on deploy | `DB_URL` me `jdbc:postgresql://` aur `?sslmode=require` check karo |
| Admin login fail | `ADMIN_KEY` env variable wahi daalo jo deploy me set kiya |
| Data redeploy pe gayab | H2 use ho raha hai; `SPRING_PROFILES_ACTIVE=prod` + PostgreSQL lagao |
| `mvn` command nahi mili | Maven install karo ya IntelliJ/Eclipse se run karo |

## 8) Security aur privacy notes (zaroor padho)

- **`ADMIN_KEY` zaroor badlo** aur kisi ko mat batao. Ye simple shared key hai; bade level pe Spring Security + login (JWT) lagana chahiye.
- Donor ka phone number public dikhta hai, isliye register form me **consent checkbox** hai. Real users ka data lene se pehle privacy policy / data protection ka dhyan rakho.
- Hamesha **HTTPS** use karo (Render/Netlify khud dete hain).
- Spam rokne ke liye baad me rate limiting / captcha add kar sakte ho.
- Ye app sirf donors aur patients ko jodta hai; blood ki medical screening hospital/blood bank hi karte hain.

## 9) Aage kya add kar sakte ho (practice ke liye)

1. Donor login (Spring Security + JWT)
2. Email/SMS alert jab matching blood group ki request aaye
3. Map / distance se nearest donor
4. Unit tests (JUnit + MockMvc) aur Postgres me Flyway migrations
