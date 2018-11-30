package com.caltruism.assist.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.adapter.RequestAdapter;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DisabledRequestHistoryFragment extends Fragment {

    private final static String TAG = "DisabledRequestHistoryFragment";

    private Group groupEmpty;

    private RequestAdapter adapter;
    private ListenerRegistration listenerRegistration;

    private ArrayList<AssistRequest> dataSet = new ArrayList<>();
    private boolean isInitialQueryCompleted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disabled_request_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();

        groupEmpty = view.findViewById(R.id.groupDisabledRequestHistoryEmpty);

        adapter = new RequestAdapter(getActivity(), dataSet, true);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewDisabledRequestHistory);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        queryData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private void setupToolbar() {
        final Toolbar toolbar = getView().findViewById(R.id.toolbarDisabledRequestHistory);
        final TextView title = getView().findViewById(R.id.textViewDisabledRequestHistoryToolbarTitle);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        title.setText("Request History");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_solid);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.postDelayed(new Runnable()
        {
            @Override
            public void run ()
            {
                int maxWidth = toolbar.getWidth();
                int titleWidth = title.getWidth();
                int iconWidth = maxWidth - titleWidth;

                if (iconWidth > 0) {
                    int width = maxWidth - iconWidth * 2;
                    title.setMinimumWidth(width);
                    title.getLayoutParams().width = width;
                }
            }
        }, 0);
    }

    private void queryData() {
        CollectionReference colRef = FirebaseFirestore.getInstance().collection("requests");
        listenerRegistration = colRef.whereEqualTo("postedBy.uid", FirebaseAuth.getInstance().getUid())
                .whereGreaterThanOrEqualTo("status", Constants.REQUEST_STATUS_COMPLETED)
                .whereLessThanOrEqualTo("status", Constants.REQUEST_STATUS_EXPIRED)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && e == null) {
                            if (!isInitialQueryCompleted) {
                                isInitialQueryCompleted = true;
                                if (queryDocumentSnapshots.size() > 0)
                                    onNewDataSet(queryDocumentSnapshots);
                            } else {
                                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    switch (docChange.getType()) {
                                        case ADDED:
                                            onDataAdded(docChange.getDocument());
                                            break;
                                    }
                                }
                            }
                        } else if (e != null) {
                            Log.e(TAG, "queryData Error: ", e);
                        }
                    }
                });
    }

    private void onNewDataSet(QuerySnapshot queryDocumentSnapshots) {
        Log.e(TAG, "on new dataset");
        if (queryDocumentSnapshots.size() == 0) {
            groupEmpty.setVisibility(View.VISIBLE);
        } else {
            groupEmpty.setVisibility(View.GONE);

            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                dataSet.add(new AssistRequest(documentSnapshot));

            Collections.sort(dataSet);
            adapter.notifyDataSetChanged();
        }
    }

    private void onDataAdded(DocumentSnapshot documentSnapshot) {
        if (groupEmpty.getVisibility() == View.VISIBLE)
            groupEmpty.setVisibility(View.GONE);

        Log.e(TAG, "Document entered id: " + documentSnapshot.getId());
        adapter.notifyItemInserted(AssistRequest.insertInOrder(dataSet, new AssistRequest(documentSnapshot)));
    }
}
