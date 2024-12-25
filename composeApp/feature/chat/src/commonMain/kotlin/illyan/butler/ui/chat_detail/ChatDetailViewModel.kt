package illyan.butler.ui.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.audio.AudioManager
import illyan.butler.audio.toWav
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import io.github.aakira.napier.Napier
import korlibs.audio.format.MP3
import korlibs.audio.sound.AudioData
import korlibs.time.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatDetailViewModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    private val audioManager: AudioManager,
) : ViewModel() {
    private val chatIdStateFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val chat = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isChatDeviceOnly = combine(chat, authManager.clientId) { chat, client -> chat?.ownerId == client }

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getMessagesByChatFlow(chatId) } ?: flowOf(emptyList()) }
        .map { messages -> messages.sortedBy { it.time }.reversed() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resources = messages.flatMapLatest { messages ->
        combine((messages).map { message ->
            chatManager.getResourcesByMessageFlow(message.id!!)
        }) { flows ->
            val resources = flows.toList().filterNotNull().flatten()
//            Napier.d("Resources: ${resources.map { resource -> resource?.id }}")
            resources
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val state = combine(
        chat,
        messages,
        audioManager.isRecording,
        audioManager.playingAudioId,
        resources,
    ) { flows ->
        val chat = flows[0] as? DomainChat
        val messages = flows[1] as? List<DomainMessage>
        val recording = flows[2] as? Boolean ?: false
        val playing = flows[3] as? String
        val resources = flows[4] as? List<DomainResource>
//        Napier.v("Gallery permission: $galleryPermission")
        Napier.v("Resources: $resources")
        val sounds = resources?.filter { it.type.startsWith("audio") }
            ?.associate {
                it.id!! to try { it.data.toAudioData(it.type)!!.totalTime.seconds.toFloat() } catch (e: Exception) { Napier.e(e) { "Audio file encode error for audio $it" }; 0f }
            } ?: emptyMap()
        val images = resources?.filter { it.type.startsWith("image") }
            ?.associate { it.id!! to it.data } ?: emptyMap()
        ChatDetailState(
            chat = chat,
            messages = messages,
            isRecording = recording,
            playingAudio = playing,
            sounds = sounds,
            images = images
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ChatDetailState()
    )

    fun loadChat(chatId: String) {
        chatIdStateFlow.update { chatId }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            chat.value?.let { chatManager.sendMessage(it.id!!, it.ownerId, message) }
        }
    }

    fun toggleRecording(senderId: String) {
        if (!audioManager.canRecordAudio) return
        viewModelScope.launch {
            if (state.value.isRecording) {
                val audioId = audioManager.stopRecording()
                chatIdStateFlow.value?.let { chatManager.sendAudioMessage(it, senderId, audioId) }
            } else {
                audioManager.startRecording()
            }
        }
    }

    fun sendImage(path: String, senderId: String) {
        viewModelScope.launch {
            chatIdStateFlow.value?.let {
                chatManager.sendImageMessage(it, senderId, path)
                Napier.d("Image sent: $path")
            }
        }
    }

    fun playAudio(audioId: String) {
        viewModelScope.launch {
            audioManager.playAudio(audioId)
        }
    }

    fun stopAudio() {
        viewModelScope.launch {
            audioManager.stopAudio()
        }
    }
}

private suspend fun ByteArray.toAudioData(mimeType: String): AudioData? {
    return when (mimeType) {
        "audio/wav" -> toWav()
        "audio/mp3" -> MP3.decode(this)
        else -> null
    }
}