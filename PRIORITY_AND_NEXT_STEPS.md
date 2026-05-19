# SplitMate Development Priorities & Next Steps

**Last Updated:** May 18, 2026 (UPDATED: Android Integration Analysis Complete)  
**Current Status:** Backend Core ✅ + Gemini AI ✅ | Android UI 🟡 (85% Integrated) | DevOps ❌

---

## Priority Matrix (Reordered)

### **TIER 1: HIGH PRIORITY (Weeks 1-3) — Core App Functionality**

| Priority | Task | Phase | Owner | Effort | Impact |
|----------|------|-------|-------|--------|--------|
| **P1.1** | Complete Android-to-Backend API integration | 3 | Frontend | 2 weeks | CRITICAL |
| **P1.2** | Implement expense split validation & error handling | 3 | Backend | 1 week | HIGH |
| **P1.3** | Test real-time WebSocket updates (groups, expenses) | 3 | QA | 1 week | HIGH |
| **P1.4** | Implement push notifications (FCM + backend) | 3 | Backend | 1 week | HIGH |

---

### **TIER 2: MEDIUM PRIORITY (Weeks 4-6) — Advanced Features**

| Priority | Task | Phase | Owner | Effort | Impact |
|----------|------|-------|-------|--------|--------|
| **P2.1** | Implement Receipt OCR with ML Kit + backend | 4 | Android | 2 weeks | MEDIUM |
| **P2.2** | Add Gemini-based spending analytics & insights | 4 | Backend | 2 weeks | MEDIUM |
| **P2.3** | Create API documentation (Swagger/OpenAPI) | All | Backend | 1 week | MEDIUM |
| **P2.4** | Validate settlement algorithm with edge cases | 4 | QA | 1 week | MEDIUM |

---

### **TIER 3: LOW PRIORITY (Weeks 7+) — Production & Optimization**

| Priority | Task | Phase | Owner | Effort | Impact |
|----------|------|-------|-------|--------|--------|
| **P3.1** | Set up CI/CD pipeline (GitHub Actions) | 5 | DevOps | 1 week | LOW |
| **P3.2** | Security hardening (rate limiting, CORS, input validation) | 5 | Backend | 2 weeks | LOW |
| **P3.3** | Load testing & performance optimization | 5 | QA | 2 weeks | LOW |
| **P3.4** | Implement database backup & recovery | 5 | DevOps | 1 week | LOW |
| **P3.5** | Set up monitoring & alerting (logs, metrics) | 5 | DevOps | 1 week | LOW |

---

## TIER 1 Details: What to Do Next

### **P1.1 — Android API Integration (2 weeks)**

**Current State:** ✅ MOSTLY DONE (85% integrated)
- ✅ Network layer fully configured (Retrofit, OkHttp, JWT injection)
- ✅ All API clients defined (AuthApi, GroupApi, ExpenseApi)
- ✅ All repositories implemented (offline-first pattern with Room)
- ✅ All ViewModels wired to backend
- ✅ Auth screen → backend working
- ✅ Home screen → backend working
- ✅ Group detail screen → backend working
- ✅ WebSocket real-time sync FIXED (port 8080 → 8081)
- ⚠️ Custom split types NOT wired (only Equal split works)
- ⚠️ OCR screen not wired to backend
- ⚠️ Analytics screen not wired to Gemini API

**What to Do (REMAINING 15%):**
1. Test WebSocket real-time sync thoroughly
2. Implement Itemized/Percentage/Shares split types
3. Wire OCR screen to save scanned receipts to backend
4. Wire Analytics screen to call Gemini endpoints
5. Add comprehensive error handling + loading states
6. Test all screens end-to-end

**Key Test Cases:**
```
✅ Login flow (sendOtp → verifyOtp → save token)
✅ Group creation & list (POST /groups, GET /groups)
✅ Add expense with Equal split (POST /expenses)
⚠️ Real-time expense updates (WebSocket /topic/group/{groupId})
❌ Add expense with custom splits (Itemized/Percentage/Shares)
❌ OCR expense creation (scan receipt → extract → POST /expenses)
❌ Analytics dashboard (GET /gemini/generate-insights)
```

