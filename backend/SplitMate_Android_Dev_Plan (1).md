# SplitMate — Android App Development Plan

> Free Splitwise with real UPI settlement, debt minimization algorithm, and real-time sync.

---

## 1. Project Overview

| Field | Detail |
|---|---|
| **App Name** | SplitMate |
| **Platform** | Android (API 26+ / Android 8.0 Oreo and above) |
| **Target Users** | College students, hostel groups, flatmates, friend circles |
| **Core Problem** | Manual expense splitting is tedious; existing apps (Splitwise) lack native UPI settlement |
| **USP** | Debt minimization + UPI deep-links + receipt OCR + WhatsApp bot + UPI SMS auto-detection — built exclusively for India |
| **Timeline** | 8 weeks |
| **Team Size** | 1–2 developers |

---

## 2. Tech Stack

### Android (Frontend)
| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Navigation | Jetpack Navigation Compose |
| DI | Hilt |
| Local DB | Room (offline support) |
| Networking | Retrofit + OkHttp |
| Real-time | WebSocket (OkHttp) + STOMP protocol |
| State Management | StateFlow + ViewModel |
| Image Loading | Coil |
| Charts | Vico (Compose-native charting) |
| OCR | Google ML Kit Text Recognition v2 (on-device, free) |
| Speech | Android SpeechRecognizer API (on-device, free) |
| SMS Reading | Android SmsManager + READ_SMS permission |

### Backend (Spring Boot)
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Database | PostgreSQL |
| Cache | Redis |
| Messaging | Apache Kafka |
| Real-time | WebSocket + STOMP |
| AI | Spring AI (expense categorization) |
| WhatsApp Bot | Twilio WhatsApp API |
| Auth | JWT + Spring Security |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## 3. Feature List

### 3.1 Authentication & Onboarding
- Phone number OTP login (Firebase Auth / custom OTP)
- Profile setup — name, avatar, UPI ID
- Invite contacts via link or phone number

### 3.2 Group Management
- Create a group with name and photo
- Invite members via shareable link or phone number
- View all group members and their balances at a glance
- Leave or archive a group
- Group categories — trip, flat, office, friends

### 3.3 Expense Logging
- Add expense with description, amount, date
- Select who paid
- Split modes:
  - Equal split
  - Percentage-based split
  - Custom amount per person
  - Unequal shares (fractions)
- Attach receipt photo
- Add notes to expense
- Edit or delete an expense (with audit trail)

### 3.4 AI Expense Categorizer
- Auto-tag expense category on description input
- Categories: Food, Transport, Rent, Groceries, Entertainment, Utilities, Other
- Category icons and color-coded chips
- Manual override of AI suggestion
- Powered by Spring AI on the backend

### 3.5 Debt Minimization Algorithm
- Greedy graph-based algorithm — reduces N*(N-1)/2 possible debts to at most N-1 transactions
- Runs on backend, result pushed via WebSocket
- Visual explanation screen: "10 people → reduced from 45 to 9 transactions"
- Recalculates automatically on every new expense

### 3.6 UPI Settlement
- UPI deep-link built entirely on the Android client — zero backend or payment gateway needed
- Deep-link format: `upi://pay?pa={receiverUpiId}&pn={receiverName}&am={amount}&cu=INR&tn=SplitMate+settlement`
- One-tap "Pay via UPI" button — Android fires `Intent.ACTION_VIEW` which opens a native app chooser (GPay, PhonePe, Paytm, BHIM — whatever the user has installed)
- No merchant account, no API key, no transaction fees — money flows directly between users
- Manual "Mark as Settled" after payment completes (honour system, same as Splitwise)
- Settlement history with timestamps
- If receiver has not set a UPI ID, show prompt asking them to add it in their profile
- Partial settlements supported — enter custom amount before generating link

### 3.7 Real-time Sync (WebSocket)
- All group members see new expenses instantly
- Live activity indicator (online members)
- Push notification when a new expense is added
- Expense added → Kafka event → WebSocket push → all clients update

