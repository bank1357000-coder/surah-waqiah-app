package com.alwaqiah.quran.audio

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Binds the app to [PlaybackService] through a MediaController. Connecting starts the service,
 * which owns the MediaSession and lets Media3 promote it to a foreground service while playing.
 * Main thread only.
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var future: ListenableFuture<MediaController>? = null

    suspend fun ensureConnected(): MediaController {
        val existing = future
        if (existing != null && existing.isDone) {
            val controller = runCatching { existing.get() }.getOrNull()
            if (controller != null && controller.isConnected) return controller
            controller?.release()
            future = null
        }
        val pending = future ?: MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java)),
        ).buildAsync().also { future = it }

        return suspendCancellableCoroutine { cont ->
            pending.addListener({
                runCatching { pending.get() }
                    .onSuccess { cont.resume(it) }
                    .onFailure {
                        future = null
                        cont.resumeWithException(it)
                    }
            }, ContextCompat.getMainExecutor(context))
        }
    }
}
