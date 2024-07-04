package illyan.butler.data.local.datasource

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow

interface ChatLocalDataSource {
    fun getChat(key: String): Flow<DomainChat?>
    fun getChatsByUser(userId: String): Flow<List<DomainChat>?>
    suspend fun upsertChat(chat: DomainChat)
    suspend fun deleteChatById(chatId: String)
    suspend fun deleteChatByUserId(userId: String)
    suspend fun deleteAllChats()
    suspend fun upsertChats(chats: List<DomainChat>)
}