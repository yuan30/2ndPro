package com.example.negativeion.ui.pager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.Attribute;
import com.example.negativeion.IMqttResponse;
import com.example.negativeion.MqttAsyncHelper;
import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.RelayRVAdapter;
import com.example.negativeion.model.RelayNameModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class RelayFragment extends Fragment implements IMqttResponse {

    private View mView;
    private Context mContext;
    private MqttAsyncHelper mMqttAsyncHelper;
    private MysqlConnect mMysqlConnect;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelayRVAdapter mRelayRVAdapter;
    private RecyclerView mRelayRecyclerView;

    private Handler mDeviceHandler;
    private Runnable getRelayConditionRunnable, uploadRelayConditionRunnable, uploadRelayNameRunnable
            , checkDeviceRunnable, getRelayNameRunnable;

    private boolean bDeviceIsAlive = true;

    String deviceId;

    public RelayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relay, container, false);
        mContext = requireContext();
        mView = view;
        mMysqlConnect = new MysqlConnect();

        mDeviceHandler = new Handler();
        initRunnable();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

            initView();
            initRelayList();
        }catch (Exception e){
            Toast.makeText(mContext, "bug:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(getRelayConditionRunnable).start();
        //Snackbar.make(getView(), deviceId, Snackbar.LENGTH_SHORT).show();
        //Toast.makeText(mContext, "更新資料中", Toast.LENGTH_SHORT).show();
        setRelayName();

        mqttConnect();

        checkDeviceAlive();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(mContext, "pause", Toast.LENGTH_SHORT).show();
        SharedPreferences appSharedPrefs = getSharedPreferences();
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.clear();
        List<String> list = mRelayRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            prefsEditor.putString(Integer.toString(i), list.get(i));
        }
        prefsEditor.apply();

        if(mMqttAsyncHelper != null)
            mqttDisconnect();
    }

    private void initView() {
        deviceId = getActivity().getIntent().getStringExtra(Attribute.DEVICE_ID);

        mRelayRecyclerView = getView().findViewById(R.id.rv_relay);
        mRelayRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRelayRVAdapter = new RelayRVAdapter(mContext);
        mRelayRVAdapter.setOnItemClickListener(onItemClickListener);
        mRelayRVAdapter.OnCheckedChangeListener(onCheckedChangeListener);
        mRelayRecyclerView.setAdapter(mRelayRVAdapter);
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mSwipeRefreshLayout = getView().findViewById(R.id.relaySwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(
                () -> {
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    updateOperation();
                }
        );
    }
    private void initRelayList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        List<String> nameList = new ArrayList<>();
        for(int i=1; i<=5; i++)
            nameList.add(" ");
        mRelayRVAdapter.setRelayList(stringList);
        mRelayRVAdapter.setRelayNameList(nameList);
        mRelayRVAdapter.notifyDataSetChanged();
    }

    private void setRelayName() {

        SharedPreferences appSharedPrefs = checkRelayNameData();

        List<String> list = mRelayRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            list.set(i, appSharedPrefs.getString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[i]
                    , "裝置 "+(i+1)));
        }
        mRelayRVAdapter.setRelayNameList(list);
        mRelayRVAdapter.notifyDataSetChanged();
    }

    private SharedPreferences checkRelayNameData() {
        SharedPreferences appSharedPrefs = null;
        appSharedPrefs = getSharedPreferences();
        boolean HasRelayName = appSharedPrefs.getBoolean(Attribute.SHARED_PREFS_BOOLEAN_RELAY_NAME
                , false);
        //try {

        //}catch (NullPointerException e){//假設為空，就從db上撈繼電器名稱
        if(!HasRelayName) {
            Snackbar.make(getView(), "RR", Snackbar.LENGTH_SHORT).show();
            Thread thread = new Thread(getRelayNameRunnable);
            thread.start();
            //Log.d("relay null name", "0");
            try {
                thread.join();
            } catch (InterruptedException Ie) {
            }

            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

            for (RelayNameModel relayNameModel : mMysqlConnect.getRelayNameModelList()) {
                prefsEditor.putString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[0],
                        relayNameModel.getRelay1Name())
                        .putString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[1],
                                relayNameModel.getRelay2Name())
                        .putString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[2],
                                relayNameModel.getRelay3Name())
                        .putString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[3],
                                relayNameModel.getRelay4Name())
                        .putString(Attribute.SHARED_PREFS_STRING_RELAYS_NAME[4],
                                relayNameModel.getRelay5Name());
            }

            prefsEditor.putBoolean(Attribute.SHARED_PREFS_BOOLEAN_RELAY_NAME, true);
            prefsEditor.apply();
        }
        //}

        return appSharedPrefs;
    }

    private void checkDeviceAlive() {
        if(bDeviceIsAlive) {
            Toast.makeText(mContext, "檢查裝置是否正常", Toast.LENGTH_SHORT).show();
            mDeviceHandler.postDelayed(checkDeviceRunnable, 3000);
        }
        else
            Toast.makeText(mContext, "裝置異常，請稍等...", Toast.LENGTH_SHORT).show();
    }

    private RelayRVAdapter.OnCheckedChangeListener onCheckedChangeListener = new RelayRVAdapter.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, int position, int relay) {

            mMysqlConnect.setRelayId(position+1);
            mMysqlConnect.setRelay(relay);

            List list = mRelayRVAdapter.getRelayList();
            list.set(position, Integer.toString(relay));
            mRelayRVAdapter.setRelayList(list);
            //在RecycleView準備layout時，不能呼叫。因在onResume有上資料庫取值，更改switch後，再進來這，上傳資料庫(剛好循環)
            //但畫面還沒準備好，所以在上傳資料庫之前，退出App。
            if(!mRelayRecyclerView.isComputingLayout()) //還在計算布局時，配適器內容不允許變動。
                mRelayRVAdapter.notifyDataSetChanged();
            //改成在執行緒裡確定裝置沒斷線才上傳 Mqtt，不然斷線時會一並將所有繼電器資料重置。
            new Thread(uploadRelayConditionRunnable).start();
        }
    };

    private RelayRVAdapter.OnItemClickListener onItemClickListener = new RelayRVAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position, String string) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            final View v = inflater.inflate(R.layout.dialog_alter_device_n_relay_name, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final EditText edtTxtRName = v.findViewById(R.id.edtTxtDnRName);
            edtTxtRName.setText(mRelayRVAdapter.getRelayNameList().get(position));
            final List list = mRelayRVAdapter.getRelayNameList();
            final int pos = position;

            mMysqlConnect.setRelayId(position+1);

            builder.setTitle(R.string.modify_relay_name)
                    .setView(v)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        list.set(pos, edtTxtRName.getText().toString());
                        mRelayRVAdapter.setRelayNameList(list);
                        mRelayRVAdapter.notifyDataSetChanged();
                        mMysqlConnect.setRelayName(edtTxtRName.getText().toString());
                        new Thread(uploadRelayNameRunnable).start();
                    }).show();
        }
    };

    private void manualRefresh()
    {
        mSwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(() -> {
            updateOperation();
        }, 200);
    }

    private void updateOperation()
    {
        new Thread(getRelayConditionRunnable).start();
    }

    void initRunnable(){
        getRelayConditionRunnable = () -> {
            mMysqlConnect.getRelayCondition(deviceId);

            mRelayRecyclerView.postDelayed(() -> {

                try {
                    List<String> list = mRelayRVAdapter.getRelayList();
                    String str = mMysqlConnect.getResponse();
                    //Toast.makeText(mContext, "re:"+list.size()+" str:"+mMysqlConnect.getResponse()+" "+str.length, Toast.LENGTH_SHORT).show();
                    for(int i=0; i<list.size(); i++) {
                        String strTemp = ""+str.charAt(i);
                        list.set(i, strTemp);
                    }
                    mRelayRVAdapter.setRelayList(list);
                    mRelayRVAdapter.notifyDataSetChanged();
                    //Toast.makeText(mContext, "資料更新成功", Toast.LENGTH_SHORT).show();
                }catch (Exception e)
                {
                    Log.d("Device分頁", "ERROR" + e.getMessage());
                    //Toast.makeText(mContext, "資料更新失敗:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            },10);
        };

        uploadRelayConditionRunnable = () -> {
            if(mMqttAsyncHelper != null && bDeviceIsAlive)  //Turn ArrayList to Array and then to String.
                mMqttAsyncHelper.publishData(Arrays.toString(mRelayRVAdapter.getRelayList().toArray()));

            mMysqlConnect.uploadRelay(deviceId);

            /*mRelayRecyclerView.postDelayed(() -> {
                //Toast.makeText(mContext, "MAC:"+deviceId, Toast.LENGTH_SHORT).show();
                //Log.d("POST Test", "body:" + mMysqlConnect.getResponse());
            },500);*/
        };

        uploadRelayNameRunnable = () -> {
            mMysqlConnect.uploadRelayName(deviceId);
        };

        checkDeviceRunnable = () -> {
            if(bDeviceIsAlive) {
                Snackbar.make(mView, "裝置異常，請稍等...", Snackbar.LENGTH_SHORT).show();
                bDeviceIsAlive = false;
                /*for (int i = 0; i < mRelayRVAdapter.getRelayList().size(); i++)
                    mRelayRVAdapter.getRelayList().set(i, "0");*/
                mRelayRVAdapter.resetItemLayout();
                mRelayRVAdapter.notifyDataSetChanged();

            }else {
                Snackbar.make(mView, "裝置重連中，請稍等...", Snackbar.LENGTH_SHORT).show();
                bDeviceIsAlive = true;
                mRelayRVAdapter.resetItemLayout();
                new Thread(getRelayConditionRunnable).start();
                new Thread(uploadRelayConditionRunnable).start();
            }
        };

        getRelayNameRunnable = () -> {
            mMysqlConnect.getRelayName(deviceId);
        };
    }

    public void mqttConnect()
    {
        if (mMqttAsyncHelper == null) {
            mMqttAsyncHelper = new MqttAsyncHelper(mContext,RelayFragment.this);
            mMqttAsyncHelper.setUsername("1")
                    .setPassword("")
                    .setClientId(deviceId)
                    .setSubscriptionTopic(deviceId + "@")
                    .setPublishTopic(deviceId)
                    .setQos(new int[]{1})
                    .build();
        } else {
            mMqttAsyncHelper.setUsername("1")
                    .setPassword("")
                    .setClientId(deviceId)
                    .setSubscriptionTopic(deviceId + "@")
                    .setPublishTopic(deviceId)
                    .setQos(new int[]{1})
                    .build();
        }
    }

    public void mqttDisconnect()
    {
        mMqttAsyncHelper.disconnect();
    }
    @Override
    public void receiveMessage(String topic, String response) {

        Message msg = new Message();
        msg.arg1 = 2;
        msg.obj = response;
        handler.sendMessage(msg);
    }

    @Override
    public void connectState(boolean connectState) {

        final String connectStateString;
        if (connectState) {
            connectStateString = "Connect";
        } else {
            connectStateString = "Disconnect";
        }
        Message msg = new Message();
        msg.arg1 = 1;
        msg.obj = connectStateString;
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 1)
            {
                //Need to reconnect Mqtt, if the state equal Disconnect this word.
                //mTxtConnectState.setText(msg.obj.toString());
            }else if(msg.arg1 == 2){
                //mTxtReceive.setText(mTxtReceive.getText().toString() + "\n" +msg.obj.toString());
                mDeviceHandler.removeCallbacks(checkDeviceRunnable);
                if(bDeviceIsAlive)
                    mDeviceHandler.postDelayed(checkDeviceRunnable, 3000);
                else {
                    mDeviceHandler.post(checkDeviceRunnable);
                    mDeviceHandler.postDelayed(checkDeviceRunnable, 3000);
                }
            }else if(msg.arg1 == 3){
                //mTxtSend.setText(msg.obj.toString());
            }
        }
    };

    private SharedPreferences getSharedPreferences() {
        return Objects.requireNonNull(getActivity()).
                getSharedPreferences("negative_relay" + deviceId, MODE_PRIVATE);
    }
}
