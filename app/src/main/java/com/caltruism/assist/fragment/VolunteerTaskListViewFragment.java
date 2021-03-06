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
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.adapter.RequestAdapter;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;

public class VolunteerTaskListViewFragment extends Fragment implements CustomCallbackListener.ListViewChildFragmentCallbackListener {
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private Group groupEmpty;

    private ArrayList<AssistRequest> dataSet = new ArrayList<>();
    private boolean isUpcomingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isUpcomingView = getArguments().getBoolean("isUpcomingView");

        return inflater.inflate(R.layout.fragment_volunteer_task_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RequestAdapter(getActivity(), dataSet, true, true);

        recyclerView = view.findViewById(R.id.recyclerViewVolunteerTaskList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        TextView textViewEmptyMainText = view.findViewById(R.id.textViewVolunteerTaskHistoryEmpty);
        if (isUpcomingView) {
            textViewEmptyMainText.setText("No upcoming tasks");
        } else {
            textViewEmptyMainText.setText("No previous tasks");
        }

        groupEmpty = view.findViewById(R.id.groupVolunteerTaskHistoryEmpty);
    }

    @Override
    public void onDataAdded(ArrayList<AssistRequest> addedDataSet) {
        if (groupEmpty.getVisibility() == View.VISIBLE)
            groupEmpty.setVisibility(View.GONE);

        for (AssistRequest assistRequest : addedDataSet)
            adapter.notifyItemInserted(AssistRequest.insertInOrder(dataSet, assistRequest, true));

        addedDataSet.clear();
    }

    @Override
    public void onDataRemoved(HashSet<String> removedId) {
        for (String id : removedId) {
            int removeIndex = dataSet.indexOf(new AssistRequest(id));
            dataSet.remove(removeIndex);
            adapter.notifyItemRemoved(removeIndex);
        }

        if (dataSet.size() == 0) {
            removedId.clear();
            if (groupEmpty.getVisibility() != View.VISIBLE)
                groupEmpty.setVisibility(View.VISIBLE);
        }

        removedId.clear();
    }

    @Override
    public void onDataModified(ArrayList<DocumentSnapshot> modifiedDataSet) {
        int index;
        for (DocumentSnapshot documentSnapshot : modifiedDataSet) {
            index = dataSet.indexOf(new AssistRequest(documentSnapshot.getId()));
            dataSet.get(index).modifiedData(documentSnapshot);
            adapter.notifyItemChanged(index);
        }

        modifiedDataSet.clear();
    }
}
