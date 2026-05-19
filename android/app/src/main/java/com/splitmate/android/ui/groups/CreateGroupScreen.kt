package com.splitmate.android.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onGroupCreated: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Trip") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.createdGroupId) {
        if (uiState.createdGroupId != null) {
            onGroupCreated()
        }
    }

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
    }

    val groupTypes = listOf("Trip", "Flat", "Office", "Friends")

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                Text("Create New Group", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createGroup(groupName, selectedType)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFF334155)
                    ),
                    enabled = groupName.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                    } else {
                        Text("Create Group", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (groupName.isNotBlank()) Color.Black else Color(0xFF94A3B8))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Group Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 32.sp
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Group Name Input
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Group Type selection
                Text("Group Type", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B))
                                .border(1.dp, if (isSelected) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF10B981) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Members", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    "Create the group first, then share its invite link from the group screen.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
