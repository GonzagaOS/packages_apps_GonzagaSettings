/*
 * Copyright (C) 2020 Komodo-OS-Rom
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

import android.content.Context;
import android.content.res.Resources;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.search.BaseSearchIndexProvider;

import com.komodo.settings.preferences.CustomSeekBarPreference;

public class Buttons extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {

    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.komodo_settings_button);
        getActivity().setTitle(R.string.buttons_title);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    protected boolean usesExtendedActionsList() {
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KOMODO_SETTINGS;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.komodo_settings_button);
}
