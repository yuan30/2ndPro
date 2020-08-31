package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.Attribute;
import com.example.negativeion.DeviceRVAdapter;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.activity.RelayActivity;
import com.example.negativeion.espsmartconfig.v1.SmartConfigActivity;
import com.example.negativeion.model.UserAndDeviceModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.app.Activity.RESULT_OK;

public class DeviceFragment extends Fragment {

    private Context mContext;
    private MysqlConnect mMysqlConnect;
    private DeviceRVAdapter mDeviceRVAdapter;
    private RecyclerView mDeviceRecyclerView;

    private FloatingActionButton fabAddDevice;
    private Runnable addUserDeviceRunnable, getUserDeviceRunnable, deleteDeviceRunnable;
    private String userId, deviceName, deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        mContext = requireContext();
        fabAddDevice = view.findViewById(R.id.fabAddDevice);

        mMysqlConnect = new MysqlConnect();

        initView();
        initRunnable();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            userId = getActivity().getIntent().getStringExtra(Attribute.USER_ID);

            mDeviceRecyclerView = getView().findViewById(R.id.rv_device);
            mDeviceRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

            mDeviceRVAdapter = new DeviceRVAdapter(mContext);
            mDeviceRVAdapter.setOnItemClickListener(onItemClickListener);

            mDeviceRecyclerView.setAdapter(mDeviceRVAdapter);
        }catch (Exception e){
            Toast.makeText(mContext, "bug:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        new Thread(getUserDeviceRunnable).start();
        /*String str = getActivity().getIntent().getStringExtra(Attribute.DEVICE_ID);
        if((str != null) && (str.compareTo("-1") != 0))
            addDevice(str);

        String deviceIdTemp="6001942cd7a6";
        String str1 = "";
        for(int i=0; i<deviceIdTemp.length(); i=i+2)
            str1 = str1 + deviceIdTemp.substring(i, i+2) + ":";
        str1 = str1.substring(0, str1.length()-1);

        Log.d("DeviceFragment/deviceId","D長度" + deviceIdTemp.length());
        Log.d("DeviceFragment/deviceId","長度" + deviceIdTemp.split("").length);
        Log.d("DeviceFragment/deviceId","長度" + str1);
        final String deviceId = str1;*/
    }

    @Override
    public void onResume() {
        super.onResume();

        /*Toast.makeText(mContext, "Id:" + getActivity().getIntent().getStringExtra(Attribute.USER_ID), Toast.LENGTH_SHORT)
                .show();*/
        //userId = getActivity().getIntent().getStringExtra("User ID");

        //Toast.makeText(mContext, "更新資料中", Toast.LENGTH_SHORT).show();

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

        //mDeviceRVAdapter.removeAllDatas();
        //Toast.makeText(mContext, "pause", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != Attribute.RECEIVED_DEIVCE_ID_CODE)
            return;

        switch (resultCode){
            case RESULT_OK:
                String str = data.getStringExtra(Attribute.DEVICE_ID);
                if((str != null) && (str.compareTo("-1") != 0))
                    addDevice(str);
                break;
        }
    }

    private void initView()
    {
        fabAddDevice.setOnClickListener(v -> { //lambda 語法 need java 1.8
            Intent intent = new Intent(mContext, SmartConfigActivity.class);
            startActivityForResult(intent, Attribute.RECEIVED_DEIVCE_ID_CODE);
        });
    }

    private void addDevice(String deviceIdTemp)
    {
        String str = "";
        for(int i=0; i<deviceIdTemp.length(); i=i+2)
            str = str + deviceIdTemp.substring(i, i+2) + ":";
        str = str.substring(0, str.length()-1);
        final String deviceId = str;
        this.deviceId = deviceId;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        //原本放在上面，按下fab，Dialog設置此View後開啟，會將此View放在主畫面上
        //而想再開一次Dialog會因為，此View已有一個parent，而出錯。
        //解:1.將view放在這，每次新開一個;2.拿到該view的parent，removeView掉該View。
        View addDeviceView = inflater.inflate(R.layout.dialog_add_device, null);

        final EditText edtTxtDName = addDeviceView.findViewById(R.id.edtTxtDName);
        //用MQTT或其他方法接收MAC Addr，在這新增進RVA。
        //mDeviceRVAdapter.setDeviceAddr();
        Toast.makeText(mContext, "MAC:" + deviceIdTemp, Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("新增裝置")
                .setView(addDeviceView)
                .setCancelable(false)
                .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceName = edtTxtDName.getText().toString();

                        mDeviceRVAdapter.setDeviceName(deviceName);
                        mDeviceRVAdapter.setDeviceAddr(deviceId);
                        mDeviceRVAdapter.notifyDataSetChanged();

                        new Thread(addUserDeviceRunnable).start();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        getActivity().getIntent().putExtra(Attribute.DEVICE_ID, "-1");
    }

    private DeviceRVAdapter.OnItemClickListener onItemClickListener = new DeviceRVAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent(getActivity(), RelayActivity.class);
            final String deviceId = mDeviceRVAdapter.getDeviceAddr(position);
            intent.putExtra(Attribute.DEVICE_ID, deviceId);
            //Toast.makeText(mContext, "D_MAC:"+deviceId,Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        @Override
        public void onItemLongClick(View view, int position) {
            deviceId = mDeviceRVAdapter.getDeviceAddr(position);
            AlertDialog.Builder Builder = new AlertDialog.Builder(mContext);
            Builder.setTitle("選擇動作")
                    .setPositiveButton("刪除裝置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder delBuilder = new AlertDialog.Builder(mContext);
                                    delBuilder.setTitle("刪除裝置")
                                            .setMessage("是否刪除裝置?")
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AlertDialog.Builder checkBuilder = new AlertDialog.Builder(mContext);
                                                    checkBuilder.setTitle("確定")
                                                            .setMessage("確定刪除?")
                                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    new Thread(deleteDeviceRunnable).start();
                                                                }
                                                            })
                                                            .setNegativeButton("取消", null)
                                                            .show();
                                                }
                                            })
                                            .setNeutralButton("否", null)
                                            .show();
                                }
                            })
                    .setNegativeButton("修改名稱", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("取消", null)
                    .show();


        }
    };

    void initRunnable(){
        addUserDeviceRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.addUserAndDevice(userId, deviceId, deviceName);
                deviceId = "";
            }
        };

        getUserDeviceRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.getUserAndDevice(userId);

                mDeviceRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            for (UserAndDeviceModel userAndDeviceModel : mMysqlConnect.getUserAndDeviceModelList()) {
                                //以位址做判斷，名稱重複沒關係
                                if (mDeviceRVAdapter.setDeviceAddr(userAndDeviceModel.getDeviceId()))
                                    mDeviceRVAdapter.setDeviceName(userAndDeviceModel.getDeviceName());
                                mDeviceRVAdapter.notifyDataSetChanged();
                            }
                        }catch (Exception e){Toast.makeText(mContext, "系統目前出錯，請稍後再試", Toast.LENGTH_SHORT).show();
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();}
                    }
                },10);
            }
        };

        deleteDeviceRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.deleteDevice(deviceId);
                deviceId = "";
            }
        };
    }
}
