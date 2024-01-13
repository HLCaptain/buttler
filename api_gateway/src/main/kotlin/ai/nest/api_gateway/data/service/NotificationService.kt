package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.notification.NotificationDto
import ai.nest.api_gateway.data.model.notification.NotificationHistoryDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.ErrorHandler
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.util.Attributes
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class NotificationService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val errorHandler: ErrorHandler
) {
    suspend fun getUserToken(id: String, languageCode: String): List<String> {
        return client.tryToExecute<List<String>>(
            APIs.NOTIFICATION_API, attributes = attributes, setErrorMessage = { errorCodes ->
                errorHandler.getLocalizedErrorMessage(errorCodes, languageCode)
            }
        ) {
            get("tokens/user/$id")
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun getAllUsersTokens(ids: List<String>, languageCode: String) {
        client.tryToExecute<List<String>>(
            APIs.NOTIFICATION_API, attributes = attributes, setErrorMessage = { errorCodes ->
                errorHandler.getLocalizedErrorMessage(errorCodes, languageCode)
            }
        ) {
            post("tokens/users") {
                body = Json.encodeToString(ListSerializer(String.serializer()), ids)
            }
        }
    }

    suspend fun deleteNotificationCollection(): Boolean {
        return client.tryToExecute<Boolean>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            method = { delete("notifications/deleteCollection") }
        )
    }

    suspend fun saveToken(userId: String, token: String, languageCode: String): Boolean {
        return client.tryToExecute<Boolean>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) },
            method = { post("tokens/save-token/$userId?token=$token") }
        )
    }

    suspend fun deleteDeviceToken(userId: String, token: String, languageCode: String): Boolean {
        return client.tryToExecute<Boolean>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) },
            method = { delete("device/token/$userId?deviceToken=$token") }
        )
    }

    suspend fun clearDevicesTokens(userId: String, languageCode: String): Boolean {
        return client.tryToExecute<Boolean>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) },
            method = { delete("device/allTokens/$userId") }
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun sendNotificationToUser(notificationDto: NotificationDto, languageCode: String): Boolean {
        return client.tryToExecute<Boolean>(
            api = APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) }
        ) {
            post("notifications/send/user") {
                body = Json.encodeToString(NotificationDto.serializer(), notificationDto)
            }
        }
    }

    suspend fun getNotificationHistoryForUser(
        userId: String,
        page: String,
        limit: String,
        languageCode: String
    ): PaginationResponse<NotificationHistoryDto> {
        return client.tryToExecute<PaginationResponse<NotificationHistoryDto>>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) }
        ) {
            get("notifications/history/$userId") {
                parameter("page", page)
                parameter("limit", limit)
            }
        }
    }

    suspend fun getNotificationHistoryForUserInLast24Hours(
        userId: String,
        languageCode: String
    ): List<NotificationHistoryDto> {
        return client.tryToExecute<List<NotificationHistoryDto>>(
            APIs.NOTIFICATION_API,
            attributes = attributes,
            setErrorMessage = { errorCodes -> errorHandler.getLocalizedErrorMessage(errorCodes, languageCode) },
            method = { get("notifications/history-24hours/$userId") }
        )
    }
}