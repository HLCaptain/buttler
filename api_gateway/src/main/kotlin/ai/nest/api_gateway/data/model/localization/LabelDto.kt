package ai.nest.api_gateway.data.model.localization

import kotlinx.serialization.Serializable

@Serializable
data class LabelDto(
    val id: Int,
    val localizedLabel: String? = null,
)
