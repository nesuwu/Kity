package com.nesu.kity

import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val listener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun onProgressUpdate(percentage: Int)
        fun onError(e: Exception)
    }

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0
        var fis: FileInputStream? = null

        try {
            fis = FileInputStream(file)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                val progress = ((uploaded.toDouble() / fileLength.toDouble()) * 100).toInt()
                listener.onProgressUpdate(progress)
            }
            sink.flush()
        } catch (e: Exception) {
            Log.e("ProgressRequestBody", "Error writing to sink or reading file", e)
            listener.onError(e)
            throw e
        } finally {
            fis?.close()
        }
    }
}