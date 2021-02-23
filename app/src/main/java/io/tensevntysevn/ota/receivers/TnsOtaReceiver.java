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

public class TnsOtaReceiver extends BroadcastReceiver implements Constants {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action.equals(MANIFEST_CHECK_BACKGROUND)) {
            if (DEBUGGING)
                Log.d(TAG, "Receiving background check confirmation");

            boolean updateAvailable = OtaManifestUtils.getUpdateAvailability(context);
            String releaseVersion = OtaManifestUtils.getReleaseVersion(context);
            String releaseVariant = OtaManifestUtils.getReleaseVariant(context);

            if (updateAvailable) {
                TnsOtaUtils.setupNotification(context, releaseVersion, releaseVariant);
                TnsOtaUtils.scheduleNotification(context, !TnsOtaPreferences.getBackgroundService(context));
            }
        }

        if (action.equals(START_UPDATE_CHECK)) {
            if (DEBUGGING)
                Log.d(TAG, "Update check started");
            new LoadUpdateManifest(context, false).execute();
        }

        if (action.equals(IGNORE_RELEASE)) {
            if (DEBUGGING) {
                Log.d(TAG, "Ignore release");
            }
            TnsOtaPreferences.setIgnoredRelease(context, Integer.toString(OtaManifestUtils.getBuildVersion
                    (context)));
            final NotificationManager mNotifyManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(context.getString(R.string.main_release_ignored))
                    .setSmallIcon(R.drawable.tns_ota_ic_information_circle)
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

            Handler h = new Handler();
            long delayInMilliseconds = 1500;
            h.postDelayed(() -> mNotifyManager.cancel(NOTIFICATION_ID), delayInMilliseconds);
        }
    }
}