### 3.8 Notifications
- FCM push notifications:
  - New expense added in your group
  - Someone settled with you
  - You were added to a group
  - Recurring expense reminder
- In-app notification bell with unread count

### 3.9 Recurring Expenses
- Mark any expense as recurring (weekly / monthly)
- Auto-log on schedule (rent, subscriptions, electricity)
- Manage recurring expenses from settings

### 3.10 Analytics Dashboard
- Monthly total spend per group
- Per-person spend breakdown
- Category-wise pie/bar chart
- Personal spend trend over last 6 months
- Export summary as PDF or share as image

### 3.11 Audit & Event Log (Kafka)
- Every expense logged as immutable Kafka event
- Full replay-able history — who added what, when, with what changes
- Visible in-app as "Activity" tab per group
- Useful for disputes: "Rohan added Goa Hotel at 10:32 PM"

### 3.12 Settings & Profile
- Edit display name and avatar
- Update UPI ID
- Currency preference (INR default)
- Notification preferences
- Dark / light mode toggle
- Sign out

### 3.13 Receipt OCR Scanner
- Camera button inside Add Expense screen
- Point camera at any bill — restaurant receipt, Swiggy/Zomato screenshot, handwritten chit
- Google ML Kit Text Recognition v2 runs fully on-device — no API key, no cost, works offline
- Parser extracts: total amount, individual line items (for itemized splits), date
- Pre-fills the amount field automatically; user reviews and confirms
- Supports mixed Hindi/English text common on Indian receipts
- Fallback: if OCR confidence is low, show raw extracted text and let user pick the amount

### 3.14 Itemized Bill Splitting
- After OCR or manual entry, toggle "Split by item" mode
- Each line item shown as a card — each person taps to claim what they ordered
- Tax and service charge distributed proportionally to each person's subtotal
- Remaining unclaimed items highlighted in amber — must be assigned before saving
- Final per-person amount auto-calculated and shown before confirming
- Directly replaces Splitwise Pro's most-used paywalled feature — offered free

### 3.15 Group Budget & Overspend Alerts
- Set a total budget when creating a group (optional, can add later)
- Live budget progress bar visible on Group Detail screen header
- Colour states: green (< 70%), amber (70–99%), red (> 100%)
- Push notification to all members at 80% consumed: "Goa Squad has used ₹12,000 of ₹15,000 budget"
- At 100%: banner on group screen showing overspend amount
- Per-category budget sub-limits optional (e.g. food ≤ ₹3,000)
- Budget resets on a configurable date for flat/recurring groups

### 3.16 UPI SMS Auto-Detection
- After a settlement is sent, app monitors incoming SMS inbox (READ_SMS permission, user opt-in)
- Regex pattern matches UPI credit messages from all major banks and UPI apps
- When a match is found whose amount equals a pending settlement, app surfaces a suggestion card: "Did Rohan settle ₹340? We detected a UPI credit of ₹340"
- One-tap confirm marks the settlement as settled
- No money is ever read, stored, or transmitted — only the amount string from SMS is pattern-matched locally on device
- Works with GPay, PhonePe, Paytm, BHIM, all bank SMS formats

### 3.17 WhatsApp Expense Bot
- Dedicated WhatsApp number (Twilio sandbox for dev, Business API for prod)
- Users link their WhatsApp number to their SplitMate account from Settings
- Supported commands (parsed by NLP on backend):
  - "Arjun paid 800 for dinner split 4 ways" → logs expense to active group
  - "Show balances" → replies with current settlement summary
  - "Settle Priya 340" → marks settlement as done
- Active group context stored in Redis per user session
- Bot replies with confirmation and expense summary
- Viral acquisition channel — users share the bot number in group chats

