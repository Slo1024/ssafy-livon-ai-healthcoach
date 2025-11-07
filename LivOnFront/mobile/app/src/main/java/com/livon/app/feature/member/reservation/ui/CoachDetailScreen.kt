package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.livon.app.feature.member.reservation.vm.CoachDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CoachDetailScreen(
    coachId: String,
    onBack: () -> Unit,
    showSchedule: Boolean = true,
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory? = null
) {
    val vm = if (viewModelFactory != null) viewModel(factory = viewModelFactory) as CoachDetailViewModel
    else viewModel() as CoachDetailViewModel

    val state by vm.uiState.collectAsState()

    CommonScreenC(
        topBar = { TopBar(title = "상세 정보", onBack = onBack) },
        fullBleedContent = null
    ) {
        // main content column
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            state.coach?.let { coach ->
                Image(
                    painter = rememberAsyncImagePainter(coach.avatarUrl ?: ""),
                    contentDescription = "coach",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(16.dp))

                Text(text = coach.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(text = coach.job ?: "", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(text = coach.intro, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(20.dp))

                Text(text = "자격증:")
                coach.certificates.forEach { cert: String ->
                    Text(text = cert)
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(coachId) { vm.load(coachId) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachDetail() = PreviewSurface {
    LivonTheme {
        CoachDetailScreen(coachId = "1", onBack = {})
    }
}
