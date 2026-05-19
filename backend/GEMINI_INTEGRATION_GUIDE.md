# Gemini AI Integration Guide for SplitMate Backend

This guide explains how to use the Gemini AI service in the SplitMate backend.

## Overview

The `GeminiService` provides a clean interface to call Google Vertex AI / Gemini models from your Spring Boot application. It handles:
- **Credential loading** via `GOOGLE_APPLICATION_CREDENTIALS` environment variable
- **Model initialization** with Spring AI's `ChatClient`
- **Error handling** and logging
- **Example use cases** like expense categorization and settlement summaries

## Configuration

### Prerequisites
1. **Service Account JSON** (or user credentials via ADC)
   - Located at: `backend/keys/gcp-sa.json`
   - Configured in `backend/.env` via `GOOGLE_APPLICATION_CREDENTIALS`

2. **Environment Variables** (set in `.env`)
   ```dotenv
   GOOGLE_APPLICATION_CREDENTIALS=./keys/gcp-sa.json
   GOOGLE_CLOUD_PROJECT_ID=ai-resume-builder-496605
   GEMINI_LOCATION=us-central1
   GEMINI_MODEL=gemini-2.5-flash
   ```

3. **Dependencies** (already in `pom.xml`)
   ```xml
   <dependency>
       <groupId>org.springframework.ai</groupId>
       <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
   </dependency>
   ```

## Usage Examples

### 1. Basic Content Generation

Inject `GeminiService` and call `generateContent()`:

```java
@RestController
@RequiredArgsConstructor
public class MyController {
    
    private final GeminiService geminiService;
    
    @PostMapping("/ask-gemini")
    public String askGemini(@RequestParam String question) {
        return geminiService.generateContent(question);
    }
}
```

**Request:**
```bash
curl -X POST "http://localhost:8081/ask-gemini?question=What%20is%202%2B2%3F"
```

**Response:**
```
"2 plus 2 equals 4."
```

### 2. Expense Categorization

Use the built-in expense categorization method:

```java
@Service
public class ExpenseService {
    
    private final GeminiService geminiService;
    
    public String categorizeExpense(String description) {
        return geminiService.categorizeExpense(description);
    }
}
```

**Example:**
```java
String category = geminiService.categorizeExpense("Dinner at Pizza Hut");
// Returns: "Food"
```

### 3. Settlement Summary Generation

Generate friendly messages for settlement notifications:

```java
public String notifySettlement(String settlementData) {
    return geminiService.generateSettlementSummary(settlementData);
}
```

**Example:**
```java
String expenseData = "Alice paid 1000 INR, split between Bob (500) and Charlie (500)";
String summary = geminiService.generateSettlementSummary(expenseData);
// Returns: "Alice has settled the expense with everyone. Bob and Charlie each owe Alice 500 INR."
```

### 4. Using the REST Endpoint

The `GeminiController` provides three REST endpoints:

#### Generate Content
```bash
curl -X POST http://localhost:8081/api/gemini/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tell me a joke about money"}'
```

Response:
```json
{
  "prompt": "Tell me a joke about money",
  "response": "Why did the money go to school? Because it wanted to get richer!"
}
```

#### Categorize Expense
```bash
curl -X POST http://localhost:8081/api/gemini/categorize-expense \
  -H "Content-Type: application/json" \
  -d '{"description": "Uber ride to airport"}'
```

Response:
```json
{
  "description": "Uber ride to airport",
  "category": "Transportation"
}
```

#### Settlement Summary
```bash
curl -X POST http://localhost:8081/api/gemini/settlement-summary \
  -H "Content-Type: application/json" \
  -d '{"expenseData": "Dinner 1500: Alice 750, Bob 750"}'
```

Response:
```json
{
  "expenseData": "Dinner 1500: Alice 750, Bob 750",
  "summary": "Alice and Bob have split the dinner expense of 1500, each paying 750."
}
```

## Architecture

### GeminiService Class

Located at: `src/main/java/com/splitmate/backend/service/GeminiService.java`

**Key Methods:**
- `generateContent(String prompt)` - Basic prompt completion
- `generateContentWithConfig(String prompt, float temperature)` - With temperature control
- `categorizeExpense(String description)` - Categorize expense descriptions
- `generateSettlementSummary(String expenseData)` - Generate settlement messages

**How it works:**
1. Spring Boot auto-configures `ChatModel` via the Gemini dependency
2. `GeminiService` injects `ChatModel` and wraps it with Spring AI's `ChatClient`
3. Credentials are loaded from `GOOGLE_APPLICATION_CREDENTIALS` environment variable
4. Each method builds a prompt and calls the chat client

### Spring AI ChatClient

