package illyan.butler.services.ai.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
fun provideDispatcher() = Dispatchers.IO

@Single
fun provideCoroutineScope(dispatcher: CoroutineDispatcher) = CoroutineScope(dispatcher)
