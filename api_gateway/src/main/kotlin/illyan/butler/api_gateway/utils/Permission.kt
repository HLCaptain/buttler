package illyan.butler.api_gateway.utils

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    END_USER,
    ADMIN
}