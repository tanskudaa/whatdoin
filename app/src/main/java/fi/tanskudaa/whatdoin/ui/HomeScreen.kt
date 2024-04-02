package fi.tanskudaa.whatdoin.ui

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fi.tanskudaa.whatdoin.R
import fi.tanskudaa.whatdoin.ui.theme.WhatDoinTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExportButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = "Export")
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Icon(imageVector = Icons.Default.Send, contentDescription = "Export all logged activities to CSV")
    }
}

@Composable
fun CurrentActivityStatusBody(
    activityDescription: String,
    formattedDuration: String,
    onChangeCurrentActivityDescription: (String) -> Unit,
) {
    val modifyDescription = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "You've been doin'")

        ElevatedCard {
            when (modifyDescription.value) {
                false ->
                    TextButton(onClick = { modifyDescription.value = true }) {
                        Text(
                            text = activityDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                true -> {
                    val newInputDescription = remember { mutableStateOf(
                        TextFieldValue(
                            text = activityDescription,
                            selection = TextRange(activityDescription.length),
                        )
                    )}

                    fun onDone() {
                        modifyDescription.value = false
                        onChangeCurrentActivityDescription(newInputDescription.value.text)
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    TextField(
                        value = newInputDescription.value,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true,
                        keyboardActions = KeyboardActions(onDone = { onDone() }),
                        onValueChange = { newInputDescription.value = it },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Text(text = "for the last")

        ElevatedCard {
            Text(
                text = formattedDuration,
                fontSize = 56.sp,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun NextActivityAnimatedButton(onClick: () -> Unit) {
    @Suppress("InfiniteTransitionLabel")
    val offsetY by rememberInfiniteTransition().animateFloat(
        label = "ButtonBoppingAnimation",
        initialValue = -10.dp.value,
        targetValue = 10.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    @Suppress("InfiniteTransitionLabel")
    val offsetAngle by rememberInfiniteTransition().animateFloat(
        label = "ButtonRotationAnimation",
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = offsetY.dp)
            .rotate(offsetAngle),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(128.dp) // Hack way to make huge button
        ) {
            Icon(
                painterResource(id = R.drawable.baseline_punch_clock_96),
                contentDescription = "Start next activity",
            )
        }
        Text(text = "Start next activity!", textAlign = TextAlign.Center)
    }
}

@Composable
fun ExportDialog(
    onDismissRequest: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        icon = { Icon(Icons.Default.Send, contentDescription = null) },
        title = { Text(
            text = "Export logged activities",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )},
        text = { Text(
            text = "Do you want to export all logged activities into a CSV file? The file will be created in your device's Downloads folder."
        )},
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onAccept
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
    )
}

@Preview
@Composable
fun PreviewExportDialog() {
    WhatDoinTheme {
        ExportDialog({}, {})
    }
}

@Composable
fun NewActivityDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    inputTextValue: String,
    onInputTextChange: (String) -> Unit,
    offsetValue: OffsetMinutes,
    onOffsetChange: (OffsetMinutes) -> Unit,
    isOffsetAvailable: (OffsetMinutes) -> Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    fun onCancel() {
        onDismiss()
        onInputTextChange("")
        onOffsetChange(OffsetMinutes.ZERO)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    @Composable
    fun OffsetRadioWithLabel(
        text: String,
        offset: OffsetMinutes,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = offsetValue == offset,
                onClick = { onOffsetChange(offset) },
                enabled = isOffsetAvailable(offset)
            )
            Text(text)
        }
    }

    Dialog(onDismissRequest = { onCancel() }) {
        ElevatedCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "What you goin' to do?",
                    style = MaterialTheme.typography.headlineSmall,
                )
                TextField(
                    value = inputTextValue,
                    singleLine = true,
                    onValueChange = { onInputTextChange(it) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(20.dp)
                )
                Text(
                    text = "Shoot! I forgot to log this when I started, it was actually:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start)
                {
                    OffsetRadioWithLabel(
                        text = "Now",
                        offset = OffsetMinutes.ZERO,
                    )
                    OffsetRadioWithLabel(
                        text = "5 minutes ago",
                        offset = OffsetMinutes.FIVE,
                    )
                    OffsetRadioWithLabel(
                        text = "10 minutes ago",
                        offset = OffsetMinutes.TEN,
                    )
                    OffsetRadioWithLabel(
                        text = "15 minutes ago",
                        offset = OffsetMinutes.FIFTEEN,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, bottom = 20.dp)
                ) {
                    TextButton(onClick = { onCancel() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onAccept) {
                        Text("Start!")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewNewActivityDialog() {
    WhatDoinTheme {
        NewActivityDialog(
            onDismiss = {},
            onAccept = {},
            onInputTextChange = {},
            inputTextValue = "user input",
            onOffsetChange = {},
            offsetValue = OffsetMinutes.TEN,
            isOffsetAvailable = { false }
        )
    }
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val openNewActivityDialog = remember { mutableStateOf(false) }
    val openExportDialog = remember { mutableStateOf(false) }
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        while(true) {
            homeViewModel.updateDurationAndUiState()
            delay(999)
        }
    }

    fun handleExportAndShowToast() = coroutineScope.launch {
            val writeSuccess = homeViewModel.exportAllToCSVFile()
            if (writeSuccess) {
                Toast.makeText(
                    context,
                    "All activities exported to Downloads folder",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Error occured in saving",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun handleCurrentActivityDescriptionChange(newDescription: String) {
        coroutineScope.launch {
            homeViewModel.updateCurrentActivityDescription(newDescription)
        }
    }

    HomeScreenBody(
        activityDescription = uiState.currentActivityDescription,
        formattedDuration = uiState.formattedActivityDuration,
        onExportClick = { openExportDialog.value = true },
        onStartNextActivityClick = { openNewActivityDialog.value = true },
        onChangeCurrentActivityDescription = { handleCurrentActivityDescriptionChange(it) },
    )

    when {
        openExportDialog.value -> {
            ExportDialog(
                onDismissRequest = { openExportDialog.value = false },
                onAccept = {
                    handleExportAndShowToast()
                    openExportDialog.value = false
                }
            )
        }
        openNewActivityDialog.value -> {
            LaunchedEffect(Unit) {
                homeViewModel.updateOffsetAvailabilityState()
            }

            NewActivityDialog(
                onDismiss = { openNewActivityDialog.value = false },
                onAccept = {
                    coroutineScope.launch {
                        homeViewModel.handleStartNextActivity()
                        openNewActivityDialog.value = false
                    }
                },
                onInputTextChange = homeViewModel::updateNextActivityInput,
                inputTextValue = uiState.nextActivityInput,
                onOffsetChange = homeViewModel::updateChosenOffset,
                offsetValue = uiState.nextActivityOffset,
                isOffsetAvailable = uiState.offsetsAvailable
            )
        }
    }
}

@Composable
fun HomeScreenBody(
    activityDescription: String,
    formattedDuration: String,
    onExportClick: () -> Unit,
    onStartNextActivityClick: () -> Unit,
    onChangeCurrentActivityDescription: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        ExportButton(onClick = onExportClick)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CurrentActivityStatusBody(
            activityDescription = activityDescription,
            formattedDuration = formattedDuration,
            onChangeCurrentActivityDescription = onChangeCurrentActivityDescription
        )
        Spacer(modifier = Modifier.size(128.dp))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        NextActivityAnimatedButton(onClick = onStartNextActivityClick)
        Spacer(modifier = Modifier.size(56.dp))
    }

}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    WhatDoinTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreenBody("placeholder activity", "1h 23min 45s", {}, {}, {})
        }
    }
}
