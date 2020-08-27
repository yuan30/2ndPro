package com.example.negativeion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceRVAdapter extends RecyclerView.Adapter<DeviceRVAdapter.ViewHolder> implements View.OnClickListener
    , View.OnLongClickListener{


    private List<String> deviceNameList;
    private List<String> deviceAddrList;
    private Integer deviceIcon;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public DeviceRVAdapter(Context context)
    {
        super();
        mContext = context;
        deviceNameList = new ArrayList<>();
        deviceAddrList = new ArrayList<>();
        deviceIcon = R.drawable.ic_videogame_asset_black_24dp;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_device_item, parent, false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this); //if want to use listener in self.
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.mDeviceTitle.setText(deviceNameList.get(position));
        holder.mDeviceIcon.setImageResource(deviceIcon);
    }

    @Override
    public void onClick(View view) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(view, (int)view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemLongClick(view,(int)view.getTag());
        }
        return false;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mDeviceTitle;
        ImageView mDeviceIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mDeviceTitle = itemView.findViewById(R.id.deviceTitle);
            mDeviceIcon = itemView.findViewById(R.id.deviceIcon);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setDeviceName(String deviceTitle) {
        this.deviceNameList.add(deviceTitle);
    }

    public String getDeviceName(int position) {
        return this.deviceNameList.get(position);
    }

    public boolean setDeviceAddr(String deviceAddr) {
        if(!deviceAddrList.contains(deviceAddr)) {
            this.deviceAddrList.add(deviceAddr);
            return true;
        }
        else
            return false;
    }

    public String getDeviceAddr(int position) {
       return this.deviceAddrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return deviceNameList.size();
    }

    public void removeAllDatas() {
        this.deviceAddrList.clear();
        this.deviceNameList.clear();
    }
}
