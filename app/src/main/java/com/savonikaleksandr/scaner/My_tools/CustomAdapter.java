package com.savonikaleksandr.scaner.My_tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.savonikaleksandr.scaner.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Alex
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(DataModel data, int position);
    }

    private final OnItemClickListener onItemClickListener;
    private final LayoutInflater inflater;
    private List<DataModel> dataSet;

    public CustomAdapter(List<DataModel> data, Context context, OnItemClickListener onItemClick) {
        this.onItemClickListener = onItemClick;
        this.inflater = LayoutInflater.from(context);
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.buityful_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TextView titlee = holder.title;
        TextView text = holder.text;
        ImageView image_ob = holder.image_obj;

        titlee.setText(dataSet.get(position).getTitle());
        text.setText(dataSet.get(position).getText());
        Picasso.get().load(dataSet.get(position).getImage_obj()).into(image_ob);

        DataModel data = dataSet.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onItemClickListener.onItemClick(data, position);
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView image_obj;
        final TextView title, text;
        MyViewHolder(View view){
            super(view);
            this.image_obj = (ImageView)view.findViewById(R.id.image_card_object);
            this.title = (TextView) view.findViewById(R.id.title_card_object);
            this.text = (TextView) view.findViewById(R.id.text_card);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}