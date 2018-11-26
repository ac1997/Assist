package com.caltruism.assist.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.activity.GetMemberTypeActivity;
import com.caltruism.assist.activity.RequestDetailsActivity;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.Constants;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final static String TAG = "RequestAdapter";

    private ColorStateList accentColorStateList;
    private ArrayList<AssistRequest> dataSet;
    private boolean isVolunteerView;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Context context;

        private final ImageView imageViewType;
        private final TextView textViewTitle;
        private final TextView textViewDistance;
        private final TextView textViewDate;
        private final TextView textViewUser;
        private final TextView textViewTime;
        private final TextView textViewDuration;

        private final ImageView imageViewTime;
        private final ImageView imageViewUser;

        private final ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            imageViewType = itemView.findViewById(R.id.imageViewCardLayoutRequestType);
            textViewTitle = itemView.findViewById(R.id.textViewCardLayoutRequestTitle);
            textViewDistance = itemView.findViewById(R.id.textViewCardLayoutRequestDistance);
            textViewDate = itemView.findViewById(R.id.textViewCardLayoutRequestDate);
            textViewUser = itemView.findViewById(R.id.textViewCardLayoutRequestUser);
            textViewTime = itemView.findViewById(R.id.textViewCardLayoutRequestTime);
            textViewDuration = itemView.findViewById(R.id.textViewCardLayoutRequestDuration);

            imageViewTime = itemView.findViewById(R.id.imageViewCardLayoutRequestTime);
            imageViewUser = itemView.findViewById(R.id.imageViewCardLayoutRequestUser);

            constraintLayout = itemView.findViewById(R.id.constraintLayoutCardLayoutRequest);
        }

        public ImageView getImageViewType() {
            return imageViewType;
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public TextView getTextViewDistance() {
            return textViewDistance;
        }

        public TextView getTextViewDate() {
            return textViewDate;
        }

        public TextView getTextViewUser() {
            return textViewUser;
        }

        public TextView getTextViewTime() {
            return textViewTime;
        }

        public TextView getTextViewDuration() {
            return textViewDuration;
        }

        public ImageView getImageViewTime() {
            return imageViewTime;
        }

        public ImageView getImageViewUser() {
            return imageViewUser;
        }

        public Context getContext() {
            return context;
        }

        public ConstraintLayout getConstraintLayout() {
            return constraintLayout;
        }
    }

    public RequestAdapter(Context context, ArrayList<AssistRequest> dataSet, boolean isVolunteerView) {
        this.accentColorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent));
        this.dataSet = dataSet;
        this.isVolunteerView = isVolunteerView;
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_layout_request, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.getTextViewTitle().setText(dataSet.get(i).getTitle());
        viewHolder.getTextViewDate().setText(dataSet.get(i).getDateString());
        viewHolder.getTextViewDuration().setText(dataSet.get(i).getDurationString());

        switch (dataSet.get(i).getType()) {
            case Constants.GROCERY_TYPE:
                viewHolder.getImageViewType().setImageResource(R.drawable.ic_add_request_grocery);
                break;

            case Constants.LAUNDRY_TYPE:
                viewHolder.getImageViewType().setImageResource(R.drawable.ic_add_request_laundry);
                break;

            case Constants.WALKING_TYPE:
                viewHolder.getImageViewType().setImageResource(R.drawable.ic_add_request_walking);
                break;

            case Constants.OTHER_TYPE:
                viewHolder.getImageViewType().setImageResource(R.drawable.ic_add_request_other);
                break;
        }

        if (isVolunteerView) {
            viewHolder.getTextViewDistance().setText(dataSet.get(i).getDistanceString());
        } else {
            viewHolder.getTextViewDistance().setVisibility(View.GONE);
        }

        if (dataSet.get(i).isNow()) {
            viewHolder.getImageViewTime().setImageTintList(this.accentColorStateList);
            viewHolder.getTextViewTime().setText(Html.fromHtml("<font color='#f48760'>Now</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            viewHolder.getTextViewTime().setText(dataSet.get(i).getTimeString());
        }

        if (isVolunteerView) {
            viewHolder.getTextViewUser().setText(dataSet.get(i).getPostedByName());
        } else {
            if (dataSet.get(i).getStatus() == Constants.REQUEST_STATUS_ACCEPTED) {
                viewHolder.getTextViewUser().setText(dataSet.get(i).getAcceptedByMainName());
            } else {
                viewHolder.getImageViewUser().setImageTintList(this.accentColorStateList);
                viewHolder.getTextViewUser().setText(Html.fromHtml("<font color='#f48760'>Waiting on volunteer</font>", Html.FROM_HTML_MODE_LEGACY));
            }
        }

        viewHolder.getConstraintLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Rewrite

                Intent intent = new Intent(viewHolder.getContext(), RequestDetailsActivity.class);
                intent.putExtra("isVolunteerView", isVolunteerView);
                intent.putExtra("requestData", dataSet.get(i));
                viewHolder.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
