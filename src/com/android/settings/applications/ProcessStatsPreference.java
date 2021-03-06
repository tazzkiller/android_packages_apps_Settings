/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.android.settings.AppProgressPreference;

public class ProcessStatsPreference extends AppProgressPreference {

    private ProcStatsPackageEntry mEntry;
    private Context mContext;

    public ProcessStatsPreference(Context context) {
        super(context, null);
        mContext = context;
    }

    public void init(ProcStatsPackageEntry entry, PackageManager pm, double maxMemory,
            double weightToRam, double totalScale, boolean avg) {
        mEntry = entry;
        setTitle(TextUtils.isEmpty(entry.mUiLabel) ? entry.mPackage : entry.mUiLabel);
        if (entry.mUiTargetApp != null) {
            setIcon(entry.mUiTargetApp.loadIcon(pm));
        } else {
            setIcon(mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon, null));
        }
        boolean statsForeground = entry.mRunWeight > entry.mBgWeight;
        double amount = avg ? (statsForeground ? entry.mRunWeight : entry.mBgWeight) * weightToRam
                : (statsForeground ? entry.mMaxRunMem : entry.mMaxBgMem) * totalScale * 1024;
        setSummary(Formatter.formatShortFileSize(getContext(), (long) amount));
        setProgress((int) (100 * amount / maxMemory));
    }

    public ProcStatsPackageEntry getEntry() {
        return mEntry;
    }
}
