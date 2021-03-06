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

package io.tensevntysevn.ota.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.tensevntysevn.ota.R;
import io.tensevntysevn.ota.tasks.Changelog;
import io.tensevntysevn.ota.tasks.LoadUpdateManifest;
import io.tensevntysevn.ota.utils.Constants;
import io.tensevntysevn.ota.utils.TnsOtaPreferences;
import io.tensevntysevn.ota.utils.OtaManifestUtils;
import io.tensevntysevn.ota.utils.Tools;
import io.tensevntysevn.ota.utils.TnsOtaUtils;

public class MainActivity extends Activity implements Constants,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CHANGE_THEME_REQUEST_CODE = 2;
    private static final boolean ENABLE_COMPATIBILITY_CHECK = true;
    public static boolean hasRoot;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar mProgressBar;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    private Builder mCompatibilityDialog;
    private Builder mDonateDialog;
    private Builder mPlayStoreDialog;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MANIFEST_LOADED)) {
                updateAllLayouts();
            }
        }
    };

    public static void updateProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
    }

    private boolean updateAllLayouts() {
        try {
            updateDonateLinkLayout();
            updateAddonsLayout();
            updateRomInformation();
            updateOtaManifestUtilsLayouts();
            updateWebsiteLayout();
            return true;
        } catch (Exception e) {
            // Suppress warning
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mContext = this;
        setTheme(TnsOtaPreferences.getTheme(mContext));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        boolean firstRun = TnsOtaPreferences.getFirstRun(mContext);
        if (firstRun) {
            TnsOtaPreferences.setFirstRun(mContext, false);
            showWhatsNew();
        }

        // Create download directories if needed
        File installAfterFlashDir = new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + INSTALL_AFTER_FLASH_DIR);
        boolean created = installAfterFlashDir.mkdirs();
        if (!created) Log.e(TAG, "Could not create installAfterFlash directory...");

        createDialogs();

        // Check the correct build prop values are installed
        // Also executes the manifest/update check

        if (!TnsOtaUtils.isConnected(mContext)) {
            Builder notConnectedDialog = new Builder(mContext);
            notConnectedDialog.setTitle(R.string.main_not_connected_title)
                    .setMessage(R.string.main_not_connected_message)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ((Activity) mContext)
                            .finish())
                    .show();
        } else {
            if (ENABLE_COMPATIBILITY_CHECK) new CompatibilityTask(mContext).execute();
        }

        // Has the download already completed?
        TnsOtaUtils.setHasFileDownloaded(mContext);

        // Update the layouts
        updateDonateLinkLayout();
        updateAddonsLayout();
        updateRomInformation();
        updateOtaManifestUtilsLayouts();
        updateWebsiteLayout();

        // But check permissions first - download will be started in the callback
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        new checkRoot().execute("");
    }

    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mReceiver, new IntentFilter(MANIFEST_LOADED));

    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0]
                        != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permission_not_granted_dialog_title)
                            .setMessage(R.string.permission_not_granted_dialog_message)
                            .setPositiveButton(R.string.dialog_ok, (dialog, which) ->
                                    MainActivity.this.finish()).show();
                    return;
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ota_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_changelog:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R
                        .string.changelog_url)));
                startActivity(browserIntent);
                return true;
            case R.id.menu_settings:
                openSettings(null);
                return true;
        }
        return false;
    }

    private void createDialogs() {
        // Compatibility Dialog
        mCompatibilityDialog = new AlertDialog.Builder(mContext);
        mCompatibilityDialog.setCancelable(false);
        mCompatibilityDialog.setTitle(R.string.main_not_compatible_title);
        mCompatibilityDialog.setMessage(R.string.main_not_compatible_message);
        mCompatibilityDialog.setPositiveButton(R.string.ok, (dialog, which) -> MainActivity.this
                .finish());

        // Donate Dialog
        mDonateDialog = new AlertDialog.Builder(this);
        String[] donateItems = {"PayPal", "BitCoin"};
        mDonateDialog.setTitle(getResources().getString(R.string.donate))
                .setSingleChoiceItems(donateItems, 0, null)
                .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                    String url;
                    int selectedPosition = ((AlertDialog) dialog).getListView()
                            .getCheckedItemPosition();
                    if (selectedPosition == 0) {
                        url = OtaManifestUtils.getDonateLink(mContext);
                    } else {
                        url = OtaManifestUtils.getBitCoinLink(mContext);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // Nothing to handle BitCoin payments. Send to Play Store
                        if (DEBUGGING)
                            Log.d(TAG, ex.getMessage());
                        mPlayStoreDialog.show();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) ->
                        dialog.cancel());

        mPlayStoreDialog = new AlertDialog.Builder(mContext);
        mPlayStoreDialog.setCancelable(true);
        mPlayStoreDialog.setTitle(R.string.main_playstore_title);
        mPlayStoreDialog.setMessage(R.string.main_playstore_message);
        mPlayStoreDialog.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://play.google.com/store/search?q=bitcoin%20wallet&c=apps";
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        mPlayStoreDialog.setNegativeButton(getResources().getString(R.string.cancel), (dialog,
                                                                                       which) ->
                dialog.cancel());
    }

    @SuppressWarnings("deprecation")
    private void updateOtaManifestUtilsLayouts() {
        View updateAvailable;
        View updateNotAvailable;
        updateAvailable = findViewById(R.id.layout_main_update_available);
        updateNotAvailable = findViewById(R.id.layout_main_no_update_available);
        updateAvailable.setVisibility(View.GONE);
        updateNotAvailable.setVisibility(View.GONE);

        TextView updateAvailableSummary = (TextView) findViewById(R.id
                .main_tv_update_available_summary);
        TextView updateNotAvailableSummary = (TextView) findViewById(R.id
                .main_tv_no_update_available_summary);

        mProgressBar = (ProgressBar) findViewById(R.id.bar_main_progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // Update is available
        if (OtaManifestUtils.getUpdateAvailability(mContext) ||
                (!OtaManifestUtils.getUpdateAvailability(mContext)) && TnsOtaUtils.isUpdateIgnored(mContext)) {
            updateAvailable.setVisibility(View.VISIBLE);
            TextView updateAvailableTitle = (TextView) findViewById(R.id
                    .main_tv_update_available_title);

            if (TnsOtaPreferences.getDownloadFinished(mContext)) { //  Update already finished?
                updateAvailableTitle.setText(getResources().getString(R.string
                        .main_update_finished));
                String htmlColorOpen;
                if (TnsOtaPreferences.getCurrentTheme(mContext) == 0) { // Light
                    htmlColorOpen = "<font color='#000000'>";
                } else {
                    htmlColorOpen = "<font color='#ffffff'>";
                }
                String htmlColorClose = "</font>";
                String updateSummary = OtaManifestUtils.getFilename(mContext)
                        + "<br />"
                        + htmlColorOpen
                        + getResources().getString(R.string.main_download_completed_details)
                        + htmlColorClose;
                updateAvailableSummary.setText(Html.fromHtml(updateSummary));
            } else if (TnsOtaPreferences.getIsDownloadOnGoing(mContext)) {
                updateAvailableTitle.setText(getResources().getString(R.string
                        .main_update_progress));
                mProgressBar.setVisibility(View.VISIBLE);
                String htmlColorOpen;
                if (TnsOtaPreferences.getCurrentTheme(mContext) == 0) { // Light
                    htmlColorOpen = "<font color='#000000'>";
                } else {
                    htmlColorOpen = "<font color='#ffffff'>";
                }
                String htmlColorClose = "</font>";
                String updateSummary = htmlColorOpen
                        + getResources().getString(R.string.main_tap_to_view_progress)
                        + htmlColorClose;
                updateAvailableSummary.setText(Html.fromHtml(updateSummary));
            } else {
                updateAvailableTitle.setText(getResources().getString(R.string
                        .main_update_available));
                String htmlColorOpen;
                if (TnsOtaPreferences.getCurrentTheme(mContext) == 0) { // Light
                    htmlColorOpen = "<font color='#000000'>";
                } else {
                    htmlColorOpen = "<font color='#ffffff'>";
                }
                String htmlColorClose = "</font>";
                String updateSummary = OtaManifestUtils.getFilename(mContext)
                        + "<br />"
                        + htmlColorOpen
                        + getResources().getString(R.string.main_tap_to_download)
                        + htmlColorClose;
                updateAvailableSummary.setText(Html.fromHtml(updateSummary));

            }
        } else {
            updateNotAvailable.setVisibility(View.VISIBLE);

            boolean is24 = DateFormat.is24HourFormat(mContext);
            Date now = new Date();
            Locale locale = Locale.getDefault();
            String time;

            if (is24) {
                time = new SimpleDateFormat("MMMM d - HH:mm", locale).format(now);
            } else {
                time = new SimpleDateFormat("MMMM d - hh:mm a", locale).format(now);
            }

            TnsOtaPreferences.setUpdateLastChecked(this, time);
            String lastChecked = getString(R.string.main_last_checked);
            updateNotAvailableSummary.setText(String.format("%s%s", lastChecked, time));
        }
    }

    private void updateAddonsLayout() {
        CardView addonsLink = (CardView) findViewById(R.id.layout_main_addons);
        addonsLink.setVisibility(View.GONE);

        if (OtaManifestUtils.getAddonsCount(mContext) > 0) {
            addonsLink.setVisibility(View.VISIBLE);
        }
    }

    private void updateDonateLinkLayout() {
        CardView donateLink = (CardView) findViewById(R.id.layout_main_dev_donate_link);
        donateLink.setVisibility(View.GONE);

        if (!(OtaManifestUtils.getDonateLink(mContext).trim().equals("null"))
                || !(OtaManifestUtils.getBitCoinLink(mContext).trim().equals("null"))) {
            donateLink.setVisibility(View.VISIBLE);
        }
    }

    private void updateWebsiteLayout() {
        CardView webLink = (CardView) findViewById(R.id.layout_main_dev_website);
        webLink.setVisibility(View.GONE);

        if (!OtaManifestUtils.getWebsite(mContext).trim().equals("null")) {
            webLink.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateRomInformation() {
        String htmlColorOpen;
        if (TnsOtaPreferences.getCurrentTheme(mContext) == 0) { // Light
            htmlColorOpen = "<font color='#000000'>";
        } else {
            htmlColorOpen = "<font color='#ffffff'>";
        }
        String htmlColorClose = "</font>";

        String space = " ";
        String separator_open = " (";
        String separator_close = ") ";

        //ROM name
        TextView romName = (TextView) findViewById(R.id.tv_main_rom_name);
        String romNameTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_name) + " ";
        String romNameActual = TnsOtaUtils.getProp(getResources().getString(R.string.prop_name));
        String romNameDevice = TnsOtaUtils.getProp(getResources().getString(R.string.prop_system_model));
        String romNameVersion = TnsOtaUtils.getProp(getResources().getString(R.string.prop_rom_version));
        romName.setText(Html.fromHtml(romNameTitle + htmlColorOpen + romNameActual + space +
                romNameVersion + separator_open + romNameDevice + separator_close +
                htmlColorClose));

        //ROM version
        TextView romVersion = (TextView) findViewById(R.id.tv_main_rom_version);
        String romVersionTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_version) + " ";
        String romVersionActual = TnsOtaUtils.getProp(getResources().getString(R.string.prop_version));
        romVersion.setText(Html.fromHtml(romVersionTitle + htmlColorOpen + romVersionActual +
                htmlColorClose));

        //ROM date
        TextView romDate = (TextView) findViewById(R.id.tv_main_rom_date);
        String romDateTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_build_date) + " ";
        String romDateActual = TnsOtaUtils.getProp(getResources().getString(R.string.prop_date));
        romDate.setText(Html.fromHtml(romDateTitle + htmlColorOpen + romDateActual +
                htmlColorClose));

        //ROM android version
        TextView romAndroid = (TextView) findViewById(R.id.tv_main_android_version);
        String romAndroidTitle = getApplicationContext().getResources().getString(R.string
                .main_android_version) + " ";
        String romAndroidActual = TnsOtaUtils.getProp(getResources().getString(R.string.prop_release));
        String romAndroidBuildID = TnsOtaUtils.getProp(getResources().getString(R.string
                .prop_release_build_id));
        romAndroid.setText(Html.fromHtml(romAndroidTitle + htmlColorOpen + romAndroidActual +
                separator_open + romAndroidBuildID + separator_close + htmlColorClose));

        //ROM developer
        TextView romDeveloper = (TextView) findViewById(R.id.tv_main_rom_developer);
        boolean showDevName = !OtaManifestUtils.getDeveloper(this).equals("null");
        //romDeveloper.setVisibility(showDevName? View.VISIBLE : View.GONE);

        String romDeveloperTitle = getApplicationContext().getResources().getString(R.string
                .main_rom_developer) + " ";
        String romDeveloperActual = showDevName ? OtaManifestUtils.getDeveloper(this) : TnsOtaUtils.getProp
                (getResources().getString(R.string.prop_developer));
        romDeveloper.setText(Html.fromHtml(romDeveloperTitle + htmlColorOpen + romDeveloperActual
                + htmlColorClose));

    }

    public void openCheckForUpdates(View v) {
        new LoadUpdateManifest(mContext, true).execute();
    }

    public void openDownload(View v) {
        Intent intent = new Intent(mContext, AvailableActivity.class);
        startActivity(intent);
    }

    public void openAddons(View v) {
        Intent intent = new Intent(mContext, AddonActivity.class);
        startActivity(intent);
    }

    public void openDonationPage(View v) {

        boolean payPalLinkAvailable = OtaManifestUtils.getDonateLink(mContext).trim().equals("null");
        boolean bitCoinLinkAvailable = OtaManifestUtils.getBitCoinLink(mContext).trim().equals("null");
        if (!payPalLinkAvailable && !bitCoinLinkAvailable) {
            mDonateDialog.show();
        } else if (!payPalLinkAvailable) {
            String url = OtaManifestUtils.getDonateLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else if (!bitCoinLinkAvailable) {
            String url = OtaManifestUtils.getBitCoinLink(mContext);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    public void openWebsitePage(View v) {
        String url = OtaManifestUtils.getWebsite(mContext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openSettings(View v) {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivityForResult(intent, CHANGE_THEME_REQUEST_CODE);
    }

    public void openChangelog(View v) {
        String title = getResources().getString(R.string.changelog);
        String changelog = OtaManifestUtils.getChangelog(mContext);
        new Changelog(this, mContext, title, changelog, false).execute();
    }

    private void showWhatsNew() {
        String title = getResources().getString(R.string.changelog);
        String changelog = getResources().getString(R.string.changelog_url);
        new Changelog(this, mContext, title, changelog, true).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_THEME_REQUEST_CODE) {
            this.recreate();
        }
    }

    static class checkRoot extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            hasRoot = Tools.isRootAvailable();
            return null;
        }
    }

    private class CompatibilityTask extends AsyncTask<Void, Boolean, Boolean> implements Constants {

        public final String TAG = this.getClass().getSimpleName();

        private Context mContext;
        private String mPropName;

        CompatibilityTask(Context context) {
            mContext = context;
            mPropName = mContext.getResources().getString(R.string.prop_name);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            return TnsOtaUtils.doesPropExist(mPropName);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                if (DEBUGGING)
                    Log.d(TAG, "Prop found");
                new LoadUpdateManifest(mContext, true).execute();
            } else {
                if (DEBUGGING)
                    Log.d(TAG, "Prop not found");
                try {
                    mCompatibilityDialog.show();
                } catch (WindowManager.BadTokenException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
            super.onPostExecute(result);
        }
    }
}
