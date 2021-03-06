package com.example.negativeion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RelayRVAdapter extends RecyclerView.Adapter<RelayRVAdapter.ViewHolder> implements CompoundButton.OnCheckedChangeListener
    ,View.OnClickListener{


    private Boolean bIsAlive = true;
    private List<String> mRelayList;
    private List<String> mRelayNameList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public RelayRVAdapter(Context context)
    {
        super();
        mContext = context;
    }

    @NonNull
    @Override
    public RelayRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_relay_item, parent, false);
        //view.setOnClickListener(this);
        //view.setOnLongClickListener(this);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.mTextView2.setText(mRelayNameList.get(position));
        if(mRelayList.get(position).equals("0")) {
            holder.mSwitch.setChecked(false);
            holder.mMainLayout.setBackgroundResource(R.drawable.corners_smooth);
        }
        else if(mRelayList.get(position).equals("1")) {
            holder.mSwitch.setChecked(true);
            holder.mMainLayout.setBackgroundResource(R.drawable.corners_smooth_switch_on);
        }
        else
            holder.mSwitch.setText("錯誤");

        if(!bIsAlive) {
            holder.mMainLayout.setBackgroundResource(R.drawable.corners_smooth);
            holder.mSwitch.setText("斷線");
        }else
            holder.mSwitch.setText("");

        holder.mSwitch.setTag(position);
        holder.mSwitch.setOnCheckedChangeListener(this);

        holder.mImageView3.setTag(position);
        holder.mImageView3.setOnClickListener(this);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void OnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String string);
    }

    public interface OnCheckedChangeListener{
        void onCheckedChanged(CompoundButton buttonView, int position, int relay);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            mOnCheckedChangeListener.onCheckedChanged(buttonView, (int)buttonView.getTag(), 1);
        else
            mOnCheckedChangeListener.onCheckedChanged(buttonView, (int)buttonView.getTag(), 0);
    }

    @Override
    public void onClick(View view) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(view, (int)view.getTag(), mRelayList.get((int)view.getTag()));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mMainLayout;
        TextView mTextView2;
        ImageView mImageView3;
        Switch mSwitch;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mMainLayout = itemView.findViewById(R.id.mainLayout);
            mTextView2 = itemView.findViewById(R.id.textView2);
            mImageView3 = itemView.findViewById(R.id.imageView3);
            mSwitch = itemView.findViewById(R.id.switch1);
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mRelayList.size();
    }

    public List<String> getRelayList() {
        return mRelayList;
    }

    public void setRelayList(List<String> relayList){
        this.mRelayList = relayList;
    }

    public List<String> getRelayNameList() {
        return mRelayNameList;
    }

    public void setRelayNameList(List<String> relayNameList){
        this.mRelayNameList = relayNameList;
    }

    public void resetItemLayout(){
        this.bIsAlive = bIsAlive == true ? false : true;
    }
}
