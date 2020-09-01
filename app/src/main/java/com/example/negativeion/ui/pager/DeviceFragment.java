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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
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

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FloatingActionButton fabAddDevice;
    private Runnable addUserDeviceRunnable, getUserDeviceRunnable
            , deleteDeviceRunnable, modeifyDeviceRunnable;
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

        mMysqlConnect = new MysqlConnect();


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            userId = getActivity().getIntent().getStringExtra(Attribute.USER_ID);

            initView();
            initRunnable();
        }catch (Exception e){
            Toast.makeText(mContext, "bug:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView()
    {
        fabAddDevice = getView().findViewById(R.id.fabAddDevice);
        fabAddDevice.setOnClickListener(v -> { //lambda 語法 need java 1.8
            Intent intent = new Intent(mContext, SmartConfigActivity.class);
            startActivityForResult(intent, Attribute.RECEIVED_DEIVCE_ID_CODE);
        });

        mDeviceRecyclerView = getView().findViewById(R.id.rv_device);
        mDeviceRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        mDeviceRVAdapter = new DeviceRVAdapter(mContext);
        mDeviceRVAdapter.setOnItemClickListener(onItemClickListener);

        mDeviceRecyclerView.setAdapter(mDeviceRVAdapter);

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mSwipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        updateOperation();
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        new Thread(getUserDeviceRunnable).start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        builder.setTitle(R.string.add_device)
                .setView(addDeviceView)
                .setCancelable(false)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceName = edtTxtDName.getText().toString();

                        mDeviceRVAdapter.setDeviceName(deviceName);
                        mDeviceRVAdapter.setDeviceAddr(deviceId);
                        mDeviceRVAdapter.notifyDataSetChanged();

                        new Thread(addUserDeviceRunnable).start();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
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
            Builder.setTitle(R.string.choice_action)
                    .setCancelable(false)
                    .setPositiveButton(R.string.delete_device, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder delBuilder = new AlertDialog.Builder(mContext);
                                    delBuilder
                                            .setMessage(R.string.check_once)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AlertDialog.Builder checkBuilder = new AlertDialog.Builder(mContext);
                                                    checkBuilder.setTitle(R.string.ok)
                                                            .setCancelable(false)
                                                            .setMessage(R.string.check_twice)
                                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    new Thread(deleteDeviceRunnable).start();
                                                                    manualRefresh();
                                                                }
                                                            })
                                                            .setNegativeButton(R.string.cancel, null)
                                                            .show();
                                                }
                                            })
                                            .setNeutralButton(R.string.no, null)
                                            .show();
                                }
                            })
                    .setNegativeButton(R.string.modify, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LayoutInflater inflater = LayoutInflater.from(mContext);
                            final View v = inflater.inflate(R.layout.dialog_alter_device_n_relay_name, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            final EditText edtTxtRName = v.findViewById(R.id.edtTxtDnRName);
                            edtTxtRName.setText(mDeviceRVAdapter.getDeviceName(position));

                            builder.setTitle(R.string.modify_device_name)
                                    .setView(v)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deviceName =  edtTxtRName.getText().toString();
                                            new Thread(modeifyDeviceRunnable).start();
                                            manualRefresh();
                                        }
                                    }).show();
                        }
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .show();


        }
    };

    private void manualRefresh()
    {
        mSwipeRefreshLayout.setRefreshing(true);
        updateOperation();
    }

    private void updateOperation()
    {
        new Thread(getUserDeviceRunnable).start();
    }

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
                        mDeviceRVAdapter.removeAllDatas();
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

                        mSwipeRefreshLayout.setRefreshing(false);
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

        modeifyDeviceRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.modifyDeviceName(deviceId, deviceName);
                deviceId = "";
            }
        };
    }
}
