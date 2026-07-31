# Deploying TripSplit (free tier)

Stack: **Aiven** (free MySQL) + **Render** (free backend web service + free static frontend).
All three are free and publicly reachable. The only limitation: the backend sleeps
after ~15 min idle and takes ~1 minute to wake on the next request.

You'll create the accounts and paste in secrets yourself — I can't create accounts or
enter credentials for you. Follow the steps in order; the order matters because each
service's public URL isn't known until it exists.

---

## 1. Database — Aiven MySQL (always-free)

1. Sign up at https://aiven.io (no credit card for the free plan).
2. Create service → **MySQL** → **Free** plan → pick a region near your friends.
3. Wait for it to reach "Running", then open the service's **Overview** tab and note:
   - Host, Port, Database name, User, Password (the "Service URI" has all of these).
4. Load the schema. From your Mac, with the MySQL client installed:
   ```sh
   mysql --host=HOST --port=PORT --user=USER --password=PASSWORD \
         --ssl-mode=REQUIRED DBNAME < server/sql/trip-split-schema-prod.sql
   ```
   (Aiven requires SSL — keep `--ssl-mode=REQUIRED`.)

Your backend `TRIP_SPLIT_DB_URL` will be:
```
jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED
```

---

## 2. Push this repo to GitHub

Render deploys from GitHub. `render.yaml` and the S3-optional fix are already committed
locally — push them (see the branch/PR your assistant prepared, or push to `main`).

---

## 3. Deploy the blueprint on Render

1. Sign up at https://render.com (connect your GitHub, no card for free tier).
2. **New → Blueprint** → pick the `TripSplit` repo. Render reads `render.yaml` and shows
   two services: `tripsplit-api` (Docker) and `tripsplit-web` (static site).
3. It will prompt for the `sync: false` env vars. Fill in for **tripsplit-api**:
   - `TRIP_SPLIT_DB_URL` = `jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED`
   - `TRIP_SPLIT_DB_USERNAME` = Aiven user
   - `TRIP_SPLIT_DB_PASSWORD` = Aiven password
   - `CORS_ALLOWED_ORIGINS` = leave blank for now (set in step 5)
   - `YOUR_ACCESS_KEY` / `YOUR_SECRET_KEY` = leave blank (S3 optional — see below)
   - `JWT_SECRET` is auto-generated; leave it.
4. Apply. The **backend** builds first (a few minutes for the Maven image).

---

## 4. Point the frontend at the backend

1. When `tripsplit-api` is live, copy its public URL, e.g.
   `https://tripsplit-api.onrender.com`.
2. On `tripsplit-web` → Environment, set:
   ```
   REACT_APP_API_URL = https://tripsplit-api.onrender.com/api
   ```
   **The `/api` suffix is required** (no trailing slash). Save → it rebuilds.

---

## 5. Open CORS to the frontend

1. When `tripsplit-web` is live, copy its URL, e.g.
   `https://tripsplit-web.onrender.com`.
2. On `tripsplit-api` → Environment, set:
   ```
   CORS_ALLOWED_ORIGINS = https://tripsplit-web.onrender.com
   ```
   Save → the backend restarts. Multiple origins are comma-separated if you ever need it.

Done — share the `tripsplit-web` URL with your friends.

---

## 6. Receipt image storage — Cloudflare R2 (free, 10 GB)

The backend uses the S3 SDK, pointed at Cloudflare R2 (S3-compatible, free forever,
zero egress). R2 needs a card on file but won't charge under the free limits.

1. Sign up / log in at https://dash.cloudflare.com and open **R2**. Add a payment method
   (required to enable R2; you stay on the free tier).
2. **Create bucket** — pick a name, e.g. `tripsplit-receipts`. Note the name.
3. Make objects publicly viewable: bucket → **Settings** → **Public access** → enable the
   **r2.dev** public URL (or connect a custom domain). Copy that public URL, e.g.
   `https://pub-abc123.r2.dev`.
4. Create API credentials: R2 → **Manage R2 API Tokens** → **Create API token** →
   Object Read & Write, scoped to this bucket. Copy the **Access Key ID**, **Secret Access
   Key**, and your account's **S3 API endpoint** (`https://<accountid>.r2.cloudflarestorage.com`).
5. On `tripsplit-api` → Environment, set:
   ```
   S3_ENDPOINT_URL = https://<accountid>.r2.cloudflarestorage.com
   S3_REGION       = auto
   S3_PUBLIC_URL   = https://pub-abc123.r2.dev
   S3_BUCKET_NAME  = tripsplit-receipts
   YOUR_ACCESS_KEY = <R2 Access Key ID>
   YOUR_SECRET_KEY = <R2 Secret Access Key>
   ```
   Save → the backend restarts and receipt uploads work.

