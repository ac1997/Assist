package com.caltruism.assist.fragment;

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

import com.caltruism.assist.R;
import com.caltruism.assist.adapter.RequestAdapter;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;

public class DisabledRequestListAcceptedFragment extends Fragment implements CustomCallbackListener.DisabledRequestListChildFragmentCallbackListener {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private Group groupEmpty;

    private ArrayList<AssistRequest> acceptedDataSet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disabled_request_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RequestAdapter(getActivity(), acceptedDataSet, false);

        recyclerView = view.findViewById(R.id.recyclerViewDisabledRequestList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        groupEmpty = view.findViewById(R.id.groupDisabledRequestListEmpty);
    }

    @Override
    public void onNewDataSet(ArrayList<AssistRequest> dataSet) {
        if (groupEmpty.getVisibility() == View.VISIBLE)
            groupEmpty.setVisibility(View.GONE);

        acceptedDataSet.clear();
        acceptedDataSet.addAll(dataSet);
        adapter.notifyDataSetChanged();

        dataSet.clear();
    }

    @Override
    public void onDataAdded(ArrayList<AssistRequest> dataSet) {
        if (groupEmpty.getVisibility() == View.VISIBLE)
            groupEmpty.setVisibility(View.GONE);

        for (AssistRequest assistRequest : dataSet)
            adapter.notifyItemInserted(AssistRequest.insertInOrder(acceptedDataSet, assistRequest));

        dataSet.clear();
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
            if (groupEmpty.getVisibility() != View.VISIBLE)
                groupEmpty.setVisibility(View.VISIBLE);
        }

        removedId.clear();
    }

    @Override
    public void onDataModified(ArrayList<DocumentSnapshot> dataSet) {
        int index;
        for (DocumentSnapshot documentSnapshot : dataSet) {
            index = acceptedDataSet.indexOf(new AssistRequest(documentSnapshot.getId()));
            acceptedDataSet.get(index).modifiedData(documentSnapshot);
            adapter.notifyItemChanged(index);
        }

        dataSet.clear();
    }
}
