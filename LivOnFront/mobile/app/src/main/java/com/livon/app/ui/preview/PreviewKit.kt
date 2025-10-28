// com/livon/app/ui/preview/PreviewKit.kt
package com.livon.app.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    LivonTheme {
        Surface {
            Box(Modifier.padding(Spacing.Horizontal)) {
                content()
            }
        }
    }
}
