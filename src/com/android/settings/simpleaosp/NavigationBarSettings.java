
package com.android.settings.simpleaosp;

import android.content.ContentResolver;
import android.os.Bundle;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String KEY_CLEAR_ALL_RECENTS_NAVBAR_ENABLED = "clear_all_recents_navbar_enabled";

    private SlimSeekBarPreference mNavigationBarHeight;
    private SwitchPreference mClearAllRecentsNavbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navigation_bar_settings);
	PreferenceScreen prefs = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean hasNavigationBar = res.getDimensionPixelSize(res.getIdentifier(
                "navigation_bar_height", "dimen", "android")) > 0;
        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);

        mNavigationBarHeight = (SlimSeekBarPreference) findPreference(KEY_NAVIGATION_BAR_HEIGHT);
        mNavigationBarHeight.setEnabled(hasNavBarByDefault);
        mNavigationBarHeight.setDefault(48);
        mNavigationBarHeight.setInterval(2);
        mNavigationBarHeight.minimumValue(2);
        mNavigationBarHeight.maximumValue(48);
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

       mClearAllRecentsNavbar = (SwitchPreference) prefScreen.findPreference(KEY_CLEAR_ALL_RECENTS_NAVBAR_ENABLED);
        if (!hasNavBarByDefault == true) {
            prefScreen.removePreference(mClearAllRecentsNavbar);
        } else 
		 mClearAllRecentsNavbar.setChecked(Settings.System.getInt(resolver,
                    Settings.System.CLEAR_ALL_RECENTS_NAVBAR_ENABLED, 1) == 1);

    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNavigationBarHeight) {
	int navbarheight = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, navbarheight);
        }
        return true;
    }

   @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mClearAllRecentsNavbar) {
            Settings.System.putInt(resolver, Settings.System.CLEAR_ALL_RECENTS_NAVBAR_ENABLED,
                    mClearAllRecentsNavbar.isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }
}
