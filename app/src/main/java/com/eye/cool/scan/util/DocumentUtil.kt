package com.eye.cool.scan.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import kotlin.math.ceil

internal object DocumentUtil {

  private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
  }

  private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
  }

  private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
  }

  private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
  }

  private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                            selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = MediaColumns.DATA
    val projection = arrayOf(column)
    try {
      cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
      if (cursor != null && cursor.moveToFirst()) {
        val index = cursor.getColumnIndexOrThrow(column)
        return cursor.getString(index)
      }
    } finally {
      cursor?.close()
    }
    return null
  }

  fun getPath(context: Context, uri: Uri): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
          return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        }
      } else if (isDownloadsDocument(uri)) {
        val id = DocumentsContract.getDocumentId(uri)
        val contentUri = ContentUris
            .withAppendedId(Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(id))
        return getDataColumn(context, contentUri, null, null)
      } else if (isMediaDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
        when (type) {
          "image" -> {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
          }
          "video" -> {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
          }
          "audio" -> {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
          }
        }
        val selection = MediaColumns._ID + "=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
      }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
      return if (isGooglePhotosUri(uri)) {
        uri.lastPathSegment
      } else getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
      return uri.path
    }
    return null
  }

  fun getBitmap(fileName: String?): Bitmap? {
    var bitmap: Bitmap? = null
    try {
      val options = BitmapFactory.Options()
      options.inJustDecodeBounds = true
      BitmapFactory.decodeFile(fileName, options)
      options.inSampleSize = 1.coerceAtLeast(
          ceil(
              (options.outWidth.toDouble() / 1024f)
                  .coerceAtLeast(options.outHeight.toDouble() / 1024f)
          ).toInt()
      )
      options.inJustDecodeBounds = false
      bitmap = BitmapFactory.decodeFile(fileName, options)
    } catch (error: OutOfMemoryError) {
      error.printStackTrace()
    }
    return bitmap
  }
}