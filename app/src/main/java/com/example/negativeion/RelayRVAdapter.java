package com.example.negativeion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RelayRVAdapter extends RecyclerView.Adapter<RelayRVAdapter.ViewHolder> implements View.OnClickListener
, View.OnLongClickListener{

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    RelayRVAdapter(Context context)
    {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(mContext).createView()
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    @Override
    public void onClick(View view) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemLongClick(view,(int)view.getTag());
        }
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
