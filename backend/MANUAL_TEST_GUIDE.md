# SplitMate Backend - Manual Testing Guide

## Quick Start

Before running tests:
1. Ensure backend is running on port 8081
2. Check that PostgreSQL is connected
3. Use Postman or PowerShell to make requests

---

## Test Sequence (Run in Order)

### 1. AUTHENTICATION - Send OTP

**Endpoint:** `POST http://localhost:8081/auth/send-otp`

```json
{
  "phone": "9999999999"
}
```

**PowerShell:**
```powershell
$body = @{phone = "9999999999"} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8081/auth/send-otp" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
```

**Expected:** OTP sent successfully

---

### 2. AUTHENTICATION - Verify OTP

**Endpoint:** `POST http://localhost:8081/auth/verify-otp`

```json
{
  "phone": "9999999999",
  "otp": "123456",
  "name": "John Doe"
}
```

**PowerShell:**
```powershell
$body = @{phone = "9999999999"; otp = "123456"; name = "John Doe"} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/verify-otp" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```

**Expected:** Get JWT token for User 1

---

### 3. AUTHENTICATION - Verify OTP (User 2)

**Endpoint:** `POST http://localhost:8081/auth/verify-otp`

```json
{
  "phone": "8888888888",
  "otp": "123456",
  "name": "Jane Smith"
}
```

**PowerShell:**
```powershell
$body = @{phone = "8888888888"; otp = "123456"; name = "Jane Smith"} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/verify-otp" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body
$token2 = ($response.Content | ConvertFrom-Json).token
Write-Host "Token 2: $token2"
```

**Expected:** Get JWT token for User 2

---

### 4. GROUPS - Create Group

**Endpoint:** `POST http://localhost:8081/groups`

**Headers:**
```
Authorization: Bearer <token_from_step_2>
Content-Type: application/json
```

```json
{
  "name": "Trip to Goa",
  "description": "Summer vacation expenses"
}
```

**PowerShell:**
```powershell
$headers = @{"Authorization" = "Bearer $token"; "Content-Type" = "application/json"}
$body = @{name = "Trip to Goa"; description = "Summer vacation expenses"} | ConvertTo-Json
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups" -Method POST -Headers $headers -Body $body
$groupId = ($response.Content | ConvertFrom-Json).id
Write-Host "Group ID: $groupId"
```

**Expected:** Group created with ID

---

### 5. GROUPS - Add Member

**Endpoint:** `POST http://localhost:8081/groups/{groupId}/members`

```json
{
  "phone": "8888888888",
  "name": "Jane Smith"
}
```

**PowerShell:**
```powershell
$body = @{phone = "8888888888"; name = "Jane Smith"} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/members" -Method POST -Headers $headers -Body $body
```

**Expected:** Member added successfully

---

### 6. GROUPS - Get Group Details

**Endpoint:** `GET http://localhost:8081/groups/{groupId}`

**PowerShell:**
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId" -Method GET -Headers $headers
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
```

**Expected:** Group with all members listed

---

### 7. GROUPS - List All Groups

**Endpoint:** `GET http://localhost:8081/groups`

**PowerShell:**
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups" -Method GET -Headers $headers
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 2
```

**Expected:** List of all user's groups

---

### 8. EXPENSES - Add Expense (Equal Split)

**Endpoint:** `POST http://localhost:8081/expenses`

```json
{
  "groupId": 1,
  "description": "Hotel booking",
  "amount": 6000.00,
  "paidBy": 1,
  "splitType": "EQUAL",
  "splitDetails": [
    {"memberId": 1, "amount": 3000.00},
    {"memberId": 2, "amount": 3000.00}
  ]
}
```

**PowerShell:**
```powershell
$body = @{
  groupId = $groupId
  description = "Hotel booking"
  amount = 6000.00
  paidBy = 1
  splitType = "EQUAL"
  splitDetails = @(
    @{memberId = 1; amount = 3000.00},
    @{memberId = 2; amount = 3000.00}
  )
} | ConvertTo-Json -Depth 3
$response = Invoke-WebRequest -Uri "http://localhost:8081/expenses" -Method POST -Headers $headers -Body $body
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
```

