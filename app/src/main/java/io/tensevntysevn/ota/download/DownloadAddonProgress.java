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
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.util.Log;

import io.tensevntysevn.ota.activities.AddonActivity;
import io.tensevntysevn.ota.utils.Constants;

class DownloadAddonProgress extends AsyncTask<Long, Integer, Void> implements Constants {
    public final String TAG = this.getClass().getSimpleName();

    private DownloadManager mDownloadManager;
    private int mViewId;
    private boolean mIsRunning = true;

    DownloadAddonProgress(DownloadManager downloadManager, int id) {
        mDownloadManager = downloadManager;
        mViewId = id;
    }

    @Override
    protected void onCancelled() {
        mIsRunning = false;
    }

    @Override
    protected Void doInBackground(Long... params) {
        int previousValue = 0;
        long mDownloadId = params[0];
        while (mIsRunning) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(mDownloadId);

            Cursor cursor = mDownloadManager.query(q);
            cursor.moveToFirst();
            try {
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                        DownloadManager.STATUS_SUCCESSFUL ||
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                                DownloadManager.STATUS_FAILED) {
                    mIsRunning = false;
                }

                final int bytesDownloaded = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                final int bytesInTotal = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);

                if (progressPercent != previousValue) {
                    // Only publish every 1%, to reduce the amount of work being done.
                    publishProgress(progressPercent, bytesDownloaded, bytesInTotal);
                    previousValue = progressPercent;
                }
            } catch (CursorIndexOutOfBoundsException | ArithmeticException e) {
                Log.e(TAG, " " + e.getMessage());
                mIsRunning = false;
            }
            cursor.close();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        if (DEBUGGING)
            Log.d(TAG, "Updating Progress - " + progress[0] + "%");
        if (mIsRunning) {
            AddonActivity.AddonsArrayAdapter.updateProgress(mViewId, progress[0], false);
        } else {
            AddonActivity.AddonsArrayAdapter.updateProgress(mViewId, 0, true);
        }
    }
}
