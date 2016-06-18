package com.honey.aaron.workoff.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honey.aaron.workoff.R;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // TODO setting

        return view;
    }
}
