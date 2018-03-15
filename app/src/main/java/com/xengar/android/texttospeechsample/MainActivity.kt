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
package com.xengar.android.texttospeechsample

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val ACT_CHECK_TTS_DATA = 1000
    private var tts: TextToSpeech? = null
    private var text: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        text = findViewById(R.id.text)

        // Check to see if we have TTS voice data
        val intent = Intent()
        intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        startActivityForResult(intent, ACT_CHECK_TTS_DATA)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Play text", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

            // Speak text
            ActivityUtils.speak(applicationContext, tts, text!!.text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Called when returning from startActivityForResult  */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // data exists, so we instantiate the TTS engine
                tts = TextToSpeech(this, TextToSpeech.OnInitListener {
                    status -> ActivityUtils.configureTextToSpeechLanguage(tts, status) })
            } else {
                // data is missing, so we start the TTS installation process
                val intent = Intent()
                intent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(intent)
            }
        }
    }
}
