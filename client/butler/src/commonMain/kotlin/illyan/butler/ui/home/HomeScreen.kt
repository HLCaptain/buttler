package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.anonymous_user
import illyan.butler.generated.resources.app_name
import illyan.butler.generated.resources.butler_logo
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.hello_x
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.ui.arbitrary.ArbitraryScreen
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.ButlerErrorDialogContent
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.model_list.ModelListScreen
import illyan.butler.ui.onboarding.OnBoardingScreen
import illyan.butler.ui.profile.ProfileDialogScreen
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreen()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    internal fun HomeScreen() {
        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        Surface {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.headlineLarge
                    )

                    val isUserSignedIn by rememberSaveable { derivedStateOf { state.isUserSignedIn } }
                    val isTutorialDone by rememberSaveable { derivedStateOf { state.isTutorialDone } }
                    var isAuthFlowEnded by rememberSaveable { mutableStateOf(isUserSignedIn) }
                    var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
                    LaunchedEffect(isUserSignedIn) {
                        if (isUserSignedIn != true) isAuthFlowEnded = false
                        isProfileDialogShowing = false
                    }
                    var isDialogClosedAfterTutorial by rememberSaveable { mutableStateOf(isTutorialDone) }
                    val isDialogOpen by rememberSaveable {
                        derivedStateOf { isAuthFlowEnded != true || !isTutorialDone || isProfileDialogShowing }
                    }
                    LaunchedEffect(isTutorialDone) {
                        if (!isTutorialDone) isDialogClosedAfterTutorial = false
                        if (isUserSignedIn == true) isAuthFlowEnded = true
                    }
                    val startScreen by remember {
                        val onBoardingScreen by lazy { OnBoardingScreen() }
                        val profileDialogScreen by lazy { ProfileDialogScreen() }
                        val authScreen by lazy { AuthScreen() }
                        derivedStateOf {
                            if (!isDialogOpen) {
                                null
                            } else {
                                if (isTutorialDone && isDialogClosedAfterTutorial) {
                                    if (isAuthFlowEnded == true && isUserSignedIn == true && isProfileDialogShowing) profileDialogScreen else authScreen
                                } else onBoardingScreen
                            }
                        }
                    }

                    ButlerDialog(
                        startScreens = listOfNotNull(startScreen),
                        isDialogOpen = isDialogOpen,
                        isDialogFullscreen = isUserSignedIn != true || !isTutorialDone,
                        onDismissDialog = {
                            if (isUserSignedIn == true) {
                                isAuthFlowEnded = true
                            }
                            isProfileDialogShowing = false
                            // Log isDialogOpen variables
                            Napier.d("isDialogOpen: $isDialogOpen\nisAuthFlowEnded: $isAuthFlowEnded\nisTutorialDone: $isTutorialDone\nisProfileDialogShowing: $isProfileDialogShowing\nisDialogClosedAfterTutorial: $isDialogClosedAfterTutorial\nisUserSignedIn: $isUserSignedIn\n")
                        },
                        onDialogClosed = {
                            if (isTutorialDone) {
                                isDialogClosedAfterTutorial = true
                            }
                        }
                    )

                    Button(onClick = { isProfileDialogShowing = true }) {
                        Text(stringResource(Res.string.profile))
                    }

                    val numberOfErrors by rememberSaveable { derivedStateOf { state.appErrors.size + state.serverErrors.size } }
                    val errorScreen by remember { derivedStateOf {
                        ArbitraryScreen {
                            val serverErrorContent = @Composable {
                                state.serverErrors.maxByOrNull { it.second.timestamp }?.let {
                                    ButlerErrorDialogContent(
                                        errorResponse = it.second,
                                        onClose = { screenModel.clearError(it.first) }
                                    )
                                }
                            }
                            val appErrorContent = @Composable {
                                state.appErrors.maxByOrNull { it.timestamp }?.let {
                                    ButlerErrorDialogContent(
                                        errorEvent = it,
                                        onClose = { screenModel.clearError(it.id) }
                                    )
                                }
                            }
                            Crossfade(
                                modifier = Modifier.animateContentSize(spring()),
                                targetState = state.appErrors + state.serverErrors
                            ) { _ ->
                                val latestAppError = state.appErrors.maxByOrNull { it.timestamp }
                                val latestServerError = state.serverErrors.maxByOrNull { it.second.timestamp }
                                if (latestAppError != null && latestServerError != null) {
                                    if (latestAppError.timestamp > latestServerError.second.timestamp) {
                                        appErrorContent()
                                    } else {
                                        serverErrorContent()
                                    }
                                } else if (latestAppError != null) {
                                    appErrorContent()
                                } else if (latestServerError != null) {
                                    serverErrorContent()
                                }
                            }
                        }
                    } }
                    ButlerDialog(
                        modifier = Modifier.zIndex(1f),
                        startScreens = listOf(errorScreen),
                        isDialogOpen = numberOfErrors > 0,
                        isDialogFullscreen = false,
                        onDismissDialog = screenModel::removeLastError
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    item {
                        Image(
                            painter = painterResource(Res.drawable.butler_logo),
                            contentDescription = "Butler logo",
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    item {
                        val signedInUserUUID = state.signedInUserUUID
                        val navigator = LocalNavigator.currentOrThrow
                        AnimatedVisibility(
                            visible = signedInUserUUID != null
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.hello_x, signedInUserUUID?.take(8) ?: Res.string.anonymous_user),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                MenuButton(
                                    text = stringResource(Res.string.chats),
                                    onClick = { navigator.push(ChatListScreen()) }
                                )
                                MenuButton(
                                    text = stringResource(Res.string.new_chat),
                                    onClick = { navigator.push(ModelListScreen()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}