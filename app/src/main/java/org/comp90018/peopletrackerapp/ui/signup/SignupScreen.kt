package org.comp90018.peopletrackerapp.ui.signup

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import org.comp90018.peopletrackerapp.models.User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import org.comp90018.peopletrackerapp.navigation.Routes
import org.comp90018.peopletrackerapp.ui.theme.PeopleTrackerTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading

    val snackbarHostState = remember {SnackbarHostState()}
    val focusManager = LocalFocusManager.current

    if(viewModel.hasLoggedIn()) {
        navController.navigate(Routes.Home.route) {
            launchSingleTop = true
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    PeopleTrackerTheme {
        Surface {
            Scaffold (
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(36.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                        modifier = Modifier
                            .padding(bottom = 36.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Column {
                        TextField(
                            value = uiState.firstName,
                            onValueChange = { viewModel.onFirstnameChange(it) },
                            label = { Text("First name") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next,
                                autoCorrect = false
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .fillMaxWidth()
                        )

                        TextField(
                            value = uiState.lastName,
                            onValueChange = { viewModel.onLastnameChange(it) },
                            label = { Text("Last name") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next,
                                autoCorrect = false
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .fillMaxWidth()
                        )

                        TextField(
                            value = uiState.username,
                            onValueChange = { viewModel.onUsernameChange(it) },
                            label = { Text("Username") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next,
                                autoCorrect = false
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .fillMaxWidth()
                        )

                        TextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                autoCorrect = false
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .fillMaxWidth()
                        )

                        TextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = { Text("Password") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {focusManager.clearFocus()}
                            ),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .fillMaxWidth()
                        )
                    }

                    // Submit/cancel buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.onSignUpClick(navController) }
                            ) {
                                Text(text = "Submit",)
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(Routes.Start.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Cancel",)
                            }
                        }
                    }
                }
            }

        }
    }
}