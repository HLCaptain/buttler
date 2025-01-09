package illyan.butler.ui.chat_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.chat.ChatManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatDetailsViewModel(
    private val chatManager: ChatManager
) : ViewModel() {
    private val _currentChatId = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _currentChat = _currentChatId.flatMapLatest { chatId ->
        chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state = _currentChat.map { ChatDetailsState(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatDetailsState())

    fun loadChat(chatId: String?) {
        _currentChatId.update { chatId }
    }
}