package com.caltruism.assist.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.adapter.RequestAdapter;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class VolunteerRequestListViewFragment extends Fragment implements CustomCallbackListener.VolunteerRequestListChildFragmentCallbackListener {

    private RequestAdapter adapter;
    private Group groupEmpty;

    private Location currentLocation;
    private HashMap<String, AssistRequest> assistRequests = new HashMap<>();
    private ArrayList<AssistRequest> dataSet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_request_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RequestAdapter(getActivity(), dataSet, true);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewVolunteerRequestList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        groupEmpty = view.findViewById(R.id.groupVolunteerRequestListEmpty);
    }


    @Override
    public void onNewLocations(Location newCurrentLocation, LatLng newCameraLocation, boolean isUsingCurrentLocation, boolean isNewSearch) {
        if (newCurrentLocation != null)
            currentLocation = newCurrentLocation;
    }

    @Override
    public void onNewDataSet(HashMap<String, AssistRequest> data) {
        TextView textView = getView().findViewById(R.id.textViewVolunteerRequestListEmpty);
        textView.setText("No posted requests");

        if (data != null) {
            if (groupEmpty.getVisibility() == View.VISIBLE)
                groupEmpty.setVisibility(View.GONE);

            assistRequests.clear();
            assistRequests.putAll(data);

            dataSet.clear();
            dataSet.addAll(data.values());
            Collections.sort(dataSet);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataAdded(DocumentSnapshot documentSnapshot) {
        if (groupEmpty.getVisibility() == View.VISIBLE)
            groupEmpty.setVisibility(View.GONE);

        AssistRequest request = new AssistRequest(documentSnapshot, currentLocation);
        assistRequests.put(documentSnapshot.getId(), request);
        adapter.notifyItemInserted(AssistRequest.insertInOrder(dataSet, new AssistRequest(documentSnapshot)));
    }

    @Override
    public void onDataRemoved(String documentId) {
        int removeIndex = dataSet.indexOf(assistRequests.get(documentId));
        assistRequests.remove(documentId);
        dataSet.remove(removeIndex);
        adapter.notifyItemRemoved(removeIndex);

        if (dataSet.size() == 0) {
            if (groupEmpty.getVisibility() != View.VISIBLE)
                groupEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDataModified(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).modifiedData(documentSnapshot, currentLocation);
        adapter.notifyItemChanged(dataSet.indexOf(assistRequests.get(documentSnapshot.getId())));
    }
}
