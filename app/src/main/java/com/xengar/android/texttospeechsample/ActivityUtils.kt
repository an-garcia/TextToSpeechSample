package com.xengar.android.texttospeechsample

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import com.xengar.android.texttospeechsample.Constants.LOG
import java.util.*

/**
 * Created by xengar on 2018-03-15.
 */

object ActivityUtils {


    /**
     * Configures the language in the speech object.
     * @param tts TextToSpeech
     */
    fun configureTextToSpeechLanguage(tts: TextToSpeech?, status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // TODO: Use the available languages and select one in app settings
            val result = tts!!.setLanguage(Locale("spa", "MEX"))
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (LOG) {
                    Log.e("TTS", "This Language is not supported")
                }
            }
        } else {
            if (LOG) {
                Log.e("TTS", "Initilization Failed!")
            }
        }
    }

    /**
     * Text we want to speak.
     * @param text String
     */
    fun speak(context: Context, tts: TextToSpeech?, text: String?) {
        if (text == null || tts == null) {
            return
        }

        // Use the current media player volume
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

        // Speak
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
}