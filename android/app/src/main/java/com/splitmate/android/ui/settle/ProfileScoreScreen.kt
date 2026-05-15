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

@Composable
fun ProfileScoreScreen(onBackClick: () -> Unit) {
    var smsEnabled by remember { mutableStateOf(false) }
    var whatsappLinked by remember { mutableStateOf(true) }
    var upiId by remember { mutableStateOf("arjun.designer@okaxis") }

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
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                Text("Arjun Kumar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("+91 98765 43210", fontSize = 14.sp, color = Color(0xFF94A3B8))

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

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Default UPI ID", color = Color(0xFF94A3B8)) },
                        trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Automations
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("AUTOMATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("UPI SMS Auto-Detection", color = Color.White, fontSize = 16.sp)
                            Text("Auto-detect when someone pays you via SMS", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(checked = smsEnabled, onCheckedChange = { smsEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f)))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("WhatsApp Bot Linked", color = Color.White, fontSize = 16.sp)
                            Text("Add expenses by messaging the SplitMate bot", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(checked = whatsappLinked, onCheckedChange = { whatsappLinked = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f)))
                    }
                }
            }
        }
    }
}