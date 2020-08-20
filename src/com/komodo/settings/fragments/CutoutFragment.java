/*
 * Copyright (C) 2018 The Potato Open Sauce Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.komodo.settings.fragments;

import android.os.Bundle;
import android.content.Context;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragment;

import com.android.settings.R;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class CutoutFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_DISPLAY_CUTOUT_STYLE = "display_cutout_style";
    private static final String KEY_FORCE_FULLSCREEN = "display_cutout_force_fullscreen_settings";
    private ListPreference mCutoutStyle;

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCutoutStyle) {
            String value = (String) newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.DISPLAY_CUTOUT_MODE, Integer.valueOf(value));
            int valueIndex = mCutoutStyle.findIndexOfValue(value);
            mCutoutStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mCutoutStyle.setSummary(mCutoutStyle.getEntries()[valueIndex]);
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cutout);
        mCutoutStyle = (ListPreference) findPreference(KEY_DISPLAY_CUTOUT_STYLE);
        int cutoutStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.DISPLAY_CUTOUT_MODE, 0);
        int valueIndex = mCutoutStyle.findIndexOfValue(String.valueOf(cutoutStyle));
        mCutoutStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mCutoutStyle.setSummary(mCutoutStyle.getEntry());
        mCutoutStyle.setOnPreferenceChangeListener(this);

        final Preference forceFullscreen = (Preference) getPreferenceScreen().findPreference(KEY_FORCE_FULLSCREEN);
        if (!hasPhysicalDisplayCutout(getContext())) {
            getPreferenceScreen().removePreference(forceFullscreen);
        }
    }

    private static boolean hasPhysicalDisplayCutout(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_physicalDisplayCutout);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KOMODO_SETTINGS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.cutout;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
        }
    };
}
