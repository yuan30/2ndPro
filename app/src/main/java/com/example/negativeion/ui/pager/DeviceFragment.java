package com.example.negativeion.ui.pager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.google.gson.annotations.Until;

public class DeviceFragment extends Fragment {

    MysqlConnect mMysqlConnect;
    EditText editText;
    private Runnable relayConditionRunnable, sendRunnable;
    public DeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editText = new EditText(getContext());
        mMysqlConnect = new MysqlConnect();
        initRunnable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(relayConditionRunnable).start();
    }

    void initRunnable(){
        relayConditionRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.connectRelay();

                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), mMysqlConnect.getResponse(), Toast.LENGTH_SHORT).show();
                    }
                },500);
            }
        };

        sendRunnable = new Runnable() {
            @Override
            public void run() {
                boolean check = mMysqlConnect.init();
                if(check)
                    mMysqlConnect.jdbcAddRelay();

            }
        };
    }
}
