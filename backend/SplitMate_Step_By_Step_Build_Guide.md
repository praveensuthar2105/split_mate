# SplitMate — Step-by-Step Build Guide

> How to build the project from zero to deployed, in order.

---

## PHASE 0 — Environment Setup (Day 1)

### Step 0.1 — Install Required Tools

**On your machine:**
```
JDK 17          → https://adoptium.net
Android Studio  → https://developer.android.com/studio
Docker Desktop  → https://docker.com/products/docker-desktop
IntelliJ IDEA   → https://jetbrains.com/idea (Community is fine)
Postman         → https://postman.com
Git             → https://git-scm.com
```

Verify installations:
```bash
java -version        # should print 17.x
docker --version     # should print 24.x or higher
git --version
```

---

### Step 0.2 — Create Project Structure

```
splitmate/
├── backend/          ← Spring Boot project
├── android/          ← Android (Jetpack Compose) project
├── docker/           ← Docker Compose files
└── README.md
```

```bash
mkdir splitmate && cd splitmate
mkdir backend android docker
git init
```

---

### Step 0.3 — Create GitHub Repository

```bash
git remote add origin https://github.com/YOUR_USERNAME/splitmate.git

# Create .gitignore
echo "*.class
*.jar
build/
.gradle/
.idea/
local.properties
*.keystore
.env" > .gitignore

git add . && git commit -m "chore: initial project structure"
git push -u origin main
```

---

## PHASE 1 — Backend Foundation (Days 2–4)

### Step 1.1 — Generate Spring Boot Project

Go to https://start.spring.io and configure:

```
Project:      Maven
Language:     Java
Spring Boot:  3.2.x
Group:        com.splitmate
Artifact:     backend
Java:         17

Dependencies to add:
  ✓ Spring Web
  ✓ Spring Data JPA
  ✓ PostgreSQL Driver
  ✓ Spring Security
  ✓ Spring WebSocket
  ✓ Validation
  ✓ Lombok
  ✓ Spring Boot DevTools
```

Click Generate → download ZIP → extract into `splitmate/backend/`

---

### Step 1.2 — Docker Compose for Local Services

Create `splitmate/docker/docker-compose.yml`:

```yaml
version: '3.8'
services:

  postgres:
    image: postgres:15
    container_name: splitmate-db
    environment:
      POSTGRES_DB: splitmate
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: splitmate-redis
    ports:
      - "6379:6379"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: splitmate-kafka
    depends_on: [zookeeper]
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  pgdata:
```

Start all services:
```bash
cd docker && docker-compose up -d
```

Verify they're running:
```bash
docker ps
# Should show: splitmate-db, splitmate-redis, splitmate-kafka
```

---

### Step 1.3 — Configure application.properties

Edit `backend/src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/splitmate
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.expiration=86400000

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=splitmate-group
spring.kafka.consumer.auto-offset-reset=earliest

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# WebSocket
spring.websocket.path=/ws
```

---

### Step 1.4 — Add Missing Dependencies to pom.xml

Add inside `<dependencies>`:

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Spring AI (OpenAI) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>0.8.1</version>
</dependency>
```

---

### Step 1.5 — Create Entity Classes

**File: `model/User.java`**
```java
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String phone;

    private String name;
    private String avatarUrl;
    private String upiId;
    private boolean upiIdVerified;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

**File: `model/Group.java`**
```java
@Entity
@Table(name = "groups")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String photoUrl;
    private String createdBy;
    private boolean archived;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

**File: `model/Expense.java`**
```java
@Entity
@Table(name = "expenses")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String groupId;
    private String description;
    private BigDecimal amount;
    private String paidBy;
    private String category;

    @Enumerated(EnumType.STRING)
    private SplitType splitType;  // EQUAL, PERCENTAGE, CUSTOM

    private LocalDate date;
    private boolean isRecurring;
    private String recurrenceInterval; // WEEKLY, MONTHLY

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**File: `model/ExpenseSplit.java`**
```java
@Entity
@Table(name = "expense_splits")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String expenseId;
    private String userId;
    private BigDecimal shareAmount;
}
```

**File: `model/Settlement.java`**
```java
@Entity
@Table(name = "settlements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String groupId;
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private boolean settled;
    private LocalDateTime settledAt;
}
```

Run the app once — Hibernate will auto-create all tables:
```bash
cd backend && ./mvnw spring-boot:run
# Check logs for "HHH000490: Using Hibernate" — tables created
```

---

## PHASE 2 — Authentication (Days 5–6)

### Step 2.1 — OTP Service

For development, use a mock OTP (print to console). For production, integrate Twilio or MSG91.

**File: `service/OtpService.java`**
```java
@Service
public class OtpService {
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String phone) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(phone, otp);
        // DEV: print to console
        System.out.println("OTP for " + phone + " → " + otp);
        // PROD: call Twilio/MSG91 here
    }

    public boolean verifyOtp(String phone, String otp) {
        return otp.equals(otpStore.getOrDefault(phone, ""));
    }
}
```

---

### Step 2.2 — JWT Utility

**File: `security/JwtUtil.java`**
```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
```

---

### Step 2.3 — Auth Controller

**File: `controller/AuthController.java`**
```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        otpService.sendOtp(body.get("phone"));
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String otp = body.get("otp");

        if (!otpService.verifyOtp(phone, otp)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid OTP"));
        }

        User user = userRepository.findByPhone(phone)
            .orElseGet(() -> userRepository.save(
                User.builder().phone(phone).build()
            ));

        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(Map.of("token", token, "userId", user.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody UserProfileRequest request
    ) {
        String userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        User user = userRepository.findById(userId).orElseThrow();
        user.setName(request.getName());
        user.setUpiId(request.getUpiId());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
```

Test with Postman:
```
POST http://localhost:8080/auth/send-otp
Body: { "phone": "9876543210" }

POST http://localhost:8080/auth/verify-otp
Body: { "phone": "9876543210", "otp": "123456" }
→ returns { "token": "eyJ..." }
```

---

## PHASE 3 — Groups & Expenses (Days 7–10)

### Step 3.1 — Group Service

