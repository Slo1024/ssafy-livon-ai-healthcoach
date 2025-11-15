package com.livon.app.feature.shared.streaming

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.livon.app.feature.coach.streaming.ui.JoinRoomScreen
import io.openvidu.android.RoomLayoutActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val quickRoomViewModel: QuickRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        checkUrls()

        super.onCreate(savedInstanceState)

        val initialParticipantName = "Participant%d".format(Random.nextInt(1, 100))
        val initialRoomName = "Test Room"

        setContent {
            val quickRoomState by quickRoomViewModel.uiState.collectAsState()

            LaunchedEffect(quickRoomState.createdConsultationId, quickRoomState.participantName) {
                val consultationId = quickRoomState.createdConsultationId
                val participantName = quickRoomState.participantName
                if (consultationId != null && participantName != null) {
                    navigateToRoomLayoutActivity(
                        participantName = participantName,
                        roomName = consultationId.toString(),
                        consultationId = consultationId
                    )
                    quickRoomViewModel.consumeNavigationEvent()
                }
            }

            JoinRoomScreen(
                initialParticipantName = initialParticipantName,
                initialRoomName = initialRoomName,
                onJoinClicked = { roomName, participantName ->
                    navigateToRoomLayoutActivity(
                        participantName = participantName,
                        roomName = roomName,
                        consultationId = roomName.toLongOrNull()
                    )
                },
                quickRoomState = quickRoomState,
                onCreateRoomClicked = { participantName ->
                    quickRoomViewModel.createInstantRoom(participantName)
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

    private fun navigateToRoomLayoutActivity(
        participantName: String,
        roomName: String,
        consultationId: Long? = null
    ) {
        if (participantName.isNotEmpty() && roomName.isNotEmpty()) {
            val intent = Intent(this, RoomLayoutActivity::class.java)
            intent.putExtra("participantName", participantName)
            intent.putExtra("roomName", roomName)
            consultationId?.let { intent.putExtra("consultationId", it) }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
