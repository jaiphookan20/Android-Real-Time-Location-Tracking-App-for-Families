package org.comp90018.peopletrackerapp.ui.circles

//import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.comp90018.peopletrackerapp.R
import org.comp90018.peopletrackerapp.models.Circle
import org.comp90018.peopletrackerapp.navigation.Routes

@Composable
fun ExpandableFAB(
    navController: NavController
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = (-16).dp,
                y = (-16).dp
            )
    ) {
        if (isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding( vertical = 10.dp ),
                ) {
                    Text(text = "Create Group", modifier=Modifier.padding(horizontal=3.dp))
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.CreateCircle.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "create group button"
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding( vertical = 5.dp)
                ) {
                    Text(text = "Join Group", modifier = Modifier.padding(horizontal=3.dp))
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.JoinCircle.route) }
                    ) {
                    Icon(
                        imageVector = Icons.Filled.SportsKabaddi,
                        contentDescription = "join group button")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
            }
        ) {
            if(isExpanded) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "open circle menu"
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "open circle menu"
                )
            }
        }
    }
}

@Composable
fun CircleScreen(
    navController: NavController,
    viewModel: CircleViewModel = hiltViewModel()
) {
    val circles = viewModel.circles.collectAsStateWithLifecycle(null)
    val uiState by viewModel.uiState

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Circles",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            fontWeight = FontWeight.Bold
        )
        if(circles.value == null) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if(circles.value!!.isEmpty()) {
                Text("Join or create a circle!")
            } else {
                LazyColumn {
                    Log.d("CIRCLES", circles.toString())
                    items(circles.value!!) { circle ->
                        CircleItem(circle)
                    }
                }
            }
        }
        ExpandableFAB(navController)
    }
    if(uiState.isDialogVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Dialog(
                onDismissRequest = { viewModel.onAlertDismiss() },
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                "${uiState.selectedCircle!!.name}",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "Invite Code: ${uiState.selectedCircle!!.inviteCode}",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            items(uiState.circleMembers) { member ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = stringResource(id = R.string.profile_icon_desc),
                                        tint = Color.Gray,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        text = member.username ?: "Unknown",
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleItem(
    circle: Circle,
    viewModel: CircleViewModel = hiltViewModel()
){
    val members = viewModel.getMemberNames(circle.members).collectAsStateWithLifecycle(emptyList())
    Log.d("Circle", members.toString())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 8.dp
            )
    ) {
        Card(
            modifier = Modifier
                .padding(
                    end = 24.dp,
                    start = 24.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
                .clickable {
                    viewModel.onCircleClick(circle, members.value)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${circle.name}",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                )
                Text(
                    text = "${circle.members.size}",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
            }
        }
    }
}