**File: `service/GroupService.java`**
```java
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;

    public Group createGroup(String name, String createdBy) {
        Group group = Group.builder()
            .name(name)
            .createdBy(createdBy)
            .build();
        group = groupRepo.save(group);

        memberRepo.save(GroupMember.builder()
            .groupId(group.getId())
            .userId(createdBy)
            .role("ADMIN")
            .build());

        return group;
    }

    public String generateInviteLink(String groupId) {
        // Returns a link the user can share via WhatsApp
        return "https://splitmate.app/join/" + groupId;
    }

    public void joinGroup(String groupId, String userId) {
        boolean alreadyMember = memberRepo
            .existsByGroupIdAndUserId(groupId, userId);
        if (!alreadyMember) {
            memberRepo.save(GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role("MEMBER")
                .build());
        }
    }
}
```

---

### Step 3.2 — Expense Service

**File: `service/ExpenseService.java`**
```java
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseSplitRepository splitRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SettlementService settlementService;
    private final SimpMessagingTemplate websocket;

    public Expense addExpense(CreateExpenseRequest req, String groupId) {
        // 1. Save expense
        Expense expense = expenseRepo.save(Expense.builder()
            .groupId(groupId)
            .description(req.getDescription())
            .amount(req.getAmount())
            .paidBy(req.getPaidBy())
            .category(req.getCategory())
            .splitType(req.getSplitType())
            .date(req.getDate())
            .build());

        // 2. Save individual splits
        List<SplitEntry> splits = calculateSplits(req);
        splits.forEach(s -> splitRepo.save(ExpenseSplit.builder()
            .expenseId(expense.getId())
            .userId(s.getUserId())
            .shareAmount(s.getAmount())
            .build()));

        // 3. Publish to Kafka (audit trail)
        kafkaTemplate.send("expense-events", Map.of(
            "event", "EXPENSE_ADDED",
            "groupId", groupId,
            "expenseId", expense.getId(),
            "description", req.getDescription(),
            "amount", req.getAmount(),
            "timestamp", Instant.now().toString()
        ));

        // 4. Recalculate settlements
        List<SettlementTransaction> settlements =
            settlementService.recalculate(groupId);

        // 5. Push to all group members via WebSocket
        websocket.convertAndSend(
            "/topic/group/" + groupId,
            Map.of("type", "EXPENSE_ADDED",
                   "expense", expense,
                   "settlements", settlements)
        );

        return expense;
    }

    private List<SplitEntry> calculateSplits(CreateExpenseRequest req) {
        if (req.getSplitType() == SplitType.EQUAL) {
            BigDecimal share = req.getAmount()
                .divide(BigDecimal.valueOf(req.getParticipants().size()),
                        2, RoundingMode.HALF_UP);
            return req.getParticipants().stream()
                .map(uid -> new SplitEntry(uid, share))
                .toList();
        }
        // Handle PERCENTAGE and CUSTOM splits similarly
        return req.getCustomSplits();
    }
}
```

---

### Step 3.3 — WebSocket Configuration

**File: `config/WebSocketConfig.java`**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

---

## PHASE 4 — Debt Minimization Algorithm (Days 11–12)

This is the most important part. Build it carefully and test it thoroughly.

### Step 4.1 — The Algorithm

**File: `algorithm/DebtMinimizer.java`**
```java
@Component
public class DebtMinimizer {

    /**
     * Takes a list of expenses, returns the minimum set of transactions
     * that fully settles all debts.
     *
     * Algorithm:
     *   1. Calculate net balance for each person
     *      (positive = owed money, negative = owes money)
     *   2. Use greedy: always settle the largest creditor
     *      with the largest debtor
     *   3. Repeat until all balances are zero
     *
     * Complexity: O(N log N) — N = number of members
     * Result: at most N-1 transactions vs N*(N-1)/2 naive
     */
    public List<SettlementTransaction> minimize(
        List<String> memberIds,
        List<ExpenseWithSplits> expenses
    ) {
        // Step 1: Calculate net balance per person
        Map<String, BigDecimal> net = new HashMap<>();
        memberIds.forEach(id -> net.put(id, BigDecimal.ZERO));

        expenses.forEach(exp -> {
            exp.getSplits().forEach(split -> {
                if (!split.getUserId().equals(exp.getPaidBy())) {
                    // Debtor owes payer
                    net.merge(split.getUserId(),
                        split.getShareAmount().negate(), BigDecimal::add);
                    net.merge(exp.getPaidBy(),
                        split.getShareAmount(), BigDecimal::add);
                }
            });
        });

        // Step 2: Separate into creditors (+) and debtors (-)
        PriorityQueue<Balance> creditors = new PriorityQueue<>(
            Comparator.comparing(Balance::getAmount).reversed()
        );
        PriorityQueue<Balance> debtors = new PriorityQueue<>(
            Comparator.comparing(Balance::getAmount).reversed()
        );

        net.forEach((id, amount) -> {
            if (amount.compareTo(BigDecimal.valueOf(0.01)) > 0)
                creditors.add(new Balance(id, amount));
            else if (amount.compareTo(BigDecimal.valueOf(-0.01)) < 0)
                debtors.add(new Balance(id, amount.negate()));
        });

        // Step 3: Greedily match largest creditor with largest debtor
        List<SettlementTransaction> result = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.poll();
            Balance debtor   = debtors.poll();

            BigDecimal settleAmount =
                creditor.getAmount().min(debtor.getAmount());

            result.add(SettlementTransaction.builder()
                .fromUserId(debtor.getUserId())
                .toUserId(creditor.getUserId())
                .amount(settleAmount.setScale(2, RoundingMode.HALF_UP))
                .build());

            BigDecimal creditorLeft =
                creditor.getAmount().subtract(settleAmount);
            BigDecimal debtorLeft =
                debtor.getAmount().subtract(settleAmount);

            if (creditorLeft.compareTo(BigDecimal.valueOf(0.01)) > 0)
                creditors.add(new Balance(creditor.getUserId(), creditorLeft));
            if (debtorLeft.compareTo(BigDecimal.valueOf(0.01)) > 0)
                debtors.add(new Balance(debtor.getUserId(), debtorLeft));
        }

        return result;
    }
}
```

