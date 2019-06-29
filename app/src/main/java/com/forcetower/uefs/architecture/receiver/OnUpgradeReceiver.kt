/*
 * Copyright (c) 2019.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.architecture.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.forcetower.uefs.core.storage.repository.UpgradeRepository
import com.forcetower.uefs.core.work.sync.SyncLinkedWorker
import com.forcetower.uefs.core.work.sync.SyncMainWorker
import dagger.android.AndroidInjection
import javax.inject.Inject

class OnUpgradeReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var repository: UpgradeRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED != intent.action) return
        AndroidInjection.inject(this, context)
        repository.onUpgrade()

        val type = preferences.getString("stg_sync_worker_type", "0")?.toIntOrNull() ?: 0
        if (type != 0) {
            var period = preferences.getString("stg_sync_frequency", "60")?.toIntOrNull() ?: 60
            preferences.edit().putString("stg_sync_worker_type", "0").apply()
            SyncLinkedWorker.stopWorker()
            if (period < 15) {
                period = 15
                preferences.edit().putString("stg_sync_frequency", "15").apply()
            }
            SyncMainWorker.createWorker(context, period)
        }
    }
}