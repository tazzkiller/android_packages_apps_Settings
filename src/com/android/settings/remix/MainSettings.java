/*
 * Copyright (C) 2014 The LiquidSmooth Project
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

package com.android.settings.remix;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import android.provider.SearchIndexableResource;
import java.util.Arrays;

import java.util.List;

public class MainSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_VOICE_WAKEUP = "voice_wakeup";
    private static final String KEY_BITSYKO_LAYERS = "bitsyko_layers";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.remix_main_settings);
        if (!isPackageInstalled("com.cyanogenmod.voicewakeup")) {
		PreferenceScreen screen = getPreferenceScreen();
		Preference pref = getPreferenceManager().findPreference(KEY_VOICE_WAKEUP);
		screen.removePreference(pref);
        } else if (!isPackageInstalled("com.lovejoy777.rroandlayersmanager")) {
		PreferenceScreen screen = getPreferenceScreen();
		Preference pref = getPreferenceManager().findPreference(KEY_BITSYKO_LAYERS);
                screen.removePreference(pref);
                }

	}

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
           pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
           installed = true;
        } catch (PackageManager.NameNotFoundException e) {
           installed = false;
        }
        return installed;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
           Index.getInstance(
           getActivity().getApplicationContext()).updateFromClassNameResource(
           MainSettings.class.getName(), true, true);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {

            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(
                    Context context, boolean enabled) {
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.remix_main_settings;
                return Arrays.asList(sir);
            }
	};
}
