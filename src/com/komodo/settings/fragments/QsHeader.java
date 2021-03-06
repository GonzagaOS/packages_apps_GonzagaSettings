/*
 * Copyright (C) 2019 The Komodo Rom Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.komodo.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.komodo.settings.preferences.CustomSeekBarPreference;
import com.komodo.settings.preferences.SystemSettingSwitchPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QsHeader extends SettingsPreferenceFragment
             implements Preference.OnPreferenceChangeListener{

    private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String FILE_HEADER_SELECT = "file_header_select";
    private static final String QS_HEADER_STYLE = "qs_header_style";
    private static final String QS_HEADER_STYLE_COLOR = "qs_header_style_color";

    private static final int REQUEST_PICK_IMAGE = 0;

    private Preference mHeaderBrowse;
    private ListPreference mDaylightHeaderPack;
    private CustomSeekBarPreference mHeaderShadow;
    private ListPreference mHeaderProvider;
    private String mDaylightHeaderProvider;
    private Preference mFileHeader;
    private String mFileHeaderProvider;
    private ListPreference mQsHeaderStyle;
    private ColorPickerPreference mQsHeaderStyleColor;

    @Override
    public void onResume() {
        super.onResume();
        updateEnablement();
        updateQsHeaderStyleColor();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.komodo_settings_quicksettings_qsheader);
        ContentResolver resolver = getActivity().getContentResolver();

        mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
        mFileHeaderProvider = getResources().getString(R.string.file_header_provider);
        mHeaderBrowse = findPreference(CUSTOM_HEADER_BROWSE);

        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        boolean headerEnabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, 0) != 0;
        updateHeaderProviderSummary(headerEnabled);
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);

        mHeaderShadow = (CustomSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);

        mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
        mHeaderProvider.setOnPreferenceChangeListener(this);

        mFileHeader = findPreference(FILE_HEADER_SELECT);

        getQsHeaderStylePref();
    }

    private void updateHeaderProviderSummary(boolean headerEnabled) {
        mDaylightHeaderPack.setSummary(getResources().getString(R.string.header_provider_disabled));
        if (headerEnabled) {
            String settingHeaderPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK);
            if (settingHeaderPackage != null) {
                int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
                mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mFileHeader) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDaylightHeaderPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
        } else if (preference == mHeaderShadow) {
            Integer headerShadow = (Integer) newValue;
            int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
        } else if (preference == mHeaderProvider) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
            int valueIndex = mHeaderProvider.findIndexOfValue(value);
            mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
            updateEnablement();
        } else if (preference == mQsHeaderStyle) {
            String value = (String) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
			    Settings.System.QS_HEADER_STYLE, Integer.valueOf(value));
            int newIndex = mQsHeaderStyle.findIndexOfValue(value);
            mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[newIndex]);
            updateQsHeaderStyleColor();
            return true;
        } else if (preference == mQsHeaderStyleColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.QS_HEADER_STYLE_COLOR, intHex);
            return true;
        }
        return true;
    }

    private void getQsHeaderStylePref() {
        mQsHeaderStyle = (ListPreference) findPreference(QS_HEADER_STYLE);
        int qsHeaderStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QS_HEADER_STYLE, 0);
        int valueIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
        mQsHeaderStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
        mQsHeaderStyle.setOnPreferenceChangeListener(this);
        updateQsHeaderStyleColor();
    }

    private void updateQsHeaderStyleColor() {
        int qsHeaderStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QS_HEADER_STYLE, 0);

        if(qsHeaderStyle == 3) {
                mQsHeaderStyleColor = (ColorPickerPreference) findPreference(QS_HEADER_STYLE_COLOR);
                int qsHeaderStyleColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.QS_HEADER_STYLE_COLOR, 0x0000000);
                mQsHeaderStyleColor.setNewPreviewColor(qsHeaderStyleColor);
                String qsHeaderStyleColorHex = String.format("#%08x", (0x0000000 & qsHeaderStyleColor));
                mQsHeaderStyleColor.setSummary(qsHeaderStyleColorHex);
                mQsHeaderStyleColor.setOnPreferenceChangeListener(this);
                mQsHeaderStyleColor.setVisible(true);
        } else {
                mQsHeaderStyleColor = (ColorPickerPreference) findPreference(QS_HEADER_STYLE_COLOR);
                if(mQsHeaderStyleColor != null) {
                        mQsHeaderStyleColor.setVisible(false);
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        updateQsHeaderStyleColor();
    }

    private boolean isBrowseHeaderAvailable() {
        PackageManager pm = getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.PickHeaderActivity");
        return pm.resolveActivity(browse, 0) != null;
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Map<String, String> headerMap = new HashMap<String, String>();
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            headerMap.put(label, packageName);
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (r.activityInfo.name.endsWith(".theme")) {
                continue;
            }
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        List<String> labelList = new ArrayList<String>();
        labelList.addAll(headerMap.keySet());
        Collections.sort(labelList);
        for (String label : labelList) {
            entries.add(label);
            values.add(headerMap.get(label));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER, "file");
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_STATUS_BAR_FILE_HEADER_IMAGE, imageUri.toString());
        }
    }

    private void updateEnablement() {
        String providerName = Settings.System.getString(getContentResolver(),
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER);
        if (providerName == null) {
            providerName = mDaylightHeaderProvider;
        }
        if (!providerName.equals(mDaylightHeaderProvider)) {
            providerName = mFileHeaderProvider;
        }
        int valueIndex = mHeaderProvider.findIndexOfValue(providerName);
        mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mHeaderProvider.setSummary(mHeaderProvider.getEntry());
        mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));
        mFileHeader.setEnabled(providerName.equals(mFileHeaderProvider));
        mHeaderBrowse.setEnabled(isBrowseHeaderAvailable() && providerName.equals(mFileHeaderProvider));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KOMODO_SETTINGS;
    }
}