---

### Step 4.2 — Write Algorithm Unit Tests First

**File: `test/DebtMinimizerTest.java`**
```java
@SpringBootTest
class DebtMinimizerTest {

    @Autowired
    DebtMinimizer minimizer;

    @Test
    void threePersonSimpleDebt() {
        // A paid 300 split 3 ways → B owes 100, C owes 100
        // Expected: 2 transactions (B→A 100, C→A 100)
        var result = minimizer.minimize(...);
        assertEquals(2, result.size());
    }

    @Test
    void alreadyBalanced() {
        // Everyone paid exactly their share → 0 transactions
        var result = minimizer.minimize(...);
        assertEquals(0, result.size());
    }

    @Test
    void tenMembersMaxTransactions() {
        // 10 members with varied expenses
        // Result must be ≤ 9 transactions (N-1)
        var result = minimizer.minimize(...);
        assertTrue(result.size() <= 9);
    }

    @Test
    void allAmountsSettleToZero() {
        // Sum of all settlement amounts must equal
        // sum of all positive net balances
        var result = minimizer.minimize(...);
        BigDecimal totalSettled = result.stream()
            .map(SettlementTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // assert totalSettled equals total positive balances
    }
}
```

Run tests:
```bash
./mvnw test -Dtest=DebtMinimizerTest
```

---

## PHASE 5 — Spring AI Expense Categorizer (Day 13)

### Step 5.1 — Categorization Service

**File: `service/CategorizerService.java`**
```java
@Service
@RequiredArgsConstructor
public class CategorizerService {

    private final ChatClient chatClient;

    private static final String PROMPT = """
        You are an expense categorizer for a bill-splitting app.
        Given a short expense description, return ONLY one of these
        category codes (nothing else):
        food, transport, rent, groceries, entertainment, utilities, other

        Description: %s
        Category:""";

    public String categorize(String description) {
        try {
            String response = chatClient
                .prompt(PROMPT.formatted(description))
                .call()
                .content()
                .trim()
                .toLowerCase();

            Set<String> valid = Set.of(
                "food","transport","rent",
                "groceries","entertainment","utilities","other"
            );
            return valid.contains(response) ? response : "other";

        } catch (Exception e) {
            return "other"; // graceful fallback
        }
    }
}
```

Add to `application.properties`:
```properties
spring.ai.openai.api-key=sk-your-openai-key
spring.ai.openai.chat.model=gpt-4o-mini
```

Add categorize endpoint:
```java
@PostMapping("/expenses/categorize")
public ResponseEntity<?> categorize(@RequestBody Map<String, String> body) {
    String category = categorizerService.categorize(body.get("description"));
    return ResponseEntity.ok(Map.of("category", category));
}
```

---

## PHASE 6 — Android Project Setup (Days 14–15)

### Step 6.1 — Create Android Project

In Android Studio:
```
New Project → Empty Activity (Compose)
Name:        SplitMate
Package:     com.splitmate.android
Save:        splitmate/android/
Min SDK:     API 26 (Android 8.0)
Language:    Kotlin
Build:       Gradle (Kotlin DSL)
```

---

### Step 6.2 — Add Dependencies to build.gradle.kts (app)

```kotlin
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel + Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit (networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WebSocket STOMP
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Room (local DB)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Vico (charts)
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // DataStore (token storage)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

Add JitPack to `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

### Step 6.3 — Project Package Structure

```
com.splitmate.android/
├── data/
│   ├── local/          ← Room DB, DAOs, entities
│   ├── remote/         ← Retrofit API interfaces
│   └── repository/     ← Repository implementations
├── domain/
│   ├── model/          ← Clean domain models
│   └── usecase/        ← Business logic use cases
├── ui/
│   ├── auth/           ← Login, OTP, Profile screens
│   ├── groups/         ← Groups list, Group detail
│   ├── expense/        ← Add expense, expense list
│   ├── settle/         ← Settle up screen
│   ├── analytics/      ← Charts screen
│   └── components/     ← Shared Composables
├── di/                 ← Hilt modules
└── util/               ← Extensions, helpers
```

Create all packages now (right-click → New Package in Android Studio).

---

## PHASE 7 — Android Auth Screens (Days 16–17)

### Step 7.1 — Token Storage with DataStore

**File: `util/TokenManager.kt`**
```kotlin
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    val tokenFlow: Flow<String?> = dataStore.data
        .map { it[TOKEN_KEY] }

    suspend fun clearToken() {
        dataStore.edit { it.clear() }
    }

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
```

---

### Step 7.2 — Auth API Interface

**File: `data/remote/AuthApi.kt`**
```kotlin
interface AuthApi {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body body: Map<String, String>): Response<Unit>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): TokenResponse

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: ProfileRequest): UserResponse
}
```

---

### Step 7.3 — Login Screen (Compose)

**File: `ui/auth/LoginScreen.kt`**
```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SplitMate",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00D4AA)
        )

        Spacer(Modifier.height(48.dp))

        if (!uiState.otpSent) {
            // Phone number input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.sendOtp(phone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.length == 10
            ) {
                Text("Send OTP")
            }
        } else {
            // OTP input
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Enter OTP") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.verifyOtp(phone, otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Verify OTP")
                }
            }
        }
    }
}
```

---

## PHASE 8 — Android Core Screens (Days 18–22)

### Step 8.1 — Room Database Setup

**File: `data/local/AppDatabase.kt`**
```kotlin
@Database(
    entities = [
        ExpenseEntity::class,
        GroupEntity::class,
        SettlementEntity::class,
        GroupMemberEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun groupDao(): GroupDao
    abstract fun settlementDao(): SettlementDao
}
```

**File: `data/local/ExpenseDao.kt`**
```kotlin
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getExpenses(groupId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
```

---

### Step 8.2 — Repository Pattern

