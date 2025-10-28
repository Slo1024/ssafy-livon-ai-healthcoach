// com/livon/app/ui/component/button/PrimaryButton.kt
package com.livon.app.ui.component.button

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

@Composable
fun PrimaryButtonCore(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        contentColor = Color.White
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
fun PrimaryButtonBottom(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    horizontalMargin: Dp = 20.dp,
    bottomMargin: Dp = 24.dp,
    targetWidth: Dp = 288.dp,
    height: Dp = 45.dp,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = horizontalMargin,
                end = horizontalMargin,
                bottom = bottomMargin
            )
    ) {
        val maxW = maxWidth
        val available = (maxW - horizontalMargin * 2)
        val buttonWidth: Dp = available.coerceAtMost(targetWidth)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            PrimaryButtonCore(
                text = text,
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .width(buttonWidth)
                    .height(height)
            )
        }
    }
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "Enabled / Bottom 24dp")
@Composable
private fun PreviewPrimaryButtonBottomEnabled() {
    LivonTheme {
        PrimaryButtonBottom(
            text = "다음",
            onClick = { },
            enabled = true
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Disabled / Bottom 24dp")
@Composable
private fun PreviewPrimaryButtonBottomDisabled() {
    LivonTheme {
        PrimaryButtonBottom(
            text = "완료",
            onClick = { },
            enabled = false
        )
    }
}
