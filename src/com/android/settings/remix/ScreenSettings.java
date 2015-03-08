/*
 * Copyright (C) 2015 crDroid Android
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

package com.android.settings.remix;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import android.provider.SearchIndexableResource;
import java.util.Arrays;
import java.util.List;

public class ScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "ScreenSettings";

    private static final int DIALOG_DENSITY = 0;
    private static final int DIALOG_DENSITY_WARNING = 1;

    private static final String PREF_QUICK_PULLDOWN = "quick_pulldown";
    private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";

    private static final String KEY_LCD_DENSITY = "lcd_density";

    private ListPreference mLcdDensityPreference;
    SwitchPreference mBlockOnSecureKeyguard;

    protected Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.remix_screen_settings);
        PreferenceScreen prefs = getPreferenceScreen();

        mContext = getActivity().getApplicationContext();
        int newDensityValue;

        mLcdDensityPreference = (ListPreference) findPreference(KEY_LCD_DENSITY);
        int defaultDensity = DisplayMetrics.DENSITY_DEVICE;
        String[] densityEntries = new String[9];
        for (int idx = 0; idx < 8; ++idx) {
            int pct = (75 + idx*5);
            densityEntries[idx] = Integer.toString(defaultDensity * pct / 100);
        }
        densityEntries[8] = getString(R.string.custom_density);
        int currentDensity = DisplayMetrics.DENSITY_CURRENT;
        mLcdDensityPreference.setEntries(densityEntries);
        mLcdDensityPreference.setEntryValues(densityEntries);
        mLcdDensityPreference.setValue(String.valueOf(currentDensity));
        mLcdDensityPreference.setOnPreferenceChangeListener(this);
        updateLcdDensityPreferenceDescription(currentDensity);

        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
        mBlockOnSecureKeyguard = (SwitchPreference) findPreference(PREF_BLOCK_ON_SECURE_KEYGUARD);
        if (lockPatternUtils.isSecure()) {
            mBlockOnSecureKeyguard.setChecked(Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD, 1) == 1);
            mBlockOnSecureKeyguard.setOnPreferenceChangeListener(this);
        } else {
            prefs.removePreference(mBlockOnSecureKeyguard);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_LCD_DENSITY.equals(key)) {
            String strValue = (String) objValue;
            if (strValue.equals(getResources().getString(R.string.custom_density))) {
                showDialog(DIALOG_DENSITY);
            } else {
                int value = Integer.parseInt((String) objValue);
                writeLcdDensityPreference(value);
                updateLcdDensityPreferenceDescription(value);
            }
        return false;
            }
            if (preference == mBlockOnSecureKeyguard) {
                Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD,
                    (Boolean) objValue ? 1 : 0);
                return true;
            }
        return false;
    }

    private void updateLcdDensityPreferenceDescription(int currentDensity) {
        ListPreference preference = mLcdDensityPreference;
        String summary;
        if (currentDensity < 10 || currentDensity >= 1000) {
            // Unsupported value 
            summary = "";
        }
        else {
            summary = Integer.toString(currentDensity) + " DPI";
        }
        preference.setSummary(summary);
    }

    public void writeLcdDensityPreference(int value) {
        // Set the value clicked on the list
        try {
            SystemProperties.set("persist.sys.lcd_density", Integer.toString(value));
        }
        catch (Exception e) {
            Log.w(TAG, "Unable to save LCD density");
        }
        // Show a dialog before restart
        // and let the user know of it
        showDialogInner(DIALOG_DENSITY_WARNING);

    }

    // Restart the system to apply changes
    static void systemRestart() {
        try {
            final IActivityManager am = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
            if (am != null) {
                am.restart();
            }
        }
        catch (RemoteException e) {
            Log.e(TAG, "Failed to restart");
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        LayoutInflater factory = LayoutInflater.from(mContext);

        switch (dialogId) {
            case DIALOG_DENSITY:
                final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
                return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.custom_density_dialog_title)
                .setMessage(getResources().getString(R.string.custom_density_dialog_summary))
                .setView(textEntryView)
                .setPositiveButton(getResources().getString(R.string.set_custom_density_set), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText dpi = (EditText) textEntryView.findViewById(R.id.dpi_edit);
                        Editable text = dpi.getText();
                        Log.i(TAG, text.toString());
                        String editText = dpi.getText().toString();
                        // Set the value of the text box
                        try {
                            SystemProperties.set("persist.sys.lcd_density", editText);
                        }
                        catch (Exception e) {
                            Log.w(TAG, "Unable to save LCD density");
                        }
                        // Show a dialog before restart
                        // and let the user know of it
                        showDialogInner(DIALOG_DENSITY_WARNING);

                    }

                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();
        }
        return null;
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DIALOG_DENSITY_WARNING:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.custom_density_warning)
                    .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // If canceled, set the density value to null avoiding
                            // the storage of the clicked value and forward change
                            // to it in a next restart of the system 
                            try {
                                SystemProperties.set("persist.sys.lcd_density", null);
                            } catch (Exception e) {
                                Log.w(TAG, "Unable to save LCD density");
                            }

                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(R.string.dialog_restart,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // If resatrt is the choosen one do it and apply the value
                            systemRestart();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }
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
