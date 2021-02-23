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

package io.tensevntysevn.ota.utils;

import android.os.Build;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;

public class OtaManifestUtils implements Constants {

    private static final String PREF_OTA_NAME = "FreshOTA";
    private static final String PREF_INFO_NAME = "FreshROM";

    private static String RELEASE_TAG = "ota_release_tag";
    private static String RELEASE_VARIANT = "ota_release_variant";
    private static String RELEASE_VERSION = "ota_release_version";

    private static String BUILD_VERSION = "ota_build_version";
    private static String ANDROID_SECURITY_PATCH = "ota_android_spl";

    private static String DIRECT_URL = "ota_url_direct";
    private static String DIRECT_URL_ALT = "ota_url_direct_alt";
    private static String MIRROR_URL = "ota_url_mirror";

    private static String PACKAGE_HASH = "ota_package_hash";
    private static String PACKAGE_SIZE = "ota_package_size";

    private static String FORUM_URL = "rom_url_forum";
    private static String DISCORD_URL = "rom_url_discord";
    private static String ISSUES_URL = "rom_url_git_issues";
    private static String DISCUSSION_URL = "rom_url_git_discussion";

    private static String DONATE_LINK = "rom_url_donate";
    private static String BTC_LINK = "rom_url_donate_btc";

    private static String ADDONS_URL = "rom_addons_url";
    private static String ADDONS_COUNT = "rom_addons_count";


    private static String CHANGELOG = "ota_build_changelog";
    private static String OTA_AVAILABILITY = "ota_update_availability";
    private static String DEF_VALUE = "null";

    public final String TAG = this.getClass().getSimpleName();

    private static SharedPreferences getOtaPrefs(Context context) {
        return context.getSharedPreferences(PREF_OTA_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getRomPrefs(Context context) {
        return context.getSharedPreferences(PREF_INFO_NAME, Context.MODE_PRIVATE);
    }

    public static String getAndroidSpl(Context context) {
        String otaSpl = getOtaPrefs(context).getString(ANDROID_SECURITY_PATCH, DEF_VALUE);
        if (!"".equals(otaSpl)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(otaSpl);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                otaSpl = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {
                // broken parse; fall through and use the raw string
            }
            return otaSpl;
        } else {
            return DEF_VALUE;
        }
    }

    public static String getReleaseTag(Context context) {
        String releaseTag = getOtaPrefs(context).getInt(RELEASE_TAG, 0);
        if (releaseTag.equals("2")) {
            return "OFFICIAL";
        } else {
            return "UNOFFICIAL";
        }
    }

    public static String getChangelog(Context context) {
        return getOtaPrefs(context).getString(CHANGELOG, DEF_VALUE);
    }

    public static String getOldChangelog(Context context) {
        return getInfoPrefs(context).getString(CHANGELOG, DEF_VALUE);
    }

    public static String getReleaseVariant(Context context) {
        return getOtaPrefs(context).getString(RELEASE_VARIANT, DEF_VALUE);
    }

    public static String getReleaseVersion(Context context) {
        return getOtaPrefs(context).getString(RELEASE_VERSION, DEF_VALUE);
    }

    public static String getOldReleaseVersion(Context context) {
        return getInfoPrefs(context).getString(RELEASE_VERSION, DEF_VALUE);
    }

    public static int getBuildVersion(Context context) {
        return getOtaPrefs(context).getInt(BUILD_VERSION, 0);
    }

    public static int getOldBuildVersion(Context context) {
        return getInfoPrefs(context).getInt(BUILD_VERSION, 0);
    }

    public static String getDirectUrl(Context context) {
        return getOtaPrefs(context).getString(DIRECT_URL, DEF_VALUE);
    }

    public static String getDirectUrlAlt(Context context) {
        return getOtaPrefs(context).getString(DIRECT_URL_ALT, DEF_VALUE);
    }

    public static String getMirrorUrl(Context context) {
        return getOtaPrefs(context).getString(MIRROR_URL, DEF_VALUE);
    }

    public static String getPackageHash(Context context) {
        return getOtaPrefs(context).getString(PACKAGE_HASH, DEF_VALUE);
    }

    public static int getPackageSize(Context context) {
        return getOtaPrefs(context).getInt(PACKAGE_SIZE, 0);
    }

    public static String getForum(Context context) {
        return getInfoPrefs(context).getString(FORUM_URL, DEF_VALUE);
    }

    public static String getDiscord(Context context) {
        return getInfoPrefs(context).getString(DISCORD_URL, DEF_VALUE);
    }

    public static String getGitIssues(Context context) {
        return getInfoPrefs(context).getString(ISSUES_URL, DEF_VALUE);
    }

    public static String getGitDiscussion(Context context) {
        return getInfoPrefs(context).getString(DISCUSSION_URL, DEF_VALUE);
    }

    public static String getDonateLink(Context context) {
        return getInfoPrefs(context).getString(DONATE_LINK, DEF_VALUE);
    }

    public static String getBitCoinLink(Context context) {
        return getInfoPrefs(context).getString(BTC_LINK, DEF_VALUE);
    }

    public static int getAddonsCount(Context context) {
        return getInfoPrefs(context).getInt(ADDONS_COUNT, 0);
    }

    public static String getAddonsUrl(Context context) {
        return getInfoPrefs(context).getString(ADDONS_URL, DEF_VALUE);
    }

    public static boolean getUpdateAvailability(Context context) {
        return getOtaPrefs(context).getBoolean(OTA_AVAILABILITY, false);
    }

    public static void setReleaseTag(Context context, int tag) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putInt(RELEASE_TAG, tag);
        editor.apply();
    }

    public static void setReleaseVersion(Context context, String variant) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(RELEASE_VARIANT, variant);
        editor.apply();
    }

