package com.splitmate.android.data.remote

import android.util.Log
import com.google.gson.Gson
import com.splitmate.android.data.remote.dto.ExpenseUpdate
import com.splitmate.android.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import javax.inject.Inject
import javax.inject.Singleton

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
            "ws://10.0.2.2:8081/ws/websocket"
        )

        stompClient?.connect()

        stompClient?.topic("/topic/group/$groupId")
            ?.subscribe({ message ->
                try {
                    val update = Gson().fromJson(
                        message.payload,
                        ExpenseUpdate::class.java
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        _expenseUpdates.emit(update)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocketManager", "Error parsing message: ${e.message}")
                }
            }, { error ->
                Log.e("WebSocketManager", "Stomp error: ${error.message}")
            })
    }

    fun disconnect() {
        stompClient?.disconnect()
    }
}
