package com.honey.aaron.workoff.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class SettingFragment extends BaseFragment {
    static SettingFragment settingFragment;

    public SettingFragment() {
        super();
    }

    public static SettingFragment newInstance() {
        if (settingFragment == null) {
            settingFragment = new SettingFragment();
        }
        return settingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = SettingFragment.class.getSimpleName();
    }
}