**File: `data/repository/ExpenseRepository.kt`**
```kotlin
@Singleton
class ExpenseRepository @Inject constructor(
    private val api: ExpenseApi,
    private val dao: ExpenseDao
) {
    // Returns local data immediately, then fetches from network
    fun getExpenses(groupId: String): Flow<List<Expense>> = flow {
        // 1. Emit cached data instantly (offline-first)
        dao.getExpenses(groupId)
            .first()
            .map { it.toDomain() }
            .let { emit(it) }

        // 2. Fetch from network and update cache
        try {
            val remote = api.getExpenses(groupId)
            dao.insertAll(remote.map { it.toEntity() })
            emit(remote.map { it.toDomain() })
        } catch (e: Exception) {
            // Network failed — cached data already emitted, no crash
        }
    }

    suspend fun addExpense(groupId: String, request: CreateExpenseRequest) {
        val created = api.addExpense(groupId, request)
        dao.insertAll(listOf(created.toEntity()))
    }
}
```

---

### Step 8.3 — Group Detail Screen (Compose)

**File: `ui/groups/GroupDetailScreen.kt`**
```kotlin
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val settlements by viewModel.settlements.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expenses", "Settle Up", "Analytics", "Activity")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddExpense() },
                containerColor = Color(0xFF00D4AA)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            // Balance summary card
            BalanceSummaryCard(balance = viewModel.myBalance)

            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> ExpensesList(expenses = expenses)
                1 -> SettleUpScreen(settlements = settlements)
                2 -> AnalyticsScreen(groupId = groupId)
                3 -> ActivityLogScreen(groupId = groupId)
            }
        }
    }
}
```

---

## PHASE 9 — UPI Settlement Screen (Day 23)

### Step 9.1 — UPI Deep-Link Builder

**File: `util/UpiDeepLink.kt`**
```kotlin
object UpiDeepLink {

    fun build(
        receiverUpiId: String,
        receiverName: String,
        amount: Double,
        note: String = "SplitMate settlement"
    ): Uri {
        return Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", receiverUpiId)
            .appendQueryParameter("pn", receiverName)
            .appendQueryParameter("am", String.format("%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()
    }

    fun isUpiAppInstalled(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        return context.packageManager
            .queryIntentActivities(intent, 0).isNotEmpty()
    }
}
```

---

### Step 9.2 — Settle Up Screen (Compose)

**File: `ui/settle/SettleUpScreen.kt`**
```kotlin
@Composable
fun SettleUpScreen(
    settlements: List<SettlementTransaction>,
    members: List<GroupMember>,
    onMarkSettled: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn {
        item {
            AlgorithmInfoCard(
                memberCount = members.size,
                transactionCount = settlements.size
            )
        }

        items(settlements) { txn ->
            val fromMember = members.find { it.id == txn.fromUserId }
            val toMember   = members.find { it.id == txn.toUserId }

            SettlementCard(
                from = fromMember,
                to = toMember,
                amount = txn.amount,
                isSettled = txn.isSettled,
                onPayUpi = {
                    val upiId = toMember?.upiId
                    if (upiId.isNullOrEmpty()) {
                        // Show snackbar: ask them to add UPI ID
                        return@SettlementCard
                    }
                    val uri = UpiDeepLink.build(
                        receiverUpiId = upiId,
                        receiverName  = toMember.name,
                        amount        = txn.amount
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(
                        Intent.createChooser(intent, "Pay via UPI")
                    )
                },
                onMarkSettled = { onMarkSettled(txn.id) }
            )
        }
    }
}

@Composable
fun AlgorithmInfoCard(memberCount: Int, transactionCount: Int) {
    val naive = memberCount * (memberCount - 1) / 2
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1420)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Debt Minimization",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4AA)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$memberCount members · reduced from $naive " +
                "possible transactions → $transactionCount",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
```

---

## PHASE 10 — WebSocket Real-time Sync (Days 24–25)

### Step 10.1 — STOMP WebSocket Client

**File: `data/remote/WebSocketManager.kt`**
```kotlin
@Singleton
class WebSocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var stompClient: StompClient? = null
    private val _expenseUpdates = MutableSharedFlow<ExpenseUpdate>()
    val expenseUpdates = _expenseUpdates.asSharedFlow()

    fun connect(groupId: String) {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://10.0.2.2:8080/ws/websocket"
            // Use 10.0.2.2 for Android emulator to reach localhost
            // Use your machine's local IP for physical device
        )

        stompClient?.connect()

        stompClient?.topic("/topic/group/$groupId")
            ?.subscribe { message ->
                val update = Gson().fromJson(
                    message.payload,
                    ExpenseUpdate::class.java
                )
                CoroutineScope(Dispatchers.IO).launch {
                    _expenseUpdates.emit(update)
                }
            }
    }

    fun disconnect() {
        stompClient?.disconnect()
    }
}
```

---

### Step 10.2 — Collect Updates in ViewModel

**File: `ui/groups/GroupDetailViewModel.kt`**
```kotlin
@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val webSocketManager: WebSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId = savedStateHandle.get<String>("groupId")!!
    val expenses = expenseRepository.getExpenses(groupId).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        // Connect WebSocket when screen opens
        webSocketManager.connect(groupId)

        // Listen for real-time updates
        viewModelScope.launch {
            webSocketManager.expenseUpdates.collect { update ->
                // Room DB update triggers expenses flow automatically
                expenseRepository.handleRemoteUpdate(update)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}
```

---

## PHASE 11 — Analytics Screen (Day 26)

### Step 11.1 — Category Bar Chart with Vico

**File: `ui/analytics/AnalyticsScreen.kt`**
```kotlin
@Composable
fun AnalyticsScreen(analytics: GroupAnalytics) {
    LazyColumn(Modifier.padding(16.dp)) {

        item {
            Text(
                "Monthly Spend",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            // Vico bar chart
            val model = CartesianChartModelProducer.build {
                columnSeries {
                    series(analytics.monthlyTotals.values.map { it.toFloat() })
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Spend by Category",
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        items(analytics.categoryBreakdown.entries.toList()) { (category, amount) ->
            CategoryRow(
                category = category,
                amount = amount,
                total = analytics.totalSpend
            )
        }
    }
}
```

