package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.error.ErrorManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class HomeViewModel(
    authManager: AuthManager,
    private val chatManager: ChatManager,
    errorManager: ErrorManager,
) : ViewModel() {
    private val _serverErrors = MutableStateFlow<List<Pair<String, DomainErrorResponse>>>(listOf())
    private val _appErrors = MutableStateFlow<List<DomainErrorEvent>>(listOf())

    val state = combine(
        authManager.signedInUserId,
        authManager.clientId,
        _serverErrors,
        _appErrors,
        chatManager.userChats,
        chatManager.deviceChats
    ) { flows ->
        val signedInUserId = flows[0] as String?
        val clientId = flows[1] as String?
        val serverErrors = flows[2] as List<Pair<String, DomainErrorResponse>>
        val appErrors = flows[3] as List<DomainErrorEvent>
        val userChats = flows[4] as List<DomainChat>
        val deviceChats = flows[5] as List<DomainChat>
        Napier.v {
            """
            HomeViewModel:
            signedInUserId: $signedInUserId
            clientId: $clientId
            serverErrors: $serverErrors
            appErrors: $appErrors
            userChats: $userChats
            deviceChats: $deviceChats
            """.trimIndent()
        }
        HomeState(
            signedInUserId = signedInUserId,
            clientId = clientId,
            serverErrors = serverErrors,
            appErrors = appErrors,
            userChats = userChats,
            deviceChats = deviceChats
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            errorManager.serverErrors.collectLatest { response ->
                _serverErrors.update { it + (Uuid.random().toString() to response) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            errorManager.appErrors.collectLatest { error ->
                _appErrors.update { it + error }
            }
        }
    }

    fun clearError(id: String) {
        _serverErrors.update { errors ->
            errors.filter { it.first != id }
        }
        _appErrors.update { errors ->
            errors.filter { it.id != id }
        }
    }

    fun removeLastError() {
        viewModelScope.launch(Dispatchers.IO) {
            val latestServerErrorId = _serverErrors.first().maxByOrNull { it.second.timestamp }?.first
            val latestAppErrorId = _appErrors.first().maxByOrNull { it.timestamp }?.id
            if (latestServerErrorId != null && latestAppErrorId != null) {
                if (latestServerErrorId > latestAppErrorId) {
                    clearError(latestServerErrorId)
                } else {
                    clearError(latestAppErrorId)
                }
            } else if (latestServerErrorId != null) {
                clearError(latestServerErrorId)
            } else if (latestAppErrorId != null) {
                clearError(latestAppErrorId)
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatManager.deleteChat(chatId)
        }
    }
}
