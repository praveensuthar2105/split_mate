package com.splitmate.android.ui.expense

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.splitmate.android.ui.settle.GroupMember
import com.splitmate.android.util.VoiceExpenseParser
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun VoiceEntryButton(
    groupMembers: List<GroupMember>,
    onParsed: (VoiceExpenseParser.ParsedExpense) -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    // Manual permission handling without accompanist
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            // Permission just granted, but user has to click again in this implementation
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val data = result.data
        if (data != null && result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                val parsed = VoiceExpenseParser.parse(
                    spokenText,
                    groupMembers.map { it.name }
                )
                onParsed(parsed)
            }
        }
    }

    FloatingActionButton(
        onClick = {
            if (hasMicPermission) {
                isListening = true
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say who paid, how much, and what for")
                }
                try {
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    isListening = false
                    e.printStackTrace() // e.g. no speech recognition app installed
                }
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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