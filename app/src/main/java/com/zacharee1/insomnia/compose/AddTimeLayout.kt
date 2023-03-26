package com.zacharee1.insomnia.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.WakeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeLayout(
    onResetClicked: () -> Unit,
    onTimeAdded: (Long) -> Unit,
    onAddStateChanged: (Boolean) -> Unit,
    items: List<WakeState>,
    isAdding: Boolean,
    prefill: Long?,
    modifier: Modifier = Modifier,
) {
    var timeToAdd by remember(items, isAdding) {
        mutableStateOf(prefill?.let { if (it == -1L) it else it / 1000 }?.toString() ?: "")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Messy workaround to keep the height consistent.
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.alpha(0f),
            label = { Text(text = "") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onResetClicked,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    modifier = Modifier.scale(scaleY = 1f, scaleX = -1f)
                )
            }

            IconButton(
                onClick = { onAddStateChanged(true) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = stringResource(id = R.string.add_time)
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isAdding,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.CenterHorizontally),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = timeToAdd,
                    onValueChange = { newValue ->
                        val longValue = newValue.toLongOrNull()

                        timeToAdd = when {
                            newValue == "-" || newValue.isEmpty() -> newValue
                            longValue == null || longValue < -1 -> timeToAdd
                            else -> longValue.toString()
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.time_ms)
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = { onAddStateChanged(false) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_clear_24),
                                contentDescription = stringResource(id = android.R.string.cancel)
                            )
                        }
                    },
                    trailingIcon = if (timeToAdd.isNotBlank()) {
                        {
                            IconButton(
                                onClick = {
                                    timeToAdd.toLongOrNull()?.let {
                                        if (it < -1) return@let

                                        onTimeAdded(if (it < 0) it else it * 1000)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_check_24),
                                    contentDescription = stringResource(id = R.string.add_time)
                                )
                            }
                        }
                    } else {
                        null
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        }
    }
}