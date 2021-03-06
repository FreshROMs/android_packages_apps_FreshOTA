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

package io.tensevntysevn.ota.download;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import io.tensevntysevn.ota.utils.Constants;
import io.tensevntysevn.ota.SoftwareUpdates;
import io.tensevntysevn.ota.utils.TnsOtaPreferences;

public class DownloadAddon implements Constants {

    public final static String TAG = "DownloadAddon";

    public void startDownload(Context context, String url, String fileName, int id) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        if (TnsOtaPreferences.getNetworkType(context).equals(WIFI_ONLY)) {
            // All network types are enabled by default
            // So if we choose Wi-Fi only, then enable the restriction
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }

        request.setTitle(fileName);

        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        fileName = fileName + ".zip";
        request.setDestinationInExternalPublicDir(INSTALL_AFTER_FLASH_DIR_ADDON, fileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        long mDownloadID = downloadManager.enqueue(request);
        SoftwareUpdates.putAddonDownload(id, mDownloadID);
        new DownloadAddonProgress(downloadManager, id).execute(mDownloadID);
        if (DEBUGGING) {
            Log.d(TAG,
                    "Starting download with manager ID " + mDownloadID + " and item id of " + id);
        }
    }

    public void cancelDownload(Context context, int id) {
        long mDownloadID = SoftwareUpdates.getAddonDownload(id);
        if (DEBUGGING) {
            Log.d(TAG,
                    "Stopping download with manager ID " + mDownloadID + " and item id of " + id);
        }
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        downloadManager.remove(mDownloadID);
    }
}