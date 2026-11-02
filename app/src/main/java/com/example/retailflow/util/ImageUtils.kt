package com.example.retailflow.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Manejo de la foto de perfil (recurso nativo del dispositivo: cámara / galería).
 * Todo el trabajo pesado (leer, decodificar, comprimir, escribir) corre en Dispatchers.IO.
 */
object ImageUtils {
    private const val MAX_SIDE_PX = 640

    private fun avatarsDir(context: Context): File =
        File(context.filesDir, "avatars").apply { mkdirs() }

    /** Archivo temporal donde la app de cámara escribe la foto. */
    fun cameraTempFile(context: Context): File =
        File(avatarsDir(context), "camera_tmp.jpg")

    /**
     * Uri "content://" segura para pasársela a la app de cámara (Android prohíbe pasar file://).
     * La autoridad debe coincidir con la declarada en AndroidManifest.xml.
     */
    fun cameraTempUri(context: Context): Uri {
        val file = cameraTempFile(context)
        if (!file.exists()) file.createNewFile()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Lee la imagen de [source] (galería o cámara), la reduce, corrige su rotación EXIF
     * y la guarda como JPG en el almacenamiento interno. Devuelve la ruta o null si falló.
     * Se usa un nombre único para que la UI detecte el cambio y recargue la imagen.
     */
    suspend fun saveAvatar(context: Context, source: Uri, userId: Int): String? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver

                // 1) Leer solo las dimensiones (sin cargar la imagen completa en memoria)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                val boundsStream = resolver.openInputStream(source) ?: return@withContext null
                boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

                // 2) Decodificar reducida (inSampleSize potencia de 2)
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
                }
                val imageStream = resolver.openInputStream(source) ?: return@withContext null
                val decoded = imageStream.use { BitmapFactory.decodeStream(it, null, options) }
                    ?: return@withContext null

                // 3) Rotar según EXIF (las fotos de cámara suelen venir "de lado")
                val degrees = resolver.openInputStream(source)?.use { readRotationDegrees(it) } ?: 0
                val finalBitmap = if (degrees != 0) {
                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }

                // 4) Guardar como JPG con nombre único
                val dest = File(avatarsDir(context), "user_${userId}_${System.currentTimeMillis()}.jpg")
                dest.outputStream().use { finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                dest.absolutePath
            } catch (e: Exception) {
                null
            }
        }

    suspend fun loadBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteFile(path: String?) {
        if (path == null) return
        withContext(Dispatchers.IO) { runCatching { File(path).delete() } }
    }

    fun deleteCameraTemp(context: Context) {
        runCatching { cameraTempFile(context).delete() }
    }

    private fun sampleSizeFor(width: Int, height: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= MAX_SIDE_PX && h / 2 >= MAX_SIDE_PX) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun readRotationDegrees(input: InputStream): Int {
        val exif = ExifInterface(input)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
