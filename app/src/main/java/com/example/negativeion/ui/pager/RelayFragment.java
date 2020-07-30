package com.example.negativeion.ui.pager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class RelayFragment extends Fragment implements IMqttResponse {

    private MqttAsyncHelper mMqttAsyncHelper;
    private MysqlConnect mMysqlConnect;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelayRVAdapter mRelayRVAdapter;
    private RecyclerView mRelayRecyclerView;

    private Runnable relayConditionRunnable, updateRunnable;

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

        mMysqlConnect = new MysqlConnect();

        initRunnable();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mRelayRecyclerView = getView().findViewById(R.id.rv_relay);
            mRelayRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            mRelayRVAdapter = new RelayRVAdapter(getContext());
            mRelayRVAdapter.setOnItemClickListener(onItemClickListener);
            mRelayRVAdapter.OnCheckedChangeListener(onCheckedChangeListener);
            mRelayRecyclerView.setAdapter(mRelayRVAdapter);
            initRelayList();
        }catch (Exception e){
            Toast.makeText(getContext(), "bug:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(relayConditionRunnable).start();
        Toast.makeText(getContext(), "更新資料中", Toast.LENGTH_SHORT).show();

        //try {
            SharedPreferences appSharedPrefs = Objects.requireNonNull(getActivity()).
                    getSharedPreferences("negative_relay", MODE_PRIVATE);
        //}catch (NullPointerException e){//假設為空，就從db上撈繼電器名稱}
        List<String> list = mRelayRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            list.set(i, appSharedPrefs.getString(Integer.toString(i), "編號00"+i));
        }
        mRelayRVAdapter.setRelayNameList(list);
        mRelayRVAdapter.notifyDataSetChanged();

        mqttConnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(getContext(), "pause", Toast.LENGTH_SHORT).show();
        SharedPreferences appSharedPrefs = getActivity().
                getSharedPreferences("negative_relay",MODE_PRIVATE);
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

    private void initRelayList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        stringList.add("0");
        List<String> nameList = new ArrayList<>();
        for(int i=0; i<5; i++)
            nameList.add("編號0"+i);
        mRelayRVAdapter.setRelayList(stringList);
        mRelayRVAdapter.setRelayNameList(nameList);
        mRelayRVAdapter.notifyDataSetChanged();
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
            //mRelayRVAdapter.notifyDataSetChanged();
            new Thread(updateRunnable).start();
        }
    };

    private RelayRVAdapter.OnItemClickListener onItemClickListener = new RelayRVAdapter.OnItemClickListener() {
        @Override
        public void onItemLongClick(View view, int position, String string) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            final View v = inflater.inflate(R.layout.dialog_alter_relay_name, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final EditText edtTxtRName = v.findViewById(R.id.edtTxtRName);
            edtTxtRName.setText(mRelayRVAdapter.getRelayNameList().get(position));
            final List list = mRelayRVAdapter.getRelayNameList();
            final int pos = position;
            builder.setTitle("修改繼電器名稱")
                    .setView(v)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list.set(pos, edtTxtRName.getText().toString());
                            mRelayRVAdapter.setRelayNameList(list);
                            mRelayRVAdapter.notifyDataSetChanged();
                        }
                    }).show();
        }
    };

    void initRunnable(){
        relayConditionRunnable = new Runnable() {
            @Override
            public void run() {
                final String deviceId = getActivity().getIntent().getStringExtra(Attribute.DEVICE_ID);
                mMysqlConnect.getRelayCondition(deviceId);

                mRelayRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            List<String> list = mRelayRVAdapter.getRelayList();
                            String str = mMysqlConnect.getResponse();
                            //Toast.makeText(getContext(), "re:"+list.size()+" str:"+mMysqlConnect.getResponse()+" "+str.length, Toast.LENGTH_SHORT).show();
                            for(int i=0; i<list.size(); i++) {
                                String strTemp = ""+str.charAt(i);
                                list.set(i, strTemp);
                            }
                            mRelayRVAdapter.setRelayList(list);
                            mRelayRVAdapter.notifyDataSetChanged();
                            //Toast.makeText(getContext(), "資料更新成功", Toast.LENGTH_SHORT).show();
                        }catch (Exception e)
                        {
                            Log.d("Device分頁", "ERROR" + e.getMessage());
                            //Toast.makeText(getContext(), "資料更新失敗:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },10);
            }
        };

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                /*boolean check = mMysqlConnect.init();
                if(check)
                    mMysqlConnect.jdbcAddRelay();*/
                if(mMqttAsyncHelper != null)  //Turn ArrayList to Array and then to String.
                    mMqttAsyncHelper.publishData(Arrays.toString(mRelayRVAdapter.getRelayList().toArray()));

                final String deviceId = getActivity().getIntent().getStringExtra(Attribute.DEVICE_ID);
                mMysqlConnect.updateRelay(deviceId);

                mRelayRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getContext(), "MAC:"+deviceId, Toast.LENGTH_SHORT).show();
                        //Log.d("POST Test", "body:" + mMysqlConnect.getResponse());
                    }
                },500);
            }
        };
    }

    public void mqttConnect()
    {
        final String deviceId = getActivity().getIntent().getStringExtra(Attribute.DEVICE_ID);
        if (mMqttAsyncHelper == null) {
            mMqttAsyncHelper = new MqttAsyncHelper(getContext(),RelayFragment.this);
            mMqttAsyncHelper.setUsername("1")
                    .setPassword("")
                    .setClientId(deviceId)
                    .setSubscriptionTopic("test123")
                    .setPublishTopic("60:01:94:2c:d7:a6")
                    .setQos(new int[]{1})
                    .build();
        } else {
            mMqttAsyncHelper.setUsername("1")
                    .setPassword("")
                    .setClientId(deviceId)
                    .setSubscriptionTopic("test123")
                    .setPublishTopic("60:01:94:2c:d7:a6")
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
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 1)
            {
                //Need to reconnect Mqtt, if the state equal Disconnect this word.
                //mTxtConnectState.setText(msg.obj.toString());
            }else if(msg.arg1 == 2){
                //mTxtReceive.setText(mTxtReceive.getText().toString() + "\n" +msg.obj.toString());
            }else if(msg.arg1 == 3){
                //mTxtSend.setText(msg.obj.toString());
            }
        }
    };
}
