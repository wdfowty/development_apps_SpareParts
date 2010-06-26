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

package com.android.spare_parts;

import com.android.spare_parts.ShellInterface;

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
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SpareParts extends PreferenceActivity
	implements Preference.OnPreferenceChangeListener,
	SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SpareParts";
    private static final String REMOUNT_RO = "mount -o ro,remount -t yaffs2 /dev/block/mtdblock3 /system";
    private static final String REMOUNT_RW = "mount -o rw,remount -t yaffs2 /dev/block/mtdblock3 /system";
    private static String REPO;

    ProgressDialog patience = null;

    final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
    	    public void run() {
    		needreboot();
    	    }
    	};

    private boolean extfsIsMounted = false;

    private static final String ROM_NAME_PREF = "rom_name";
    private static final String ROM_VERSION_PREF = "rom_version";
    private static final String ROM_BUILD_PREF = "rom_build";
    private static final String ROM_FINGERPRINT_PREF = "rom_fingerprint";
    private static final String ROM_RADIO_PREF = "rom_radio";
    private static final String ROM_KERNEL_PREF = "rom_kernel";
    private static final String ROM_SYS_VERSION_PREF = "rom_sys_version";
    private static final String ROM_UPDATE_PREF = "rom_update";

    private static final String APP2SD_PREF = "app2sd";
    private static final String UI_SOUNDS_PREF = "ui_sounds";
    private static final String FIX_PERMS_PREF = "fix_perms";

    private static final String REBOOT_PREF = "reboot_reboot";
    private static final String RECOVERY_PREF = "reboot_recovery";
    private static final String BOOTLOADER_PREF = "reboot_bootloader";
    private static final String REMOUNT_RW_PREF = "remount_rw";
    private static final String REMOUNT_RO_PREF = "remount_ro";

    private static final String CAR_HOME_PREF = "car_home";
    private static final String EMAIL_PREF = "email";
    private static final String FACEBOOK_PREF = "facebook";
    private static final String TWITTER_PREF = "twitter";
    private static final String YOUTUBE_PREF = "youtube";

    private static final String BOOTANIM_PREF = "bootanim";
    private static final String NOTIFBAR_PREF = "notifbar";
    private static final String TRACKBALL_PREF = "trackball";
    private static final String REMVOL_PREF = "remvol";
    private static final String WAKE_PREF = "wake";
    private static final String HTC_IME_PREF = "htc_ime";
    private static final String CPU_LED_PREF = "cpu_led";
    private static final String LAUNCHER2_PREF = "launcher2";
    private static final String GALAXY_LWP_PREF = "galaxy_lwp";

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
    private static final String OLD_APP2SD_PREF = "old_app2sd_opt";
    private static final String DCCACHE_PREF = "dccache_opt";
    private static final String DCSDCARD_PREF = "dcsdcard_opt";

    private static final String ABOUT_AUTHOR = "about_author";
    private static final String ABOUT_DONATE = "about_donate";
    private static final String ABOUT_SOURCES = "about_sources";

    private static final String BATTERY_HISTORY_PREF = "battery_history_settings";
    private static final String BATTERY_INFORMATION_PREF = "battery_information_settings";
    private static final String USAGE_STATISTICS_PREF = "usage_statistics_settings";

    private static final String WINDOW_ANIMATIONS_PREF = "window_animations";
    private static final String TRANSITION_ANIMATIONS_PREF = "transition_animations";
    private static final String FANCY_IME_ANIMATIONS_PREF = "fancy_ime_animations";
    private static final String HAPTIC_FEEDBACK_PREF = "haptic_feedback";
    private static final String FONT_SIZE_PREF = "font_size";
    private static final String END_BUTTON_PREF = "end_button";
    private static final String MAPS_COMPASS_PREF = "maps_compass";
    private static final String KEY_COMPATIBILITY_MODE = "compatibility_mode";

    private final Configuration mCurConfig = new Configuration();

    private Preference mUpdatePref;

    private ListPreference mApp2sdPref;
    private CheckBoxPreference mUiSoundsPref;
    private CheckBoxPreference mFixPermsPref;

    private Preference mRebootPref;
    private Preference mBootloaderPref;
    private Preference mRecoveryPref;
    private Preference mRemountRWPref;
    private Preference mRemountROPref;

    private CheckBoxPreference mCarHomePref;
    private CheckBoxPreference mEmailPref;
    private CheckBoxPreference mFacebookPref;
    private CheckBoxPreference mTwitterPref;
    private CheckBoxPreference mYouTubePref;

    private ListPreference mBootanimPref;
    private ListPreference mNotifbarPref;
    private CheckBoxPreference mTrackballPref;
    private CheckBoxPreference mRemvolPref;
    private CheckBoxPreference mWakePref;
    private CheckBoxPreference mHtcImePref;
    private CheckBoxPreference mCpuLedPref;
    private CheckBoxPreference mLauncher2Pref;
    private CheckBoxPreference mGalaxyLWPPref;

    private Preference mSystemSize;
    private Preference mDataSize;
    private Preference mCacheSize;
    private Preference mSDCardFATSize;
    private Preference mSDCardEXTSize;
    private CheckBoxPreference mOldApp2sdPref;
    private CheckBoxPreference mDcCachePref;
    private CheckBoxPreference mDcSdcardPref;

    private Preference mAboutAuthor;
    private Preference mAboutDonate;
    private Preference mAboutSources;

    private ListPreference mWindowAnimationsPref;
    private ListPreference mTransitionAnimationsPref;
    private CheckBoxPreference mFancyImeAnimationsPref;
    private CheckBoxPreference mHapticFeedbackPref;
    private ListPreference mFontSizePref;
    private ListPreference mEndButtonPref;
    private CheckBoxPreference mShowMapsCompassPref;
    private CheckBoxPreference mCompatibilityMode;

    private IWindowManager mWindowManager;

    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context,
	    PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {

	Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
	if (preference == null) {
	    return false;
	}

	Intent intent = preference.getIntent();
	if (intent != null) {
	    // Find the activity that is in the system image
	    PackageManager pm = context.getPackageManager();
	    List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
	    int listSize = list.size();
	    for (int i = 0; i < listSize; i++) {
		ResolveInfo resolveInfo = list.get(i);
		if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
			!= 0) {

		    // Replace the intent with this specific activity
		    preference.setIntent(new Intent().setClassName(
			    resolveInfo.activityInfo.packageName,
			    resolveInfo.activityInfo.name));

		    return true;
		}
	    }
	}

	// Did not find a matching activity, so remove the preference
	parentPreferenceGroup.removePreference(preference);

	return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	addPreferencesFromResource(R.xml.spare_parts);

	if (!fileExists("/system/bin/su") && !fileExists("/system/xbin/su"))
	    bad("Root requiered", "This SpareParts Mod NEEDS full root!");

	PreferenceScreen prefSet = getPreferenceScreen();
	REPO = getResources().getString(R.string.repo_url);

	String build = Build.DISPLAY;
	String rom_name = build;
	if (build.contains("_"))
	    rom_name = build.substring(0, build.indexOf('_'));

	setStringSummary(ROM_NAME_PREF, rom_name);
	if (build.contains("_"))
	    setStringSummary(ROM_VERSION_PREF, build.substring(build.indexOf('_') + 1));
	else
	    setStringSummary(ROM_VERSION_PREF, " Unavailable");
	setStringSummary(ROM_BUILD_PREF, Build.ID + " " + (fileExists("/system/framework/framework.odex") ? "odex" : "deodex"));
	setStringSummary(ROM_FINGERPRINT_PREF, getFormattedFingerprint());
	String radio = getSystemValue("gsm.version.baseband");
	setStringSummary(ROM_RADIO_PREF, radio.substring(radio.indexOf('_') + 1, radio.length()));
	setStringSummary(ROM_SYS_VERSION_PREF, "Android " + Build.VERSION.RELEASE);
	findPreference(ROM_KERNEL_PREF).setSummary(getFormattedKernelVersion());

	mUpdatePref = (Preference) prefSet.findPreference(ROM_UPDATE_PREF);
	findPreference(ROM_UPDATE_PREF)
	    .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
			try {
			    URL url = new URL(REPO + "version");
			    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			    String inputLine = in.readLine();
			    in.close();
			} catch (MalformedURLException e) {
			    Log.w(TAG, "Bad URL");
			} catch (IOException e) {
			    Log.w(TAG, "Could not read from URL");
			}
			return true;
		    }
		});
	mUpdatePref.setEnabled(false);

	mApp2sdPref = (ListPreference) prefSet.findPreference(APP2SD_PREF);
	mApp2sdPref.setOnPreferenceChangeListener(this);
	mUiSoundsPref = (CheckBoxPreference) prefSet.findPreference(UI_SOUNDS_PREF);
	mUiSoundsPref.setOnPreferenceChangeListener(this);
	mFixPermsPref = (CheckBoxPreference) prefSet.findPreference(FIX_PERMS_PREF);
	mFixPermsPref.setOnPreferenceChangeListener(this);

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
	mTwitterPref = (CheckBoxPreference) prefSet.findPreference(TWITTER_PREF);
	mTwitterPref.setOnPreferenceChangeListener(this);
	mYouTubePref = (CheckBoxPreference) prefSet.findPreference(YOUTUBE_PREF);
	mYouTubePref.setOnPreferenceChangeListener(this);

	mBootanimPref = (ListPreference) prefSet.findPreference(BOOTANIM_PREF);
	mBootanimPref.setOnPreferenceChangeListener(this);
	mNotifbarPref = (ListPreference) prefSet.findPreference(NOTIFBAR_PREF);
	mNotifbarPref.setOnPreferenceChangeListener(this);
	mNotifbarPref.setEnabled(false);
	mTrackballPref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_PREF);
	mTrackballPref.setOnPreferenceChangeListener(this);
	mRemvolPref = (CheckBoxPreference) prefSet.findPreference(REMVOL_PREF);
	mRemvolPref.setOnPreferenceChangeListener(this);
	mWakePref = (CheckBoxPreference) prefSet.findPreference(WAKE_PREF);
	mWakePref.setOnPreferenceChangeListener(this);
	mHtcImePref = (CheckBoxPreference) prefSet.findPreference(HTC_IME_PREF);
	mHtcImePref.setOnPreferenceChangeListener(this);
	mCpuLedPref = (CheckBoxPreference) prefSet.findPreference(CPU_LED_PREF);
	mCpuLedPref.setOnPreferenceChangeListener(this);
	mLauncher2Pref = (CheckBoxPreference) prefSet.findPreference(LAUNCHER2_PREF);
	mLauncher2Pref.setOnPreferenceChangeListener(this);
	mGalaxyLWPPref = (CheckBoxPreference) prefSet.findPreference(GALAXY_LWP_PREF);
	mGalaxyLWPPref.setOnPreferenceChangeListener(this);

	mOldApp2sdPref = (CheckBoxPreference) prefSet.findPreference(OLD_APP2SD_PREF);
	mOldApp2sdPref.setOnPreferenceChangeListener(this);
	mOldApp2sdPref.setEnabled(false);
	mDcCachePref = (CheckBoxPreference) prefSet.findPreference(DCCACHE_PREF);
	mDcCachePref.setOnPreferenceChangeListener(this);
	mDcCachePref.setEnabled(false);
	mDcSdcardPref = (CheckBoxPreference) prefSet.findPreference(DCSDCARD_PREF);
	mDcSdcardPref.setOnPreferenceChangeListener(this);
	mDcSdcardPref.setEnabled(false);

	extfsIsMounted     = SystemProperties.get("ep.extfs.mounted", "0").equals("1");
	mSystemSize        = (Preference) prefSet.findPreference(SYSTEM_PART_SIZE);
	mDataSize          = (Preference) prefSet.findPreference(DATA_PART_SIZE);
	mCacheSize         = (Preference) prefSet.findPreference(CACHE_PART_SIZE);
	mSDCardFATSize     = (Preference) prefSet.findPreference(SDCARDFAT_PART_SIZE);
	mSDCardEXTSize     = (Preference) prefSet.findPreference(SDCARDEXT_PART_SIZE);
	SetupFSPartSize();

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

	mWindowAnimationsPref = (ListPreference) prefSet.findPreference(WINDOW_ANIMATIONS_PREF);
	mWindowAnimationsPref.setOnPreferenceChangeListener(this);
	mTransitionAnimationsPref = (ListPreference) prefSet.findPreference(TRANSITION_ANIMATIONS_PREF);
	mTransitionAnimationsPref.setOnPreferenceChangeListener(this);
	mFancyImeAnimationsPref = (CheckBoxPreference) prefSet.findPreference(FANCY_IME_ANIMATIONS_PREF);
	mHapticFeedbackPref = (CheckBoxPreference) prefSet.findPreference(HAPTIC_FEEDBACK_PREF);
	mFontSizePref = (ListPreference) prefSet.findPreference(FONT_SIZE_PREF);
	mFontSizePref.setOnPreferenceChangeListener(this);
	mEndButtonPref = (ListPreference) prefSet.findPreference(END_BUTTON_PREF);
	mEndButtonPref.setOnPreferenceChangeListener(this);
	mShowMapsCompassPref = (CheckBoxPreference) prefSet.findPreference(MAPS_COMPASS_PREF);
	mCompatibilityMode = (CheckBoxPreference) findPreference(KEY_COMPATIBILITY_MODE);
	mCompatibilityMode.setPersistent(false);
	mCompatibilityMode.setChecked(Settings.System.getInt(getContentResolver(),
		Settings.System.COMPATIBILITY_MODE, 1) != 0);

	mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

	// Current
	mUiSoundsPref.setChecked(fileExists("/system/media/audio/ui/.bak"));
	mCarHomePref.setChecked(fileExists("/system/app/CarHomeGoogle.apk"));
	mEmailPref.setChecked(fileExists("/system/app/EmailGoogle.apk"));
	mFacebookPref.setChecked(fileExists("/system/app/Facebook.apk"));
	mTwitterPref.setChecked(fileExists("/system/app/Twitter.apk"));
	mYouTubePref.setChecked(fileExists("/system/app/YouTube.apk"));
	mLauncher2Pref.setChecked(fileExists("/system/app/Launcher2.apk"));

	// Defaults
	mFixPermsPref.setChecked(false);
	mBootanimPref.setDefaultValue(1);
	mNotifbarPref.setDefaultValue(1);

	// Enabled
	if(!extfsIsMounted){
	    mOldApp2sdPref.setEnabled(false);
	    mDcSdcardPref.setEnabled(false);
	    mSDCardEXTSize.setEnabled(false);
	    mSDCardEXTSize.setSummary("no extfs partition");
	}
	mUiSoundsPref.setEnabled(fileExists("/system/xbin/nouisounds"));
	mFixPermsPref.setEnabled(fileExists("/system/xbin/fix_permissions"));

	if (!fileExists("/system/bin/busybox") && !fileExists("/system/xbin/busybox")) {
	    popup("Full root requiered", "This SpareParts Mod NEEDS root, plus busybox installed!");
	    // disable all Addons
	    mBootanimPref.setEnabled(false);
	    mNotifbarPref.setEnabled(false);
	    mTrackballPref.setEnabled(false);
	    mRemvolPref.setEnabled(false);
	    mWakePref.setEnabled(false);
	    mHtcImePref.setEnabled(false);
	    mCpuLedPref.setEnabled(false);
	    mLauncher2Pref.setEnabled(false);
	    mGalaxyLWPPref.setEnabled(false);
	}

	final PreferenceGroup parentPreference = getPreferenceScreen();
	updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
						   BATTERY_HISTORY_PREF, 0);
	updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
						   BATTERY_INFORMATION_PREF, 0);
	updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
						   USAGE_STATISTICS_PREF, 0);

	parentPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

	retstr = formatSize(eAvailableBlocks * eBlockSize);
	retstr += "  /  ";
	retstr += formatSize(eTotalBlocks * eBlockSize);

	return retstr;
    }

    private void SetupFSPartSize() {
	try {
	    mSystemSize.setSummary(ObtainFSPartSize    (SYSTEM_STORAGE_PATH));
	    mDataSize.setSummary(ObtainFSPartSize      (DATA_STORAGE_PATH));
	    mCacheSize.setSummary(ObtainFSPartSize     (CACHE_STORAGE_PATH));
	    mSDCardFATSize.setSummary(ObtainFSPartSize (SDCARDFAT_STORAGE_PATH));
	    if(extfsIsMounted)
		mSDCardEXTSize.setSummary(ObtainFSPartSize (SDCARDEXT_STORAGE_PATH));
	} catch (IllegalArgumentException e) {
	    Log.w(TAG, "Failed to obtain FS partition sizes");
	    e.printStackTrace();
	}
    }

    private void updateToggles() {
	try {
	    mFancyImeAnimationsPref.setChecked(Settings.System.getInt(
		    getContentResolver(),
		    Settings.System.FANCY_IME_ANIMATIONS, 0) != 0);
	    mHapticFeedbackPref.setChecked(Settings.System.getInt(
		    getContentResolver(),
		    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0);
	    Context c = createPackageContext("com.google.android.apps.maps", 0);
	    mShowMapsCompassPref.setChecked(c.getSharedPreferences("extra-features", MODE_WORLD_READABLE)
		.getBoolean("compass", false));
	} catch (NameNotFoundException e) {
	    Log.w(TAG, "Failed reading maps compass");
	    e.printStackTrace();
	}
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mWindowAnimationsPref) {
	    writeAnimationPreference(0, objValue);
	} else if (preference == mTransitionAnimationsPref) {
	    writeAnimationPreference(1, objValue);
	} else if (preference == mFontSizePref) {
	    writeFontSizePreference(objValue);
	} else if (preference == mEndButtonPref) {
	    writeEndButtonPreference(objValue);
	}
	else if (preference == mApp2sdPref) {
	    String[] commands = {
		"pm setInstallLocation " + objValue
	    };
	    sendshell(commands, false, "Activating app2sd...");
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
	    sendshell(commands, false, "Fixing permissions...");
	    return false;
	}
	else if (preference == mCarHomePref) {
	    String[] commands = {
		REMOUNT_RW,
		"rm /system/app/CarHomeGoogle.apk",
		"rm /system/app/CarHomeLauncher.apk",
		REMOUNT_RO,
	    };
	    sendshell(commands, false, "Removing Car Home...");
	}
	else if (preference == mEmailPref) {
	    String[] commands = {
		REMOUNT_RW,
		"rm /system/app/EmailGoogle.apk",
		REMOUNT_RO,
	    };
	    sendshell(commands, false, "Removing Email...");
	}
	else if (preference == mFacebookPref) {
	    String[] commands = {
		REMOUNT_RW,
		"rm /system/app/Facebook.apk",
		REMOUNT_RO,
	    };
	    sendshell(commands, false, "Removing Facebook...");
	}
	else if (preference == mTwitterPref) {
	    String[] commands = {
		REMOUNT_RW,
		"rm /system/app/Twitter.apk",
		REMOUNT_RO,
	    };
	    sendshell(commands, false, "Removing Twitter...");
	}
	else if (preference == mYouTubePref) {
	    String[] commands = {
		REMOUNT_RW,
		"rm /system/app/YouTube.apk",
		REMOUNT_RO,
	    };
	    sendshell(commands, false, "Removing YouTube...");
	}
	else if (preference == mBootanimPref) {
	    String[] commands = {
		REMOUNT_RW,
		"busybox wget -q " + REPO + objValue.toString() + " -O /data/local/tmp/bootanimation.zip",
		"busybox mv /data/local/tmp/bootanimation.zip /system/media/bootanimation.zip",
		REMOUNT_RO,
	    };
	    sendshell(commands, true, "Downloading and installing bootanimation...");
	}
	else if (preference == mNotifbarPref) {
	    if (objValue.toString().equals("0")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "blackonwhite-framework.jar -O /data/local/tmp/framework.jar",
		    "busybox mv /data/local/tmp/tmp/framework.jar /system/framework/framework.jar",
		    "busybox wget -q " + REPO + "blackonwhite-services.jar -O /data/local/tmp/services.jar",
		    "busybox mv /data/local/tmp/services.jar /system/framework/services.jar",
		    "busybox wget -q " + REPO + "whitebg-framework-res.apk -O /data/local/tmp/framework-res.apk",
		    "busybox mv /data/local/tmp/framework-res.apk /system/framework/framework-res.apk",
		    REMOUNT_RO,
		};
		sendshell(commands, true, "Downloading and installing framework...");
	    } else if (objValue.toString().equals("1")) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "whiteonblack-framework.jar -O /data/local/tmp/framework.jar",
		    "busybox mv /data/local/tmp/tmp/framework.jar /system/framework/framework.jar",
		    "busybox wget -q " + REPO + "whiteonblack-services.jar -O /data/local/tmp/services.jar",
		    "busybox mv /data/local/tmp/services.jar /system/framework/services.jar",
		    "busybox wget -q " + REPO + "blackbg-framework-res.apk -O /data/local/tmp/framework-res.apk",
		    "busybox mv /data/local/tmp/framework-res.apk /system/framework/framework-res.apk",
		    REMOUNT_RO,
		};
		sendshell(commands, true, "Downloading and installing framework...");
	    }
	}
	else if (preference == mTrackballPref) {
	    boolean have = mTrackballPref.isChecked();
	    if (!have) {
		String[] commands = {
		    // REMOUNT_RW,
		    // "busybox wget -q " + REPO + "framework.jar -O /data/local/tmp/framework.jar",
		    // "busybox mv /data/local/tmp/tmp/framework.jar /system/framework/framework.jar",
		    // REMOUNT_RO,
		    "busybox wget -q " + REPO + "trackball.apk -O /data/local/tmp/trackball.apk",
		    "pm install -r /data/local/tmp/trackball.apk"
		};
		sendshell(commands, false, "Downloading and installing Trackball Alert...");
	    } else {
		String[] commands = {
		    "pm uninstall uk.co.lilhermit.android.TrackballAlert"
		};
		sendshell(commands, false, "Removing Trackball Alert...");
	    }
	}
	else if (preference == mRemvolPref) {
	    boolean have = mRemvolPref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "remvol.apk -O /data/local/tmp/remvol.apk",
		    "pm install -r /data/local/tmp/remvol.apk"
		};
		sendshell(commands, false, "Downloading and installing RemVol...");
	    } else {
		String[] commands = {
		    "pm uninstall com.too.remvol"
		};
		sendshell(commands, false, "Removing RemVol...");
	    }
	}
	else if (preference == mWakePref) {
	    boolean have = mWakePref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "wake.apk -O /data/local/tmp/wake.apk",
		    "pm install -r /data/local/tmp/wake.apk",
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "myLock.xml -O /data/local/tmp/myLock.xml",
		    "busybox mkdir -p /data/data/i4nc4mp.myLock.froyo/shared_prefs",
		    "busybox mv /data/local/tmp/myLock.xml /data/data/i4nc4mp.myLock.froyo/shared_prefs/myLock.xml",
		    REMOUNT_RO,
		};
		sendshell(commands, false, "Downloading and installing myLock...");
	    } else {
		String[] commands = {
		    "pm uninstall i4nc4mp.myLock.froyo"
		};
		sendshell(commands, false, "Removing myLock...");
	    }
	}
	else if (preference == mHtcImePref) {
	    boolean have = mHtcImePref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "clicker.apk -O /data/local/tmp/clicker.apk",
		    "pm install -r /data/local/tmp/clicker.apk",
		    "busybox wget -q " + REPO + "htc_ime.apk -O /data/local/tmp/htc_ime.apk",
		    "pm install -r /data/local/tmp/htc_ime.apk"
		};
		sendshell(commands, false, "Downloading and installing HTC_IME...");
	    } else {
		String[] commands = {
		    "pm uninstall com.htc.clicker",
		    "pm uninstall jonasl.ime"
		};
		sendshell(commands, false, "Removing HTC_IME...");
	    }
	}
	else if (preference == mCpuLedPref) {
	    boolean have = mCpuLedPref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "cpu_led.apk -O /data/local/tmp/cpu_led.apk",
		    "pm install -r /data/local/tmp/cpu_led.apk"
		};
		sendshell(commands, false, "Downloading and installing NetMeter+LED...");
	    } else {
		String[] commands = {
		    "pm uninstall com.google.android.netmeterled"
		};
		sendshell(commands, false, "Removing NetMeter+LED...");
	    }
	}
	else if (preference == mLauncher2Pref) {
	    boolean have = mLauncher2Pref.isChecked();
	    if (!have) {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox wget -q " + REPO + "Launcher2.apk -O /data/local/tmp/Launcher2.apk",
		    "busybox mv /data/local/tmp/Launcher2.apk /system/app/Launcher2.apk",
		    REMOUNT_RO,
		};
		sendshell(commands, false, "Downloading and installing stock Launcher2...");
	    } else {
		String[] commands = {
		    REMOUNT_RW,
		    "busybox rm /system/app/Launcher2.apk",
		    REMOUNT_RO,
		};
		sendshell(commands, false, "Removing stock Launcher2...");
	    }
	}
	else if (preference == mGalaxyLWPPref) {
	    boolean have = mGalaxyLWPPref.isChecked();
	    if (!have) {
		String[] commands = {
		    "busybox wget -q " + REPO + "TATLiveWallpapersAurora.apk -O /data/local/tmp/TATLiveWallpapersAurora.apk",
		    "pm install -r /data/local/tmp/TATLiveWallpapersAurora.apk",
		    "busybox wget -q " + REPO + "TATLiveWallpapersBlueSea.apk -O /data/local/tmp/TATLiveWallpapersBlueSea.apk",
		    "pm install -r /data/local/tmp/TATLiveWallpapersBlueSea.apk",
		    "busybox wget -q " + REPO + "TATLiveWallpapersDandelion.apk -O /data/local/tmp/TATLiveWallpapersDandelion.apk",
		    "pm install -r /data/local/tmp/TATLiveWallpapersDandelion.apk",
		    "busybox wget -q " + REPO + "TATLiveWallpapersOceanWave.apk -O /data/local/tmp/TATLiveWallpapersOceanWave.apk",
		    "pm install -r /data/local/tmp/TATLiveWallpapersOceanWave.apk"
		};
		sendshell(commands, true, "Downloading and installing Galaxy LWPs...");
	    } else {
		String[] commands = {
		    "pm uninstall ",
		    "pm uninstall ",
		    "pm uninstall ",
		    "pm uninstall "
		};
		sendshell(commands, false, "Removing Galaxy LWPs...");
	    }
	}
	// always let the preference setting proceed.
	return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	if (preference == mCompatibilityMode) {
	    Settings.System.putInt(getContentResolver(),
		    Settings.System.COMPATIBILITY_MODE,
		    mCompatibilityMode.isChecked() ? 1 : 0);
	    return true;
	}
	return false;
    }

    public void writeAnimationPreference(int which, Object objValue) {
	try {
	    float val = Float.parseFloat(objValue.toString());
	    mWindowManager.setAnimationScale(which, val);
	} catch (NumberFormatException e) {
	} catch (RemoteException e) {
	}
    }

    public void writeFontSizePreference(Object objValue) {
	try {
	    mCurConfig.fontScale = Float.parseFloat(objValue.toString());
	    ActivityManagerNative.getDefault().updateConfiguration(mCurConfig);
	} catch (RemoteException e) {
	}
    }

    public void writeEndButtonPreference(Object objValue) {
	try {
	    int val = Integer.parseInt(objValue.toString());
	    Settings.System.putInt(getContentResolver(),
		    Settings.System.END_BUTTON_BEHAVIOR, val);
	} catch (NumberFormatException e) {
	}
    }

    int floatToIndex(float val, int resid) {
	String[] indices = getResources().getStringArray(resid);
	float lastVal = Float.parseFloat(indices[0]);
	for (int i=1; i<indices.length; i++) {
	    float thisVal = Float.parseFloat(indices[i]);
	    if (val < (lastVal + (thisVal-lastVal)*.5f)) {
		return i-1;
	    }
	    lastVal = thisVal;
	}
	return indices.length-1;
    }

    public void readAnimationPreference(int which, ListPreference pref) {
	try {
	    float scale = mWindowManager.getAnimationScale(which);
	    pref.setValueIndex(floatToIndex(scale,
		    R.array.entryvalues_animations));
	} catch (RemoteException e) {
	}
    }

    public void readFontSizePreference(ListPreference pref) {
	try {
	    mCurConfig.updateFrom(
		ActivityManagerNative.getDefault().getConfiguration());
	} catch (RemoteException e) {
	}
	pref.setValueIndex(floatToIndex(mCurConfig.fontScale,
		R.array.entryvalues_font_size));
    }

    public void readEndButtonPreference(ListPreference pref) {
	try {
	    pref.setValueIndex(Settings.System.getInt(getContentResolver(),
		    Settings.System.END_BUTTON_BEHAVIOR));
	} catch (SettingNotFoundException e) {
	}
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
	if (FANCY_IME_ANIMATIONS_PREF.equals(key)) {
	    Settings.System.putInt(getContentResolver(),
		    Settings.System.FANCY_IME_ANIMATIONS,
		    mFancyImeAnimationsPref.isChecked() ? 1 : 0);
	} else if (HAPTIC_FEEDBACK_PREF.equals(key)) {
	    Settings.System.putInt(getContentResolver(),
		    Settings.System.HAPTIC_FEEDBACK_ENABLED,
		    mHapticFeedbackPref.isChecked() ? 1 : 0);
	} else if (MAPS_COMPASS_PREF.equals(key)) {
	    try {
		Context c = createPackageContext("com.google.android.apps.maps", 0);
		c.getSharedPreferences("extra-features", MODE_WORLD_WRITEABLE)
		    .edit()
		    .putBoolean("compass", mShowMapsCompassPref.isChecked())
		    .commit();
	    } catch (NameNotFoundException e) {
		Log.w(TAG, "Failed setting maps compass");
		e.printStackTrace();
	    }
	}
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

    public long getFileSize(String filename) { // TODO
    	File file = new File(filename);
    	if (!file.exists() || !file.isFile()) {
    	    Log.w(TAG, "No such file: " + filename);
    	    return -1;
    	}
    	return file.length();
    }

    public boolean sendshell(final String[] commands, final boolean reboot, String message) {
	patience = ProgressDialog.show(this, "", message, true);
    	Thread t = new Thread() {
    		public void run() {
    		    ShellInterface.doExec(commands);
    		    patience.dismiss();
    		    if (reboot == true)
    			mHandler.post(mUpdateResults);
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
			try {
			    Runtime.getRuntime().exec("su -c reboot");
			} catch(IOException e) {
			}
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
	readAnimationPreference(0, mWindowAnimationsPref);
	readAnimationPreference(1, mTransitionAnimationsPref);
	readFontSizePreference(mFontSizePref);
	readEndButtonPreference(mEndButtonPref);
	updateToggles();
    }
}
