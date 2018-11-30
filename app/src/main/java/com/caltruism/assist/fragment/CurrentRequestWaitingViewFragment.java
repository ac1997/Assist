package com.caltruism.assist.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.util.CustomCallbackListener;

public class CurrentRequestWaitingViewFragment extends Fragment implements CustomCallbackListener.CurrentRequestWaitingViewFragmentCallbackListener {

    private ImageView imageView;
    private TextView textViewMainText;
    private TextView textViewSubText;
    private TextView textViewFootNote;

    private boolean isOnlineView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_request_waiting_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.imageViewCurrentRequestWaiting);
        textViewMainText = view.findViewById(R.id.textViewCurrentRequestWaitingMainText);
        textViewSubText = view.findViewById(R.id.textViewCurrentRequestWaitingSubtext);
        textViewFootNote = view.findViewById(R.id.textViewCurrentRequestWaitingFootnote);

//        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(imageView);

        setOfflineView();
    }

    @Override
    public void onNewDuration(String durationInMins) {
        if (!isOnlineView)
            setOnlineView();

        textViewMainText.setText(durationInMins);
    }

    @Override
    public void onVolunteerOffline() {
        setOfflineView();
    }

    private void setOnlineView() {
        isOnlineView = true;

        Glide.with(getActivity()).setDefaultRequestOptions(new RequestOptions().centerCrop())
                .load(R.drawable.walking).into(imageView);
        textViewMainText.setVisibility(View.VISIBLE);
        textViewSubText.setText("MIN AWAY");
        textViewFootNote.setText("Switching to map view when volunteer shares their location");
    }

    private void setOfflineView() {
        isOnlineView = false;

        Glide.with(getActivity()).setDefaultRequestOptions(new RequestOptions().centerCrop())
                .load(R.drawable.walking_static).into(imageView);
        // TODO: Rephrase
        textViewMainText.setVisibility(View.INVISIBLE);
        textViewSubText.setText("Volunteer is currently offline.");
        textViewFootNote.setText("We will show the minute countdown once the volunteer is online");
    }
}
