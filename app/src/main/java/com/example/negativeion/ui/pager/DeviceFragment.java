package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.DeviceRVAdapter;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.activity.RelayActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import static android.content.Context.MODE_PRIVATE;

public class DeviceFragment extends Fragment {

    MysqlConnect mMysqlConnect;
    DeviceRVAdapter mDeviceRVAdapter;
    RecyclerView mDeviceRecyclerView;

    private Runnable deviceRunnable;
    public DeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        //AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        FloatingActionButton fabAddDevice = view.findViewById(R.id.fabAddDevice);
        fabAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                //原本放在上面，按下fab，Dialog設置此View後開啟，會將此View放在主畫面上
                //而想再開一次Dialog會因為，此View已有一個parent，而出錯。
                //解:1.將view放在這，每次新開一個;2.拿到該view的parent，removeView掉該View。
                View addDeviceView = inflater.inflate(R.layout.dialog_add_device, null);
                final EditText edtTxtDAddr = addDeviceView.findViewById(R.id.edtTxtDAddr);
                final EditText edtTxtDName = addDeviceView.findViewById(R.id.edtTxtDName);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("新增裝置")
                        .setView(addDeviceView)
                        .setCancelable(false)
                        .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDeviceRVAdapter.setDeviceTitle(edtTxtDName.getText().toString());
                                mDeviceRVAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        mMysqlConnect = new MysqlConnect();

        initRunnable();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mDeviceRecyclerView = getView().findViewById(R.id.rv_device);
            mDeviceRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

            mDeviceRVAdapter = new DeviceRVAdapter(getContext());
            mDeviceRVAdapter.setOnItemClickListener(onItemClickListener);

            mDeviceRecyclerView.setAdapter(mDeviceRVAdapter);
        }catch (Exception e){
            Toast.makeText(getContext(), "bug:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getContext(), "Id:" + getActivity().getIntent().getStringExtra("User ID"), Toast.LENGTH_SHORT)
                .show();
        //new Thread(deviceRunnable).start();
        //Toast.makeText(getContext(), "更新資料中", Toast.LENGTH_SHORT).show();

        /*SharedPreferences appSharedPrefs  = Objects.requireNonNull(getActivity()).
                getSharedPreferences("negative_relay",MODE_PRIVATE);
        List<String> list = mDeviceRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            list.set(i, appSharedPrefs.getString(Integer.toString(i), "編號0"+i));
        }*/
        //mDeviceRVAdapter.setRelayNameList(list);
        //mDeviceRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(getContext(), "pause", Toast.LENGTH_SHORT).show();
       /* SharedPreferences appSharedPrefs = getActivity().
                getSharedPreferences("negative_relay",MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.clear();
        List<String> list = mDeviceRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            prefsEditor.putString(Integer.toString(i), list.get(i));
        }
        prefsEditor.apply();*/
    }

    private DeviceRVAdapter.OnItemClickListener onItemClickListener = new DeviceRVAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent(getActivity(), RelayActivity.class);
            startActivity(intent);
        }
    };

    void initRunnable(){
        deviceRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.connectRelay();

                mDeviceRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        /*try {
                            List<String> list = mDeviceRVAdapter.getRelayList();
                            String str = mMysqlConnect.getResponse();
                            //Toast.makeText(getContext(), "re:"+list.size()+" str:"+mMysqlConnect.getResponse()+" "+str.length, Toast.LENGTH_SHORT).show();
                            for(int i=0; i<list.size(); i++) {
                                String strTemp = ""+str.charAt(i);
                                list.set(i, strTemp);
                            }
                            mDeviceRVAdapter.setRelayList(list);
                            mDeviceRVAdapter.notifyDataSetChanged();
                            //Toast.makeText(getContext(), "資料更新成功", Toast.LENGTH_SHORT).show();
                        }catch (Exception e)
                        {
                            Log.d("Device分頁", "ERROR" + e.getMessage());
                            //Toast.makeText(getContext(), "資料更新失敗:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }*/

                    }
                },500);
            }
        };

    }
}
