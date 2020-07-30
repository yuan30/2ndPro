package com.example.negativeion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangedReceiver extends BroadcastReceiver {

    private boolean isConnect = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        /*int netWorkStates = NetworkUtil.getNetWorkStates(context);

        switch (netWorkStates) {
            case NetworkUtil.TYPE_NONE:
                //斷網了
                break;
            case NetworkUtil.TYPE_MOBILE:
                //打開了行動網路
                break;
            case NetworkUtil.TYPE_WIFI:
                //打開了WIFI
                break;

            default:
                break;
        }*/
    }

    public boolean gerStatus()
    {return isConnect;}
}