Leave these unset and the app still runs — only receipt upload is disabled.

---

## 7. Keeping the backend and database awake

Two independent sleep mechanisms, and they chain:

- **Render** spins a free web service down after **15 minutes** without inbound traffic.
  Waking it takes ~1 minute, during which visitors see a loading page.
- **Aiven** powers off a free database with no "continuative activity". When Render
  sleeps, the JDBC pool dies, the database goes idle, and eventually it powers off —
  which shows up as a DNS failure on the next request.

`GET /api/health` handles both. It is the only unauthenticated route, and it runs
`select 1`, so every ping is database traffic too. One schedule keeps both ends alive.

### The budget that constrains everything

Render grants **750 free instance hours per workspace per calendar month**. Exceed it
and Render **suspends every free web service until the next month**. Static sites (the
frontend) don't draw from this pool, so only the backend counts.

The rule that matters: **instance hours are wall-clock time the container is running,
not a per-request charge.** Real visitors arriving while the service is already awake
cost *nothing extra*. So the only lever on the bill is **how many hours per day the
service is kept awake** — not how often you ping inside that window, and not how much
real traffic you get.

That means "leaving room for real use" is about reserving headroom for visitors who
arrive **while the app is asleep** and wake it themselves, plus deploy overlap.

### The schedule

Keep a **nightly quiet window**. Skipping pings for a 2-hour slot costs 1h45m of
downtime (the service stays up for 15 minutes after the last ping before sleeping):

| | |
|---|---|
| Ping interval | every 10 minutes (5 minutes of slack under the 15-minute timer) |
| Quiet window | 2 hours, at your local ~3–5 AM |
| Awake | 22h15m/day → **~690 hours** in a 31-day month |
| Headroom | **~60 hours** for off-window visitor wakeups and deploy overlap |

Pick the interval for *reliability*, not cost: 10 minutes survives one missed or
delayed ping before the container sleeps. A 14-minute interval saves no hours at all
(the app is up either way) and risks an accidental nap.

Tighten the window only if the dashboard shows you're well under budget. A 75-minute
quiet window yields ~715 hours — still legal, but ~35 hours of headroom is thin.

### Setting up the pinger

Use a hosted scheduler, **not** a `cron` job on your Mac — a laptop cron only fires
while the laptop is awake, which is exactly when it's not needed.

This repo ships one: `.github/workflows/keep-alive.yml` runs the schedule above on
GitHub Actions. The repo is public, so the minutes are free. It needs one setting:

1. Copy the backend's URL from the Render dashboard. **Don't assume the subdomain** —
   Render names are globally unique and appends a suffix when the name you picked is
   taken, so the service may not be at the name in `render.yaml`.
2. In GitHub: **Settings → Secrets and variables → Actions → Variables → New
   repository variable**, named `HEALTH_URL`, set to that URL plus `/api/health`.
3. Run it once by hand from the **Actions** tab (`Keep backend awake` →
   *Run workflow*) to confirm it reports `{"status":"up","database":"up"}`.

Two things to know about Actions as a scheduler: GitHub **delays scheduled runs
under load**, occasionally past the 15-minute window, so the odd nap is expected; and
it **disables scheduled workflows after 60 days with no repo activity**, emailing you
first. If naps become common, tighten the cron to `3-59/5` (it costs no extra instance
hours — the service is already awake) or move the schedule to **cron-job.org**, which
fires far more punctually. There, create a GET job on the same URL every 10 minutes,
deselect the quiet-window hours, confirm the account timezone, and turn on failure
notifications.

Verify by hand:

```sh
curl -s https://<your-api>.onrender.com/api/health
# {"status":"up","database":"up"}
```

`"database":"down"` with `"status":"up"` means the API is healthy but Aiven is
unreachable — check whether the service powered off in the Aiven console.

### Watch the meter

Render's dashboard reports free instance hours used for the current month. Check it
about a week in: if you're tracking well under 690, you can shrink the quiet window;
if you're near the cap, widen it. Don't build custom usage tracking — the dashboard is
authoritative and the app restarts on every wake, so it can't count its own hours
reliably anyway.

Deploys are the sneaky line item: each one can briefly run the old and new container
at once. A heavy deploy day eats into the headroom.

---

## Notes

- **Cold starts:** with the pinger above, only visitors arriving during the nightly
  quiet window pay the ~1 minute wake. Without it, anyone arriving after 15 idle
  minutes does. A paid Render instance removes spin-down entirely; Fly.io runs the
  same Docker image always-on for roughly $3–6/mo, which also keeps Aiven alive for
  free and would let you drop the memory flags in `server/Dockerfile`.
- **Aiven free MySQL** is 1 GB — plenty for a friends-and-family app, but not for scale.
