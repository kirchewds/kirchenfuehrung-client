package de.kirchewds.kirchenfuehrung.client.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil.imageLoader
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future

@UnstableApi
class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope
): BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = scope.future(Dispatchers.IO) {
        BitmapFactory.decodeByteArray(data, 0, data.size) ?: error("Could not decode image data")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> = scope.future(Dispatchers.IO) {
        val result = context.imageLoader.execute(uri.asRequest(context))
        (result.drawable as BitmapDrawable).bitmap
    }

    fun preload(uri: Uri) = preload(context, uri)

    companion object {
        fun preload(context: Context, uri: Uri) {
            context.imageLoader.enqueue(uri.asRequest(context))
        }
        private fun Uri.asRequest(context: Context) = ImageRequest.Builder(context).data(this).build()
    }
}