package com.zacharee1.insomnia.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun CardSwitch(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: Painter? = null,
    contentDescription: String? = null,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    summaryTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    backgroundColor: Color = Color.Transparent,
) {
    Row(modifier = modifier) {
        Card(
            onClick = {
                onEnabledChanged(!enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .animateContentSize(),
            colors = CardDefaults.outlinedCardColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = backgroundColor,
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let {
                    Image(
                        painter = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = titleTextStyle
                    )

                    if (!summary.isNullOrBlank()) {
                        Spacer(Modifier.size(4.dp))

                        Text(
                            text = summary,
                            style = summaryTextStyle
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChanged,
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}