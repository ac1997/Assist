package com.caltruism.assist.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.activity.AddRequestActivity;
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

public class DisabledRequestListFragment extends Fragment {
    private static final String TAG = "DisabledRequestListFragment";
    private static final int REQUEST_CODE_ADD_REQUEST = 0;

    private ListenerRegistration listenerRegistration;
    private boolean isViewCreated = false;

    private DisabledRequestListViewFragment disabledRequestListWaitingFragment;
    private DisabledRequestListViewFragment disabledRequestListAcceptedFragment;

    private HashMap<String, Integer> assistRequestStatus = new HashMap<>();
    private ArrayList<AssistRequest> waitingDataSet = new ArrayList<>();
    private ArrayList<AssistRequest> acceptedDataSet = new ArrayList<>();
    private HashSet<String> waitingDataRemoved = new HashSet<>();
    private HashSet<String> acceptedDataRemoved = new HashSet<>();
    private ArrayList<DocumentSnapshot> waitingDataModified = new ArrayList<>();
    private ArrayList<DocumentSnapshot> acceptedDataModified = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disabled_request_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.fabAddRequest);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), AddRequestActivity.class), REQUEST_CODE_ADD_REQUEST);
            }
        });

        setupToolbar();

        disabledRequestListWaitingFragment = new DisabledRequestListViewFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean("isWaitingView", true);
        disabledRequestListWaitingFragment.setArguments(arguments);

        disabledRequestListAcceptedFragment = new DisabledRequestListViewFragment();
        Bundle arguments1 = new Bundle();
        arguments1.putBoolean("isWaitingView", false);
        disabledRequestListAcceptedFragment.setArguments(arguments1);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutDisabledRequestList);
        ViewPager viewPager = view.findViewById(R.id.viewPagerDisabledRequestList);
        DisabledRequestListPageAdapter pageAdapter = new DisabledRequestListPageAdapter(getFragmentManager());
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.getAdapter();

        if (!isViewCreated) {
            queryData();
            isViewCreated = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_REQUEST && resultCode == Activity.RESULT_OK) {
            // TODO: Rephrase
            new AlertDialog.Builder(getActivity()).setTitle("Request Posted")
                    .setMessage("You will be notified when someone accepts your request.")
                    .setPositiveButton("OK", null).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private void setupToolbar() {
        final Toolbar toolbar = getView().findViewById(R.id.toolbarDisabledRequestList);
        final TextView title = getView().findViewById(R.id.textViewDisabledRequestListToolbarTitle);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        title.setText("Current Requests");

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
                .whereLessThanOrEqualTo("status", Constants.REQUEST_STATUS_ACCEPTED)
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

                    if (waitingDataSet.size() > 0)
                        disabledRequestListWaitingFragment.onDataAdded(waitingDataSet);

                    if (acceptedDataSet.size() > 0)
                        disabledRequestListAcceptedFragment.onDataAdded(acceptedDataSet);

                    if (waitingDataRemoved.size() > 0)
                        disabledRequestListWaitingFragment.onDataRemoved(waitingDataRemoved);

                    if (acceptedDataRemoved.size() > 0)
                        disabledRequestListAcceptedFragment.onDataRemoved(acceptedDataRemoved);

                    if (waitingDataModified.size() > 0)
                        disabledRequestListWaitingFragment.onDataModified(waitingDataModified);

                    if (acceptedDataModified.size() > 0)
                        disabledRequestListAcceptedFragment.onDataModified(acceptedDataModified);
                } else if (e != null) {
                    Log.e(TAG, "queryData Error: ", e);
                }
            }
        });
    }

    private void onDataAdded(DocumentSnapshot documentSnapshot) {
        AssistRequest assistRequest = new AssistRequest(documentSnapshot);
        assistRequestStatus.put(documentSnapshot.getId(), assistRequest.getStatus());

        if (assistRequest.getStatus() == Constants.REQUEST_STATUS_WAITING)
            waitingDataSet.add(assistRequest);
        else if (assistRequest.getStatus() == Constants.REQUEST_STATUS_ACCEPTED)
            acceptedDataSet.add(assistRequest);
        else
            Log.e(TAG, "notifyDataAdded error: Unexpected status " + assistRequest.getStatus());
    }

    private void onDataRemoved(DocumentSnapshot documentSnapshot) {
        assistRequestStatus.remove(documentSnapshot.getId());
        int status = documentSnapshot.getLong("status").intValue();

        if (status == Constants.REQUEST_STATUS_WAITING)
            waitingDataRemoved.add(documentSnapshot.getId());
        else if (status == Constants.REQUEST_STATUS_ACCEPTED)
            acceptedDataRemoved.add(documentSnapshot.getId());
        else
            Log.e(TAG, "onDataRemoved error: Unexpected status " + status);
    }

    private void onDataModified(DocumentSnapshot documentSnapshot) {
        int newStatus = documentSnapshot.getLong("status").intValue();

        if (assistRequestStatus.get(documentSnapshot.getId()) == newStatus) {
            if (newStatus == Constants.REQUEST_STATUS_WAITING)
                waitingDataModified.add(documentSnapshot);
            else if (newStatus == Constants.REQUEST_STATUS_ACCEPTED)
                acceptedDataModified.add(documentSnapshot);
            else
                Log.e(TAG, "onDataModified error: Unexpected status " + newStatus);
        } else {
            AssistRequest assistRequest = new AssistRequest(documentSnapshot);
            assistRequestStatus.put(documentSnapshot.getId(), newStatus);

            if (newStatus == Constants.REQUEST_STATUS_WAITING) {
                waitingDataSet.add(assistRequest);
                acceptedDataRemoved.add(documentSnapshot.getId());
            } else if (newStatus == Constants.REQUEST_STATUS_ACCEPTED) {
                acceptedDataSet.add(assistRequest);
                waitingDataRemoved.add(documentSnapshot.getId());
            } else {
                Log.e(TAG, "onDataModified (moved) error: Unexpected status " + newStatus);
            }
        }
    }

    public class DisabledRequestListPageAdapter extends FragmentStatePagerAdapter {
        private static final String TAG = "DisabledRequestListPageAdapter";

        public DisabledRequestListPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return disabledRequestListWaitingFragment;
                case 1:
                    return disabledRequestListAcceptedFragment;
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