The `ChatClient` is Spring AI's abstraction over LLM providers. It provides:
- Fluent API for prompt building: `.prompt().user("text").call().content()`
- Support for multiple LLM providers (Gemini, OpenAI, Anthropic, etc.)
- Built-in error handling and logging

## Testing

### Unit Test

Run the connectivity test to verify credentials are loaded:

```bash
mvn -Dtest=GeminiConnectivityTest test
```

This validates:
- `GOOGLE_APPLICATION_CREDENTIALS` is set correctly
- The credential file exists and is readable
- Application Default Credentials (ADC) can be loaded

### Integration Test

Run the GeminiServiceTest to verify the service works:

```bash
mvn -Dtest=GeminiServiceTest test
```

**Note:** The test makes real API calls to Vertex AI. Tests may be skipped in CI/CD environments without live access. Uncomment the test methods to enable live testing.

### Manual Testing with cURL

After starting the backend with `mvn spring-boot:run`:

```bash
# Test basic generation
curl -X POST http://localhost:8081/api/gemini/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Hello"}'
```

## Error Handling

The service throws `RuntimeException` if:
- Credentials cannot be loaded
- API call fails
- Response is empty

**Example error handling:**

```java
try {
    String response = geminiService.generateContent(prompt);
    return ResponseEntity.ok(response);
} catch (RuntimeException e) {
    logger.error("Gemini call failed: {}", e.getMessage());
    return ResponseEntity.status(500).body(
        Map.of("error", "Failed to generate content")
    );
}
```

## Security Best Practices

1. **Never commit credentials**
   - Service account JSON is in `backend/keys/` (protected by `.gitignore`)
   - Use `GOOGLE_APPLICATION_CREDENTIALS` env var to reference the file path

2. **Restrict API access**
   - The Gemini API is called from the backend only (not from client)
   - Add authentication/authorization checks to `GeminiController` if needed

3. **Monitor API usage**
   - Enable audit logging in Google Cloud Console
   - Set up quota limits to prevent unexpected costs

4. **Rotate credentials**
   - Regularly rotate service account keys
   - Update `.env` to point to the new key file

## Future Enhancements

### 1. Advanced Prompt Engineering
```java
public String classifyExpenseType(String description) {
    String prompt = """
        You are an expense classification expert.
        Classify this expense into one of: ESSENTIAL, DISCRETIONARY, or SAVINGS.
        Provide a brief reason (1 sentence).
        
        Expense: """ + description;
    return generateContent(prompt);
}
```

### 2. Structured Responses
Use JSON parsing to extract structured data from Gemini responses:
```java
public Map<String, Object> analyzeExpenseTrends(String expenseHistory) {
    String prompt = "Analyze these expenses and return JSON with summary stats";
    String response = generateContent(prompt);
    return JsonParser.parseJson(response); // Parse response to Map
}
```

### 3. Multi-turn Conversations
Maintain conversation history for more context-aware responses:
```java
private List<Message> conversationHistory = new ArrayList<>();

public String continueConversation(String userMessage) {
    // Store user message
    conversationHistory.add(new Message("user", userMessage));
    
    // Call Gemini with full history
    String response = chatClient.prompt()
        .messages(conversationHistory)
        .call()
        .content();
    
    // Store assistant response
    conversationHistory.add(new Message("assistant", response));
    return response;
}
```

### 4. Function Calling (Tool Use)
Let Gemini call backend endpoints:
```java
public String smartExpenseHandler(String userInput) {
    // Gemini can decide to call categorizeExpense, calculateTip, etc.
    // Based on understanding the user's intent
}
```

## Troubleshooting

### Issue: "Failed to load Application Default Credentials"
**Solution:** Verify `GOOGLE_APPLICATION_CREDENTIALS` points to a valid file
```bash
echo $env:GOOGLE_APPLICATION_CREDENTIALS
Test-Path "C:\path\to\gcp-sa.json"
```

### Issue: "Project ID not set"
**Solution:** Ensure `GOOGLE_CLOUD_PROJECT_ID` is in `.env`
```bash
echo $env:GOOGLE_CLOUD_PROJECT_ID
# Should output: ai-resume-builder-496605
```

### Issue: Gemini API quota exceeded
**Solution:** Check quota in Google Cloud Console and wait for reset, or upgrade quota limits

### Issue: "Unknown model: gemini-2.5-flash"
**Solution:** Verify the model name in `.env` matches available Gemini models
- Check [Gemini models documentation](https://ai.google.dev/models)
- Use alternatives: `gemini-pro`, `gemini-1.5-flash`, etc.

## References

- [Spring AI Vertex AI Gemini Documentation](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Google Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Gemini API Reference](https://ai.google.dev/api)
- [Spring AI Chat Completions](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)
