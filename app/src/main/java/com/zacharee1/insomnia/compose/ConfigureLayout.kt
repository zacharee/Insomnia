package com.zacharee1.insomnia.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.KEY_STATES
import com.zacharee1.insomnia.util.TURN_ON_WHEN_PLUGGED_IN
import com.zacharee1.insomnia.util.WakeState
import com.zacharee1.insomnia.util.activateWhenPlugged
import com.zacharee1.insomnia.util.getSavedTimes
import com.zacharee1.insomnia.util.rememberPreferenceState
import com.zacharee1.insomnia.util.saveTimes
import com.zacharee1.insomnia.util.setActivateWhenPlugged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfigureLayout() {
    val context = LocalContext.current

    var infiniteTimeOnCharge by context.rememberPreferenceState(
        key = TURN_ON_WHEN_PLUGGED_IN,
        value = { context.activateWhenPlugged() },
        onChanged = { context.setActivateWhenPlugged(it) }
    )
    var times by context.rememberPreferenceState(
        key = KEY_STATES,
        value = { context.getSavedTimes().toSet().toList() },
        onChanged = { context.saveTimes(it) }
    )

    val listState = rememberLazyListState()
    val dragState = rememberDragDropState(lazyListState = listState, onMove = { from, to ->
        val newList = times.toMutableList()
        newList.add(to, newList.removeAt(from))

        times = newList
    })

    var timeToEdit by remember {
        mutableStateOf<Long?>(null)
    }
    var addingTime by remember {
        mutableStateOf(false)
    }
    var removedTime by remember {
        mutableStateOf<Pair<WakeState, Int>?>(null)
    }

    LaunchedEffect(key1 = removedTime) {
        withContext(Dispatchers.IO) {
            delay(5000)
        }
        removedTime = null
    }

    InsomniaTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .systemBarsPadding(),
            ) {
                Text(
                    text = stringResource(id = R.string.config_hint),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(4.dp))

                CardSwitch(
                    enabled = infiniteTimeOnCharge,
                    onEnabledChanged = { infiniteTimeOnCharge = it },
                    title = stringResource(id = R.string.turn_on_plugged),
                    summary = stringResource(id = R.string.turn_on_plugged_desc),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    titleTextStyle = MaterialTheme.typography.titleMedium,
                )

                HorizontalDivider(modifier = Modifier.padding(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .dragContainer(dragDropState = dragState),
                    state = listState,
                ) {
                    itemsIndexed(times, { _, time -> time.time }) { index, time ->
                        val state = rememberSwipeToDismissBoxState(
                            positionalThreshold = { it / 2f },
                            confirmValueChange = { true },
                        )

                        LaunchedEffect(state.currentValue) {
                            if (state.currentValue == SwipeToDismissBoxValue.EndToStart || state.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                                val timeIndex = times.indexOf(time)
                                times = times - time

                                removedTime = time to timeIndex

                                state.snapTo(SwipeToDismissBoxValue.Settled)
                            }
                        }

                        DraggableItem(dragDropState = dragState, index = index) { dragging ->
                            val elevation by animateDpAsState(
                                targetValue = if (dragging) 8.dp else 0.dp,
                                label = "${time.time}"
                            )

                            SwipeToDismissBox(
                                state = state,
                                backgroundContent = {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .fillMaxHeight()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = stringResource(id = R.string.remove),
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Icon(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = stringResource(id = R.string.remove),
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                },
                                content = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 64.dp)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.drag),
                                            contentDescription = stringResource(id = R.string.reorder),
                                            modifier = Modifier.dragContainerForDragHandle(
                                                dragDropState = dragState,
                                                key = time.time,
                                            ),
                                        )

                                        Text(
                                            text = time.createLabelFromTime(context),
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleLarge,
                                        )

                                        IconButton(
                                            onClick = { timeToEdit = time.time },
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.edit),
                                                contentDescription = stringResource(id = R.string.edit_time),
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .shadow(elevation),
                            )
                        }
                    }
                }

                AddTimeLayout(
                    onResetClicked = {
                        times = ArrayList(App.DEFAULT_STATES)
                        removedTime = null
                    },
                    onTimeAdded = { newTime ->
                        if (!times.contains(WakeState(newTime))) {
                            times = if (timeToEdit != null) {
                                val newList = times.toMutableList()
                                val index = newList.indexOfFirst { time -> time.time == timeToEdit }

                                newList[index] = WakeState(newTime)
                                newList
                            } else {
                                times + WakeState(newTime)
                            }

                            if (timeToEdit == null) {
                                addingTime = false
                            }
                            timeToEdit = null
                        }

                        if (newTime == timeToEdit) {
                            timeToEdit = null
                        }
                    },
                    items = times,
                    isAdding = timeToEdit != null || addingTime,
                    onAddStateChanged = {
                        addingTime = it

                        if (!it) {
                            timeToEdit = null
                        }
                    },
                    prefill = timeToEdit,
                )

                AnimatedVisibility(visible = removedTime != null) {
                    val rTime = remember(times) {
                        removedTime
                    }

                    Snackbar(
                        dismissAction = {
                            TextButton(
                                onClick = {
                                    if (rTime != null) {
                                        times = ArrayList(times).apply {
                                            add(rTime.second, rTime.first)
                                        }
                                        removedTime = null
                                    }
                                }
                            ) {
                                Text(text = stringResource(id = R.string.undo))
                            }
                        },
                        action = {
                            TextButton(
                                onClick = {
                                    removedTime = null
                                }
                            ) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.time_removed_format,
                                rTime?.first?.createLabelFromTime(context) ?: "",
                            ),
                        )
                    }
                }
            }
        }
    }
}