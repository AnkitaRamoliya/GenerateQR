package com.androideditors.qrstudio

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/** Android 10+; saves to Pictures/QR Studio without storage permissions. */
object QrImageStorage {

    fun saveToGallery(context: Context, bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val imageName = "QR_${System.currentTimeMillis()}.png"
        val metadata = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/QR Studio")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, metadata)
            ?: throw IOException("Could not create the image in MediaStore.")

        try {
            val output = resolver.openOutputStream(uri)
                ?: throw IOException("Could not open the image for writing.")
            output.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("PNG encoding failed.")
                }
            }
            val publish = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            check(resolver.update(uri, publish, null, null) > 0) {
                "Could not publish saved image."
            }
            return uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    /** Cache a temporary PNG then return a read-only shareable content:// URI. */
    fun createShareUri(context: Context, bitmap: Bitmap): Uri {
        val folder = File(context.cacheDir, "shared").apply {
            if (!exists() && !mkdirs()) throw IOException("Could not create share folder.")
        }
        // Remove only older app-generated temporary images.
        val cutoff = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
        folder.listFiles()?.filter {
            it.name.startsWith("QR_share_") && it.lastModified() < cutoff
        }?.forEach { it.delete() }

        val imageFile = File(folder, "QR_share_${System.currentTimeMillis()}.png")
        imageFile.outputStream().use { stream ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw IOException("PNG encoding failed.")
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun share(context: Context, uri: Uri) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("QR image", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share QR code")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooser)
    }
}
