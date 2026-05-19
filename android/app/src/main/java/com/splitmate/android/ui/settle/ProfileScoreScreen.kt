package com.splitmate.android.ui.settle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitmate.android.ui.profile.ProfileViewModel

@Composable
fun ProfileScoreScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var editingUpiId by remember { mutableStateOf(false) }
    var tempUpiId by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user
    
    LaunchedEffect(user.name) {
        if (tempName.isEmpty()) {
            tempName = user.name
        }
    }
    
    LaunchedEffect(user.upiId) {
        if (tempUpiId.isEmpty()) {
            tempUpiId = user.upiId ?: ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Profile & Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                    return@Column
                }

                // Error message
                if (uiState.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f))
                    ) {
                        Text(
                            uiState.error!!,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Success message
                if (uiState.saveSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f))
                    ) {
                        Text(
                            "Profile updated successfully!",
                            color = Color(0xFF10B981),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                
                // Name Section with Edit capability
                if (editingName) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Name", color = Color(0xFF94A3B8)) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Button(
                            onClick = {
                                viewModel.updateName(tempName)
                                editingName = false
                            },
                            enabled = !uiState.isSaving
                        ) {
                            Text(if (uiState.isSaving) "Saving..." else "Save")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(user.name.ifBlank { "User" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Button(
                            onClick = { editingName = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Edit Name")
                        }
                    }
                }
                
                Text(user.phone, fontSize = 14.sp, color = Color(0xFF94A3B8))

                Spacer(Modifier.height(32.dp))

                // Gamified Settle Score Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Settle Score", fontSize = 14.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("92", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                            Text("/100", fontSize = 16.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Fast Settler", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "You usually settle your debts within 2 days! Keep it up to stay in the green.",
                            fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // UPI Settings
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("PAYMENT SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp))

                    if (editingUpiId) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = tempUpiId,
                                onValueChange = { tempUpiId = it },
                                label = { Text("UPI ID", color = Color(0xFF94A3B8)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    viewModel.updateUpiId(tempUpiId)
                                    editingUpiId = false
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                enabled = !uiState.isSaving
                            ) {
                                Text(if (uiState.isSaving) "Saving..." else "Save")
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = user.upiId ?: "",
                                onValueChange = { },
                                label = { Text("Default UPI ID", color = Color(0xFF94A3B8)) },
                                readOnly = true,
                                trailingIcon = {
                                    if (user.upiIdVerified) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = { editingUpiId = true },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            ) {
                                Text("Edit")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Automations (placeholder for future features)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("AUTOMATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp))
                    Text("Coming soon...", fontSize = 14.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}