/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eye.cool.scan.util

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.util.Log
import com.eye.cool.scan.R
import java.io.Closeable
import java.io.IOException

internal class BeepManager(
    private val context: Context
) : OnCompletionListener, MediaPlayer.OnErrorListener, Closeable {

  private var mediaPlayer: MediaPlayer? = null
  private var beepAble = false

  @Synchronized
  fun updatePrefs() {
    beepAble = shouldBeep(context)
    if (beepAble && mediaPlayer == null) {
      // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
      // so we now play on the music stream.
      if (context is Activity) {
        context.volumeControlStream = AudioManager.STREAM_MUSIC
      }
      mediaPlayer = buildMediaPlayer(context)
    }
  }

  @Synchronized
  fun playBeepSound() {
    if (beepAble && mediaPlayer != null) {
      mediaPlayer!!.start()
    }
  }

  private fun buildMediaPlayer(context: Context): MediaPlayer? {
    val mediaPlayer = MediaPlayer()
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
    mediaPlayer.setOnCompletionListener(this)
    mediaPlayer.setOnErrorListener(this)
    return try {
      val file = context.resources.openRawResourceFd(R.raw.scan_beep)
      try {
        mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file
            .length)
      } finally {
        file.close()
      }
      mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
      mediaPlayer.prepare()
      mediaPlayer
    } catch (ioe: IOException) {
      Log.w(TAG, ioe)
      mediaPlayer.release()
      null
    }
  }

  override fun onCompletion(mp: MediaPlayer) {
    // When the beep has finished playing, rewind to queue up another one.
    mp.seekTo(0)
  }

  @Synchronized
  override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
    if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
      // we are finished, so put up an appropriate error toast if required and finish
      // context.finish();
      // fixme ignore
    } else {
      // possibly media player error, so release and recreate
      mp.release()
      mediaPlayer = null
      updatePrefs()
    }
    return true
  }

  @Synchronized
  override fun close() {
    if (mediaPlayer != null) {
      mediaPlayer!!.release()
      mediaPlayer = null
    }
  }

  companion object {
    private val TAG = BeepManager::class.java.simpleName
    private const val BEEP_VOLUME = 0.20f
    private fun shouldBeep(context: Context): Boolean {
      var shouldPlayBeep = true
      // See if sound settings overrides this
      val audioService = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
        shouldPlayBeep = false
      }
      return shouldPlayBeep
    }
  }

  init {
    updatePrefs()
  }
}