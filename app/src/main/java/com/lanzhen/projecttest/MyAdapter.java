package com.lanzhen.projecttest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leiden.model.LeidenDevice;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;

    List<LeidenDevice> leidenDevices;

    public MyAdapter(Context context,List<LeidenDevice> leidenDevices){
        this.context = context;
        this.leidenDevices  = leidenDevices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        MyViewHolder myViewHolder = new MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item, parent, false)
        );

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        LeidenDevice leidenDevice = leidenDevices.get(position);
        holder.textView.setText(leidenDevice.getName() + " | " + leidenDevice.getMac());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickListener != null){
                    itemClickListener.click(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return leidenDevices.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void click(int position);
    }

}
