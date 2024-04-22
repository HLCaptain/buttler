package illyan.butler.repository

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import kotlinx.coroutines.flow.StateFlow

interface AppRepository {
    val appSettings: StateFlow<AppSettings?>
    val firstSignInHappenedYet: StateFlow<Boolean>
    val isTutorialDone: StateFlow<Boolean>

    suspend fun setTutorialDone(isTutorialDone: Boolean)
    suspend fun setUserPreferences(preferences: DomainPreferences)
}