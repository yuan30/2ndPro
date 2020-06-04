package com.example.negativeion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class RelayRVAdapter extends RecyclerView.Adapter<RelayRVAdapter.ViewHolder> implements View.OnClickListener
, View.OnLongClickListener{



    private List<String> mRelayList;
    private List<String> mRelayNameList;
    private Context mContext;
    private EditText mEdtTxtRName;
    private OnItemClickListener mOnItemClickListener;

    public RelayRVAdapter(Context context, EditText EdtTxtRName)
    {
        super();
        mContext = context;
        mEdtTxtRName = EdtTxtRName;
    }

    public void setRelayList(List<String> relayList, List<String> relayNameList){
        this.mRelayList = relayList;
        this.mRelayNameList = relayNameList;
    }

    public List<String> getmRelayList() {
        return mRelayList;
    }

    public List<String> getmRelayNameList() {
        return mRelayNameList;
    }

    @NonNull
    @Override
    public RelayRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.mTextView2.setText(mRelayNameList.get(position));
        if(mRelayList.get(position).equals("0"))
            holder.mTextView3.setText("關");
        else
            holder.mTextView3.setText("開");
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position, String string);
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
            mOnItemClickListener.onItemLongClick(view,(int)view.getTag(), mRelayList.get((int)view.getTag()));
            /*final int position = (int)view.getTag();
            mEdtTxtRName.setText(mRelayNameList.get(position)+"123");
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("修改繼電器資料")
                    .setView(R.layout.dialog_alter_relay_name)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRelayNameList.set(position, mEdtTxtRName.getText().toString());
                        }
                    }).show();*/
        }
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView2, mTextView3;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView2 = itemView.findViewById(R.id.textView2);
            mTextView3 = itemView.findViewById(R.id.textView3);
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


}
