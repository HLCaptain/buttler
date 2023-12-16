package illyan.butler.ui.chat

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.Res
import illyan.butler.domain.model.ChatMessage
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import org.koin.core.parameter.parametersOf

class ChatScreen(private val chatUUID: String) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ChatScreenModel> { parametersOf(chatUUID) }
        val chat by screenModel.chat.collectAsState()
        LaunchedEffect(Unit) { Napier.d("ChatScreen: $chat") }
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .imePadding()
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = chat?.name ?: Res.string.new_chat,
                style = MaterialTheme.typography.headlineLarge
            )
            MessageList(
                modifier = Modifier.weight(1f, fill = true),
                chat = chat
            )
            MessageField(sendMessage = screenModel::sendMessage)
        }
    }

    @Composable
    fun MessageList(
        modifier: Modifier = Modifier,
        chat: DomainChat?,
    ) {
        Crossfade(
            modifier = modifier,
            targetState = chat == null || chat.messages.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (it) {
                    Text(
                        text = Res.string.no_messages,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                LazyColumn(
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(chat?.messages ?: emptyList()) { message ->
                        MessageItem(message = message)
                    }
                }
            }
        }
    }

    @Composable
    fun MessageItem(
        message: ChatMessage,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = if (message.role == ChatMessage.UserRole) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.role.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium
            )
            Card {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = message.message
                )
            }
        }
    }

    @Composable
    fun MessageField(
        modifier: Modifier = Modifier,
        sendMessage: (String) -> Unit,
    ) {
        Card(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var textMessage by rememberSaveable { mutableStateOf("") }
                TextField(
                    modifier = Modifier.weight(1f, fill = true),
                    value = textMessage,
                    onValueChange = { textMessage = it },
                )

                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        sendMessage(textMessage)
                        textMessage = ""
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = Res.string.send,
                        tint =  MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}