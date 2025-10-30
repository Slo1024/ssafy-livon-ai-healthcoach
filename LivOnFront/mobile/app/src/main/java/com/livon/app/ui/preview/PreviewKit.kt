// com/livon/app/ui/preview/PreviewKit.kt
package com.livon.app.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.livon.app.ui.theme.LivonTheme

@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    LivonTheme {
        Surface {
            content()
        }
    }
}
