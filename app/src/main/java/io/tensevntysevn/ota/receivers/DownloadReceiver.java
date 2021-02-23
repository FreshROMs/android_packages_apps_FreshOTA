/*
 * Copyright (C) 2021 John Vincent (TenSeventy7).
 * Copyright (C) 2017 Nicholas Chum (nicholaschum) and Matt Booth (Kryten2k35).
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International 
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tensevntysevn.ota.receivers;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

import io.tensevntysevn.ota.R;
import io.tensevntysevn.ota.activities.AddonActivity;
import io.tensevntysevn.ota.activities.AvailableActivity;
import io.tensevntysevn.ota.tasks.LoadUpdateManifest;
import io.tensevntysevn.ota.utils.Constants;
import io.tensevntysevn.ota.SoftwareUpdates;
import io.tensevntysevn.ota.utils.TnsOtaPreferences;
import io.tensevntysevn.ota.utils.OtaManifestUtils;
import io.tensevntysevn.ota.utils.TnsOtaUtils;

public class DownloadReceiver extends BroadcastReceiver implements Constants {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        long mRomDownloadID = TnsOtaPreferences.getDownloadID(context);

        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            boolean isAddonDownload = false;
            int keyForAddonDownload = 0;

            Set<Integer> set = SoftwareUpdates.getAddonDownloadKeySet();
            Iterator<Integer> iterator = set.iterator();

            while (iterator.hasNext() && !isAddonDownload) {
                int nextValue = iterator.next();
                if (id == SoftwareUpdates.getAddonDownload(nextValue)) {
                    isAddonDownload = true;
                    keyForAddonDownload = nextValue;
                    if (DEBUGGING) {
                        Log.d(TAG, "Checking ID " + nextValue);
                    }
                }
            }

            if (isAddonDownload) {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService
                        (Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    if (DEBUGGING)
                        Log.e(TAG, "Addon Download Empty row");
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    if (DEBUGGING)
                        Log.w(TAG, "Download Failed");
                    Log.d(TAG, "Removing Addon download with id " + keyForAddonDownload);
                    SoftwareUpdates.removeAddonDownload(keyForAddonDownload);
                    AddonActivity.AddonsArrayAdapter.updateProgress(keyForAddonDownload, 0, true);
                    AddonActivity.AddonsArrayAdapter.updateButtons(keyForAddonDownload, false);
                    return;
                } else {
                    if (DEBUGGING)
                        Log.v(TAG, "Download Succeeded");
                    Log.d(TAG, "Removing Addon download with id " + keyForAddonDownload);
                    SoftwareUpdates.removeAddonDownload(keyForAddonDownload);
                    AddonActivity.AddonsArrayAdapter.updateButtons(keyForAddonDownload, true);
                    return;
                }
            } else {
                if (DEBUGGING)
                    Log.v(TAG, "Receiving " + mRomDownloadID);

                if (id != mRomDownloadID) {
                    if (DEBUGGING)
                        Log.v(TAG, "Ignoring unrelated non-ROM download " + id);
                    return;
                }

                DownloadManager downloadManager = (DownloadManager) context.getSystemService
                        (Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);

                // it shouldn't be empty, but just in case
                if (!cursor.moveToFirst()) {
                    if (DEBUGGING)
                        Log.e(TAG, "Rom download Empty row");
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                    if (DEBUGGING)
                        Log.w(TAG, "Download Failed");
                    TnsOtaPreferences.setDownloadFinished(context, false);
                    AvailableActivity.setupMenuToolbar(context); // Reset options menu
                    return;
                } else {
                    if (DEBUGGING)
                        Log.v(TAG, "Download Succeeded");
                    TnsOtaPreferences.setDownloadFinished(context, true);
                    AvailableActivity.setupProgress(context);
                    AvailableActivity.setupMenuToolbar(context); // Reset options menu
                    return;
                }
            }
        }
        

        if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {

            long[] ids = extras.getLongArray(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);

            assert ids != null;
            for (long id : ids) {
                if (id != mRomDownloadID) {
                    if (DEBUGGING)
                        Log.v(TAG, "mDownloadID is " + mRomDownloadID + " and ID is " + id);
                    return;
                } else {
                    Intent i = new Intent(context, AvailableActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }

    }
}