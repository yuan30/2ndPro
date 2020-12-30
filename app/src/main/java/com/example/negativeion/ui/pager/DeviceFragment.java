package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

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
            startActivity(intent);
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
        mSwipeRefreshLayout = getView().findViewById(R.id.deviceSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(
                () -> {
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    updateOperation();
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        manualRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();

        deviceName = getString(R.string.device_default);
        checkSharedPrefs();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != Attribute.RECEIVED_DEVICE_ID_CODE)
            return;

        switch (resultCode){
            case RESULT_OK:
                String str = data.getStringExtra(Attribute.DEVICE_ID);
                if((str != null) && (str.compareTo("-1") != 0))
                    addDevice(str);
                break;
        }
    }*/

    private void checkSharedPrefs() {
        try {
            SharedPreferences appSharedPrefs = getSharedPreferences();
            String str = appSharedPrefs.
                    getString(Attribute.SHARED_P_EDITOR_STRING_DEVICE_RAW, "0");
            if(str.equals("0")) return;

            addDevice(str);
        }catch (Exception e){Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();}
    }

    private void addDevice(String deviceIdTemp)
    {
        String str = "";
        for(int i=0; i<deviceIdTemp.length(); i=i+2)
            str = str + deviceIdTemp.substring(i, i+2) + ":";
        str = str.substring(0, str.length()-1);
        final String deviceId = str;
        this.deviceId = deviceId;

        mDeviceRVAdapter.setDeviceName(deviceName);
        mDeviceRVAdapter.setDeviceAddr(deviceId);
        mDeviceRVAdapter.notifyDataSetChanged();

        new Thread(addUserDeviceRunnable).start();

        //原本放在上面，按下fab，Dialog設置此View後開啟，會將此View放在主畫面上
        //而想再開一次Dialog會因為，此View已有一個parent，而出錯。
        //解:1.將view放在這，每次新開一個;2.拿到該view的parent，removeView掉該View。
        //LayoutInflater inflater = LayoutInflater.from(mContext);
        //View addDeviceView = inflater.inflate(R.layout.dialog_add_device, null);

        //final EditText edtTxtDName = addDeviceView.findViewById(R.id.edtTxtDName);
        //用MQTT或其他方法接收MAC Addr，在這新增進RVA。
        //mDeviceRVAdapter.setDeviceAddr();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.add_device)
                //.setView(addDeviceView)
                .setMessage(getString(R.string.add_meg))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .show();
        ClearSharedPrefs();
        //getActivity().getIntent().putExtra(Attribute.DEVICE_ID, "-1");
    }

    void ClearSharedPrefs() {
        SharedPreferences appSharedPrefs = getSharedPreferences();
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.clear();
        prefsEditor.apply();

    }
    private DeviceRVAdapter.OnItemClickListener onItemClickListener = new DeviceRVAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent(getActivity(), RelayActivity.class);
            final String deviceId = mDeviceRVAdapter.getDeviceAddr(position);
            intent.putExtra(Attribute.DEVICE_ID, deviceId);

            startActivity(intent);
        }

        @Override
        public void onItemLongClick(View view, int position) {
            deviceId = mDeviceRVAdapter.getDeviceAddr(position);
            AlertDialog.Builder Builder = new AlertDialog.Builder(mContext);
            Builder.setTitle(R.string.choice_action)
                    .setCancelable(false)
                    .setPositiveButton(R.string.delete_device, (dialog, which) -> {
                        AlertDialog.Builder delBuilder = new AlertDialog.Builder(mContext);
                        delBuilder
                                .setMessage(R.string.check_once)
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes, (dialog13, which13) -> {
                                    AlertDialog.Builder checkBuilder = new AlertDialog.Builder(mContext);
                                    checkBuilder.setTitle(R.string.ok)
                                            .setCancelable(false)
                                            .setMessage(R.string.check_twice)
                                            .setPositiveButton(R.string.ok, (dialog12, which12) -> {
                                                new Thread(deleteDeviceRunnable).start();
                                                manualRefresh();
                                            })
                                            .setNegativeButton(R.string.cancel, null)
                                            .show();
                                })
                                .setNeutralButton(R.string.no, null)
                                .show();
                    })
                    .setNegativeButton(R.string.modify, (dialog, which) -> {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        final View v = inflater.inflate(R.layout.dialog_alter_device_n_relay_name, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        final EditText edtTxtRName = v.findViewById(R.id.edtTxtDnRName);
                        edtTxtRName.setText(mDeviceRVAdapter.getDeviceName(position));

                        builder.setTitle(R.string.modify_device_name)
                                .setView(v)
                                .setCancelable(false)
                                .setPositiveButton(R.string.ok, (dialog1, which1) -> {
                                    deviceName = edtTxtRName.getText().toString();
                                    new Thread(modeifyDeviceRunnable).start();
                                    manualRefresh();
                                }).show();
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .show();


        }
    };

    private void manualRefresh()
    {
        mSwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(() -> {
            updateOperation();
        }, 300);
    }

    private void updateOperation()
    {
        new Thread(getUserDeviceRunnable).start();
    }

    void initRunnable(){
        addUserDeviceRunnable = () -> {
            mMysqlConnect.addUserAndDevice(userId, deviceId, deviceName);
            deviceId = "";
        };

        getUserDeviceRunnable = () -> {
            mMysqlConnect.getUserAndDevice(userId);

            mDeviceRecyclerView.postDelayed(() -> {
                mDeviceRVAdapter.removeAllDatas();
                try {
                    for (UserAndDeviceModel userAndDeviceModel : mMysqlConnect.getUserAndDeviceModelList()) {
                        //以位址做判斷，名稱重複沒關係
                        if (mDeviceRVAdapter.setDeviceAddr(userAndDeviceModel.getDeviceId()))
                            mDeviceRVAdapter.setDeviceName(userAndDeviceModel.getDeviceName());
                    }
                    mDeviceRVAdapter.notifyDataSetChanged();
                }catch (Exception e){Toast.makeText(mContext, "系統目前出錯，請稍後再試", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();}

                mSwipeRefreshLayout.setRefreshing(false);
            },10);
        };

        deleteDeviceRunnable = () -> {
            mMysqlConnect.deleteDevice(deviceId);
            deviceId = "";
        };

        modeifyDeviceRunnable = () -> {
            mMysqlConnect.modifyDeviceName(deviceId, deviceName);
            deviceId = "";
        };
    }

    private SharedPreferences getSharedPreferences() {
        return Objects.requireNonNull(getActivity()).
                getSharedPreferences(Attribute.SHARED_PREFS_DEVICE_ID_RAW_DATA, MODE_PRIVATE);
    }
}
