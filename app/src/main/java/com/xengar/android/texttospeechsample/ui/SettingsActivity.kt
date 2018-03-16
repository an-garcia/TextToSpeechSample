/*
 * Copyright (C) 2018 Angel Garcia
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
package com.xengar.android.texttospeechsample.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import com.xengar.android.texttospeechsample.R
import com.xengar.android.texttospeechsample.utils.ActivityUtils
import com.xengar.android.texttospeechsample.utils.Constants.LOG
import java.util.*
import kotlin.collections.ArrayList

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || NotificationPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {


        private val ACT_CHECK_TTS_DATA = 1000
        private var tts: TextToSpeech? = null

        private val sharedPrefsChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == getString(R.string.pref_font_size)) {
                        updateSummary()
                    } else if (key == getString(R.string.pref_text_to_speech_locale) ) {
                        // Notify MainActivity to change language
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                        startActivityForResult(intent, ACT_CHECK_TTS_DATA)
                    }
                }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"))
            bindPreferenceSummaryToValue(findPreference("example_list"))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_font_size)))

            // Check to see if we have TTS voice data
            val intent = Intent()
            intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            startActivityForResult(intent, ACT_CHECK_TTS_DATA)
        }


        /** Called when returning from startActivityForResult  */
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            if (requestCode == ACT_CHECK_TTS_DATA) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // data exists, so we instantiate the TTS engine
                    tts = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
                        ActivityUtils.configureTextToSpeechLanguage(tts, status, Locale.UK)
                        setLanguageOptions()
                    })


                } else {

                    val langPref = findPreference(getString(R.string.pref_text_to_speech_locale)) as ListPreference
                    langPref.entries = arrayOf("None detected")
                    langPref.entryValues = arrayOf("None")
                    if (langPref.value == null) {
                        langPref.setValueIndex(0)
                        langPref.summary = "None detected"
                    }
                    bindPreferenceSummaryToValue(langPref)


                    // data is missing, so we start the TTS installation process
                    val intent = Intent()
                    intent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivity(intent)
                }
            }
        }

        private fun setLanguageOptions() {

            val languagesAll: ArrayList<Locale> =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getSupportedLanguagesLollipop()
                    } else {
                        getSupportedLanguagesLegacy()
                    }
            // filter to English
            val languages: List<Locale> = languagesAll.filter { s -> s.toString().contains("en") }

            val entries     = arrayOfNulls<String>(languages.size)
            val entryValues = arrayOfNulls<String>(languages.size)
            val currentLocale = ActivityUtils.getPreferenceTextToSpeechLocale(activity)
            var currentLocaleIndex = 0
            for (i in languages.indices) {
                entries[i]     = languages[i].getDisplayName(languages[i])
                val code = languages[i].isO3Language + ", " + languages[i].isO3Country
                entryValues[i] = code
                if (currentLocale.equals(entryValues[i])) {
                    currentLocaleIndex = i
                }
            }

            val langPref = findPreference(getString(R.string.pref_text_to_speech_locale)) as ListPreference
            langPref.entries = entries
            langPref.entryValues = entryValues

            if (langPref.value == null) {
                langPref.setValueIndex(currentLocaleIndex)
                langPref.summary = currentLocale
            }
            bindPreferenceSummaryToValue(langPref)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getSupportedLanguagesLollipop() : ArrayList<Locale> {
            val languages: ArrayList<Locale> = ArrayList<Locale>()
            val availableLocales = tts?.getAvailableLanguages()!!
            for (locale in availableLocales) {
                languages.add(locale)
            }
            return languages
        }

        private fun getSupportedLanguagesLegacy() : ArrayList<Locale> {
            val languages: ArrayList<Locale> = ArrayList<Locale>()
            val allLocales = Locale.getAvailableLocales()
            for (locale  in allLocales)
            {
                try
                {
                    val res : Int = tts?.isLanguageAvailable(locale)!!
                    val hasVariant = null != locale.variant && locale.variant.isNotEmpty()
                    val hasCountry = null != locale.country && locale.country.isNotEmpty()

                    val isLocaleSupported: Boolean =
                            !hasVariant && !hasCountry && res == TextToSpeech.LANG_AVAILABLE
                            || !hasVariant && hasCountry && res == TextToSpeech.LANG_COUNTRY_AVAILABLE
                            || res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE

                    if (LOG) {
                        Log.d("TTS", "TextToSpeech Engine isLanguageAvailable " + locale
                                + " (supported=" + isLocaleSupported
                                + ",res=" + res + ", country=" + locale.getCountry()
                                + ", variant=" + locale.getVariant() + ")")
                    }

                    if (isLocaleSupported) {
                        languages.add(locale)
                    }
                }
                catch (ex : Exception)
                {
                    if (LOG) {
                        Log.e("TTS",
                                "Error checking if language is available for TTS (locale="
                                        + locale + "): " + "-" + ex.message)
                    }
                }
            }
            return languages
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun updateSummary() {
            val fontPref = findPreference(getString(R.string.pref_font_size))
            fontPref.summary = ActivityUtils.getPreferenceFontSize(activity)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen
                    .sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeListener)
        }

        override fun onResume() {
            super.onResume()
            updateSummary()
            preferenceScreen
                    .sharedPreferences
                    .registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class NotificationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