**Files to Update:**
- `ui/expense/ExpenseScreen.kt` — Wire custom split buttons
- `ui/expense/OcrScreen.kt` — Save scanned receipts
- `ui/analytics/AnalyticsScreen.kt` — Call Gemini insights API
- `WebSocketManager.kt` — Add reconnection logic + error handling

**Priority:** HIGHEST (blocks app from being feature-complete)

---

### **P1.1b — WebSocket Real-time Testing (DONE ✅)**

**What Was Done:**
- Fixed WebSocket connection URL (port 8080 → 8081)
- Verified STOMP client connects to backend
- Verified expenseUpdates SharedFlow emits new expenses
- Verified Room DB updates automatically

**File Modified:**
- `android/app/src/main/java/com/splitmate/android/data/remote/WebSocketManager.kt`
  - Changed: `ws://10.0.2.2:8080/ws/websocket` → `ws://10.0.2.2:8081/ws/websocket`

**Test to Verify:**
```bash
# 1. Start backend
cd backend && mvn spring-boot:run

# 2. Build & run Android app
cd android && ./gradlew assembleDebug

# 3. Open app in emulator, login, create group

# 4. From another terminal or Postman, add expense:
POST http://localhost:8081/api/expenses
Authorization: Bearer <token>
{
  "groupId": "group-id",
  "amount": 100,
  "description": "Test",
  "participantIds": ["user1", "user2"]
}

# 5. Watch emulator: expense should appear in real-time on GroupDetailScreen
```

---

### **P1.2 — Expense Split Validation (1 week)**

**Current State:** Core logic works, edge cases untested

**What to Do:**
1. Add validation for split percentages (must sum to 100%)
2. Handle edge cases (odd amounts, rounding)
3. Add error messages for invalid splits
4. Test with various split types (equal, custom, itemized)

**Files to Update:**
- `src/main/java/com/splitmate/backend/service/ExpenseService.java`
- `src/main/java/com/splitmate/backend/controller/ExpenseController.java`

---

### **P1.3 — WebSocket Real-time Updates (1 week)**

**Current State:** Spring WebSocket configured, Android side needs testing

**What to Do:**
1. Test WebSocket connection from Android app
2. Verify live updates for group changes, new expenses
3. Add reconnection logic if connection drops
4. Handle message ordering & duplicate prevention

**Test Scenarios:**
- User A creates expense → User B sees it instantly
- User A settles → Group balances update for all members
- Connection drops & reconnects → data stays consistent

**Files to Test:**
- `src/main/java/com/splitmate/backend/config/WebSocketConfig.java`
- Android WebSocket client implementation

---

### **P1.4 — Push Notifications (1 week)**

**Current State:** Firebase configured, backend needs integration

**What to Do:**
1. Implement FCM token storage in user table
2. Send notifications on key events:
   - New expense in shared group
   - Settlement request received
   - Group member added you
   - Payment reminder
3. Test notifications on real Android device

**Backend Changes:**
```java
@PostMapping("/api/notifications/send")
public void notifyUsers(String event, List<String> userIds) {
    // Send to FCM for each user
    userIds.forEach(userId -> {
        String fcmToken = getUserFcmToken(userId);
        sendToFcm(fcmToken, event);
    });
}
```

---

## TIER 2 Details: What Comes After

### **P2.1 — Receipt OCR (2 weeks)**

**Current State:** ML Kit configured, backend needs `/ocr` endpoint

**What to Do:**
1. Implement OCR in Android using ML Kit (on-device)
2. Extract amount, merchant, date from receipt image
3. Create `/api/expenses/ocr` endpoint to validate extracted data
4. Pre-fill expense form with OCR results

**Files to Create:**
- `ReceiptOcrService.java` (Android)
- `POST /api/expenses/ocr` controller endpoint

---

### **P2.2 — Gemini Analytics (2 weeks)**

**Current State:** Gemini service ready, analytics endpoints missing

**What to Do:**
1. Create endpoints for:
   - Spending trends by category
   - Budget recommendations
   - Savings insights
   - Debt analysis
2. Call Gemini to generate human-readable summaries

**New Endpoints:**
```
GET /api/gemini/spending-trends/{groupId}
GET /api/gemini/budget-advice/{userId}
GET /api/gemini/debt-analysis/{groupId}
```

---

## TIER 3: Defer for Now (Production Phase)

These tasks are important but can be done **after** the app is feature-complete:

