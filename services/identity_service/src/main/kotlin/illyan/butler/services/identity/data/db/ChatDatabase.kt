package illyan.butler.services.identity.data.db

import illyan.butler.services.identity.data.model.chat.ChatDto
import illyan.butler.services.identity.data.utils.getLastMonthDate
import illyan.butler.services.identity.data.utils.getLastWeekDate
import kotlinx.datetime.Clock

interface ChatDatabase {
    suspend fun getChat(userId: String, chatId: String): ChatDto
    suspend fun createChat(userId: String, chat: ChatDto): ChatDto
    suspend fun editChat(userId: String, chat: ChatDto)
    suspend fun deleteChat(userId: String, chatId: String): Boolean
    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )
    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )
    suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    suspend fun getChats(userId: String, fromDate: Long, toDate: Long = Clock.System.now().toEpochMilliseconds()): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto>
}