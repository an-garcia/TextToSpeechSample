/*
 * Copyright (C) 2018 Angel Newton
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
package com.xengar.android.texttospeechsample.utils

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.xengar.android.texttospeechsample.R
import com.xengar.android.texttospeechsample.utils.Constants.LOG
import com.xengar.android.texttospeechsample.ui.SettingsActivity
import com.xengar.android.texttospeechsample.utils.Constants.DEFAULT_FONT_SIZE
import com.xengar.android.texttospeechsample.utils.Constants.DEFAULT_TTS_LOCALE
import java.util.*

/**
 * ActivityUtils
 */
object ActivityUtils {


    /**
     * Configures the language in the speech object.
     * @param tts TextToSpeech
     */
    fun configureTextToSpeechLanguage(tts: TextToSpeech?, status: Int, locale: Locale) {
        if (status == TextToSpeech.SUCCESS) {
            // TODO: Use the available languages and select one in app settings
            val result = tts!!.setLanguage(locale)
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


    /**
     * Launches Settings Activity.
     * @param context context
     */
    fun launchSettingsActivity(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        //intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
        //        SettingsActivity.GeneralPreferenceFragment::class.java.name)
        //intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
        //intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.settings)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    /**
     * Returns the value of show definitions from preferences.
     * @param context context
     * @return boolean or default(true)
     */
    fun getPreferenceFontSize(context: Context): String {
        val key = context.getString(R.string.pref_font_size)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, DEFAULT_FONT_SIZE)
    }

    /**
     * Returns the value of text to speech locale from preferences.
     * @param context context
     * @return String
     */
    fun getPreferenceTextToSpeechLocale(context: Context): String {
        val key = context.getString(R.string.pref_text_to_speech_locale)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, DEFAULT_TTS_LOCALE)
    }
    /**
     * Returns the local from a string.
     * @param localStr preference local string in "language, country" 3 letter codes.
     * @return Locale
     */
    fun getTextToSpeechLocale(localStr: String): Locale {
        val values = localStr.split(", ")
        return if (values.size == 2) {
            when {
                values[1].contentEquals("GBR") -> Locale.UK
                values[0].contentEquals("en")
                        || values[1].contentEquals("USA") -> Locale.US
                else -> Locale(values[0], values[1])
            }
        } else {
            Locale.US
        }
    }
}