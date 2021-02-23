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

import java.io.File;

import io.tensevntysevn.ota.R;
import io.tensevntysevn.ota.utils.Constants;
import io.tensevntysevn.ota.utils.TnsOtaPreferences;
import io.tensevntysevn.ota.utils.OtaManifestUtils;
import io.tensevntysevn.ota.utils.TnsOtaUtils;

public class DownloadRom implements Constants {

    public final static String TAG = "DownloadOtaManifestUtils";

    public void startDownload(Context context) {
        String url = OtaManifestUtils.getDirectUrl(context);
        String fileName = OtaManifestUtils.getFilename(context) + ".zip";
        String version = OtaManifestUtils.getReleaseVersion(context);
        String variant = OtaManifestUtils.getReleaseVariant(context);
        String notificationTitle = context.getResources().getString(R.string."OTA_SWUPDATE_MAIN_INSTALL_DOWNLOADING");
        File file = OtaManifestUtils.getFullFile(context);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        if (TnsOtaPreferences.getNetworkType(context).equals(WIFI_ONLY)) {
            // All network types are enabled by default
            // So if we choose Wi-Fi only, then enable the restriction
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }

        request.setTitle(notificationTitle);

        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(OTA_DOWNLOAD_DIR, fileName);

        // Delete any existing files
        TnsOtaUtils.deleteFile(file);

        // Enqueue the download
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        long mDownloadID = downloadManager.enqueue(request);

        // Store the download ID
        TnsOtaPreferences.setDownloadID(context, mDownloadID);

        // Set a setting indicating the download is now running
        TnsOtaPreferences.setIsDownloadRunning(context, true);

        // Start updating the progress
        new DownloadRomProgress(context, downloadManager).execute(mDownloadID);

        // MD5 checker has not been run, nor passed
        TnsOtaPreferences.setMD5Passed(context, false);
        TnsOtaPreferences.setHasMD5Run(context, false);
    }

    public void cancelDownload(Context context) {
        // Grab the download ID from settings
        long mDownloadID = TnsOtaPreferences.getDownloadID(context);

        // Remove the download
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        downloadManager.remove(mDownloadID);

        // Indicate that the download is no longer running
        TnsOtaPreferences.setIsDownloadRunning(context, false);
    }
}