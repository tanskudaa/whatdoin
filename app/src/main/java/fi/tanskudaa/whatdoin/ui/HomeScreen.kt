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
import androidx.compose.runtime.setValue
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
    onClickDescriptionText: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "You've been doin'")

        ElevatedCard {
            TextButton(onClick = onClickDescriptionText) {
                Text(
                    text = activityDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
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
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
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
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onAccept
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
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
fun ChangeCurrentDescriptionDialog(
    onDismiss: () -> Unit,
    initialDescriptionText: String,
    updateCurrentActivityDescription: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var inputTextValue by remember { mutableStateOf(TextFieldValue(
        text = initialDescriptionText,
        selection = TextRange(initialDescriptionText.length)
    ))}

    fun onAccept() {
        onDismiss()
        updateCurrentActivityDescription(inputTextValue.text)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Change current activity description",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = inputTextValue,
                    singleLine = true,
                    onValueChange = { inputTextValue = it },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(20.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, bottom = 20.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onAccept() }) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewChangeCurrentDescriptionDialog() {
    WhatDoinTheme {
        ChangeCurrentDescriptionDialog({}, "", {})
    }
}

@Composable
fun NewActivityDialog(
    onDismiss: () -> Unit,
    switchToNewActivity: (String, OffsetMinutes) -> Unit,
    isOffsetAvailable: (OffsetMinutes) -> Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    var textInputValue by remember { mutableStateOf(TextFieldValue()) }
    var offsetInputValue by remember { mutableStateOf(OffsetMinutes.ZERO) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun isFormValid() =
        isOffsetAvailable(offsetInputValue) && textInputValue.text.length >= 3

    fun isUserTooFast() = !isOffsetAvailable(offsetInputValue)

    fun onAccept() {
        onDismiss()
        switchToNewActivity(textInputValue.text, offsetInputValue)
    }

    @Composable
    fun OffsetRadioWithLabel(
        text: String,
        offset: OffsetMinutes,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = offsetInputValue == offset,
                onClick = { offsetInputValue = offset },
                enabled = isOffsetAvailable(offset)
            )
            Text(text)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                    value = textInputValue,
                    singleLine = true,
                    onValueChange = { textInputValue = it },
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = { onAccept() },
                        enabled = isFormValid()
                    ) {
                        Text(
                            text = if (!isUserTooFast())
                                "Start!"
                            else
                                "Slow down!"
                        )
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
            switchToNewActivity = { _, _ -> },
            isOffsetAvailable = { true }
        )
    }
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by homeViewModel.uiState.collectAsState()

    var openExportDialog by remember { mutableStateOf(false) }
    var openChangeDescriptionDialog by remember { mutableStateOf(false) }
    var openNewActivityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            homeViewModel.updateDurationAndUiState()
            delay(999)
        }
    }

    fun handleExportAndShowToast() = coroutineScope.launch {
            val writeSuccess = homeViewModel.exportAllToCSVFile()
            val toastMessageText =
                if (writeSuccess)
                    "All activities exported to Downloads folder"
                else
                    "Error occured during saving"

            Toast.makeText(
                context,
                toastMessageText,
                Toast.LENGTH_LONG
            ).show()
        }

    HomeScreenStateless(
        currentActivityDescription = uiState.currentActivityDescription,
        formattedOngoingDuration = uiState.formattedActivityDuration,
        onExportClick = { openExportDialog = true },
        onStartNextActivityClick = { openNewActivityDialog = true },
        onDescriptionTextClick = { openChangeDescriptionDialog = true },
    )

    when {
        openExportDialog -> {
            ExportDialog(
                onDismiss = {
                    openExportDialog = false
                },
                onAccept = {
                    handleExportAndShowToast()
                    openExportDialog = false
                }
            )
        }
        openChangeDescriptionDialog -> {
            ChangeCurrentDescriptionDialog(
                onDismiss = { openChangeDescriptionDialog = false },
                initialDescriptionText = uiState.currentActivityDescription,
                updateCurrentActivityDescription = {
                    coroutineScope.launch {
                        homeViewModel.updateCurrentActivityDescription(it)
                    }
                }
            )
        }
        openNewActivityDialog -> {
            NewActivityDialog(
                onDismiss = { openNewActivityDialog = false },
                switchToNewActivity = { newDescription, offset ->
                    coroutineScope.launch {
                        homeViewModel.switchToNewActivity(newDescription, offset)
                    }
                },
                isOffsetAvailable = homeViewModel.getOffsetAvailability(),
            )
        }
    }
}

@Composable
fun HomeScreenStateless(
    currentActivityDescription: String,
    formattedOngoingDuration: String,
    onExportClick: () -> Unit,
    onStartNextActivityClick: () -> Unit,
    onDescriptionTextClick: () -> Unit,
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
            activityDescription = currentActivityDescription,
            formattedDuration = formattedOngoingDuration,
            onClickDescriptionText = onDescriptionTextClick,
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
            HomeScreenStateless("placeholder activity", "1h 23min 45s", {}, {}, {})
        }
    }
}
