package com.nowfloats.on_boarding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinksity.R;

/**
 * Created by Admin on 16-03-2018.
 */

public class OnBoardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private String[] titles, descriptions;
    LinearLayout.LayoutParams linLayoutParams;
    int leftSpace, topSpace;
    DisplayMetrics metrics;
    public OnBoardingAdapter(Context context){
        mContext = context;
        metrics = mContext.getResources().getDisplayMetrics();
        leftSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        topSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        linLayoutParams = new LinearLayout.LayoutParams(metrics.widthPixels * 80 / 100,
                metrics.heightPixels * 50 / 100);
        linLayoutParams.setMargins(leftSpace, topSpace, leftSpace, topSpace);
        descriptions = mContext.getResources().getStringArray(R.array.on_boarding_description);
        titles = mContext.getResources().getStringArray(R.array.on_boarding_titles);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_onboarding_item,parent,false);
                break;

            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_onboarding_item,parent,false);
                break;

        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder){
            MyViewHolder myHolder = (MyViewHolder) holder;
            myHolder.titleTv.setText(titles[position]);
            myHolder.descriptionTv.setText(descriptions[position]);
            //myHolder.btnTv.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return descriptions.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView titleTv, descriptionTv, btnTv;
        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setLayoutParams(linLayoutParams);

            titleTv = itemView.findViewById(R.id.tv_title);
            descriptionTv = itemView.findViewById(R.id.tv_description);
            btnTv = itemView.findViewById(R.id.btn_tv);
        }
    }
}