---

## PHASE 12 — Push Notifications (Day 27)

### Step 12.1 — Firebase Setup

1. Go to https://console.firebase.google.com
2. Create project → Add Android app → Package: `com.splitmate.android`
3. Download `google-services.json` → paste into `android/app/`
4. Add to `build.gradle.kts` (project level):
   ```kotlin
   id("com.google.gms.google-services") version "4.4.1" apply false
   ```
5. Add to `build.gradle.kts` (app level):
   ```kotlin
   id("com.google.gms.google-services")
   implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
   ```

---

### Step 12.2 — FCM Service

**File: `SplitMateMessagingService.kt`**
```kotlin
class SplitMateMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body  = message.notification?.body  ?: return

        val notification = NotificationCompat.Builder(this, "splitmate")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Send FCM token to your backend
        CoroutineScope(Dispatchers.IO).launch {
            // api.updateFcmToken(token)
        }
    }
}
```

---

## PHASE 13 — Testing (Days 28–30)

### Step 13.1 — Algorithm Unit Tests (Backend)

Already covered in Phase 4.2. Run full test suite:
```bash
cd backend && ./mvnw test
```

---

### Step 13.2 — API Integration Tests (Backend)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void addExpenseReturns201() throws Exception {
        mockMvc.perform(
            post("/groups/{id}/expenses", groupId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Dinner",
                      "amount": 1200,
                      "paidBy": "u1",
                      "splitType": "EQUAL",
                      "participants": ["u1","u2","u3"]
                    }
                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.category").exists());
    }
}
```

---

### Step 13.3 — Android UI Tests (Compose)

**File: `androidTest/LoginScreenTest.kt`**
```kotlin
@HiltAndroidTest
class LoginScreenTest {

    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun enterPhoneAndTapSend() {
        composeRule.onNodeWithText("Phone Number")
            .performTextInput("9876543210")

        composeRule.onNodeWithText("Send OTP")
            .performClick()

        composeRule.onNodeWithText("Enter OTP")
            .assertIsDisplayed()
    }
}
```

---

## PHASE 14 — Docker & Deployment (Day 31)

### Step 14.1 — Backend Dockerfile

**File: `backend/Dockerfile`**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Step 14.2 — Full docker-compose.yml (with backend)

Add to `docker/docker-compose.yml`:
```yaml
  backend:
    build: ../backend
    container_name: splitmate-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/splitmate
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_DATA_REDIS_HOST: redis
    depends_on: [postgres, kafka, redis]
```

Deploy everything:
```bash
cd docker && docker-compose up --build -d
```

---

### Step 14.3 — GitHub Actions CI/CD

**File: `.github/workflows/backend.yml`**
```yaml
name: Backend CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: splitmate
        ports: ["5432:5432"]

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: cd backend && ./mvnw test
      - name: Build Docker image
        run: cd backend && docker build -t splitmate-backend .
```

---

## PHASE 15 — Beta Launch (Day 32+)

### Step 15.1 — Android Release Build

In Android Studio:
```
Build → Generate Signed Bundle / APK
  → Android App Bundle
  → Create new keystore (save the password somewhere safe)
  → Release build type
```

---

### Step 15.2 — Play Store Internal Testing

1. Go to https://play.google.com/console
2. Create app → All apps → Create app
3. Fill store listing (name, description, screenshots)
4. Internal testing → Create release → Upload your `.aab` file
5. Add testers via email (your friends/college group)
6. Share the opt-in link with them

---

### Step 15.3 — Share with Real Users

```
WhatsApp message template:
"Hey! I built a free Splitwise with one-tap UPI settlement.
 Try it here: [Play Store link]
 Add our [trip/flat/group] expenses and settle with GPay in one tap 🙌"
```

Target channels in order:
1. Your own friend group — add real expenses together
2. College batch WhatsApp group
3. Hostel floor group
4. LinkedIn post with a 60-second screen recording
5. Reddit r/india after 50+ active users

---

## Build Order Summary

```
Week 1  →  Phase 0, 1, 2        Environment + Backend foundation + Auth
Week 2  →  Phase 3, 4           Groups, Expenses, Debt algorithm
Week 3  →  Phase 5, 6, 7        Spring AI + Android setup + Auth screens
Week 4  →  Phase 8, 9, 10       Core Android screens + UPI + WebSocket
Week 5  →  Phase 11, 12         Analytics + Push notifications
Week 6  →  Phase 16, 17, 18     OCR + Voice + Itemized Split
Week 7  →  Phase 19, 20, 21     Budget + SMS Detection + Reminders + Settle Score
Week 8  →  Phase 13, 14, 15     Testing + Docker + Beta launch
```

---

## PHASE 16 — Receipt OCR Scanner (Days 33–34)

### Step 16.1 — Add ML Kit Dependency

```kotlin
// build.gradle.kts (app)
implementation("com.google.mlkit:text-recognition:16.0.0")
// No API key needed — runs fully on-device
```

Add camera permission to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

### Step 16.2 — OCR Amount Extractor

**File: `util/ReceiptOcrExtractor.kt`**
```kotlin
object ReceiptOcrExtractor {