**Expected:** Expense created with ID

---

### 9. EXPENSES - Add Expense (Custom Split)

**Endpoint:** `POST http://localhost:8081/expenses`

```json
{
  "groupId": 1,
  "description": "Dinner at restaurant",
  "amount": 3000.00,
  "paidBy": 2,
  "splitType": "CUSTOM",
  "splitDetails": [
    {"memberId": 1, "amount": 1000.00},
    {"memberId": 2, "amount": 2000.00}
  ]
}
```

**PowerShell:**
```powershell
$body = @{
  groupId = $groupId
  description = "Dinner at restaurant"
  amount = 3000.00
  paidBy = 2
  splitType = "CUSTOM"
  splitDetails = @(
    @{memberId = 1; amount = 1000.00},
    @{memberId = 2; amount = 2000.00}
  )
} | ConvertTo-Json -Depth 3
Invoke-WebRequest -Uri "http://localhost:8081/expenses" -Method POST -Headers $headers -Body $body
```

**Expected:** Expense created with custom splits

---

### 10. EXPENSES - List Group Expenses

**Endpoint:** `GET http://localhost:8081/groups/{groupId}/expenses`

**PowerShell:**
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/expenses" -Method GET -Headers $headers
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
```

**Expected:** All expenses for the group

---

### 11. BALANCES - Get Balance Summary

**Endpoint:** `GET http://localhost:8081/groups/{groupId}/balances`

**PowerShell:**
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/balances" -Method GET -Headers $headers
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 2
```

**Expected:** Member balances showing who owes whom

---

### 12. SETTLEMENTS - Mark Settlement

**Endpoint:** `POST http://localhost:8081/settlements`

```json
{
  "groupId": 1,
  "fromMemberId": 2,
  "toMemberId": 1,
  "amount": 3000.00,
  "description": "Payment via UPI"
}
```

**PowerShell:**
```powershell
$body = @{
  groupId = $groupId
  fromMemberId = 2
  toMemberId = 1
  amount = 3000.00
  description = "Payment via UPI"
} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8081/settlements" -Method POST -Headers $headers -Body $body
```

**Expected:** Settlement recorded

---

### 13. SETTLEMENTS - Get Updated Balances

**Endpoint:** `GET http://localhost:8081/groups/{groupId}/balances`

**PowerShell:**
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8081/groups/$groupId/balances" -Method GET -Headers $headers
Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 2
```

**Expected:** Updated balances after settlement

---

## Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid data |
| 401 | Unauthorized - Invalid/missing token |
| 404 | Not Found - Resource doesn't exist |
| 500 | Server Error |

---

## Testing Tips

1. **Store tokens:** After authentication, keep token in variable for subsequent requests
2. **Store IDs:** Save group and expense IDs for use in related endpoints
3. **Check headers:** Always include Authorization header with Bearer token
4. **Validate JSON:** Ensure JSON is properly formatted before sending
5. **Check logs:** Monitor backend console for errors

---

## Test Data Reference

| Field | Value |
|-------|-------|
| User 1 Phone | 9999999999 |
| User 1 Name | John Doe |
| User 2 Phone | 8888888888 |
| User 2 Name | Jane Smith |
| OTP | 123456 |
| Group | Trip to Goa |

---

## Troubleshooting

**Connection Refused**
- Check if backend is running
- Verify port 8081 is open
- Check PostgreSQL is running

**Invalid Token**
- Ensure token is from step 2
- Token may be expired
- Check Bearer prefix in header

**Resource Not Found (404)**
- Verify groupId/expenseId is correct
- Check resource exists before accessing
- Ensure you're using correct endpoint

**Bad Request (400)**
- Validate JSON format
- Check required fields are present
- Verify data types (numbers as numbers, not strings)
