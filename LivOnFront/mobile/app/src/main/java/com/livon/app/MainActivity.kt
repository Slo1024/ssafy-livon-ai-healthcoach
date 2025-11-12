
package com.livon.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.data.remote.socket.ChatStompManager
import com.livon.app.feature.coach.streaming.ui.JoinRoomScreen
import com.livon.app.ui.theme.LivonTheme
import io.openvidu.android.RoomLayoutActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JoinRoomScreen(
                initialParticipantName = "User-${(0..99).random()}",
                initialRoomName = "LiveKitRoom",
                onJoinClicked = { roomName, participantName ->

                    val intent = Intent(this, RoomLayoutActivity::class.java).apply {
                        putExtra("roomName", roomName)
                        putExtra("participantName", participantName)
                    }

                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LivonTheme {
        Greeting("Android")
    }
}

