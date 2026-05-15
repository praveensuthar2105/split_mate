package com.splitmate.android.ui.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

data class ReceiptItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var price: String = "",
    val assignedMembers: MutableList<String> = mutableListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemizedSplitScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val items = remember { mutableStateListOf(ReceiptItem()) }
    val groupMembers = listOf("Arjun", "Priya", "Rohan", "Neha")

    val subtotal = items.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    val taxAndTip = subtotal * 0.15 // Mock 15% tax and tip
    val totalAmount = subtotal + taxAndTip

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Itemized Split", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Total: ₹${totalAmount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF10B981)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("Confirm Split", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    // Header card with AI Scanner option
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { /* TODO: Trigger OCR */ },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📷", fontSize = 24.sp)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Scan Receipt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Let AI auto-fill the items for you", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }

                items(items) { receiptItem ->
                    ItemCard(
                        item = receiptItem,
                        groupMembers = groupMembers,
                        onItemChange = { updatedItem ->
                            val index = items.indexOfFirst { it.id == updatedItem.id }
                            if (index != -1) {
                                items[index] = updatedItem
                            }
                        },
                        onRemoveClick = {
                            items.remove(receiptItem)
                        }
                    )
                }

                item {
                    TextButton(
                        onClick = { items.add(ReceiptItem()) },
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color(0xFF10B981))
                        Spacer(Modifier.width(8.dp))
                        Text("Add another item", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))

                    // Summary Section
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = Color(0xFF94A3B8))
                            Text("₹${subtotal.toInt()}", color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax & Tip (15%)", color = Color(0xFF94A3B8))
                            Text("₹${taxAndTip.toInt()}", color = Color.White)
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: ReceiptItem,
    groupMembers: List<String>,
    onItemChange: (ReceiptItem) -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item Name
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onItemChange(item.copy(name = it)) },
                    placeholder = { Text("Item Name", color = Color(0xFF64748B)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedContainerColor = Color(0xFF0F172A)
                    )
                )

                Spacer(Modifier.width(12.dp))

                // Item Price
                OutlinedTextField(
                    value = item.price,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            onItemChange(item.copy(price = it))
                        }
                    },
                    placeholder = { Text("₹0.00", color = Color(0xFF64748B)) },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF10B981),
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedContainerColor = Color(0xFF0F172A)
                    )
                )

                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF64748B))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Shared by", fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 8.dp))

            // Member Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groupMembers) { member ->
                    val isSelected = item.assignedMembers.contains(member)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF334155))
                            .border(1.dp, if (isSelected) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable {
                                val newAssigned = item.assignedMembers.toMutableList()
                                if (isSelected) newAssigned.remove(member) else newAssigned.add(member)
                                onItemChange(item.copy(assignedMembers = newAssigned))
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = member,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF10B981) else Color.White
                        )
                    }
                }
            }
        }
    }
}