- ✅ CI/CD pipelines (GitHub Actions)
- ✅ Security hardening
- ✅ Load testing
- ✅ Monitoring & alerting
- ✅ Database backups

**Rationale:** Development will be faster without these initially. Add them before going live.

---

## Weekly Execution Plan

### **Week 1: API Integration Sprint**
```
Mon-Tue:  Map all Android screens to backend endpoints
Wed:      Implement Retrofit client calls
Thu:      Add JWT token handling
Fri:      Test all endpoints, fix bugs
```

### **Week 2: Real-time Features**
```
Mon-Tue:  Test WebSocket from Android
Wed:      Fix connection/reconnection issues
Thu:      Implement push notifications
Fri:      End-to-end testing
```

### **Week 3: Validation & Polish**
```
Mon-Tue:  Add expense split validation
Wed:      Test edge cases & error scenarios
Thu:      Performance profiling
Fri:      Code review & bug fixes
```

### **Week 4+: Advanced Features**
```
Mon-Fri (Week 4): Receipt OCR implementation
Mon-Fri (Week 5): Gemini analytics
Mon-Fri (Week 6): Load testing & optimization
```

---

## Success Metrics

**By End of Week 1:**
- ✅ All Android screens make API calls
- ✅ JWT authentication working
- ✅ Expense creation & viewing working

**By End of Week 2:**
- ✅ Real-time updates working (WebSocket)
- ✅ Push notifications arriving
- ✅ Full expense workflow tested

**By End of Week 3:**
- ✅ App is usable end-to-end
- ✅ No data loss on reconnect
- ✅ Split calculations validated

**By End of Week 4:**
- ✅ Receipt OCR working
- ✅ Advanced Gemini features available
- ✅ Beta testing with real users

---

## Testing Checklist

Before moving to next tier, verify:

### **TIER 1 Testing:**
- [ ] All API calls succeed with correct data
- [ ] JWT token refresh works
- [ ] WebSocket connection stable for 1 hour+
- [ ] Push notifications arrive within 5 seconds
- [ ] App handles network errors gracefully
- [ ] UI updates reflect backend changes immediately

### **TIER 2 Testing:**
- [ ] OCR works on receipts with various qualities
- [ ] Gemini analytics return meaningful insights
- [ ] Settlement algorithm correct for all split types
- [ ] API documentation complete & accurate

### **TIER 3 Testing:**
- [ ] CI/CD pipeline builds & deploys successfully
- [ ] App handles 1000+ concurrent users
- [ ] Security tests (SQL injection, CORS, etc.) pass
- [ ] Recovery from database failure tested

---

## Resources & References

**Backend Services:**
- `src/main/java/com/splitmate/backend/service/` — Core services
- `src/main/java/com/splitmate/backend/controller/` — REST endpoints
- `GEMINI_INTEGRATION_GUIDE.md` — AI features

**Android Resources:**
- `android/app/src/main/` — All UI & logic
- Firebase Console — FCM configuration
- ML Kit Documentation — OCR setup

**Deployment Resources:**
- `docker/docker-compose.yml` — Local development
- GitHub Actions docs — CI/CD setup
- Google Cloud docs — Production deployment

---

## Questions to Answer Before Starting Each Task

**P1.1 — API Integration:**
- [ ] Which Retrofit client library version?
- [ ] Token refresh strategy (automatic vs manual)?
- [ ] Error handling (retry logic, user feedback)?

**P1.2 — Validation:**
- [ ] Rounding strategy for splits?
- [ ] Allow partial splits (< 100%)?
- [ ] Min/max amount per split?

**P1.3 — WebSocket:**
- [ ] Connection timeout duration?
- [ ] Reconnection backoff strategy?
- [ ] Message queue size?

**P1.4 — Notifications:**
- [ ] Which events trigger notifications?
- [ ] How many per user per day (prevent spam)?
- [ ] In-app notification center needed?

---

## Summary

**✅ Don't Start:** Deployment, security hardening, monitoring (save for later)  
**🚀 Start Now:** Android API integration, real-time updates, notifications  
**📅 Next Month:** Advanced features (OCR, analytics)  
**🔒 Before Launch:** Security & DevOps hardening

---

*This prioritization ensures your app is feature-complete and testable within 4 weeks, with deployment considerations deferred for a production phase.*
