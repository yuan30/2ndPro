package com.example.negativeion.ui.pager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.negativeion.MysqlConnect;
import com.example.negativeion.R;
import com.example.negativeion.RelayRVAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends Fragment {

    MysqlConnect mMysqlConnect;
    EditText mEdtTxtRName;
    AlertDialog.Builder builder;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RelayRVAdapter mRelayRVAdapter;
    RecyclerView mRecyclerView;
    private Runnable relayConditionRunnable, sendRunnable;
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
        mMysqlConnect = new MysqlConnect();

        initRunnable();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View viewDialog = View.inflate(getContext(), R.layout.dialog_alter_relay_name, null);
        builder = new AlertDialog.Builder(getContext()).setView(viewDialog);
        mEdtTxtRName = viewDialog.findViewById(R.id.edtTxtRName);
        mRecyclerView = getView().findViewById(R.id.rv_relay);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRelayRVAdapter = new RelayRVAdapter(getContext(), mEdtTxtRName);
        mRelayRVAdapter.setOnItemClickListener(onItemClickListener);
        mRecyclerView.setAdapter(mRelayRVAdapter);
        initRelayList();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(relayConditionRunnable).start();

    }

    private void initRelayList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("0");
        stringList.add("0");
        stringList.add("1");
        stringList.add("0");
        stringList.add("0");
        List<String> nameList = new ArrayList<>();
        for(int i=0; i<5; i++)
            nameList.add("編號0"+i);
        mRelayRVAdapter.setRelayList(stringList, nameList);
        mRelayRVAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), " " + stringList.size(), Toast.LENGTH_SHORT).show();
    }

    private RelayRVAdapter.OnItemClickListener onItemClickListener = new RelayRVAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Toast.makeText(getContext(), "輕點 開關 ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onItemLongClick(View view, int position, String string) {
            Toast.makeText(getContext(), "長" + position, Toast.LENGTH_LONG).show();


            mEdtTxtRName.setText(mRelayRVAdapter.getmRelayNameList().get(position)+"123");
            final List list = mRelayRVAdapter.getmRelayNameList();
            final int pos = position;
            builder.setTitle("修改繼電器資料")
                    .setView(R.layout.dialog_alter_relay_name)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list.set(pos, mEdtTxtRName.getText().toString());
                            mRelayRVAdapter.setRelayList(mRelayRVAdapter.getmRelayList(),
                                    list);
                            //mRelayNameList.set(position, mEdtTxtRName.getText().toString());
                        }
                    }).show();
            mRelayRVAdapter.notifyDataSetChanged();
        }
    };

    void initRunnable(){
        relayConditionRunnable = new Runnable() {
            @Override
            public void run() {
                mMysqlConnect.connectRelay();

                mRecyclerView.postDelayed(new Runnable() {
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
