package com.pjm9355.hospitalmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Activity activity;
    private ArrayList<HospitalItem> hospitalItemList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvAddr, tvTelNo, tvUrl;
        private final View vTelNo;

        public ViewHolder(View view) {
            super(view);

            tvName = view.findViewById(R.id.hosp_name_textview);
            tvAddr = view.findViewById(R.id.address_textview);
            tvTelNo = view.findViewById(R.id.tel_no_textview);
            tvUrl = view.findViewById(R.id.hosp_url_textview);
            vTelNo = view.findViewById(R.id.tel_no_view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(activity, DetailActivity.class);
                        intent.putExtra("hospitalItem", hospitalItemList.get(pos));
                        activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    }
                }
            });
        }

        public TextView getTvName() {
            return tvName;
        }

        public TextView getTvAddr() { return tvAddr; }

        public TextView getTvTelNo() {
            return tvTelNo;
        }

        public TextView getTvUrl() {
            return tvUrl;
        }

        public View getVTelNo() { return vTelNo; }
    }

    public RecyclerViewAdapter(Activity a, ArrayList<HospitalItem> hospitalItemList) {
        activity = a;
        this.hospitalItemList = hospitalItemList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_hosp_info, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HospitalItem item = hospitalItemList.get(position);

        holder.getTvName().setText(item.getHospName());
        holder.getTvAddr().setText(item.getAddress());

        String telno = item.getTelNo();
        if (telno==null || telno.isEmpty()) {
            holder.getVTelNo().setVisibility(View.GONE);
        } else {
            holder.getVTelNo().setVisibility(View.VISIBLE);
            holder.getTvTelNo().setText(telno);
        }

        String url = item.getHospUrl();
        if (url==null || url.isEmpty()) {
            holder.getTvUrl().setVisibility(View.GONE);
        } else {
            holder.getTvUrl().setVisibility(View.VISIBLE);
            holder.getTvUrl().setPaintFlags(holder.getTvUrl().getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.getTvUrl().setText(url);
        }
    }

    @Override
    public int getItemCount() {
        return hospitalItemList.size();
    }
}
