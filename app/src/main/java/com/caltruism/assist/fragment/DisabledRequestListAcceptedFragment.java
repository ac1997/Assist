package com.caltruism.assist.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caltruism.assist.R;
import com.caltruism.assist.adapter.RequestAdapter;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class DisabledRequestListAcceptedFragment extends Fragment implements CustomCallbackListener.DisabledRequestListChildFragmentCallbackListener {

    private CustomCallbackListener.DisabledMainActivityCallbackListener callbackListener;

    private RecyclerView recyclerView;
    private RequestAdapter adapter;

    private Location currentLocation;
    private ArrayList<AssistRequest> acceptedDataSet = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CustomCallbackListener.DisabledMainActivityCallbackListener) {
            callbackListener = (CustomCallbackListener.DisabledMainActivityCallbackListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CustomCallbackListener.DisabledMainActivityCallbackListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ArrayList<AssistRequest> data = (ArrayList<AssistRequest>) getArguments().getSerializable("acceptedDataSet");
        acceptedDataSet.addAll(data);

        return inflater.inflate(R.layout.fragment_disabled_request_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RequestAdapter(getActivity(), acceptedDataSet, false);

        recyclerView = view.findViewById(R.id.recyclerViewDisabledRequestList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onNewDataSet(ArrayList<AssistRequest> dataSet) {
        acceptedDataSet.clear();
        acceptedDataSet.addAll(dataSet);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDataAdded(ArrayList<AssistRequest> dataSet) {
        for (AssistRequest assistRequest : dataSet)
            adapter.notifyItemInserted(AssistRequest.insertInOrder(acceptedDataSet, assistRequest));
    }

    @Override
    public void onDataRemoved(HashSet<String> removedId) {
        for (String id : removedId) {
            int removeIndex = acceptedDataSet.indexOf(new AssistRequest(id));
            acceptedDataSet.remove(removeIndex);
            adapter.notifyItemRemoved(removeIndex);
        }

        if (acceptedDataSet.size() == 0) {
            removedId.clear();
            callbackListener.onDisabledRequestListChildFragmentDataSetEmpty(false);
        }
    }

    @Override
    public void onDataModified(ArrayList<DocumentSnapshot> dataSet) {
        int index;
        for (DocumentSnapshot documentSnapshot : dataSet) {
            index = acceptedDataSet.indexOf(new AssistRequest(documentSnapshot.getId()));
            acceptedDataSet.get(index).modifiedData(documentSnapshot);
            adapter.notifyItemChanged(index);
        }
    }
}
