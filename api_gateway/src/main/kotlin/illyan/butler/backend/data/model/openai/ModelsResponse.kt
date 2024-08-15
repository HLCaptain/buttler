package illyan.butler.backend.data.model.openai

import illyan.butler.backend.data.model.openai.Model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//{"object":"list","data":[{"id":"phi-2","object":"model"},{"id":"open-llama-13b-open-instruct.ggmlv3.q3_K_M.bin","object":"model"}]}
@Serializable
data class ModelsResponse(
    @SerialName("object") val responseObject: String,
    val data: List<Model>
)
