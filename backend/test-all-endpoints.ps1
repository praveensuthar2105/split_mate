# SplitMate Backend - Complete API Test Script
# This script tests all endpoints systematically

# Configuration
$baseUrl = "http://localhost:8081"
$phone1 = "9999999999"
$phone2 = "8888888888"
$phone3 = "7777777777"
$name1 = "John Doe"
$name2 = "Jane Smith"
$name3 = "Bob Johnson"

# Test Results Tracking
$testResults = @()

function Test-Endpoint {
    param(
        [string]$name,
        [string]$method,
        [string]$url,
        [hashtable]$headers = @{},
        [string]$body = $null,
        [int]$expectedStatus = 200
    )
    
    try {
        Write-Host "Testing: $name" -ForegroundColor Cyan
        
        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
        }
        
        if ($body) {
            $params["Body"] = $body
        }
        
        $response = Invoke-WebRequest @params -UseBasicParsing
        $statusCode = $response.StatusCode
        
        if ($statusCode -eq $expectedStatus -or $statusCode -eq 201) {
            Write-Host "✓ PASSED (Status: $statusCode)" -ForegroundColor Green
            $testResults += @{Name = $name; Status = "PASSED"; Code = $statusCode}
            return $response
        } else {
            Write-Host "✗ FAILED (Expected: $expectedStatus, Got: $statusCode)" -ForegroundColor Red
            $testResults += @{Name = $name; Status = "FAILED"; Code = $statusCode}
            return $null
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "✗ ERROR (Status: $statusCode): $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Name = $name; Status = "ERROR"; Code = $statusCode; Error = $_.Exception.Message}
        return $null
    }
}

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "SplitMate Backend - Complete API Tests" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# ============================================================================
# PHASE 1: AUTHENTICATION TESTS
# ============================================================================
Write-Host "`n### PHASE 1: AUTHENTICATION TESTS ###`n" -ForegroundColor Magenta

# Test 1.1: Send OTP to Phone Number 1
$otpResponse1 = Test-Endpoint -name "Send OTP (User 1)" `
    -method "POST" `
    -url "$baseUrl/auth/send-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{phone = $phone1} | ConvertTo-Json) `
    -expectedStatus 200

if ($otpResponse1) {
    Write-Host $otpResponse1.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
}
Write-Host ""

# Test 1.2: Send OTP to Phone Number 2
$otpResponse2 = Test-Endpoint -name "Send OTP (User 2)" `
    -method "POST" `
    -url "$baseUrl/auth/send-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{phone = $phone2} | ConvertTo-Json) `
    -expectedStatus 200

if ($otpResponse2) {
    Write-Host $otpResponse2.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
}
Write-Host ""

# Test 1.3: Send OTP to Phone Number 3
$otpResponse3 = Test-Endpoint -name "Send OTP (User 3)" `
    -method "POST" `
    -url "$baseUrl/auth/send-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{phone = $phone3} | ConvertTo-Json) `
    -expectedStatus 200

if ($otpResponse3) {
    Write-Host $otpResponse3.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
}
Write-Host ""

# Test 1.4: Verify OTP and Create User 1
$verifyResponse1 = Test-Endpoint -name "Verify OTP (User 1)" `
    -method "POST" `
    -url "$baseUrl/auth/verify-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{
        phone = $phone1
        otp = "123456"
        name = $name1
    } | ConvertTo-Json) `
    -expectedStatus 200

$token1 = $null
if ($verifyResponse1) {
    $content = $verifyResponse1.Content | ConvertFrom-Json
    $token1 = $content.token
    Write-Host "Token 1: $($token1.Substring(0, 30))..." -ForegroundColor Green
    Write-Host $content | ConvertTo-Json -Depth 2
}
Write-Host ""

# Test 1.5: Verify OTP and Create User 2
$verifyResponse2 = Test-Endpoint -name "Verify OTP (User 2)" `
    -method "POST" `
    -url "$baseUrl/auth/verify-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{
        phone = $phone2
        otp = "123456"
        name = $name2
    } | ConvertTo-Json) `
    -expectedStatus 200

$token2 = $null
if ($verifyResponse2) {
    $content = $verifyResponse2.Content | ConvertFrom-Json
    $token2 = $content.token
    Write-Host "Token 2: $($token2.Substring(0, 30))..." -ForegroundColor Green
}
Write-Host ""

# Test 1.6: Verify OTP and Create User 3
$verifyResponse3 = Test-Endpoint -name "Verify OTP (User 3)" `
    -method "POST" `
    -url "$baseUrl/auth/verify-otp" `
    -headers @{"Content-Type"="application/json"} `
    -body (@{
        phone = $phone3
        otp = "123456"
        name = $name3
    } | ConvertTo-Json) `
    -expectedStatus 200

$token3 = $null
if ($verifyResponse3) {
    $content = $verifyResponse3.Content | ConvertFrom-Json
    $token3 = $content.token
    Write-Host "Token 3: $($token3.Substring(0, 30))..." -ForegroundColor Green
}
Write-Host ""

# ============================================================================
# PHASE 2: GROUP MANAGEMENT TESTS
# ============================================================================
Write-Host "`n### PHASE 2: GROUP MANAGEMENT TESTS ###`n" -ForegroundColor Magenta