    public static void setReleaseVersion(Context context, String version) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(RELEASE_VERSION, version);
        editor.apply();
    }

    public static void setBuildVersion(Context context, int version) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putInt(BUILD_VERSION, version);
        editor.apply();
    }

    public static void setDirectUrl(Context context, String url) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(DIRECT_URL, url);
        editor.apply();
    }

    public static void setDirectUrlAlt(Context context, String url) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(DIRECT_URL_ALT, url);
        editor.apply();
    }

    public static void setMirrorUrl(Context context, String url) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(MIRROR_URL, url);
        editor.apply();
    }

    public static void setPackageSize(Context context, int size) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putInt(PACKAGE_SIZE, size);
        editor.apply();
    }

    public static void setPackageHash(Context context, String hash) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(PACKAGE_HASH, hash);
        editor.apply();
    }

    public static void setChangelog(Context context, String change) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(CHANGELOG, change);
        editor.apply();
    }

    public static void setOldVersion(Context context, String version) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(RELEASE_VERSION, version);
        editor.apply();
    }

    public static void setOldBuildVersion(Context context, int version) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putInt(BUILD_VERSION, version);
        editor.apply();
    }

    public static void setOldChangelog(Context context, String change) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(CHANGELOG, change);
        editor.apply();
    }

    public static void setAndroidSpl(Context context, String spl) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putString(ANDROID_SECURITY_PATCH, spl);
        editor.apply();
    }

    public static void setForum(Context context, String url) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(FORUM_URL, url);
        editor.apply();
    }

    public static void setDiscord(Context context, String url) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(DISCORD_URL, url);
        editor.apply();
    }

    public static void setGitIssues(Context context, String url) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(ISSUES_URL, url);
        editor.apply();
    }

    public static void setGitDiscussion(Context context, String url) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(DISCUSSION_URL, url);
        editor.apply();
    }

    public static void setDonateLink(Context context, String donateLink) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(DONATE_LINK, donateLink);
        editor.apply();
    }

    public static void setBtcLink(Context context, String donateLink) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(BTC_LINK, donateLink);
        editor.apply();
    }

    public static void setAddonsCount(Context context, int addons_count) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putInt(ADDONS_COUNT, addons_count);
        editor.apply();
    }

    public static void setAddonsUrl(Context context, String addons_url) {
        SharedPreferencesEditor editor = getInfoPrefs(context).edit();
        editor.putString(ADDONS_URL, addons_url);
        editor.apply();
    }

    public static void setUpdateAvailable(Context context, boolean availability) {
        SharedPreferencesEditor editor = getOtaPrefs(context).edit();
        editor.putBoolean(OTA_AVAILABILITY, availability);
        editor.apply();
    }

    public static String getFilename(Context context) {

        String result = "FRSH-OTA_"
                        + TnsOtaUtils.getDeviceCodename();
                        + "_"
                        + getVersionName(context);
                        + "_"
                        + getBuildVersion(context);
                        + "_"
                        + getReleaseTag(context);

        return result.replace(" ", "");
    }

    public static File getFullFile(Context context) {
        return new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + OtaManifestUtils.getFilename(context)
                + ".zip");
    }
}
