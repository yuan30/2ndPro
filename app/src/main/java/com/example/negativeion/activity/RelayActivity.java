package com.example.negativeion.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.negativeion.R;
import com.example.negativeion.ui.pager.AboutFragment;
import com.example.negativeion.ui.pager.DeviceFragment;
import com.example.negativeion.ui.pager.RelayFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RelayActivity extends AppCompatActivity {

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        setDefaultFragment();
    }

    // 設定預設tab顯示頁面
    private void setDefaultFragment() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_relay, new RelayFragment()).commit();
    }
}
