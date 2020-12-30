package com.example.negativeion.espsmartconfig.v1;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.example.negativeion.Attribute;
import com.example.negativeion.activity.MainActivity;
import com.example.negativeion.espsmartconfig.SmartConfigActivityAbs;
import com.example.negativeion.espsmartconfig.SmartConfigApp;
import com.example.negativeion.R;
import com.negativeion.espsmartconfig.EsptouchTask;
import com.negativeion.espsmartconfig.IEsptouchResult;
import com.negativeion.espsmartconfig.IEsptouchTask;
import com.negativeion.espsmartconfig.util.ByteUtil;
import com.negativeion.espsmartconfig.util.TouchNetUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SmartConfigActivity extends SmartConfigActivityAbs {
    private static final String TAG = SmartConfigActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION = 0x01;

    private static String sResultBssid;

    private SmartConfigViewModel mViewModel;

    private EsptouchAsyncTask4 mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartconfig);
        mViewModel = new SmartConfigViewModel();
        mViewModel.apSsidTV = findViewById(R.id.apSsidText);
        //mViewModel.apBssidTV = findViewById(R.id.apBssidText);
        mViewModel.apPasswordEdit = findViewById(R.id.apPasswordEdit);
        //mViewModel.deviceCountEdit = findViewById(R.id.deviceCountEdit);
        //mViewModel.packageModeGroup = findViewById(R.id.packageModeGroup);
        mViewModel.messageView = findViewById(R.id.messageView);
        mViewModel.confirmBtn = findViewById(R.id.confirmBtn);
        mViewModel.confirmBtn.setOnClickListener(v -> executeEsptouch());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, REQUEST_PERMISSION);
        }

        SmartConfigApp.getInstance().observeBroadcast(this, broadcast -> {
            Log.d(TAG, "onCreate: Broadcast=" + broadcast);
            onWifiChanged();
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onWifiChanged();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.smartconfig_location_permission_title)
                        .setMessage(R.string.smartconfig_location_permission_message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .show();
            }

            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected String getEspTouchVersion() {
        return "";//getString(R.string.smartconfig_about_version, IEsptouchTask.ESPTOUCH_VERSION);
    }

    private StateResult check() {
        StateResult result = checkPermission();
        if (!result.permissionGranted) {
            return result;
        }
        result = checkLocation();
        result.permissionGranted = true;
        if (result.locationRequirement) {
            return result;
        }
        result = checkWifi();
        result.permissionGranted = true;
        result.locationRequirement = false;
        return result;
    }

    private void onWifiChanged() {
        StateResult stateResult = check();
        mViewModel.message = stateResult.message;
        mViewModel.ssid = stateResult.ssid;
        mViewModel.ssidBytes = stateResult.ssidBytes;
        mViewModel.bssid = stateResult.bssid;
        mViewModel.confirmEnable = false;
        if (stateResult.wifiConnected) {
            mViewModel.confirmEnable = true;
            if (stateResult.is5G) {
                mViewModel.message = getString(R.string.smartconfig_wifi_5g_message);
            }
        } else {
            if (mTask != null) {
                mTask.cancelEsptouch();
                mTask = null;
                new AlertDialog.Builder(SmartConfigActivity.this)
                        .setMessage(R.string.smartconfig_configure_wifi_change_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        }
        mViewModel.invalidateAll();
    }

    private void executeEsptouch() {
        SmartConfigViewModel viewModel = mViewModel;
        byte[] ssid = viewModel.ssidBytes == null ? ByteUtil.getBytesByString(viewModel.ssid)
                : viewModel.ssidBytes;
        CharSequence pwdStr = mViewModel.apPasswordEdit.getText();
        byte[] password = pwdStr == null ? null : ByteUtil.getBytesByString(pwdStr.toString());
        byte[] bssid = TouchNetUtil.parseBssid2bytes(viewModel.bssid);
        CharSequence devCountStr = "1";//mViewModel.deviceCountEdit.getText();
        byte[] deviceCount = devCountStr == null ? new byte[0] : devCountStr.toString().getBytes();
        byte[] broadcast = {(byte) (1)};
                /*{(byte) (mViewModel.packageModeGroup.getCheckedRadioButtonId() == R.id.packageBroadcast
                ? 1 : 0)};*/

        if (mTask != null) {
            mTask.cancelEsptouch();
        }
        mTask = new EsptouchAsyncTask4(this);
        mTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    private static class EsptouchAsyncTask4 extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {
        private WeakReference<SmartConfigActivity> mActivity;

        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        private AlertDialog mResultDialog;
        private IEsptouchTask mEsptouchTask;

        EsptouchAsyncTask4(SmartConfigActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        void cancelEsptouch() {
            cancel(true);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mResultDialog != null) {
                mResultDialog.dismiss();
            }
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected void onPreExecute() {
            Activity activity = mActivity.get();
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getString(R.string.smartconfig_configuring_message));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(dialog -> {
                synchronized (mLock) {
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel),
                    (dialog, which) -> {
                        synchronized (mLock) {
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                    });
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(IEsptouchResult... values) {
            Context context = mActivity.get();
            if (context != null) {
                IEsptouchResult result = values[0];
                Log.i(TAG, "EspTouchResult: " + result);
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            SmartConfigActivity activity = mActivity.get();
            int taskResultCount;
            synchronized (mLock) {
                byte[] apSsid = params[0];
                byte[] apBssid = params[1];
                byte[] apPassword = params[2];
                byte[] deviceCountData = params[3];
                byte[] broadcastData = params[4];
                taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
                Context context = activity.getApplicationContext();
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
                mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
                mEsptouchTask.setEsptouchListener(this::publishProgress);
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            SmartConfigActivity activity = mActivity.get();
            activity.mTask = null;
            mProgressDialog.dismiss();
            if (result == null) {
                mResultDialog = new AlertDialog.Builder(activity)
                        .setMessage(R.string.smartconfig_configure_result_failed_port)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            // check whether the task is cancelled and no results received
            IEsptouchResult firstResult = result.get(0);
            if (firstResult.isCancelled()) {
                return;
            }
            // the task received some results including cancelled while
            // executing before receiving enough results

            if (!firstResult.isSuc()) {
                mResultDialog = new AlertDialog.Builder(activity)
                        .setMessage(R.string.smartconfig_configure_result_failed)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                mResultDialog.setCanceledOnTouchOutside(false);
                return;
            }

            ArrayList<CharSequence> resultMsgList = new ArrayList<>(result.size());
            for (IEsptouchResult touchResult : result) {
                String message = activity.getString(R.string.smartconfig_configure_result_success_item,
                        touchResult.getBssid(), touchResult.getInetAddress().getHostAddress());
                resultMsgList.add(message);
                sResultBssid = touchResult.getBssid();
            }
            CharSequence[] items = new CharSequence[resultMsgList.size()];
            mResultDialog = new AlertDialog.Builder(activity)
                    .setTitle(R.string.smartconfig_configure_result_success)
                    .setItems(resultMsgList.toArray(items), null)
                    .setPositiveButton(android.R.string.ok, activity.resultRack)
                    .show();
            mResultDialog.setCanceledOnTouchOutside(false);
        }
    }

    protected DialogInterface.OnClickListener resultRack = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            SharedPreferences appSharedPrefs =
                    getSharedPreferences(Attribute.SHARED_PREFS_DEVICE_ID_RAW_DATA,MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
            prefsEditor.clear();
            prefsEditor.putString(Attribute.SHARED_P_EDITOR_STRING_DEVICE_RAW, sResultBssid);
            prefsEditor.apply();

            finish();
        }
    };

}