    // Matches Indian amount formats:
    // ₹ 1,200.00 / Rs. 800 / Total: 450 / TOTAL 2450.50
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:total|amount|grand total|subtotal|net amount)[:\s]*[₹Rs.]*\s*([\d,]+\.?\d*)""",
            RegexOption.IGNORE_CASE),
        Regex("""[₹Rs.]\s*([\d,]+\.?\d*)"""),
        Regex("""([\d,]+\.?\d*)\s*(?:/-|only)""", RegexOption.IGNORE_CASE)
    )

    fun extractAmount(rawText: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(rawText) ?: continue
            val amountStr = match.groupValues[1]
                .replace(",", "")
                .trim()
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    fun extractLineItems(rawText: String): List<LineItem> {
        // Each line: item name followed by a price
        val linePattern = Regex("""^(.+?)\s+([\d,]+\.?\d*)$""", RegexOption.MULTILINE)
        return linePattern.findAll(rawText).map { match ->
            LineItem(
                name = match.groupValues[1].trim(),
                amount = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
            )
        }.filter { it.amount > 0 && it.amount < 100_000 }.toList()
    }
}
```

---

### Step 16.3 — OCR Camera Screen (Compose)

**File: `ui/expense/OcrCameraScreen.kt`**
```kotlin
@Composable
fun OcrCameraScreen(
    onAmountExtracted: (Double, List<LineItem>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                                if (!isScanning) {
                                    isScanning = true
                                    processImage(proxy, context) { text ->
                                        val amount = ReceiptOcrExtractor.extractAmount(text)
                                        val items = ReceiptOcrExtractor.extractLineItems(text)
                                        if (amount != null) {
                                            onAmountExtracted(amount, items)
                                        }
                                        isScanning = false
                                    }
                                }
                                proxy.close()
                            }
                        }

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, imageAnalyzer
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Point at the total amount on the bill",
                color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

private fun processImage(
    proxy: ImageProxy,
    context: Context,
    onResult: (String) -> Unit
) {
    val mediaImage = proxy.image ?: return
    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        .process(image)
        .addOnSuccessListener { result -> onResult(result.text) }
}
```

---

## PHASE 17 — Voice Expense Entry (Day 35)

### Step 17.1 — Voice Parser

**File: `util/VoiceExpenseParser.kt`**
```kotlin
object VoiceExpenseParser {

    data class ParsedExpense(
        val payer: String?,
        val amount: Double?,
        val description: String?,
        val participants: List<String>
    )

    // Convert spoken numbers to digits
    private val NUMBER_WORDS = mapOf(
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "hundred" to 100, "thousand" to 1000, "k" to 1000,
        "fifteen hundred" to 1500, "five hundred" to 500
    )

    fun parse(speech: String, groupMembers: List<String>): ParsedExpense {
        val lower = speech.lowercase()

        // Extract amount — "five hundred", "800", "1.5k", "₹400"
        val amount = extractAmount(lower)

        // Extract payer — first member name mentioned before "paid"
        val payer = groupMembers.firstOrNull { name ->
            lower.contains(name.lowercase()) &&
            lower.indexOf(name.lowercase()) <
            lower.indexOf("paid").takeIf { it >= 0 } ?: Int.MAX_VALUE
        }

        // Extract participants — members mentioned after "between" or "split"
        val splitIndex = maxOf(
            lower.indexOf("between"), lower.indexOf("split with"), lower.indexOf("among")
        )
        val participants = if (splitIndex >= 0) {
            groupMembers.filter { name ->
                lower.substring(splitIndex).contains(name.lowercase())
            }
        } else groupMembers  // Default to all members

        // Extract description — text between payer and amount
        val description = extractDescription(lower, payer, amount)

        return ParsedExpense(payer, amount, description, participants)
    }

    private fun extractAmount(text: String): Double? {
        // Numeric: "800", "1200.50", "₹500"
        val numericPattern = Regex("""[₹Rs.]?\s*(\d+\.?\d*)""")
        numericPattern.find(text)?.let {
            return it.groupValues[1].toDoubleOrNull()
        }
        // Word-based: "five hundred", "1.5k"
        NUMBER_WORDS.entries.sortedByDescending { it.key.length }.forEach { (word, value) ->
            if (text.contains(word)) return value.toDouble()
        }
        return null
    }

    private fun extractDescription(text: String, payer: String?, amount: Double?): String? {
        // Simple heuristic: words between "for" and a number/member name
        val forIndex = text.indexOf(" for ")
        if (forIndex < 0) return null
        return text.substring(forIndex + 5)
            .split(Regex("\\d|split|between|paid"))
            .firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }
}
```

---

### Step 17.2 — Voice Entry Button in Compose

**File: `ui/expense/VoiceEntryButton.kt`**
```kotlin
@Composable
fun VoiceEntryButton(
    groupMembers: List<GroupMember>,
    onParsed: (VoiceExpenseParser.ParsedExpense) -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull() ?: return@rememberLauncherForActivityResult

        val parsed = VoiceExpenseParser.parse(
            spokenText,
            groupMembers.map { it.name }
        )
        onParsed(parsed)
    }

    FloatingActionButton(
        onClick = {
            if (micPermission.status.isGranted) {
                isListening = true
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Say who paid, how much, and what for")
                }
                speechLauncher.launch(intent)
            } else {
                micPermission.launchPermissionRequest()
            }
        },
        containerColor = if (isListening) Color.Red else Color(0xFF00D4AA)
    ) {
        Icon(
            if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Voice entry"
        )
    }
}
```

---

## PHASE 18 — Itemized Bill Splitting (Day 36)

### Step 18.1 — Itemized Split Screen

**File: `ui/expense/ItemizedSplitScreen.kt`**
```kotlin
@Composable
fun ItemizedSplitScreen(
    lineItems: List<LineItem>,
    members: List<GroupMember>,
    onConfirm: (Map<String, Double>) -> Unit  // userId → their total
) {
    // Track which member claimed each item
    val claims = remember {
        mutableStateMapOf<Int, String>()  // itemIndex → userId
    }

    val perPersonTotals by derivedStateOf {
        members.associate { member ->
            member.id to lineItems
                .filterIndexed { i, _ -> claims[i] == member.id }
                .sumOf { it.amount }
        }
    }

    val allClaimed by derivedStateOf {
        lineItems.indices.all { i -> claims.containsKey(i) }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onConfirm(perPersonTotals) },
                enabled = allClaimed,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(if (allClaimed) "Confirm Split" else "Assign all items to continue")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            itemsIndexed(lineItems) { index, item ->
                ItemClaimCard(
                    item = item,
                    members = members,
                    claimedBy = claims[index],
                    onClaim = { memberId -> claims[index] = memberId }
                )
            }

            item {
                // Per-person summary
                Spacer(Modifier.height(16.dp))
                Text("Summary", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
                members.forEach { member ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(member.name)
                        Text("₹${perPersonTotals[member.id]?.let { "%.2f".format(it) } ?: "0.00"}")
                    }
                }
            }
        }
    }
}
```

---

## PHASE 19 — Group Budget & Overspend Alerts (Day 37)

### Step 19.1 — Budget Service (Backend)

**File: `service/BudgetService.java`**
```java
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final GroupBudgetRepository budgetRepo;
    private final ExpenseRepository expenseRepo;
    private final FcmService fcmService;

    public void checkBudgetAlert(String groupId) {
        GroupBudget budget = budgetRepo.findByGroupId(groupId)
            .orElse(null);
        if (budget == null) return;

        BigDecimal totalSpend = expenseRepo
            .sumAmountByGroupId(groupId);
        double pct = totalSpend
            .divide(budget.getTotalAmount(), 4, RoundingMode.HALF_UP)
            .doubleValue() * 100;

        if (pct >= 100 && !budget.isAlert100Sent()) {
            fcmService.sendToGroup(groupId,
                "Budget Exceeded 🔴",
                "Your group has spent ₹" + totalSpend +
                " — over the ₹" + budget.getTotalAmount() + " budget");
            budget.setAlert100Sent(true);
            budgetRepo.save(budget);
        } else if (pct >= 80 && !budget.isAlert80Sent()) {
            fcmService.sendToGroup(groupId,
                "Budget Alert 🟡",
                "Your group has used " + (int)pct + "% of the budget");
            budget.setAlert80Sent(true);
            budgetRepo.save(budget);
        }
    }
}
```

Call `budgetService.checkBudgetAlert(groupId)` at the end of `ExpenseService.addExpense()`.

---

### Step 19.2 — Budget Progress Bar (Android)

**File: `ui/groups/BudgetProgressBar.kt`**
```kotlin
@Composable
fun BudgetProgressBar(budget: GroupBudget, totalSpend: Double) {
    val percent = (totalSpend / budget.totalAmount).coerceIn(0.0, 1.0)
    val color = when {
        percent >= 1.0  -> Color(0xFFF43660)  // red
        percent >= 0.8  -> Color(0xFFF59E0B)  // amber
        else            -> Color(0xFF00D4AA)  // green
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Budget", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(
                "₹${totalSpend.toInt()} / ₹${budget.totalAmount.toInt()}",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percent.toFloat(),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E2D3D)
        )
        if (percent >= 1.0) {
            Text(
                "Over budget by ₹${(totalSpend - budget.totalAmount).toInt()}",
                fontSize = 11.sp, color = Color(0xFFF43660),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
```

---

## PHASE 20 — UPI SMS Auto-Detection (Day 38)

### Step 20.1 — SMS Permission + Reader

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.READ_SMS" />
```

**File: `util/UpiSmsDetector.kt`**
```kotlin
object UpiSmsDetector {

    // Matches UPI credit messages from all major Indian banks
    private val UPI_CREDIT_PATTERNS = listOf(
        Regex("""credited.*?Rs\.?\s*([\d,]+\.?\d*).*?UPI""", RegexOption.IGNORE_CASE),
        Regex("""received.*?Rs\.?\s*([\d,]+\.?\d*).*?(?:GPay|PhonePe|Paytm|BHIM|UPI)""", RegexOption.IGNORE_CASE),
        Regex("""A\/c.*?credited.*?([\d,]+\.?\d*).*?UPI""", RegexOption.IGNORE_CASE),
        Regex("""UPI.*?credit.*?INR\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    )

    data class DetectedPayment(val amount: Double, val rawSms: String)

    fun scanRecentSms(context: Context, lookbackHours: Int = 24): List<DetectedPayment> {
        val results = mutableListOf<DetectedPayment>()
        val cutoff = System.currentTimeMillis() - (lookbackHours * 3600 * 1000)

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("body", "date"),
            "date > ?",
            arrayOf(cutoff.toString()),
            "date DESC"
        ) ?: return results

        cursor.use {
            while (it.moveToNext()) {
                val body = it.getString(0) ?: continue
                for (pattern in UPI_CREDIT_PATTERNS) {
                    val match = pattern.find(body) ?: continue
                    val amount = match.groupValues[1]
                        .replace(",", "").toDoubleOrNull() ?: continue
                    results.add(DetectedPayment(amount, body))
                    break
                }
            }
        }
        return results
    }

    fun findMatchingSettlement(
        detectedPayments: List<DetectedPayment>,
        pendingSettlements: List<Settlement>,
        toleranceRupees: Double = 1.0
    ): List<Pair<DetectedPayment, Settlement>> {
        return detectedPayments.flatMap { payment ->
            pendingSettlements
                .filter { s -> Math.abs(s.amount - payment.amount) <= toleranceRupees }
                .map { Pair(payment, it) }
        }
    }
}
```

---

### Step 20.2 — SMS Suggestion Card (Compose)

**File: `ui/settle/SmsSuggestionCard.kt`**
```kotlin
@Composable
fun SmsSuggestionCard(
    settlement: Settlement,
    detectedAmount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF052016)),
        border = BorderStroke(1.dp, Color(0xFF00D4AA30))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sms, contentDescription = null,
                    tint = Color(0xFF00D4AA), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("UPI payment detected", fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00D4AA), fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "We detected a credit of ₹${detectedAmount.toInt()}. " +
                "Did ${settlement.fromUserName} settle their ₹${settlement.amount.toInt()}?",
                fontSize = 13.sp, color = Color(0xFFCBD5E1)
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D4AA))) {
                    Text("Yes, mark settled", color = Color.Black, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss", fontSize = 12.sp)
                }
            }
        }
    }
}
```

---

## PHASE 21 — WhatsApp Bot (Days 39–40)

### Step 21.1 — Twilio WhatsApp Webhook (Backend)

Add Twilio dependency to `pom.xml`:
```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.14.0</version>
</dependency>
```

**File: `controller/WhatsAppBotController.java`**
```java
@RestController
@RequestMapping("/bot/whatsapp")
@RequiredArgsConstructor
public class WhatsAppBotController {

