# SplitMate Backend - Step by Step Testing Guide

## Prerequisites
- Backend is running on `http://localhost:8081`
- PostgreSQL database is connected
- Postman or curl is available for API testing

---

## Phase 1: Authentication Testing

### Step 1.1: Send OTP to Phone Number
**Endpoint:** `POST /auth/send-otp`

**Request:**
```json
{
  "phone": "9999999999"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "OTP sent successfully",
  "phone": "9999999999"
}
```

**Test Command (PowerShell):**
```powershell
$body = @{
    phone = "9999999999"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/auth/send-otp" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

---

### Step 1.2: Verify OTP and Create User
**Endpoint:** `POST /auth/verify-otp`

**Request:**
```json
{
  "phone": "9999999999",
  "otp": "123456",
  "name": "John Doe"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "User registered/authenticated successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "phone": "9999999999",
    "name": "John Doe"
  }
}
```

**Test Command (PowerShell):**
```powershell
$body = @{
    phone = "9999999999"
    otp = "123456"
    name = "John Doe"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/verify-otp" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body

$token = ($response.Content | ConvertFrom-Json).token
Write-Host "JWT Token: $token"
```

---

## Phase 2: Group Management Testing

### Step 2.1: Create a New Group
**Endpoint:** `POST /groups`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request:**
```json
{
  "name": "Trip to Goa",
  "description": "Summer vacation expenses"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "name": "Trip to Goa",
  "description": "Summer vacation expenses",
  "createdBy": 1,
  "createdAt": "2026-05-12T01:30:00Z",
  "members": [
    {
      "id": 1,
      "phone": "9999999999",
      "name": "John Doe",
      "role": "ADMIN"
    }
  ]
}
```

**Test Command (PowerShell):**
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$body = @{
    name = "Trip to Goa"
    description = "Summer vacation expenses"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/groups" `
  -Method POST `
  -Headers $headers `
  -Body $body

$groupId = ($response.Content | ConvertFrom-Json).id
Write-Host "Group ID: $groupId"
```

---

### Step 2.2: Add Members to Group
**Endpoint:** `POST /groups/{groupId}/members`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request:**
```json
{
  "phone": "8888888888",
  "name": "Jane Smith"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Member added successfully",
  "member": {
    "id": 2,
    "phone": "8888888888",
    "name": "Jane Smith",
    "role": "MEMBER"
  }
}
```

**Test Command (PowerShell):**
```powershell
$body = @{
    phone = "8888888888"
    name = "Jane Smith"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/members" `
  -Method POST `
  -Headers $headers `
  -Body $body
```

---

### Step 2.3: Get Group Details
**Endpoint:** `GET /groups/{groupId}`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Trip to Goa",
  "description": "Summer vacation expenses",
  "createdBy": 1,
  "members": [
    {
      "id": 1,
      "phone": "9999999999",
      "name": "John Doe",
      "role": "ADMIN"
    },
    {
      "id": 2,
      "phone": "8888888888",
      "name": "Jane Smith",
      "role": "MEMBER"
    }
  ]
}
```

**Test Command (PowerShell):**
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId" `
  -Method GET `
  -Headers $headers
```

---

## Phase 3: Expense Testing

### Step 3.1: Add Expense
**Endpoint:** `POST /expenses`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request (Equal Split):**
```json
{
  "groupId": 1,
  "description": "Hotel booking",
  "amount": 6000.00,
  "paidBy": 1,
  "splitType": "EQUAL",
  "splitDetails": [
    {
      "memberId": 1,
      "amount": 3000.00
    },
    {
      "memberId": 2,
      "amount": 3000.00
    }
  ]
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "groupId": 1,
  "description": "Hotel booking",
  "amount": 6000.00,
  "paidBy": 1,
  "splitType": "EQUAL",
  "createdAt": "2026-05-12T01:35:00Z",
  "splits": [
    {
      "id": 1,
      "expenseId": 1,
      "memberId": 1,
      "amount": 3000.00
    },
    {
      "id": 2,
      "expenseId": 1,
      "memberId": 2,
      "amount": 3000.00
    }
  ]
}
```

**Test Command (PowerShell):**
```powershell
$body = @{
    groupId = $groupId
    description = "Hotel booking"
    amount = 6000.00
    paidBy = 1
    splitType = "EQUAL"
    splitDetails = @(
        @{ memberId = 1; amount = 3000.00 },
        @{ memberId = 2; amount = 3000.00 }
    )
} | ConvertTo-Json -Depth 3

$response = Invoke-WebRequest -Uri "http://localhost:8081/expenses" `
  -Method POST `
  -Headers $headers `
  -Body $body

$expenseId = ($response.Content | ConvertFrom-Json).id
Write-Host "Expense ID: $expenseId"
```

---

### Step 3.2: Get Group Expenses
**Endpoint:** `GET /groups/{groupId}/expenses`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "groupId": 1,
  "expenses": [
    {
      "id": 1,
      "groupId": 1,
      "description": "Hotel booking",
      "amount": 6000.00,
      "paidBy": 1,
      "splitType": "EQUAL",
      "createdAt": "2026-05-12T01:35:00Z"
    }
  ]
}
```

**Test Command (PowerShell):**
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/expenses" `
  -Method GET `
  -Headers $headers
```

---

## Phase 4: Settlement Testing

### Step 4.1: Get Balance Summary
**Endpoint:** `GET /groups/{groupId}/balances`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "groupId": 1,
  "balances": [
    {
      "memberId": 1,
      "memberName": "John Doe",
      "balance": 3000.00
    },
    {
      "memberId": 2,
      "memberName": "Jane Smith",
      "balance": -3000.00
    }
  ]
}
```

**Test Command (PowerShell):**
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/balances" `
  -Method GET `
  -Headers $headers
```

---

### Step 4.2: Mark Settlement
**Endpoint:** `POST /settlements`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request:**
```json
{
  "groupId": 1,
  "fromMemberId": 2,
  "toMemberId": 1,
  "amount": 3000.00,
  "description": "Payment via UPI"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "groupId": 1,
  "fromMemberId": 2,
  "toMemberId": 1,
  "amount": 3000.00,
  "description": "Payment via UPI",
  "settledAt": "2026-05-12T01:40:00Z"
}
```

**Test Command (PowerShell):**
```powershell
$body = @{
    groupId = $groupId
    fromMemberId = 2
    toMemberId = 1
    amount = 3000.00
    description = "Payment via UPI"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/settlements" `
  -Method POST `
  -Headers $headers `
  -Body $body
```

---

## Phase 5: Automated Test Script

**Save as `test-api.ps1`:**

```powershell
# Configuration
$baseUrl = "http://localhost:8081"
$phone1 = "9999999999"
$phone2 = "8888888888"
$name1 = "John Doe"
$name2 = "Jane Smith"

# Test 1: Send OTP
Write-Host "=== Test 1: Send OTP ===" -ForegroundColor Green
$body = @{ phone = $phone1 } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "$baseUrl/auth/send-otp" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
Write-Host $response.Content

# Test 2: Verify OTP (use actual OTP from logs/database)
Write-Host "`n=== Test 2: Verify OTP ===" -ForegroundColor Green
$body = @{
    phone = $phone1
    otp = "123456"
    name = $name1
} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "$baseUrl/auth/verify-otp" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token received: $($token.Substring(0, 20))..."

# Test 3: Create Group
Write-Host "`n=== Test 3: Create Group ===" -ForegroundColor Green
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}
$body = @{
    name = "Trip to Goa"
    description = "Summer vacation"
} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "$baseUrl/groups" `
  -Method POST `
  -Headers $headers `
  -Body $body
$groupId = ($response.Content | ConvertFrom-Json).id
Write-Host "Group created with ID: $groupId"

# Test 4: Add Member
Write-Host "`n=== Test 4: Add Member ===" -ForegroundColor Green
$body = @{
    phone = $phone2
    name = $name2
} | ConvertTo-Json
Invoke-WebRequest -Uri "$baseUrl/groups/$groupId/members" `
  -Method POST `
  -Headers $headers `
  -Body $body
Write-Host "Member added successfully"

# Test 5: Add Expense
Write-Host "`n=== Test 5: Add Expense ===" -ForegroundColor Green
$body = @{
    groupId = $groupId
    description = "Hotel"
    amount = 6000.00
    paidBy = 1
    splitType = "EQUAL"
    splitDetails = @(
        @{ memberId = 1; amount = 3000.00 },
        @{ memberId = 2; amount = 3000.00 }
    )
} | ConvertTo-Json -Depth 3
Invoke-WebRequest -Uri "$baseUrl/expenses" `
  -Method POST `
  -Headers $headers `
  -Body $body
Write-Host "Expense added successfully"

# Test 6: Get Balances
Write-Host "`n=== Test 6: Get Balances ===" -ForegroundColor Green
$response = Invoke-WebRequest -Uri "$baseUrl/groups/$groupId/balances" `
  -Method GET `
  -Headers $headers
Write-Host $response.Content

Write-Host "`n=== All Tests Completed ===" -ForegroundColor Green
```

---

## Common Issues & Troubleshooting

| Issue | Solution |
|-------|----------|
| "Connection refused" | Ensure backend is running on port 8081 |
| "Invalid token" | Token expired or malformed; re-authenticate |
| "Group not found" | Verify groupId is correct and user has access |
| "Member not found" | Ensure member exists in group before operations |
| "CORS error" | Check SecurityConfig allows requests |
| "Database error" | Verify PostgreSQL is running and connected |

---

## Test Data Summary

After completing all tests, your database should contain:

- **Users:** 2 users (John Doe, Jane Smith)
- **Groups:** 1 group (Trip to Goa)
- **Expenses:** 1 expense (Hotel - 6000.00)
- **Settlements:** 1 settlement (3000.00 from Jane to John)

---

## Next Steps

1. Test with different split types (PERCENTAGE, CUSTOM)
2. Test multiple groups
3. Test error scenarios (invalid data, unauthorized access)
4. Test pagination for large datasets
5. Performance testing with multiple concurrent users
