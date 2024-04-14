package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ChatKtorDataSource(
    private val client: HttpClient
) : ChatNetworkDataSource {
    private var newChatsStateFlow = MutableStateFlow<List<ChatDto>?>(null)
    private var isLoadingNewChatsWebSocketSession = false
    private suspend fun createNewChatsFlow() {
        Napier.v { "Receiving new chats" }
        client.webSocket("/chats") { // UserID is sent with JWT
            incoming.receiveAsFlow().collect { _ ->
                Napier.v { "Received new chat" }
                newChatsStateFlow.update { receiveDeserialized<List<ChatDto>>() }
            }
        }
    }

    override fun fetchNewChats(): Flow<List<ChatDto>> {
        return if (newChatsStateFlow.value == null && !isLoadingNewChatsWebSocketSession) {
            isLoadingNewChatsWebSocketSession = true
            flow {
                createNewChatsFlow()
                emitAll(newChatsStateFlow)
            }
        } else {
            newChatsStateFlow
        }.map { it ?: emptyList() }
    }

    override suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto> {
        return client.get("/chats") {
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body()
    }

    override suspend fun fetch(): List<ChatDto> {
        return client.get("/chats").body()
    }

    override suspend fun fetchByModel(modelUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        return if (chat.id == null) {
            client.post("/chats") { setBody(chat) }
        } else {
            client.put("/chats/${chat.id}") { setBody(chat) }
        }.body()
    }

    override suspend fun delete(uuid: String): Boolean {
        return client.delete("/chats/$uuid").status.isSuccess()
    }
}