### 3.18 Expense Nudge & Reminder System
- Automated reminder escalation per unsettled debt:
  - Day 3: in-app badge only (silent)
  - Day 7: push notification to debtor — "You owe Arjun ₹340 from Goa trip"
  - Day 14: creditor gets action button — "Send a reminder to Rohan"
  - Tapping sends a pre-drafted WhatsApp message or in-app notification
- Reminder tone is friendly, never aggressive
- Debtor can snooze reminders for 3 days from notification
- Creditor can disable reminders per settlement if uncomfortable

### 3.19 Settle Score (Gamification)
- Each user has a Settle Score 0–100 shown on their profile within a group
- Score increases the faster they pay back debts; decays slowly if debts age
- Visual badge system: 🟢 Fast Settler (< 3 days avg), 🟡 Normal, 🔴 Slow Payer
- Shown on group member list as a subtle indicator — not shamed, just visible
- Leaderboard tab inside group showing all members ranked by score
- Score resets per group, not global — so a slow payer in one group isn't penalised everywhere

### 3.20 Trip Mode — Timeline View & Summary Card
- Groups tagged as "Trip" get a special timeline layout: Day 1, Day 2, Day 3 sections
- Each day shows expenses in chronological order with a daily subtotal
- Set per-day budget in addition to total trip budget
- At trip end, one-tap generates a "Trip Summary Card" as a shareable image:
  - Total spend, per-person cost, biggest expense, most expensive day
  - Branded with SplitMate logo — organic viral reach
- Share to Instagram Stories, WhatsApp, or save to gallery

### 3.21 Voice Expense Entry
- Microphone button on the Add Expense FAB and home screen
- Uses Android SpeechRecognizer API — fully on-device, free, works in noisy environments
- Speaks naturally: "Rohan paid five hundred for petrol split between me, Rohan, and Priya"
- Parser extracts: payer, amount (handles spoken numbers), description, participants
- Shows parsed result for confirmation before saving — user can correct any field
- Falls back to manual entry if speech recognition confidence is low
- Supports Hinglish: "Mera share kya hai" → shows your balance

### 3.22 Personal Finance Dashboard
- Separate bottom nav tab — "My Spend" — showing cross-group personal analytics
- This month's total spend on friend activities
- Biggest spending friend (who you share most expenses with)
- Year-to-date total across all groups
- Month-over-month trend chart
- Category breakdown across all groups (not just one)
- Insight cards: "You spend 40% of your social budget on food" — generated by Spring AI

---

## 4. Functional Requirements

### FR-01 — User Registration
- User must register with a phone number
- OTP verified before account creation
- UPI ID is optional at signup but required before settling

### FR-02 — Group Creation
- Any user can create a group
- Creator becomes admin
- Admin can add/remove members and rename the group

### FR-03 — Expense Addition
- Expense must have: description, amount, payer, participants
- Amount must be a positive number
- At least 2 participants required for a split
- Payer must be a member of the group

### FR-04 — Debt Calculation
- All balances recalculated server-side after every expense mutation
- Debt minimization result returned via WebSocket within 500ms
- Net balance = sum of what you paid minus sum of your share

### FR-05 — UPI Settlement
- UPI deep-link constructed client-side from receiver's stored UPI ID + amount — no server call required
- Receiver must have a valid UPI ID in their profile before a link can be generated
- UPI ID format validated on save: must match `^[a-zA-Z0-9._-]+@[a-zA-Z]+$`
- UPI ID partially masked in UI (e.g. `arj***@okaxis`) for privacy
- Settlement marked complete only when user explicitly confirms
- Partial settlements supported — user can enter a custom amount
- No payment gateway, no merchant account, no fees — app never handles money

### FR-06 — Real-time Updates
- WebSocket connection maintained per group session
- Reconnect automatically on network restore
- Offline-first: Room DB stores latest state, syncs on reconnect

### FR-07 — AI Categorization
- Category suggestion on description input (debounced 600ms)
- User can reject and pick manually
- Fallback to "Other" if AI confidence < 0.5