    private final BotNlpService nlpService;
    private final ExpenseService expenseService;
    private final UserRepository userRepo;
    private final RedisTemplate<String, String> redis;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleMessage(
        @RequestParam("From") String from,      // whatsapp:+919876543210
        @RequestParam("Body") String body
    ) {
        String phone = from.replace("whatsapp:+91", "");
        User user = userRepo.findByPhone(phone).orElse(null);

        if (user == null) {
            return twilioReply("Your number is not linked to SplitMate. " +
                "Open the app → Settings → Link WhatsApp.");
        }

        // Get user's active group from Redis session
        String activeGroupId = redis.opsForValue().get("bot:group:" + user.getId());

        BotCommand command = nlpService.parse(body.trim(), user, activeGroupId);

        String reply = switch (command.getType()) {
            case ADD_EXPENSE -> handleAddExpense(command, user, activeGroupId);
            case SHOW_BALANCE -> handleShowBalance(user, activeGroupId);
            case SETTLE       -> handleSettle(command, user, activeGroupId);
            case SET_GROUP    -> handleSetGroup(command, user);
            default           -> "I didn't understand that. Try:\n" +
                                 "• \"Arjun paid 800 for dinner split 4\"\n" +
                                 "• \"Show balances\"\n" +
                                 "• \"Settle Priya 340\"";
        };

        return twilioReply(reply);
    }

