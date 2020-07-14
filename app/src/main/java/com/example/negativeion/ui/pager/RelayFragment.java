package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.RelayRVAdapter;
import com.example.negativeion.activity.RelayActivity;
import com.example.negativeion.activity.SignInActivity;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class RelayFragment extends Fragment {

    MysqlConnect mMysqlConnect;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RelayRVAdapter mRelayRVAdapter;
    RecyclerView mRelayRecyclerView;

    private Runnable relayConditionRunnable, sendRunnable;

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

        SharedPreferences appSharedPrefs  = Objects.requireNonNull(getActivity()).
                getSharedPreferences("negative_relay",MODE_PRIVATE);
        List<String> list = mRelayRVAdapter.getRelayNameList();
        for(int i = 0; i<list.size(); i++){
            list.set(i, appSharedPrefs.getString(Integer.toString(i), "編號0"+i));
        }
        mRelayRVAdapter.setRelayNameList(list);
        mRelayRVAdapter.notifyDataSetChanged();
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
            new Thread(sendRunnable).start();
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
                mMysqlConnect.connectRelay();

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
                },500);
            }
        };

        sendRunnable = new Runnable() {
            @Override
            public void run() {
                /*boolean check = mMysqlConnect.init();
                if(check)
                    mMysqlConnect.jdbcAddRelay();*/
                mMysqlConnect.sendRelay();

                mRelayRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("POST Test", "body:" + mMysqlConnect.getResponse());
                    }
                },500);
            }
        };
    }
}
