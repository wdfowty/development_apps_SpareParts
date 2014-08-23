/* //device/apps/Settings/src/com/android/settings/Keyguard.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.leo_parts;

import com.android.leo_parts.ShellInterface;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;

import java.util.List;
import android.text.format.Formatter;
import android.net.Uri;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class LeoParts extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "LeoParts";
    private static final String REMOUNT_RO = "mount -o ro,remount -t yaffs2 /dev/block/mtdblock3 /system";
    private static final String REMOUNT_RW = "mount -o rw,remount -t yaffs2 /dev/block/mtdblock3 /system";
    private static String REPO;

    public ProgressDialog patience = null;

    final Handler mHandler = new Handler();

    private boolean extfsIsMounted = false;

    private static final String ROM_NAME_VERSION_PREF = "rom_name_version";
    private static final String ROM_SYSTEM_BUILD_PREF = "rom_system_build";
    private static final String ROM_RADIO_PREF = "rom_radio";
    private static final String ROM_KERNEL_PREF = "rom_kernel";
    private static final String ROM_UPDATE_PREF = "rom_update";

    private static final String APP2SD_PREF = "app2sd";
    private static final String NOTIF_BAR_PREF = "notif_bar";
    private static final String UI_SOUNDS_PREF = "ui_sounds";
    private static final String FIX_PERMS_PREF = "fix_perms";
    private static final String FIX_MARKET_PREF = "fix_market";
    private static final String ZIPALIGN_PREF = "zipalign";

    private static final String REBOOT_PREF = "reboot_reboot";
    private static final String RECOVERY_PREF = "reboot_recovery";
    private static final String BOOTLOADER_PREF = "reboot_bootloader";
    private static final String REMOUNT_RW_PREF = "remount_rw";
    private static final String REMOUNT_RO_PREF = "remount_ro";

    private static final String CAR_HOME_PREF = "car_home";
    private static final String EMAIL_PREF = "email";
    private static final String FACEBOOK_PREF = "facebook";
    private static final String GOOGLE_GOGGLES_PREF = "google_goggles";
    private static final String GOOGLE_TALK_PREF = "google_talk";
    private static final String GOOGLE_VOICE_PREF = "google_voice";
    private static final String TWITTER_PREF = "twitter";
    private static final String YOUTUBE_PREF = "youtube";

    private static final String ADWLAUNCHER_PREF = "adwlauncher";
    private static final String FILEMANAGER_PREF = "filemanager";
    private static final String SAVENUM_PREF = "savenum";
    private static final String TERMINAL_PREF = "terminal";
    private static final String TESLA_FLASHLITE_PREF = "tesla_flashlight";
    private static final String TRACKBALL_ALERT_PREF = "trackball_alert";
    private static final String METAMORPH_PREF = "metamorph";
    private static final String BARCODE_PREF = "barcode";
    private static final String CALLLOG_PREF = "notcalllog";

    private static final String BOOTANIM_PREF = "bootanim";
    private static final String WAKE_PREF = "trackball_wake";
    private static final String FONTS_PREF = "fonts";
    private static final String HTC_IME_PREF = "htc_ime";
    private static final String CPU_LED_PREF = "cpu_led";
    private static final String PLAYER_PREF = "player";

    private static final String RELOAD_APPS_PREF = "reload_apps";

    private static final String SYSTEM_PART_SIZE = "system_storage_levels";
    private static final String SYSTEM_STORAGE_PATH = "/system";
    private static final String DATA_PART_SIZE = "data_storage_levels";
    private static final String DATA_STORAGE_PATH = "/data";
    private static final String CACHE_PART_SIZE = "cache_storage_levels";
    private static final String CACHE_STORAGE_PATH = "/cache";
    private static final String SDCARDFAT_PART_SIZE = "sdcardfat_storage_levels";
    private static final String SDCARDFAT_STORAGE_PATH = "/sdcard";
    private static final String SDCARDEXT_PART_SIZE = "sdcardext_storage_levels";
    private static final String SDCARDEXT_STORAGE_PATH = "/system/sd";
    private static final String REFRESH_PREF = "refresh";
    private static final String OLD_APP2SD_PREF = "app2sd_opt";
    private static final String DALVIK2SD_PREF = "dalvik2sd_opt";
    private static final String DATA2SD_PREF = "data2sd_opt";
    private static final String MEDIA2SD_PREF = "media2sd_opt";

    private static final String ABOUT_AUTHOR = "about_author";
    private static final String ABOUT_DONATE = "about_donate";
    private static final String ABOUT_SOURCES = "about_sources";

    private final Configuration mCurConfig = new Configuration();

    private Preference mUpdatePref;
    private int PATCH = 0;

    private ListPreference mApp2sdPref;
    private ListPreference mNotifBarPref;
    private CheckBoxPreference mUiSoundsPref;
    private CheckBoxPreference mFixPermsPref;
    private CheckBoxPreference mFixMarketPref;
    private CheckBoxPreference mZipAlignPref;

    private Preference mRebootPref;
    private Preference mBootloaderPref;
    private Preference mRecoveryPref;
    private Preference mRemountRWPref;
    private Preference mRemountROPref;

    private CheckBoxPreference mCarHomePref;
    private CheckBoxPreference mEmailPref;
    private CheckBoxPreference mFacebookPref;
    private CheckBoxPreference mGoogleGogglesPref;
    private CheckBoxPreference mGoogleTalkPref;
    private CheckBoxPreference mGoogleVoicePref;
    private CheckBoxPreference mTwitterPref;
    private CheckBoxPreference mYouTubePref;

    private CheckBoxPreference mADWLauncherPref;
    private CheckBoxPreference mFileManagerPref;
    private CheckBoxPreference mSaveNumPref;
    private CheckBoxPreference mTerminalPref;
    private CheckBoxPreference mTeslaFlashlightPref;
    private CheckBoxPreference mTrackballAlertPref;
    private CheckBoxPreference mMetamorphPref;
    private CheckBoxPreference mBarcodePref;
    private CheckBoxPreference mCallLogPref;

    private ListPreference mBootanimPref;
    private ListPreference mWakePref;
    private CheckBoxPreference mFontsPref;
    private CheckBoxPreference mHtcImePref;
    private CheckBoxPreference mCpuLedPref;
    private CheckBoxPreference mPlayerPref;

    private Preference mReloadAppsPref;

    private Preference mSystemSize;
    private Preference mDataSize;
    private Preference mCacheSize;
    private Preference mSDCardFATSize;
    private Preference mSDCardEXTSize;
    private Preference mRefresh;
    private CheckBoxPreference mOldApp2sdPref;
    private CheckBoxPreference mDalvik2sdPref;
    private CheckBoxPreference mData2sdPref;
    private CheckBoxPreference mMedia2sdPref;

    private Preference mAboutAuthor;
    private Preference mAboutDonate;
    private Preference mAboutSources;

    private IWindowManager mWindowManager;

    public static String removeChar(String s, char c) {
	String r = "";
	for (int i = 0; i < s.length(); i ++)
	    if (s.charAt(i) != c)
		r += s.charAt(i);
	return r;
    }

    // N: name
    // V: version
    // P: patch
    // B: BETA-build

    public String getRomName() {
	String name = Build.DISPLAY; // N_VpP(-B)?
	name = name.substring(0, name.indexOf('_')); // N
	return name;
    }

    public String getRomVersion() {
	String version = Build.DISPLAY; // N_VpP(-B)?
	version = version.substring(version.indexOf('_') + 1, version.indexOf('p')); // V
	return version;
    }

    public String getRomPatch() {
	String patch = Build.DISPLAY; // N_VpP(-B)?
	if (isRomBeta()) // P-B
	    patch = patch.substring(patch.indexOf('p') + 1, patch.indexOf('-')); // P
	else // P
	    patch = patch.substring(patch.indexOf('p') + 1, patch.length()); // P
	return patch;
    }

    public int getRomBeta() {
	String beta = Build.DISPLAY; // N_VpP(-B)?
	if (isRomBeta()) {
	    beta = beta.substring(beta.indexOf('p'), beta.length());
	    beta = beta.substring(beta.indexOf('-') + 5, beta.indexOf('-') + 6);
	    return Integer.parseInt(beta);
	}
	return 0;
    }

    public boolean isRomBeta() {
	String beta = Build.DISPLAY; // N_VpP(-B)?
	return beta.contains("-BETA");
    }

    public void askToApplyPatch() {
	final int patch = (PATCH - Integer.parseInt(removeChar(getRomVersion(), '.')) * 10);
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Leo Updater")
	    .setMessage("Patch: " + getRomVersion() + "-patch" + patch + "\nWould you like to apply?")
	    .setCancelable(false)
	    .setPositiveButton("Hell yeah", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			setStringSummary(ROM_UPDATE_PREF, " Patch #" + PATCH + " applied.");
			setStringSummary(ROM_NAME_VERSION_PREF, getRomName() + "  /  " + getRomVersion() + "  /  patch" + patch);
			String[] commands = {
			    "/data/local/patch",
			    REMOUNT_RW,
			    "busybox sed -i 's/_" + getRomVersion() + "p" + getRomPatch() + "/_" + getRomVersion() + "p" + patch + "/' /system/build.prop",
			    REMOUNT_RO,
			    "busybox rm -f /data/local/patch"
			};
			sendshell(commands, true, "Applying patch #" + PATCH + "...");
		    }
		})
	    .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
    }

    final Runnable mApplyPatch = new Runnable() {
    	    public void run() {
		askToApplyPatch();
	    }
	};

    public boolean askToUpgrade(final int latestPatch) {
	PATCH = latestPatch;
	Log.i(TAG, "getting: patch-" + PATCH);
	patience = ProgressDialog.show(LeoParts.this, "", "Getting patch #" + PATCH + "...", true);
	Thread t = new Thread() {
		public void run() {
		    String[] commands = {
			"busybox wget -q " + REPO + "patch/patch-" + PATCH + " -O /data/local/patch" +
			" && busybox chmod 755 /data/local/patch"
		    };
		    ShellInterface shell = new ShellInterface(commands);
		    shell.start();
		    while (shell.isAlive())
			{
			    try {
				Thread.sleep(500);
			    }
			    catch (InterruptedException e) {
			    }
			}
		    if (shell.interrupted())
			popup("Error", "Download has finished unexpectedly!");
		    patience.cancel();
		    mHandler.post(mApplyPatch);
		}
	    };
	t.start();
	return true;
    }

    public void launchUpgrade(final String currentVersion, final int latestPatch) {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Leo Updater")
	    .setMessage("Your ROM (" + currentVersion + ") is up-to-date, but new patch is available.")
	    .setCancelable(false)
	    .setPositiveButton("Let's grab it", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			Log.i(TAG, "launchUpgrade");
			dialog.cancel();
			askToUpgrade(latestPatch);
		    }
		})
	    .setNegativeButton("I don't care", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
    }

    final Runnable mUpdateDownloaded = new Runnable() {
    	    public void run() {
		patience.cancel();
		File file = new File("/data/local/tmp/version");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		String version = "0";
		try {
		    fis = new FileInputStream(file);
		    bis = new BufferedInputStream(fis);
		    dis = new DataInputStream(bis);
		    while (dis.available() != 0) {
			version = dis.readLine();
			break ;
		    }
		    fis.close();
		    bis.close();
		    dis.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		// latest
		final int latestVersion = Integer.parseInt(removeChar(version.substring(0, version.indexOf('p')), '.'));
		final int latestPatch = Integer.parseInt(version.substring(version.indexOf('p') + 1, version.length()));
		Log.i(TAG, "latest: " + version);
		setStringSummary(ROM_UPDATE_PREF, " Latest: " + version.replaceFirst("p", "-patch"));
		// current
		final String currentVersionS = getRomVersion();
		final String currentPatchS = getRomPatch();
		final int currentVersion = Integer.parseInt(removeChar(currentVersionS, '.'));
		final int currentPatch = Integer.parseInt(currentPatchS);
		// check
		if (currentVersion < latestVersion)
		    popup("Leo Updater", "Your ROM (" + currentVersionS + "-patch" + currentPatchS + ") is OUTDATED!\nGet the new one (" + version.replaceFirst("p", "-patch") + ") ;)");
		else if (currentVersion == latestVersion)
		    {
			if (currentPatch < latestPatch)
			    launchUpgrade(currentVersionS + "-patch" + currentPatchS,
					  currentVersion * 10 + latestPatch);
			else if (currentPatch == latestPatch)
			    popup("Leo Updater", "Your ROM (" + currentVersionS + "-patch" + currentPatchS + ") is up-to-date!");
			else
			    popup("Leo Updater", "Your ROM (" + currentVersionS + "-patch" + currentPatchS + ") has a BETA patch applied.\nLeo would be proud ;)");
		    }
		else
		    popup("Leo Updater", "Your ROM (" + currentVersionS + "-patch" + currentPatchS + ") is a BETA.\nLeo would be proud ;)");
    	    }
    	};

    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	addPreferencesFromResource(R.xml.leo_parts);

	if (!fileExists("/system/bin/su") && !fileExists("/system/xbin/su"))
	    bad("Full root requiered", "This SpareParts Mod NEEDS full root!\n\n - su binary\n - Superuser.apk\n - busybox binary");

	PreferenceScreen prefSet = getPreferenceScreen();
	REPO = getResources().getString(R.string.repo_url);

	setStringSummary(ROM_NAME_VERSION_PREF, getRomName() + "  /  " + (isRomBeta() ? getRomVersion() + "-BETA" + getRomBeta() : getRomVersion() )+ "  /  patch" + getRomPatch());
	setStringSummary(ROM_SYSTEM_BUILD_PREF, "Android " + Build.VERSION.RELEASE + "  /  " + Build.ID + " " + (fileExists("/system/framework/framework.odex") ? "odex" : "deodex") + "  /  " + getFormattedFingerprint());
	setStringSummary(ROM_RADIO_PREF, getSystemValue("gsm.version.baseband"));
	String kernel = getFormattedKernelVersion();
	findPreference(ROM_KERNEL_PREF).setSummary((kernel.equals("2.6.32.9\nandroid-build@apa26") ? "stock " : "") + kernel);

	mUpdatePref = (Preference) prefSet.findPreference(ROM_UPDATE_PREF);
	findPreference(ROM_UPDATE_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			patience = ProgressDialog.show(LeoParts.this, "", "Checking for latest ROM version and patches...", true);
			Thread t = new Thread() {
				public void run() {
				    String[] commands = {
					"busybox wget -q " + REPO + "version -O /data/local/tmp/version"
				    };
				    ShellInterface shell = new ShellInterface(commands);
				    shell.start();
				    while (shell.isAlive())
					{
					    try {
						Thread.sleep(500);
					    }
					    catch (InterruptedException e) {
					    }
					}
				    if (shell.interrupted())
					popup("Error", "Download or install has finished unexpectedly!");
				    else
					mHandler.post(mUpdateDownloaded);
				}
			    };
			t.start();
			return true;
		    }
		});

	mApp2sdPref = (ListPreference) prefSet.findPreference(APP2SD_PREF);
	mApp2sdPref.setOnPreferenceChangeListener(this);
	mNotifBarPref = (ListPreference) prefSet.findPreference(NOTIF_BAR_PREF);
	mNotifBarPref.setOnPreferenceChangeListener(this);
	mNotifBarPref.setEnabled(false);
	mUiSoundsPref = (CheckBoxPreference) prefSet.findPreference(UI_SOUNDS_PREF);
	mUiSoundsPref.setOnPreferenceChangeListener(this);
	mFixPermsPref = (CheckBoxPreference) prefSet.findPreference(FIX_PERMS_PREF);
	mFixPermsPref.setOnPreferenceChangeListener(this);
	mFixMarketPref = (CheckBoxPreference) prefSet.findPreference(FIX_MARKET_PREF);
	mFixMarketPref.setOnPreferenceChangeListener(this);
	mZipAlignPref = (CheckBoxPreference) prefSet.findPreference(ZIPALIGN_PREF);
	mZipAlignPref.setOnPreferenceChangeListener(this);

	mRebootPref = (Preference) prefSet.findPreference(REBOOT_PREF);
	findPreference(REBOOT_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    "reboot"
			};
			sendshell(commands, false, "Rebooting...");
			return true;
		    }
		});
	mRecoveryPref = (Preference) prefSet.findPreference(BOOTLOADER_PREF);
	findPreference(RECOVERY_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    "reboot recovery"
			};
			sendshell(commands, false, "Rebooting...");
			return true;
		    }
		});
	mBootloaderPref = (Preference) prefSet.findPreference(RECOVERY_PREF);
	findPreference(BOOTLOADER_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    "reboot bootloader"
			};
			sendshell(commands, false, "Rebooting...");
			return true;
		    }
		});
	mRemountRWPref = (Preference) prefSet.findPreference(REMOUNT_RW_PREF);
	findPreference(REMOUNT_RO_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    REMOUNT_RO,
			};
			sendshell(commands, false, "Remounting...");
			return true;
		    }
		});
	mRemountROPref = (Preference) prefSet.findPreference(REMOUNT_RO_PREF);
	findPreference(REMOUNT_RW_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    REMOUNT_RW,
			};
			sendshell(commands, false, "Remounting...");
			return true;
		    }
		});

	mCarHomePref = (CheckBoxPreference) prefSet.findPreference(CAR_HOME_PREF);
	mCarHomePref.setOnPreferenceChangeListener(this);
	mEmailPref = (CheckBoxPreference) prefSet.findPreference(EMAIL_PREF);
	mEmailPref.setOnPreferenceChangeListener(this);
	mFacebookPref = (CheckBoxPreference) prefSet.findPreference(FACEBOOK_PREF);
	mFacebookPref.setOnPreferenceChangeListener(this);
	mGoogleGogglesPref = (CheckBoxPreference) prefSet.findPreference(GOOGLE_GOGGLES_PREF);
	mGoogleGogglesPref.setOnPreferenceChangeListener(this);
	mGoogleTalkPref = (CheckBoxPreference) prefSet.findPreference(GOOGLE_TALK_PREF);
	mGoogleTalkPref.setOnPreferenceChangeListener(this);
	mGoogleVoicePref = (CheckBoxPreference) prefSet.findPreference(GOOGLE_VOICE_PREF);
	mGoogleVoicePref.setOnPreferenceChangeListener(this);
	mTwitterPref = (CheckBoxPreference) prefSet.findPreference(TWITTER_PREF);
	mTwitterPref.setOnPreferenceChangeListener(this);
	mYouTubePref = (CheckBoxPreference) prefSet.findPreference(YOUTUBE_PREF);
	mYouTubePref.setOnPreferenceChangeListener(this);

	mADWLauncherPref = (CheckBoxPreference) prefSet.findPreference(ADWLAUNCHER_PREF);
	mADWLauncherPref.setOnPreferenceChangeListener(this);
	mFileManagerPref = (CheckBoxPreference) prefSet.findPreference(FILEMANAGER_PREF);
	mFileManagerPref.setOnPreferenceChangeListener(this);
	mSaveNumPref = (CheckBoxPreference) prefSet.findPreference(SAVENUM_PREF);
	mSaveNumPref.setOnPreferenceChangeListener(this);
	mTerminalPref = (CheckBoxPreference) prefSet.findPreference(TERMINAL_PREF);
	mTerminalPref.setOnPreferenceChangeListener(this);
	mTeslaFlashlightPref = (CheckBoxPreference) prefSet.findPreference(TESLA_FLASHLITE_PREF);
	mTeslaFlashlightPref.setOnPreferenceChangeListener(this);
	mTrackballAlertPref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_ALERT_PREF);
	mTrackballAlertPref.setOnPreferenceChangeListener(this);
	mMetamorphPref = (CheckBoxPreference) prefSet.findPreference(METAMORPH_PREF);
	mMetamorphPref.setOnPreferenceChangeListener(this);
	mBarcodePref = (CheckBoxPreference) prefSet.findPreference(BARCODE_PREF);
	mBarcodePref.setOnPreferenceChangeListener(this);
	mCallLogPref = (CheckBoxPreference) prefSet.findPreference(CALLLOG_PREF);
	mCallLogPref.setOnPreferenceChangeListener(this);

	mBootanimPref = (ListPreference) prefSet.findPreference(BOOTANIM_PREF);
	mBootanimPref.setOnPreferenceChangeListener(this);
	mWakePref = (ListPreference) prefSet.findPreference(WAKE_PREF);
	mWakePref.setOnPreferenceChangeListener(this);
	mFontsPref = (CheckBoxPreference) prefSet.findPreference(FONTS_PREF);
	mFontsPref.setOnPreferenceChangeListener(this);
	mHtcImePref = (CheckBoxPreference) prefSet.findPreference(HTC_IME_PREF);
	mHtcImePref.setOnPreferenceChangeListener(this);
	mCpuLedPref = (CheckBoxPreference) prefSet.findPreference(CPU_LED_PREF);
	mCpuLedPref.setOnPreferenceChangeListener(this);
	mPlayerPref = (CheckBoxPreference) prefSet.findPreference(PLAYER_PREF);
	mPlayerPref.setOnPreferenceChangeListener(this);

	mReloadAppsPref = (Preference) prefSet.findPreference(RELOAD_APPS_PREF);
	findPreference(RELOAD_APPS_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String[] commands = {
			    "echo 42"
			};
			sendshell(commands, false, "Checking for apps...");
			// popup("", "X apps found!");
			return true;
		    }
		});
	mReloadAppsPref.setEnabled(false);

	mOldApp2sdPref = (CheckBoxPreference) prefSet.findPreference(OLD_APP2SD_PREF);
	mOldApp2sdPref.setOnPreferenceChangeListener(this);
	mDalvik2sdPref = (CheckBoxPreference) prefSet.findPreference(DALVIK2SD_PREF);
	mDalvik2sdPref.setOnPreferenceChangeListener(this);
	mData2sdPref = (CheckBoxPreference) prefSet.findPreference(DATA2SD_PREF);
	mData2sdPref.setOnPreferenceChangeListener(this);
	mMedia2sdPref = (CheckBoxPreference) prefSet.findPreference(MEDIA2SD_PREF);
	mMedia2sdPref.setOnPreferenceChangeListener(this);

	extfsIsMounted     = fileExists("/dev/block/mmcblk0p2");
	mSystemSize        = (Preference) prefSet.findPreference(SYSTEM_PART_SIZE);
	mDataSize          = (Preference) prefSet.findPreference(DATA_PART_SIZE);
	mCacheSize         = (Preference) prefSet.findPreference(CACHE_PART_SIZE);
	mSDCardFATSize     = (Preference) prefSet.findPreference(SDCARDFAT_PART_SIZE);
	mSDCardEXTSize     = (Preference) prefSet.findPreference(SDCARDEXT_PART_SIZE);
	SetupFSPartSize();

	mRefresh = (Preference) prefSet.findPreference(REFRESH_PREF);
	findPreference(REFRESH_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			SetupFSPartSize();
			return true;
		    }
		});

	mAboutAuthor = (Preference) prefSet.findPreference(ABOUT_AUTHOR);
	findPreference(ABOUT_AUTHOR)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String url = "http://forum.xda-developers.com/member.php?u=2398805";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		    }
		});
	mAboutDonate = (Preference) prefSet.findPreference(ABOUT_DONATE);
	findPreference(ABOUT_DONATE)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=FP4JTHPKJPKS6&lc=FR&item_name=leonnib4&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		    }
		});
	mAboutSources = (Preference) prefSet.findPreference(ABOUT_SOURCES);
	findPreference(ABOUT_SOURCES)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			String url = "http://github.com/leonnib4/development_apps_SpareParts";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		    }
		});

	mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

	// Current stock
	mUiSoundsPref.setChecked(fileExists("/system/media/audio/ui/.bak"));
	mOldApp2sdPref.setChecked(fileExists("/system/sd/app") || fileExists("/system/sd/app-private"));
	mDalvik2sdPref.setChecked(fileExists("/system/sd/dalvik-cache"));
	mData2sdPref.setChecked(fileExists("/system/sd/data"));
	mMedia2sdPref.setChecked(fileExists("/system/sd/media"));

	mCarHomePref.setChecked(fileExists("/system/app/CarHomeGoogle.apk"));
	mCarHomePref.setEnabled(fileExists("/system/app/CarHomeGoogle.apk"));
	mEmailPref.setChecked(fileExists("/system/app/EmailGoogle.apk"));
	mEmailPref.setEnabled(fileExists("/system/app/EmailGoogle.apk"));
	mFacebookPref.setChecked(fileExists("/system/app/Facebook.apk"));
	mFacebookPref.setEnabled(fileExists("/system/app/Facebook.apk"));
	mGoogleGogglesPref.setChecked(fileExists("/system/app/GoogleGoggles.apk"));
	mGoogleGogglesPref.setEnabled(fileExists("/system/app/GoogleGoggles.apk"));
	mGoogleTalkPref.setChecked(fileExists("/system/app/Talk.apk"));
	mGoogleTalkPref.setEnabled(fileExists("/system/app/Talk.apk"));
	mGoogleVoicePref.setChecked(fileExists("/system/app/googlevoice.apk"));
	mGoogleVoicePref.setEnabled(fileExists("/system/app/googlevoice.apk"));
	mTwitterPref.setChecked(fileExists("/system/app/Twitter.apk"));
	mTwitterPref.setEnabled(fileExists("/system/app/Twitter.apk"));
	mYouTubePref.setChecked(fileExists("/system/app/YouTube.apk"));
	mYouTubePref.setEnabled(fileExists("/system/app/YouTube.apk"));

	mADWLauncherPref.setChecked(fileExists("/system/app/ADWLauncher.apk"));
	mADWLauncherPref.setEnabled(fileExists("/system/app/ADWLauncher.apk"));
	mFileManagerPref.setChecked(fileExists("/system/app/FileManager.apk"));
	mFileManagerPref.setEnabled(fileExists("/system/app/FileManager.apk"));
	mSaveNumPref.setChecked(fileExists("/system/app/SaveNum.apk"));
	mSaveNumPref.setEnabled(fileExists("/system/app/SaveNum.apk"));
	mTerminalPref.setChecked(fileExists("/system/app/Terminal.apk"));
	mTerminalPref.setEnabled(fileExists("/system/app/Terminal.apk"));
	mTeslaFlashlightPref.setChecked(fileExists("/system/app/TeslaFlashlight.apk"));
	mTeslaFlashlightPref.setEnabled(fileExists("/system/app/TeslaFlashlight.apk"));
	mTrackballAlertPref.setChecked(fileExists("/system/app/TrackballAlert.apk"));
	mTrackballAlertPref.setEnabled(fileExists("/system/app/TrackballAlert.apk"));
	mMetamorphPref.setChecked(fileExists("/system/app/Metamorph.apk"));
	mMetamorphPref.setEnabled(fileExists("/system/app/Metamorph.apk"));
	mBarcodePref.setChecked(fileExists("/system/app/BarcodeScanner.apk"));
	mBarcodePref.setEnabled(fileExists("/system/app/BarcodeScanner.apk"));
	mCallLogPref.setChecked(fileExists("/system/app/NotCallLog.apk"));
	mCallLogPref.setEnabled(fileExists("/system/app/NotCallLog.apk"));

	// Current custom
	if (!fileExists("/system/xbin/nouisounds")) {
	    mUiSoundsPref.setEnabled(false);
	    setStringSummary(UI_SOUNDS_PREF, "Script not found");
	}
	if (!fileExists("/system/xbin/fix_permissions")) {
	    mFixPermsPref.setEnabled(false);
	    setStringSummary(FIX_PERMS_PREF, "Script not found");
	}
	if (!fileExists("/system/xbin/a2sd")) {
	    mOldApp2sdPref.setEnabled(false);
	    setStringSummary(OLD_APP2SD_PREF, "Script not found");
	}
	if (!fileExists("/system/xbin/dalvik2sd")) {
	    mDalvik2sdPref.setEnabled(false);
	    setStringSummary(DALVIK2SD_PREF, "Script not found");
	}
	if (!fileExists("/system/xbin/data2sd")) {
	    mData2sdPref.setEnabled(false);
	    setStringSummary(DATA2SD_PREF, "Script not found");
	}
	if (!fileExists("/system/xbin/media2sd")) {
	    mMedia2sdPref.setEnabled(false);
	    setStringSummary(MEDIA2SD_PREF, "Script not found");
	}

	// Defaults
	mFixPermsPref.setChecked(false);
	mFixMarketPref.setChecked(false);

	// ext relativ
	if (!extfsIsMounted){
	    mOldApp2sdPref.setEnabled(false);
	    setStringSummary(OLD_APP2SD_PREF, "You need an ext3 parition on sdcard");
	    mDalvik2sdPref.setEnabled(false);
	    setStringSummary(DALVIK2SD_PREF, "You need an ext3 parition on sdcard");
	    mData2sdPref.setEnabled(false);
	    setStringSummary(DATA2SD_PREF, "You need an ext3 parition on sdcard");
	    mMedia2sdPref.setEnabled(false);
	    setStringSummary(MEDIA2SD_PREF, "You need an ext3 parition on sdcard");
	}
    }

    private String formatSize(long size) {
	return Formatter.formatFileSize(this, size);
    }

    private String ObtainFSPartSize(String PartitionPath) {
	String retstr;
	File extraPath = new File(PartitionPath);
	StatFs extraStat = new StatFs(extraPath.getPath());
	long eBlockSize = extraStat.getBlockSize();
	long eTotalBlocks = extraStat.getBlockCount();
	long eAvailableBlocks = extraStat.getAvailableBlocks();

	retstr = formatSize((eTotalBlocks * eBlockSize) - (eAvailableBlocks * eBlockSize));
	retstr += "  used out of  ";
	retstr += formatSize(eTotalBlocks * eBlockSize);

	return retstr;
    }

    private void SetupFSPartSize() {
	try {
	    mSystemSize.setSummary(ObtainFSPartSize    (SYSTEM_STORAGE_PATH));
	    mDataSize.setSummary(ObtainFSPartSize      (DATA_STORAGE_PATH));
	    mCacheSize.setSummary(ObtainFSPartSize     (CACHE_STORAGE_PATH));
	    mSDCardFATSize.setSummary(ObtainFSPartSize (SDCARDFAT_STORAGE_PATH));
	    if (extfsIsMounted)
		mSDCardEXTSize.setSummary(ObtainFSPartSize (SDCARDEXT_STORAGE_PATH));
	} catch (IllegalArgumentException e) {
	    Log.w(TAG, "Failed to obtain FS partition sizes");
	    e.printStackTrace();
	}
    }

    public boolean removeSystemApp(final CheckBoxPreference preference, final String name) {
	if (!fileExists("/system/app/" + name + ".apk")) {
	    popup("Error", name + " does not exists in /system/app!");
	    preference.setChecked(false);
	    return false;
	}
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Confirm")
	    .setMessage("Are you sure you want to remove " + name + "?")
	    .setCancelable(false)
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			String[] commands = {
			    REMOUNT_RW,
			    "busybox rm -f /system/app/" + name + ".apk",
			    REMOUNT_RO
			};
			sendshell(commands, false, "Removing " + name + "...");
			preference.setEnabled(false);
		    }
		})
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			preference.setChecked(true);
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
	return true;
    }

    public boolean installOrRemoveAddon(CheckBoxPreference preference, final String src, final boolean reboot, final String name, final String activity) {
	boolean have = preference.isChecked();
	if (!have) {
	    String[] commands = {
		"busybox wget -q " + src + " -O /data/local/tmp/" + activity + ".apk" +
		" && pm install -r /data/local/tmp/" + activity + ".apk ; busybox rm -f /data/local/tmp/" + activity + ".apk"
	    };
	    sendshell(commands, false, "Downloading and installing " + name + "...");
	} else {
	    if (fileExists("/system/app/" + name + ".apk")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox rm -f /system/app/" + name + ".apk",
		    REMOUNT_RO
		};
		sendshell(commands, false, "Removing " + name + "...");
	    }
	    else {
		String[] commands = {
		    "pm uninstall " + activity
		};
		sendshell(commands, false, "Removing " + name + "...");
	    }
	}
	return true;
    }

    public boolean activate2sd(final CheckBoxPreference preference, final String script) {
	boolean have = preference.isChecked();
	if (!have) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Warning")
		.setMessage("With low class SDCard, your phone might get really slow...")
		.setCancelable(false)
		.setPositiveButton("I have a good one", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    dialog.cancel();
			    String[] commands = {
				script + " on"
			    };
			    sendshell(commands, false, "Moving to sdcard...");
			}
		    })
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    dialog.cancel();
			    preference.setChecked(false);
			}
		    });
	    AlertDialog alert = builder.create();
	    alert.show();
	} else {
	    String[] commands = {
		script + " off",
	    };
	    sendshell(commands, false, "Restoring to phone...");
	}
	return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mApp2sdPref) {
	    String[] commands = {
		"pm setInstallLocation " + objValue
	    };
	    sendshell(commands, false, "Activating stock app2sd...");
	}
	else if (preference == mNotifBarPref) {
	    String[] commands = {
		REMOUNT_RW,
		"busybox wget -q " + REPO + objValue.toString() + " -O /data/local/tmp/services.jar" +
		" && busybox mv /data/local/tmp/services.jar /system/framework/services.jar",
		REMOUNT_RO
	    };
	    sendshell(commands, true, "Downloading files and applying new theme...");
	}
	else if (preference == mUiSoundsPref) {
	    boolean have = mUiSoundsPref.isChecked();
	    String[] commands = {
		"nouisounds"
	    };
	    if (!have)
		sendshell(commands, false, "Deactivating UI Sounds...");
	    else
		sendshell(commands, false, "Reactivating UI Sounds...");
	}
	else if (preference == mFixPermsPref) {
	    String[] commands = {
		"fix_permission"
	    };
	    sendshell(commands, true, "Fixing permissions...");
	    return false;
	}
	else if (preference == mFixMarketPref) {
	    String[] commands = {
		"sed -i 's/false/true/' /data/data/com.android.vending/shared_prefs/vending_preferences.xml"
	    };
	    sendshell(commands, true, "Fixing market...");
	    return false;
	}
	else if (preference == mZipAlignPref) {
	    boolean have = mZipAlignPref.isChecked();
	    if (!have) {
		String[] commands = {
		    "zaab on"
		};
		sendshell(commands, true, "Activating zipalign at boot...");
	    }
	    else {
		String[] commands = {
		    "zaab off"
		};
		sendshell(commands, false, "Deactivating zipalign at boot...");
	    }
	}
	else if (preference == mCarHomePref) {
	    final CheckBoxPreference locale = mCarHomePref;
	    if (!fileExists("/system/app/CarHomeGoogle.apk") && !fileExists("/system/app/CarHomeLauncher.apk")) {
		popup("Error", "CarHome does not exists in /system/app!");
		locale.setChecked(false);
		return false;
	    }
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Confirm")
		.setMessage("Are you sure you want to remove CarHome?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    dialog.cancel();
			    String[] commands = {
				REMOUNT_RW,
				"busybox rm -f /system/app/CarHomeGoogle.apk",
				"busybox rm -f /system/app/CarHomeLauncher.apk",
				REMOUNT_RO
			    };
			    sendshell(commands, false, "Removing CarHome...");
			    locale.setEnabled(false);
			}
		    })
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    dialog.cancel();
			    locale.setChecked(true);
			}
		    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	else if (preference == mEmailPref)
	    return removeSystemApp(mEmailPref, "EmailGoogle");
	else if (preference == mFacebookPref)
	    return removeSystemApp(mFacebookPref, "Facebook");
	else if (preference == mGoogleGogglesPref)
	    return removeSystemApp(mGoogleGogglesPref, "GoogleGoggles");
	else if (preference == mGoogleTalkPref)
	    return removeSystemApp(mGoogleTalkPref, "Talk");
	else if (preference == mGoogleVoicePref)
	    return removeSystemApp(mFacebookPref, "googlevoice");
	else if (preference == mTwitterPref)
	    return removeSystemApp(mTwitterPref, "Twitter");
	else if (preference == mYouTubePref)
	    return removeSystemApp(mYouTubePref, "YouTube");
	else if (preference == mADWLauncherPref)
	    return removeSystemApp(mYouTubePref, "ADWLauncher");
	else if (preference == mFileManagerPref)
	    return removeSystemApp(mFileManagerPref, "FileManager");
	else if (preference == mSaveNumPref)
	    return removeSystemApp(mSaveNumPref, "SaveNum");
	else if (preference == mTerminalPref)
	    return removeSystemApp(mTerminalPref, "Terminal");
	else if (preference == mTeslaFlashlightPref)
	    return removeSystemApp(mTeslaFlashlightPref, "TeslaFlashlight");
	else if (preference == mTrackballAlertPref)
	    return removeSystemApp(mTrackballAlertPref, "TrackballAlert");
	else if (preference == mMetamorphPref)
	    return removeSystemApp(mMetamorphPref, "Metamorph");
	else if (preference == mBarcodePref)
	    return removeSystemApp(mBarcodePref, "BarcodeScanner");
	else if (preference == mCallLogPref)
	    return removeSystemApp(mCallLogPref, "NotCallLog");
	else if (preference == mBootanimPref) {
	    String[] commands = {
		REMOUNT_RW,
		"busybox wget -q " + REPO + objValue.toString() + " -O /data/local/tmp/bootanimation.zip" +
		" && busybox mv /data/local/tmp/bootanimation.zip /system/media/bootanimation.zip",
		REMOUNT_RO
	    };
	    sendshell(commands, true, "Downloading and installing " + objValue.toString() + "...");
	}
	else if (preference == mWakePref) {
	    if (objValue.toString().equals("0")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "stock-android.policy.jar -O /data/local/tmp/android.policy.jar" +
		    " && busybox mv /data/local/tmp/android.policy.jar /system/framework/android.policy.jar" +
		    " && busybox chmod 644 /system/framework/android.policy.jar",
		    REMOUNT_RO
		};
		sendshell(commands, true, "Restoring stock parameters...");
	    }
	    else if (objValue.toString().equals("1")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "wake-android.policy.jar -O /data/local/tmp/android.policy.jar" +
		    " && busybox mv /data/local/tmp/android.policy.jar /system/framework/android.policy.jar" +
		    " && busybox chmod 644 /system/framework/android.policy.jar",
		    REMOUNT_RO
		};
		sendshell(commands, true, "Downloading and patching Trackball Wake...");
	    }
	    else if (objValue.toString().equals("2")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "unlock-android.policy.jar -O /data/local/tmp/android.policy.jar" +
		    " && busybox mv /data/local/tmp/android.policy.jar /system/framework/android.policy.jar" +
		    " && busybox chmod 644 /system/framework/android.policy.jar",
		    REMOUNT_RO
		};
		sendshell(commands, true, "Downloading and patching Trackball Wake+unlock...");
	    }
	}
	else if (preference == mFontsPref)
	    return installOrRemoveAddon(mFontsPref, REPO + "Fonts.apk", false, "Fonts", "com.betterandroid.fonts");
	else if (preference == mHtcImePref) {
	    boolean have = mHtcImePref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "clicker.apk -O /data/local/tmp/clicker.apk" +
		    " && pm install -r /data/local/tmp/clicker.apk ; busybox rm -f /data/local/tmp/clicker.apk",
		    "busybox wget -q " + REPO + "htc_ime.apk -O /data/local/tmp/htc_ime.apk" +
		    " && pm install -r /data/local/tmp/htc_ime.apk ; busybox rm -f /data/local/tmp/htc_ime.apk"
		};
		sendshell(commands, false, "Downloading and installing HTC_IME...");
	    }
	    else {
		String[] commands = {
		    "pm uninstall com.htc.clicker",
		    "pm uninstall jonasl.ime"
		};
		sendshell(commands, false, "Removing HTC_IME...");
	    }
	}
	else if (preference == mCpuLedPref)
	    return installOrRemoveAddon(mCpuLedPref, REPO + "cpu_led.apk", false, "CPU Led", "com.britoso.cpustatusled");
	else if (preference == mPlayerPref)
	    return installOrRemoveAddon(mPlayerPref, REPO + "player.apk", false, "RockPlayer", "org.freecoder.android.cmplayer");
	else if (preference == mOldApp2sdPref)
	    return activate2sd(mOldApp2sdPref, "a2sd");
	else if (preference == mDalvik2sdPref)
	    return activate2sd(mDalvik2sdPref, "dalvik2sd");
	else if (preference == mData2sdPref)
	    return activate2sd(mData2sdPref, "data2sd");
	else if (preference == mMedia2sdPref)
	    return activate2sd(mMedia2sdPref, "media2sd");

	// always let the preference setting proceed.
	return true;
    }

    public boolean fileExists(String filename) {
    	File f = new File(filename);
    	return f.exists();
    }

    public void setStringSummary(String preference, String value) {
    	try {
    	    findPreference(preference).setSummary(value);
    	} catch (RuntimeException e) {
    	    findPreference(preference).setSummary(" Unavailable");
    	}
    }

    public String getSystemValue(String property) {
	try {
	    return SystemProperties.get(property, " Unavailable");
	} catch (RuntimeException e) {
	}
	return " Unavailable";
    }

    public String getFormattedFingerprint() {
	String[] tab = new String(Build.FINGERPRINT).split("/");
	return new String(tab[4]);
    }

    private String getFormattedKernelVersion() {
	String procVersionStr;
	try {
	    BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
	    try {
		procVersionStr = reader.readLine();
	    } finally {
		reader.close();
	    }
	    final String PROC_VERSION_REGEX =
		"\\w+\\s+" + /* ignore: Linux */
		"\\w+\\s+" + /* ignore: version */
		"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
		"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
		"\\(.*?(?:\\(.*?\\)).*?\\)\\s+" + /* ignore: (gcc ..) */
		"([^\\s]+)\\s+" + /* group 3: #26 */
		"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
		"(.+)"; /* group 4: date */
	    Pattern p = Pattern.compile(PROC_VERSION_REGEX);
	    Matcher m = p.matcher(procVersionStr);
	    if (!m.matches()) {
		Log.e(TAG, "Regex did not match on /proc/version: " + procVersionStr);
		return " Unavailable";
	    } else if (m.groupCount() < 4) {
		Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount() + " groups");
		return " Unavailable";
	    } else {
		return (new StringBuilder(m.group(1).substring(0, m.group(1).indexOf('-')))
			.append("\n")
			.append(m.group(2))
			.toString());
	    }
	} catch (IOException e) {
	    Log.e(TAG, "IO Exception when getting kernel version for Device Info screen", e);
	    return " Unavailable";
	}
    }

    public long getFileSize(String filename) {
    	File file = new File(filename);
    	if (!file.exists() || !file.isFile()) {
    	    Log.w(TAG, "No such file: " + filename);
    	    return -1;
    	}
    	return file.length();
    }

    final Runnable mCommandFinished = new Runnable() {
    	    public void run() {
		patience.cancel();
    	    }
    	};

    final Runnable mNeedReboot = new Runnable() {
    	    public void run() {
    		needreboot();
    	    }
    	};

    public boolean sendshell(final String[] commands, final boolean reboot, final String message) {
	if (message != null)
	    patience = ProgressDialog.show(this, "", message, true);
	Thread t = new Thread() {
		public void run() {
		    ShellInterface shell = new ShellInterface(commands);
		    shell.start();
		    while (shell.isAlive())
			{
			    if (message != null)
				patience.setProgress(shell.getStatus());
			    try {
				Thread.sleep(500);
			    }
			    catch (InterruptedException e) {
			    }
			}
		    if (message != null)
			mHandler.post(mCommandFinished);
		    if (shell.interrupted())
			popup("Error", "Download or install has finished unexpectedly!");
		    if (reboot == true)
			mHandler.post(mNeedReboot);
		}
	    };
	t.start();
	return true;
    }

    public void needreboot() {
	Log.i(TAG, "needreboot");
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage("Reboot is requiered to apply. Would you like to reboot now?")
	    .setCancelable(false)
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			String[] commands = {
			    "reboot"
			};
			sendshell(commands, false, "Rebooting...");
		    }
		})
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
    }

    public void popup(final String title, final String message) {
	Log.i(TAG, "popup");
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle(title)
	    .setMessage(message)
	    .setCancelable(false)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
    }

    public void bad(final String title, final String message) {
	Log.i(TAG, "bad");
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle(title)
	    .setMessage(message)
	    .setCancelable(false)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			finish();
		    }
		});
	AlertDialog alert = builder.create();
	alert.show();
    }

    @Override
    public void onResume() {
	super.onResume();
    }
}