    private ResponseEntity<String> twilioReply(String message) {
        String twiml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response><Message>%s</Message></Response>
            """.formatted(message);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(twiml);
    }

    private String handleAddExpense(BotCommand cmd, User user, String groupId) {
        if (groupId == null) return "No active group. Set one first: \"Use group [name]\"";
        expenseService.addExpense(cmd.toExpenseRequest(), groupId);
        return "✅ Added: " + cmd.getDescription() +
               " · ₹" + cmd.getAmount() +
               " paid by " + cmd.getPayer();
    }
}
```

---

### Step 21.2 — NLP Parser (Backend)

**File: `service/BotNlpService.java`**
```java
@Service
public class BotNlpService {

    public BotCommand parse(String text, User user, String activeGroupId) {
        String lower = text.toLowerCase();

        // "Show balance" / "my balance" / "balances"
        if (lower.matches(".*(balance|owe|settle up).*")) {
            return BotCommand.of(CommandType.SHOW_BALANCE);
        }

        // "Settle Priya 340" / "settled with Rohan 500"
        if (lower.matches(".*(settle|settled|paid back).*")) {
            return parseSettleCommand(text);
        }

        // "Use group Goa Squad" / "switch to Flat group"
        if (lower.matches(".*(use group|switch to|set group).*")) {
            return parseSetGroupCommand(text);
        }

        // Default: try to parse as expense
        // "Arjun paid 800 for dinner split 4 ways"
        return parseExpenseCommand(text, user);
    }

    private BotCommand parseExpenseCommand(String text, User user) {
        // Extract amount
        Matcher amountMatcher = Pattern
            .compile("(\\d+\\.?\\d*)").matcher(text);
        if (!amountMatcher.find()) {
            return BotCommand.of(CommandType.UNKNOWN);
        }
        double amount = Double.parseDouble(amountMatcher.group(1));

        // Extract description (text after "for")
        String desc = "Expense";
        Matcher forMatcher = Pattern
            .compile("for (.+?)(?:\\s+split|$)", Pattern.CASE_INSENSITIVE)
            .matcher(text);
        if (forMatcher.find()) desc = forMatcher.group(1).trim();

        return BotCommand.builder()
            .type(CommandType.ADD_EXPENSE)
            .amount(amount)
            .description(desc)
            .payer(user.getName())
            .build();
    }
}
```

---

## PHASE 22 — Settle Score (Day 41)

### Step 22.1 — Score Calculator (Backend)

**File: `service/SettleScoreService.java`**
```java
@Service
@RequiredArgsConstructor
public class SettleScoreService {

    private final SettlementRepository settlementRepo;
    private final SettleScoreRepository scoreRepo;

    /**
     * Score formula:
     *   base = 100
     *   -5 per day a debt is unsettled (after day 3 grace)
     *   +10 bonus for settling same day
     *   Clamped to 0–100
     */
    public void recalculateScore(String userId, String groupId) {
        List<Settlement> history = settlementRepo
            .findByFromUserAndGroupId(userId, groupId);

        if (history.isEmpty()) {
            scoreRepo.save(SettleScore.builder()
                .userId(userId).groupId(groupId).score(100).build());
            return;
        }

        double totalScore = history.stream().mapToDouble(s -> {
            if (!s.isSettled()) {
                long daysOld = ChronoUnit.DAYS.between(
                    s.getCreatedAt().toLocalDate(), LocalDate.now());
                return Math.max(0, 100 - Math.max(0, daysOld - 3) * 5);
            }
            long daysToSettle = ChronoUnit.DAYS.between(
                s.getCreatedAt().toLocalDate(),
                s.getSettledAt().toLocalDate());
            if (daysToSettle == 0) return 100;
            if (daysToSettle <= 3) return 85;
            if (daysToSettle <= 7) return 65;
            return Math.max(0, 65 - (daysToSettle - 7) * 5);
        }).average().orElse(100);

        scoreRepo.save(SettleScore.builder()
            .userId(userId)
            .groupId(groupId)
            .score((int) totalScore)
            .build());
    }

    public String getBadge(int score) {
        if (score >= 85) return "🟢 Fast Settler";
        if (score >= 60) return "🟡 Normal";
        return "🔴 Slow Payer";
    }
}
```

---

## Key Commands Reference

```bash
# Start all local services
cd docker && docker-compose up -d

# Run backend
cd backend && ./mvnw spring-boot:run

# Run backend tests
cd backend && ./mvnw test

# Run Android on emulator
# → Use Android Studio "Run" button (Shift+F10)

# Build release APK
cd android && ./gradlew assembleRelease

# Check backend logs
docker logs splitmate-backend -f

# Connect to DB
docker exec -it splitmate-db psql -U postgres -d splitmate

# Test WhatsApp bot locally via ngrok
ngrok http 8080
# Set Twilio webhook to: https://YOUR_NGROK_URL/bot/whatsapp/webhook
```
