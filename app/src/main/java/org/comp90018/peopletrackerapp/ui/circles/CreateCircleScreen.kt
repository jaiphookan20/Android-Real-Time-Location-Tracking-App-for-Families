package org.comp90018.peopletrackerapp.ui.circles

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCircleScreen(
    navController: NavController,
    viewModel: CircleViewModel = hiltViewModel()
) {

    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading

    PeopleTrackerTheme {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Create a circle",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "go back"
                                )
                            }
                        },
                        actions = {}
                    )
                }
            ) { _ ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(36.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "placeholder to fix spacing!",
                        modifier = Modifier.alpha(0f)
                    )
                    Column {
                        // Text field to input circle name
                        TextField(
                            value = uiState.circleName,
                            onValueChange = { viewModel.onCircleNameChange(it) },
                            label = { Text("Circle Name") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.size(250.dp))

                        // Button column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    32.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            if (isLoading) {
                                CircularProgressIndicator()
                            }
                            else {
                                // Button to create the circle
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        viewModel.onCreateCircle(navController)
                                    }
                                ) {
                                    Text(text = "Create circle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}