### FR-08 — Security
- All API calls authenticated via JWT
- Tokens refreshed automatically
- Users can only view/edit groups they belong to
- UPI IDs partially masked in UI (e.g., arj***@okaxis)

### FR-09 — Receipt OCR
- Camera permission required; graceful fallback if denied
- ML Kit runs on-device — no data leaves the device during OCR
- If extracted amount differs from user-entered amount by > 20%, show a warning before saving
- OCR result treated as a suggestion only — user must confirm before expense is logged

### FR-10 — Group Budget
- Budget is optional; groups without a budget work identically to current behaviour
- Budget alerts sent as push notifications via FCM — not just in-app
- Overspend does not block adding new expenses — it only alerts
- Budget amount must be a positive number greater than the current total spend at time of setting

### FR-11 — UPI SMS Auto-Detection
- READ_SMS permission is opt-in — shown with a clear explanation screen before requesting
- SMS scanning happens entirely on-device; no SMS content is ever sent to the server
- Only the matched amount (a number) is used; full SMS text is never stored or logged
- Suggestion card shown only when amount matches within ±₹1 tolerance (rounding)
- User can dismiss suggestion permanently for a specific settlement

### FR-12 — WhatsApp Bot
- WhatsApp number must be verified against the user's SplitMate account before bot accepts commands
- Bot commands processed within 3 seconds
- If parsing fails or is ambiguous, bot replies asking for clarification — never silently logs a wrong expense
- Bot only has access to groups the user is a member of

### FR-13 — Reminder System
- Reminders only sent for unsettled debts older than 3 days
- Users can globally disable reminders from Settings
- Reminder messages never mention specific amounts in push notification titles (privacy — lock screen visible)
- Maximum 1 reminder push notification per debt per week

### FR-14 — Voice Entry
- Voice input requires RECORD_AUDIO permission
- Must recognise amounts spoken in English and basic Hinglish number patterns
- Parsed result always shown for confirmation — voice alone cannot save an expense
- If fewer than 2 participants identified from speech, show participant picker before saving

---

## 5. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | App cold start < 2s; expense sync < 500ms via WebSocket |
| **Offline Support** | View existing expenses and balances without internet (Room cache) |
| **Scalability** | Backend supports 1,000 concurrent WebSocket connections at MVP |
| **Reliability** | Kafka guarantees at-least-once delivery for expense events |
| **Security** | HTTPS only; JWT auth; UPI IDs masked in UI; app never processes or stores payment data |
| **Compatibility** | Android 8.0+ (API 26+); covers ~95% of active Android devices |
| **Accessibility** | Content descriptions on all icons; minimum tap target 48dp |
| **Data Integrity** | Expenses immutable after 24 hours (edit window) |
| **OCR Privacy** | ML Kit runs on-device; no image or text data sent to any server |
| **SMS Privacy** | SMS content never leaves the device; only matched amount used locally |
| **Voice** | Speech recognition on-device via Android SpeechRecognizer; no audio recorded or stored |
| **WhatsApp Bot** | Bot response time < 3s; commands that fail to parse never silently log data |

---

## 6. Android Screen Map

```
Splash / Onboarding
  └── Phone Login → OTP Verify → Profile Setup → SMS Permission Screen

Bottom Navigation
  ├── Home (Groups List)
  │     └── Group Detail
  │           ├── Expenses Tab
  │           │     ├── Add Expense Sheet
  │           │     │     ├── Split Options Screen (equal/percent/custom)
  │           │     │     ├── Itemized Split Screen
  │           │     │     └── Receipt OCR Camera Screen
  │           │     └── Voice Entry (FAB mic button)
  │           ├── Settle Up Tab
  │           │     ├── UPI Payment Sheet
  │           │     └── SMS Detection Suggestion Card
  │           ├── Budget Tab (Trip Mode / Budget Progress)
  │           ├── Analytics Tab
  │           └── Activity Log Tab
  │
  ├── My Spend (Personal Finance Dashboard)
  │
  ├── Notifications (Nudges + Reminders)
  │
  └── Profile / Settings
        ├── WhatsApp Bot Link Screen
        ├── Settle Score Profile Card
        └── Reminder Preferences
```

