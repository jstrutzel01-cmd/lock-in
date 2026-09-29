package com.example.lock_in.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DK = darkColorScheme(primary = P80, secondary = PG80, tertiary = Pk80)
private val LT = lightColorScheme(
    primary = P40,
    secondary = PG40,
    tertiary = Pk40
)

@Composable
fun LTh(dk: Boolean = isSystemInDarkTheme(), dyn: Boolean = true, cnt: @Composable () -> Unit) {
    val cs = when {
        dyn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val c = LocalContext.current
            if (dk) dynamicDarkColorScheme(c) else dynamicLightColorScheme(c)
        }
        dk -> DK
        else -> LT
    }
    MaterialTheme(colorScheme = cs, typography = Typo, content = cnt)
}
