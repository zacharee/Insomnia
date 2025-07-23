package com.zacharee1.insomnia.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.zacharee1.insomnia.R

@Composable
fun InsomniaTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkMode = isSystemInDarkTheme()
    val isAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorPrimary = colorResource(R.color.colorPrimary)
    val colorAccent = colorResource(R.color.colorAccent)

    val colors = when {
        darkMode && isAndroid12 -> dynamicDarkColorScheme(context)
        !darkMode && isAndroid12 -> dynamicLightColorScheme(context)
        darkMode -> darkColorScheme(
            primary = colorPrimary,
            secondary = colorAccent,
        )
        else -> lightColorScheme(
            primary = colorPrimary,
            secondary = colorAccent,
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
