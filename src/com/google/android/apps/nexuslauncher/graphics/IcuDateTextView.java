package com.google.android.apps.nexuslauncher.graphics;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.util.Locale;

public class IcuDateTextView extends DoubleShadowTextView {
    private DateFormat mDateFormat;
    private final BroadcastReceiver mTimeChangeReceiver;
    private boolean mIsVisible = false;

    public IcuDateTextView(final Context context) {
        this(context, null);
    }

    public IcuDateTextView(final Context context, final AttributeSet set) {
        super(context, set, 0);
        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reloadDateFormat(!Intent.ACTION_TIME_TICK.equals(intent.getAction()));
            }
        };
    }

    @TargetApi(24)
    public void reloadDateFormat(boolean forcedChange) {
        String format;
        boolean time = Utilities.getPrefs((getContext())).getBoolean("pref_smartspase_time", false);
        if (Utilities.ATLEAST_NOUGAT) {
            if (mDateFormat == null || forcedChange) {
                (mDateFormat = DateFormat.getInstanceForSkeleton(getContext()
                        .getString(R.string.icu_abbrev_wday_month_day_no_year), Locale.getDefault()))
                        .setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            }
            String loaddateformat = Utilities.getPrefs((getContext())).getString("pref_DateFormats", "EEEEMMMd");
            if (time) {
                (mDateFormat = DateFormat.getInstanceForSkeleton(loaddateformat + "HH:mm", Locale.getDefault()))
                        .setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            }
            else (mDateFormat = DateFormat.getInstanceForSkeleton(loaddateformat, Locale.getDefault()))
                    .setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            format = mDateFormat.format(System.currentTimeMillis());
        } else {
            format = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
            if (time) {
                format = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME);
            }
        }
        setText(format);
        setContentDescription(format);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeChangeReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mTimeChangeReceiver);
    }

    public void onVisibilityAggregated(boolean isVisible) {
        if (Utilities.ATLEAST_NOUGAT) {
            super.onVisibilityAggregated(isVisible);
        }
        if (!mIsVisible && isVisible) {
            mIsVisible = true;
            registerReceiver();
            reloadDateFormat(true);
        } else if (mIsVisible && !isVisible) {
            unregisterReceiver();
            mIsVisible = false;
        }
    }
}
