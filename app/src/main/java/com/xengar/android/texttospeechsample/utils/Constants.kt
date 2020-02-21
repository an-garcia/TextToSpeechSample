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

/**
 * Constants
 */
object Constants {

    /**
     * Boolean used to log or not lines
     * Usage:
     * if (LOG) {
     * if (condition) Log.i(...);
     * }
     * When you set LOG to false, the compiler will strip out all code inside such checks
     * (since it is a static final, it knows at compile time that code is not used.)
     * http://stackoverflow.com/questions/2446248/remove-all-debug-logging-calls-before-publishing-are-there-tools-to-do-this
     */
    val LOG = true


    val DEFAULT_FONT_SIZE = "16"
    val DEFAULT_TTS_LOCALE = "en"

}
