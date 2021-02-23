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

import android.os.Environment;

public interface Constants {
    // Developer
    boolean DEBUGGING = false;
    boolean DEBUG_NOTIFICATIONS = false;

    String ROM_CODE = "fresh";

    // Storage
    String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    String OTA_DOWNLOAD_DIR = "Download/Fresh";
    String INSTALL_AFTER_FLASH_DIR = "Postflash";
    String INSTALL_AFTER_FLASH_DIR_ADDON = "Download/Fresh/Postflash";

    // Networks
    String WIFI_ONLY = "2";

    // Theme
    String THEME_LIGHT = "0";

    // Settings
    String CURRENT_THEME = "current_theme";
    String LAST_CHECKED = "updater_last_update_check";
    String IS_DOWNLOAD_FINISHED = "is_download_finished";
    String DELETE_AFTER_INSTALL = "delete_after_install";
    String INSTALL_PREFS = "install_prefs";
    String WIPE_DATA = "wipe_data";
    String WIPE_CACHE = "wipe_cache";
    String WIPE_DALVIK = "wipe_dalvik";
    String MD5_PASSED = "md5_passed";
    String MD5_RUN = "md5_run";
    String DOWNLOAD_RUNNING = "download_running";
    String NETWORK_TYPE = "network_type";
    String DOWNLOAD_ID = "download_id";
    String UPDATER_BACK_SERVICE = "background_service";
    String UPDATER_BACK_FREQ = "background_frequency";
    String UPDATER_ENABLE_ORS = "updater_twrp_ors";
    String NOTIFICATIONS_SOUND = "notifications_sound";
    String NOTIFICATIONS_VIBRATE = "notifications_vibrate";
    String IGNORE_RELEASE_VERSION = "ignored_release";
    String FIRST_RUN = "first_run";
    String ABOUT_ACTIVITY_PREF = "about_activity_pref";

    // Broadcast intents
    String MANIFEST_LOADED = "tns.ota.intent.action.MANIFEST_LOADED";
    String MANIFEST_CHECK_BACKGROUND = "tns.ota.intent.action.MANIFEST_CHECK_BACKGROUND";
    String START_UPDATE_CHECK = "tns.ota.intent.action.START_UPDATE_CHECK";
    String IGNORE_RELEASE = "tns.ota.intent.action.IGNORE_RELEASE";

    //Notification
    int NOTIFICATION_ID = 101;

    String DEVICE_UPDATING = "ota_device_updating"
    
}