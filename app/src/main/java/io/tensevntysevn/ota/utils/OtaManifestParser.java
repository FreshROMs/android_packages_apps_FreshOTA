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

import android.content.Context;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.tensevntysevn.ota.utils.Constants;
import io.tensevntysevn.ota.utils.OtaManifestUtils;
import io.tensevntysevn.ota.utils.TnsOtaUtils;

class OtaManifestParser extends DefaultHandler implements Constants {

    public final String TAG = this.getClass().getSimpleName();
    private boolean tagReleaseTag = false;
    private boolean tagReleaseVariant = false;
    private boolean tagReleaseVersion = false;
    private boolean tagSecurityPatch = false;
    private boolean tagReleaseDate = false;
    private boolean tagVersionNumber = false;

    private boolean tagDirectUrl = false;
    private boolean tagDirectUrlAlt = false;
    private boolean tagMirrorUrl = false;
    private boolean tagPackageHash = false;
    private boolean tagPackageSize = false;
    private boolean tagLog = false;

    private boolean tagForumUrl = false;
    private boolean tagDiscordUrl = false;
    private boolean tagIssuesUrl = false;
    private boolean tagDiscussionUrl = false;

    private boolean tagDonateUrl = false;
    private boolean tagBitCoinUrl = false;

    private boolean tagAddonsCount = false;
    private boolean tagAddonUrl = false;

    private StringBuffer value = new StringBuffer();
    private Context mContext;

    public void parse(File xmlFile, Context context) throws IOException {
        mContext = context;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlFile, this);

