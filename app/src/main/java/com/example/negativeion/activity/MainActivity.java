package com.example.negativeion.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.negativeion.R;
import com.example.negativeion.ui.pager.AboutFragment;
import com.example.negativeion.ui.pager.DeviceFragment;
import com.example.negativeion.ui.pager.InfoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private AboutFragment aboutFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefaultFragment();

        aboutFragment = new AboutFragment();
        Bundle bundle = new Bundle();
        bundle.putString("User photoUrl",getIntent().getStringExtra("User photoUrl"));
        bundle.putString("User name",getIntent().getStringExtra("User name"));
        bundle.putString("User ID",getIntent().getStringExtra("User ID"));
        aboutFragment.setArguments(bundle);

        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // 設定預設tab顯示頁面
    private void setDefaultFragment() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new DeviceFragment()).commit();

    }
    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this, "想登出?", Toast.LENGTH_SHORT).show();
    }
    //設定3-5個碎片化介面
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.item_bottom_home:
                    transaction.replace(R.id.content, new DeviceFragment());
                    transaction.commit();
                    return true;
                case R.id.item_bottom_information:
                    transaction.replace(R.id.content, new InfoFragment());
                    transaction.commit();
                    return true;
                case R.id.item_bottom_about:
                    transaction.replace(R.id.content, aboutFragment);
                    transaction.commit();
                    return true;
            }
            return false;
        }
    };
}
