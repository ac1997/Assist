package com.caltruism.assist.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class VolunteerTaskListFragment extends Fragment {

    private static final String TAG = "VolunteerTaskListFragment";

    private ListenerRegistration listenerRegistration;

    private VolunteerTaskListViewFragment volunteerTaskListUpcomingFragment;
    private VolunteerTaskListViewFragment volunteerTaskListPastFragment;

    private HashMap<String, Integer> assistRequestStatus = new HashMap<>();
    private ArrayList<AssistRequest> upcomingDataSet = new ArrayList<>();
    private ArrayList<AssistRequest> pastDataSet = new ArrayList<>();
    private HashSet<String> upcomingDataRemoved = new HashSet<>();
    private HashSet<String> pastDataRemoved = new HashSet<>();
    private ArrayList<DocumentSnapshot> upcomingDataModified = new ArrayList<>();
    private ArrayList<DocumentSnapshot> pastDataModified = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();

        volunteerTaskListUpcomingFragment = new VolunteerTaskListViewFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean("isUpcomingView", true);
        volunteerTaskListUpcomingFragment.setArguments(arguments);

        volunteerTaskListPastFragment = new VolunteerTaskListViewFragment();
        Bundle arguments1 = new Bundle();
        arguments1.putBoolean("isUpcomingView", false);
        volunteerTaskListPastFragment.setArguments(arguments1);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutVolunteerTaskList);
        ViewPager viewPager = view.findViewById(R.id.viewPagerVolunteerTaskList);
        VolunteerTaskListPageAdapter pageAdapter = new VolunteerTaskListPageAdapter(getFragmentManager());
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.getAdapter();

        queryData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private void setupToolbar() {
        final Toolbar toolbar = getView().findViewById(R.id.toolbarVolunteerTaskList);
        final TextView title = getView().findViewById(R.id.textViewVolunteerTaskListToolbarTitle);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        title.setText("Tasks");

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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", sharedPreferences.getString("name", null));
        userData.put("uid", FirebaseAuth.getInstance().getUid());

        CollectionReference colRef = FirebaseFirestore.getInstance().collection("requests");
        listenerRegistration = colRef.whereArrayContains("acceptedBy", userData)
                .whereGreaterThanOrEqualTo("status", Constants.REQUEST_STATUS_ACCEPTED)
                .whereLessThanOrEqualTo("status", Constants.REQUEST_STATUS_CANCELLED)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && e == null) {
                            for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (docChange.getType()) {
                                    case ADDED:
                                        onDataAdded(docChange.getDocument());
                                        break;
                                    case MODIFIED:
                                        onDataModified(docChange.getDocument());
                                        break;
                                    case REMOVED:
                                        onDataRemoved(docChange.getDocument());
                                        break;
                                }
                            }

                            if (upcomingDataSet.size() > 0)
                                volunteerTaskListUpcomingFragment.onDataAdded(upcomingDataSet);

                            if (pastDataSet.size() > 0)
                                volunteerTaskListPastFragment.onDataAdded(pastDataSet);

                            if (upcomingDataRemoved.size() > 0)
                                volunteerTaskListUpcomingFragment.onDataRemoved(upcomingDataRemoved);

                            if (pastDataRemoved.size() > 0)
                                volunteerTaskListPastFragment.onDataRemoved(pastDataRemoved);

                            if (upcomingDataModified.size() > 0)
                                volunteerTaskListUpcomingFragment.onDataModified(upcomingDataModified);

                            if (pastDataModified.size() > 0)
                                volunteerTaskListPastFragment.onDataModified(pastDataModified);
                        } else if (e != null) {
                            Log.e(TAG, "queryData Error: ", e);
                        }
                    }
                });
    }

    private void onDataAdded(DocumentSnapshot documentSnapshot) {
        Log.e(TAG, "Document entered id: " + documentSnapshot.getId());
        AssistRequest assistRequest = new AssistRequest(documentSnapshot);
        assistRequestStatus.put(documentSnapshot.getId(), assistRequest.getStatus());

        if (assistRequest.getStatus() == Constants.REQUEST_STATUS_ACCEPTED)
            upcomingDataSet.add(assistRequest);
        else
            pastDataSet.add(assistRequest);
    }

    private void onDataRemoved(DocumentSnapshot documentSnapshot) {
        Log.e(TAG, "Document removed id: " + documentSnapshot.getId());
        assistRequestStatus.remove(documentSnapshot.getId());
        int status = documentSnapshot.getLong("status").intValue();

        if (status == Constants.REQUEST_STATUS_ACCEPTED)
            upcomingDataRemoved.add(documentSnapshot.getId());
        else
            pastDataRemoved.add(documentSnapshot.getId());
    }

    private void onDataModified(DocumentSnapshot documentSnapshot) {
        Log.e(TAG, "Document modified id: " + documentSnapshot.getId());
        int newStatus = documentSnapshot.getLong("status").intValue();

        if (assistRequestStatus.get(documentSnapshot.getId()) == newStatus) {
            if (newStatus == Constants.REQUEST_STATUS_ACCEPTED)
                upcomingDataModified.add(documentSnapshot);
            else
                pastDataModified.add(documentSnapshot);
        } else {
            AssistRequest assistRequest = new AssistRequest(documentSnapshot);
            assistRequestStatus.put(documentSnapshot.getId(), newStatus);

            if (newStatus == Constants.REQUEST_STATUS_ACCEPTED) {
                upcomingDataSet.add(assistRequest);
                pastDataRemoved.add(documentSnapshot.getId());
            } else {
                pastDataSet.add(assistRequest);
                upcomingDataRemoved.add(documentSnapshot.getId());
            }
        }
    }


    public class VolunteerTaskListPageAdapter extends FragmentStatePagerAdapter {
        private static final String TAG = "VolunteerTaskListPageAdapter";

        public VolunteerTaskListPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return volunteerTaskListUpcomingFragment;
                case 1:
                    return volunteerTaskListPastFragment;
                default:
                    Log.e(TAG, "Invalid tab number: " + i);
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