            TnsOtaUtils.setUpdateAvailability(context);

        } catch (ParserConfigurationException | SAXException ex) {
            Log.e(TAG, "", ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        value.setLength(0);

        // Release Tag (Release)
        if (qName.equalsIgnoreCase("release")) {
            tagReleaseTag = true;
        }

        // Release Variant (ReleaseVariant)
        if (qName.equalsIgnoreCase("releasevariant")) {
            tagReleaseVariant = true;
        }

        // Release Version (ReleaseVersion)
        if (qName.equalsIgnoreCase("releaseversion")) {
            tagReleaseVersion = true;
        }

        // Security Patch Level (AndroidSpl)
        if (qName.equalsIgnoreCase("androidspl")) {
            tagSecurityPatch = true;
        }

        // Release Tag (BuildVersion)
        if (qName.equalsIgnoreCase("buildversion")) {
            tagVersionNumber = true;
        }

        // Download Server URL (DirectUrl)
        if (qName.equalsIgnoreCase("directurl")) {
            tagDirectUrl = true;
        }

        // Alternative Download Server URL (DirectUrlAlt)
        if (qName.equalsIgnoreCase("directurlalt")) {
            tagDirectUrlAlt = true;
        }

        // Mirror URL (MirrorUrl)
        if (qName.equalsIgnoreCase("mirrorurl")) {
            tagMirrorUrl = true;
        }

        // OTA Package Hash (PackageHash)
        if (qName.equalsIgnoreCase("packagehash")) {
            tagPackageHash = true;
        }

        // OTA Package Size (PackageSize)
        if (qName.equalsIgnoreCase("packagesize")) {
            tagPackageSize = true;
        }

        // Forum URL (ForumUrl)
        if (qName.equalsIgnoreCase("forumurl")) {
            tagForumUrl = true;
        }

        // Discord Invite URL (DiscordUrl)
        if (qName.equalsIgnoreCase("discordurl")) {
            tagDiscordUrl = true;
        }

        // Git Issues (GitIssues)
        if (qName.equalsIgnoreCase("gitissues")) {
            tagIssuesUrl = true;
        }

        // Git Discussion (GitDiscussion)
        if (qName.equalsIgnoreCase("gitdiscussion")) {
            tagDiscussionUrl = true;
        }

        // Donate URL (DonateUrl)
        if (qName.equalsIgnoreCase("donateurl")) {
            tagDonateUrl = true;
        }

        // Bitcoin Address (BitcoinAddress)
        if (qName.equalsIgnoreCase("bitcoinaddress")) {
            tagBitCoinUrl = true;
        }

        // Changelog (Changelog)
        if (qName.equalsIgnoreCase("changelog")) {
            tagLog = true;
        }

        // Addons Count (AddonsCount)
        if (qName.equalsIgnoreCase("addonscount")) {
            tagAddonsCount = true;
        }

        // Addons Manifest URL (AddonsManifest)
        if (qName.equalsIgnoreCase("addonssmanifest")) {
            tagAddonUrl = true;
        }

    }

    @Override
    public void characters(char[] buffer, int start, int length)
            throws SAXException {
        value.append(buffer, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        String input = value.toString().trim();

        if (tagReleaseTag) {
            OtaManifestUtils.setReleaseTag(mContext, Integer.parseInt(input));
            tagReleaseTag = false;
        }

        if (tagReleaseVersion) {
            OtaManifestUtils.setReleaseVersion(mContext, input);
            tagReleaseVersion = false;
        }

        if (tagSecurityPatch) {
            OtaManifestUtils.setAndroidSpl(mContext, input);
            tagSecurityPatch = false;
        }

        if (tagReleaseVariant) {
            OtaManifestUtils.setReleseVariant(mContext, input);
            tagReleaseVariant = false;
        }

        if (tagVersionNumber) {
            OtaManifestUtils.setBuildVersion(mContext, Integer.parseInt(input));
            tagVersionNumber = false;
        }

        if (tagDirectUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setDirectUrl(mContext, input);
            } else {
                OtaManifestUtils.setDirectUrl(mContext, "null");
            }
            tagDirectUrl = false;
        }

        if (tagDirectUrlAlt) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setDirectUrlAlt(mContext, input);
            } else {
                OtaManifestUtils.setDirectUrlAlt(mContext, "null");
            }
            tagDirectUrlAlt = false;
        }

        if (tagMirrorUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setMirrorUrl(mContext, input);
            } else {
                OtaManifestUtils.setMirrorUrl(mContext, "null");
            }
            tagMirrorUrl = false;
        }

        if (tagPackageHash) {
            OtaManifestUtils.setPackageHash(mContext, input);
            tagPackageHash = false;
        }

        if (tagPackageSize) {
            OtaManifestUtils.setPackageSize(mContext, Integer.parseInt(input));
            tagPackageSize = false;
        }

        if (tagForumUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setForum(mContext, input);
            } else {
                OtaManifestUtils.setForum(mContext, "null");
            }
            tagForumUrl = false;
        }

        if (tagDiscordUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setDiscord(mContext, input);
            } else {
                OtaManifestUtils.setDiscord(mContext, "null");
            }
            tagDiscordUrl = false;
        }

        if (tagIssuesUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setGitIssues(mContext, input);
            } else {
                OtaManifestUtils.setGitIssues(mContext, "null");
            }
            tagIssuesUrl = false;
        }

        if (tagDiscussionUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setGitDiscussion(mContext, input);
            } else {
                OtaManifestUtils.setGitDiscussion(mContext, "null");
            }
            tagDiscussionUrl = false;
        }

        if (tagDonateUrl) {
            if (!input.isEmpty()) {
                OtaManifestUtils.setDonateLink(mContext, input);
            } else {
                OtaManifestUtils.setDonateLink(mContext, "null");
            }
            tagDonateUrl = false;
        }

        if (tagBitCoinUrl) {
            if (input.contains("bitcoin:")) {
                OtaManifestUtils.setBtcLink(mContext, input);
            } else if (input.isEmpty()) {
                OtaManifestUtils.setBtcLink(mContext, "null");
            } else {
                OtaManifestUtils.setBtcLink(mContext, "bitcoin:" + input);
            }

            tagBitCoinUrl = false;
            if (DEBUGGING)
                Log.d(TAG, "BitCoin URL = " + input);
        }

        if (tagLog) {
            OtaManifestUtils.setChangelog(mContext, input);
            tagLog = false;
        }

        if (tagAddonsCount) {
            OtaManifestUtils.setAddonsCount(mContext, Integer.parseInt(input));
            tagAddonsCount = false;
        }

        if (tagAddonUrl) {
            OtaManifestUtils.setAddonsUrl(mContext, input);
            tagAddonUrl = false;
        }
    }
}