---

## 7. API Endpoints (Backend Contract)

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/send-otp` | Send OTP to phone |
| POST | `/auth/verify-otp` | Verify OTP, return JWT |
| PUT | `/auth/profile` | Update name, avatar, UPI ID |

### Groups
| Method | Endpoint | Description |
|---|---|---|
| GET | `/groups` | List user's groups |
| POST | `/groups` | Create group |
| GET | `/groups/{id}` | Group detail + members |
| POST | `/groups/{id}/invite` | Generate invite link |
| POST | `/groups/{id}/join` | Join via invite link |
| DELETE | `/groups/{id}/leave` | Leave group |

### Expenses
| Method | Endpoint | Description |
|---|---|---|
| GET | `/groups/{id}/expenses` | Paginated expense list |
| POST | `/groups/{id}/expenses` | Add new expense |
| PUT | `/groups/{id}/expenses/{eid}` | Edit expense (within 24h) |
| DELETE | `/groups/{id}/expenses/{eid}` | Delete expense |

### Settlements
| Method | Endpoint | Description |
|---|---|---|
| GET | `/groups/{id}/settlements` | Minimized settlement list |
| POST | `/groups/{id}/settlements/{sid}/settle` | Mark settled |

### Analytics
| Method | Endpoint | Description |
|---|---|---|
| GET | `/groups/{id}/analytics` | Category breakdown, monthly totals |
| GET | `/users/me/analytics` | Personal spend across groups |

### Budget
| Method | Endpoint | Description |
|---|---|---|
| POST | `/groups/{id}/budget` | Set group budget |
| GET | `/groups/{id}/budget` | Get budget + current spend % |
| PUT | `/groups/{id}/budget` | Update budget amount or category limits |

### WhatsApp Bot
| Method | Endpoint | Description |
|---|---|---|
| POST | `/bot/whatsapp/webhook` | Twilio incoming message webhook |
| POST | `/bot/whatsapp/link` | Link user's WhatsApp number to account |

### Reminders
| Method | Endpoint | Description |
|---|---|---|
| GET | `/users/me/reminders` | List pending reminder schedules |
| PUT | `/users/me/reminders/preferences` | Update snooze/disable settings |

### Settle Score
| Method | Endpoint | Description |
|---|---|---|
| GET | `/groups/{id}/scores` | Settle scores for all group members |
| GET | `/users/me/score` | Personal score across all groups |

### WebSocket
| Topic | Direction | Description |
|---|---|---|
| `/topic/group/{id}` | Server → Client | New expense broadcast |
| `/topic/group/{id}/settle` | Server → Client | Settlement update |
| `/app/group/{id}/ping` | Client → Server | Keep-alive |

---

## 8. Data Models

### User
```
id, phone, name, avatar_url, upi_id, upi_id_verified (bool),
whatsapp_linked (bool), settle_score (int), created_at
```

### Group
```
id, name, photo_url, created_by, created_at, is_archived,
group_type (trip/flat/office/friends), budget_amount, budget_currency
```

### GroupMember
```
group_id, user_id, joined_at, role (admin/member)
```

### Expense
```
id, group_id, description, amount, paid_by (user_id),
category, split_type (equal/percent/custom/itemized),
date, created_at, updated_at, is_recurring, recurrence_interval,
source (manual/ocr/voice/whatsapp_bot)
```

### ExpenseSplit
```
id, expense_id, user_id, share_amount
```

### ExpenseLineItem  ← new, for itemized splits
```
id, expense_id, description, amount, assigned_to (user_id)
```

### Settlement
```
id, group_id, from_user, to_user, amount, is_settled, settled_at,
reminder_count, last_reminder_at, reminder_snoozed_until
```

### GroupBudget
```
id, group_id, total_amount, alert_at_percent (default 80),
created_at, reset_day (for recurring groups)
```

### CategoryBudget  ← new, optional sub-limits
```
id, group_budget_id, category, limit_amount
```

### SettleScore
```
id, user_id, group_id, score (0-100), avg_settle_days,
total_debts_settled, last_updated
```

---

## 9. 8-Week Development Roadmap

### Week 1 — Foundation
- Project setup: Android (Compose + Hilt + Room + Retrofit)
- Backend setup: Spring Boot + PostgreSQL + Docker
- Auth flow: Phone OTP → JWT → Profile screen (include UPI ID + WhatsApp number fields)
- Basic navigation scaffold with all bottom nav tabs stubbed

### Week 2 — Core Expense Features
- Group creation with group type (trip/flat/office/friends)
- Invite link generation and join flow
- Add expense screen — equal split
- Expense list screen with category chips
- Room DB for offline caching

### Week 3 — Algorithm + Settlement + UPI
- Debt minimization algorithm (backend) with unit tests
- Settlement screen with minimized transactions
- Client-side UPI deep-link builder (Intent.ACTION_VIEW)
- UPI ID validation on profile screen
- Mark as settled flow with confirmation dialog

### Week 4 — Real-time + AI + WhatsApp Bot
- WebSocket integration (OkHttp STOMP)
- Live expense push to group members
- Spring AI expense categorizer
- FCM push notifications
- Twilio WhatsApp bot — link account + basic command parsing (paid/split/balance)

### Week 5 — OCR + Voice + Itemized Split
- Google ML Kit receipt OCR camera screen
- Amount auto-extraction and pre-fill
- Itemized bill splitting screen
- Android SpeechRecognizer voice entry with NLP parser
- Split by percentage and custom amounts

### Week 6 — Budget + SMS Detection + Reminders
- Group budget setup screen and progress bar
- FCM budget alert at 80% and 100%
- READ_SMS permission flow + UPI credit SMS pattern matcher
- Settlement suggestion card from SMS match
- Reminder scheduler (Day 3 / 7 / 14 escalation) using Spring Scheduler + Kafka

### Week 7 — Analytics + Settle Score + Trip Mode
- Analytics dashboard (Vico charts) — group and personal
- Personal Finance Dashboard tab (cross-group spend)
- Settle Score calculation and display on member profiles
- Trip Mode timeline view
- Trip Summary shareable card image generation (Android Canvas API)

### Week 8 — Testing + Polish + Launch
- Unit tests: algorithm, split calculation, SMS parser, voice NLP
- Integration tests: WebSocket, Kafka, WhatsApp bot webhook
- Compose UI tests for all critical flows
- Manual QA: OCR on 10 different receipts, voice in noisy environment, UPI on 4 apps
- Play Store internal testing track + beta to 20–30 friends
- LinkedIn demo video + Reddit post

---

## 10. Testing Plan

### Unit Tests
- Debt minimization algorithm — edge cases (one person owes all, already balanced, 10 members)
- Split calculation — equal, percentage, custom, itemized
- SMS UPI pattern matcher — test against 10+ real bank SMS formats (HDFC, SBI, ICICI, Axis, Kotak)
- Voice NLP parser — English amounts, Hinglish, edge cases ("fifteen hundred", "1.5k")
- Budget alert threshold logic
- Settle Score calculation
- Room DAO queries

### Integration Tests
- REST API end-to-end (MockMvc) for all endpoints
- WebSocket connection and message broadcast
- Kafka event production and consumption
- WhatsApp bot webhook — parse and respond to all supported commands
- Budget alert FCM trigger at 80% and 100%
- Reminder scheduler — verify escalation at correct day intervals

### Android Instrumented Tests
- Compose UI tests: Login, Add Expense, Itemized Split, Settlement, OCR confirm screen, Voice confirm screen
- Room DB integration tests
- Permission denial flows — OCR without camera, voice without mic, SMS without READ_SMS

### Manual QA Checklist
- OCR tested on: restaurant bill, Swiggy screenshot, handwritten receipt, mixed Hindi/English bill
- Voice tested in: quiet room, noisy restaurant, with Hinglish phrasing
- UPI deep-link on GPay, PhonePe, Paytm, BHIM — amount and name pre-filled correctly
- SMS detection: simulate UPI credit SMS, verify suggestion card appears with correct amount
- WhatsApp bot: all 3 commands from a linked and unlinked number
- Budget alert: add expenses past 80% and 100%, verify push notifications fire
- Trip Mode summary card: generate and share image, verify branding correct
- Offline → online sync: add expense offline, verify sync and WebSocket push on reconnect
- Group with 10 members: debt minimization produces ≤ 9 transactions

---

## 11. Go-to-Market (Real Users Strategy)

| Channel | Message |
|---|---|
| College WhatsApp groups | "Free Splitwise with UPI + just point your camera at the bill" |
| Hostel groups | Demo receipt OCR live — scan the mess bill in front of them |
| Trip planning groups | "Set a trip budget, track live, get alerts before you overspend" |
| Reddit r/india, r/IndianBros | "Built a Splitwise clone — receipt scan, UPI settle, WhatsApp bot, all free" |
| LinkedIn | 60-second screen recording showing OCR → itemized split → UPI settle in one flow |
| Product Hunt | Launch after 100+ active users with trip summary card as hero visual |

**WhatsApp bot as acquisition:** The bot number itself spreads organically — users share it in trip group chats so everyone can log expenses without downloading the app. Every person who interacts with the bot is a potential install.

**Target:** 100 active users in Month 1, 500 by Month 2 (WhatsApp bot accelerates this significantly)

---

## 12. Interview Talking Points

**UPI Settlement (No Gateway):**
> "I skipped payment gateways entirely. The app never touches money — it just builds a standard UPI deep-link from the receiver's UPI ID and fires an Android Intent. The OS presents a chooser and the user pays in GPay or PhonePe directly. Zero merchant account, zero fees, zero compliance overhead."

**Receipt OCR:**
> "I used Google ML Kit Text Recognition v2 which runs fully on-device — no API key, no cost, works offline. I wrote a regex-based amount extractor that handles Indian receipt formats including mixed Hindi/English. The key decision was keeping it on-device for both cost and privacy reasons."

**SMS Auto-Detection:**
> "The biggest gap in every splitter app is not knowing if payment actually happened. I solved it with a READ_SMS permission that scans incoming UPI credit messages locally on the device — nothing is ever sent to the server. A regex matches the credited amount against pending settlements and surfaces a one-tap confirm card. It closes the confirmation loop without a payment gateway."

**WhatsApp Bot:**
> "The bot is a viral acquisition channel disguised as a feature. Users share the bot number in trip group chats so everyone can log expenses by texting — no app install required. Each bot interaction is a touchpoint to convert that user to an install. Backend is a Twilio webhook hitting a Spring Boot endpoint with an NLP parser and Redis session state per user."

**Debt Minimization:**
> "I modeled the group as a weighted directed graph. Each person is a node. A debt is a directed edge with weight = amount owed. The greedy algorithm finds the max creditor and max debtor, settles between them, and repeats. For N people this reduces up to N(N-1)/2 pairwise debts to at most N-1 transactions."

**System Design:**
> "Every expense is published as an immutable Kafka event. This gives us a full audit log that can be replayed to reconstruct any group's state at any point in time. WebSocket pushes the recalculated settlement graph to all clients within 500ms of an expense being added."

**Android Architecture:**
> "Clean Architecture with MVVM — Repository pattern abstracts Room (offline) and Retrofit (online). StateFlow drives Compose UI reactively. Hilt manages the dependency graph. The app works fully offline and syncs automatically via WebSocket on reconnect."
