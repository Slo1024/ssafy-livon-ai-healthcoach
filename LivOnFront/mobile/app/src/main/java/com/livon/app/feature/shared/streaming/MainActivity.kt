package com.livon.app.feature.shared.streaming

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.livon.app.feature.coach.streaming.ui.JoinRoomScreen
import io.openvidu.android.RoomLayoutActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        checkUrls()

        super.onCreate(savedInstanceState)

        val initialParticipantName = "Participant%d".format(Random.nextInt(1, 100))
        val initialRoomName = "Test Room"

        setContent {
            JoinRoomScreen(
                initialParticipantName = initialParticipantName,
                initialRoomName = initialRoomName,
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

    private fun checkUrls() {
        if (Urls.livekitUrl.isEmpty() || Urls.applicationServerUrl.isEmpty()) {
            val intent = Intent(this, ConfigureUrlsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToRoomLayoutActivity(participantName: String, roomName: String) {
        if (participantName.isNotEmpty() && roomName.isNotEmpty()) {
            val intent = Intent(this, RoomLayoutActivity::class.java)
            intent.putExtra("participantName", participantName)
            intent.putExtra("roomName", roomName)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}