if ($token1) {
    $headers1 = @{
        "Authorization" = "Bearer $token1"
        "Content-Type" = "application/json"
    }
    
    # Test 2.1: Create Group
    $groupResponse = Test-Endpoint -name "Create Group (User 1)" `
        -method "POST" `
        -url "$baseUrl/groups" `
        -headers $headers1 `
        -body (@{
            name = "Trip to Goa"
            description = "Summer vacation expenses"
        } | ConvertTo-Json) `
        -expectedStatus 201
    
    $groupId = $null
    if ($groupResponse) {
        $content = $groupResponse.Content | ConvertFrom-Json
        $groupId = $content.id
        Write-Host "Group ID: $groupId" -ForegroundColor Green
        Write-Host $content | ConvertTo-Json -Depth 3
    }
    Write-Host ""
    
    # Test 2.2: Add Member 2 to Group
    if ($groupId -and $token2) {
        $addMemberResponse = Test-Endpoint -name "Add Member (User 2) to Group" `
            -method "POST" `
            -url "$baseUrl/groups/$groupId/members" `
            -headers $headers1 `
            -body (@{
                phone = $phone2
                name = $name2
            } | ConvertTo-Json) `
            -expectedStatus 200
        
        if ($addMemberResponse) {
            Write-Host $addMemberResponse.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
        }
        Write-Host ""
    }
    
    # Test 2.3: Add Member 3 to Group
    if ($groupId) {
        $addMemberResponse = Test-Endpoint -name "Add Member (User 3) to Group" `
            -method "POST" `
            -url "$baseUrl/groups/$groupId/members" `
            -headers $headers1 `
            -body (@{
                phone = $phone3
                name = $name3
            } | ConvertTo-Json) `
            -expectedStatus 200
        
        if ($addMemberResponse) {
            Write-Host $addMemberResponse.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
        }
        Write-Host ""
    }
    
    # Test 2.4: Get Group Details
    if ($groupId) {
        $getGroupResponse = Test-Endpoint -name "Get Group Details" `
            -method "GET" `
            -url "$baseUrl/groups/$groupId" `
            -headers $headers1 `
            -expectedStatus 200
        
        if ($getGroupResponse) {
            Write-Host $getGroupResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
        }
        Write-Host ""
    }
    
    # Test 2.5: List All Groups
    $listGroupsResponse = Test-Endpoint -name "List All Groups (User 1)" `
        -method "GET" `
        -url "$baseUrl/groups" `
        -headers $headers1 `
        -expectedStatus 200
    
    if ($listGroupsResponse) {
        Write-Host $listGroupsResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 2
    }
    Write-Host ""
    
    # ============================================================================
    # PHASE 3: EXPENSE TESTS
    # ============================================================================
    Write-Host "`n### PHASE 3: EXPENSE TESTS ###`n" -ForegroundColor Magenta
    
    if ($groupId) {
        # Test 3.1: Add Expense (Equal Split)
        $expenseResponse1 = Test-Endpoint -name "Add Expense - Equal Split (Hotel)" `
            -method "POST" `
            -url "$baseUrl/expenses" `
            -headers $headers1 `
            -body (@{
                groupId = $groupId
                description = "Hotel booking"
                amount = 6000.00
                paidBy = 1
                splitType = "EQUAL"
                splitDetails = @(
                    @{ memberId = 1; amount = 2000.00 },
                    @{ memberId = 2; amount = 2000.00 },
                    @{ memberId = 3; amount = 2000.00 }
                )
            } | ConvertTo-Json -Depth 3) `
            -expectedStatus 201
        
        $expenseId1 = $null
        if ($expenseResponse1) {
            $content = $expenseResponse1.Content | ConvertFrom-Json
            $expenseId1 = $content.id
            Write-Host "Expense ID 1: $expenseId1" -ForegroundColor Green
            Write-Host $content | ConvertTo-Json -Depth 3
        }
        Write-Host ""
        
        # Test 3.2: Add Expense (Custom Split)
        $expenseResponse2 = Test-Endpoint -name "Add Expense - Custom Split (Dinner)" `
            -method "POST" `
            -url "$baseUrl/expenses" `
            -headers $headers1 `
            -body (@{
                groupId = $groupId
                description = "Dinner at restaurant"
                amount = 3000.00
                paidBy = 2
                splitType = "CUSTOM"
                splitDetails = @(
                    @{ memberId = 1; amount = 1000.00 },
                    @{ memberId = 2; amount = 1500.00 },
                    @{ memberId = 3; amount = 500.00 }
                )
            } | ConvertTo-Json -Depth 3) `
            -expectedStatus 201
        
        $expenseId2 = $null
        if ($expenseResponse2) {
            $content = $expenseResponse2.Content | ConvertFrom-Json
            $expenseId2 = $content.id
            Write-Host "Expense ID 2: $expenseId2" -ForegroundColor Green
            Write-Host $content | ConvertTo-Json -Depth 3
        }
        Write-Host ""
        
        # Test 3.3: Get Group Expenses
        $listExpensesResponse = Test-Endpoint -name "List All Expenses for Group" `
            -method "GET" `
            -url "$baseUrl/groups/$groupId/expenses" `
            -headers $headers1 `
            -expectedStatus 200
        
        if ($listExpensesResponse) {
            Write-Host $listExpensesResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
        }
        Write-Host ""
        
        # Test 3.4: Get Expense Details
        if ($expenseId1) {
            $getExpenseResponse = Test-Endpoint -name "Get Expense Details (Expense 1)" `
                -method "GET" `
                -url "$baseUrl/expenses/$expenseId1" `
                -headers $headers1 `
                -expectedStatus 200
            
            if ($getExpenseResponse) {
                Write-Host $getExpenseResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
            }
            Write-Host ""
        }
    }
    
    # ============================================================================
    # PHASE 4: SETTLEMENT & BALANCE TESTS
    # ============================================================================
    Write-Host "`n### PHASE 4: SETTLEMENT & BALANCE TESTS ###`n" -ForegroundColor Magenta
    
    if ($groupId) {
        # Test 4.1: Get Balance Summary
        $balanceResponse = Test-Endpoint -name "Get Balance Summary for Group" `
            -method "GET" `
            -url "$baseUrl/groups/$groupId/balances" `
            -headers $headers1 `
            -expectedStatus 200
        
        if ($balanceResponse) {
            Write-Host $balanceResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
        }
        Write-Host ""
        
        # Test 4.2: Mark Settlement
        $settlementResponse = Test-Endpoint -name "Mark Settlement (User 2 pays User 1)" `
            -method "POST" `
            -url "$baseUrl/settlements" `
            -headers $headers1 `
            -body (@{
                groupId = $groupId
                fromMemberId = 2
                toMemberId = 1
                amount = 1000.00
                description = "Payment via UPI"
            } | ConvertTo-Json) `
            -expectedStatus 201
        
        if ($settlementResponse) {
            Write-Host $settlementResponse.Content | ConvertFrom-Json | ConvertTo-Json -Indent 2
        }
        Write-Host ""
        
        # Test 4.3: Get Updated Balance
        $balanceResponse2 = Test-Endpoint -name "Get Updated Balance Summary" `
            -method "GET" `
            -url "$baseUrl/groups/$groupId/balances" `
            -headers $headers1 `
            -expectedStatus 200
        
        if ($balanceResponse2) {
            Write-Host $balanceResponse2.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
        }
        Write-Host ""
        
        # Test 4.4: Get Group Settlements
        $settlementsResponse = Test-Endpoint -name "Get All Settlements for Group" `
            -method "GET" `
            -url "$baseUrl/groups/$groupId/settlements" `
            -headers $headers1 `
            -expectedStatus 200
        
        if ($settlementsResponse) {
            Write-Host $settlementsResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
        }
        Write-Host ""
    }
}

# ============================================================================
# TEST SUMMARY
# ============================================================================
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "TEST SUMMARY" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

$passedCount = ($testResults | Where-Object {$_.Status -eq "PASSED"}).Count
$failedCount = ($testResults | Where-Object {$_.Status -eq "FAILED"}).Count
$errorCount = ($testResults | Where-Object {$_.Status -eq "ERROR"}).Count
$totalCount = $testResults.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor Cyan
Write-Host "Passed: $passedCount" -ForegroundColor Green
Write-Host "Failed: $failedCount" -ForegroundColor Red
Write-Host "Errors: $errorCount" -ForegroundColor Yellow

Write-Host "`nDetailed Results:`n" -ForegroundColor Cyan

foreach ($result in $testResults) {
    $color = switch ($result.Status) {
        "PASSED" { "Green" }
        "FAILED" { "Red" }
        "ERROR" { "Yellow" }
    }
    
    Write-Host "$($result.Name): $($result.Status) (Code: $($result.Code))" -ForegroundColor $color
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Tests Complete!" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# Return summary
$summary = @{
    TotalTests = $totalCount
    Passed = $passedCount
    Failed = $failedCount
    Errors = $errorCount
    SuccessRate = if ($totalCount -gt 0) { [math]::Round(($passedCount / $totalCount) * 100, 2) } else { 0 }
}

Write-Host "Success Rate: $($summary.SuccessRate)%" -ForegroundColor Cyan

if ($failedCount -eq 0 -and $errorCount -eq 0) {
    Write-Host "`n✓ ALL TESTS PASSED!" -ForegroundColor Green
} else {
    Write-Host "`n✗ Some tests failed. Review the details above." -ForegroundColor Red
}
