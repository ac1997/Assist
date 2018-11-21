package com.caltruism.assist.util;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.arlib.floatingsearchview.FloatingSearchView;

public class VolunteerBaseFragment extends Fragment {

    private VolunteerBaseFragmentCallback callback;

    public interface VolunteerBaseFragmentCallback {
        void onAttachSearchViewToDrawer(FloatingSearchView searchView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof VolunteerBaseFragmentCallback)
            callback = (VolunteerBaseFragmentCallback) context;
        else
            throw new RuntimeException(context.toString() + " must implement BaseExampleFragmentCallbacks");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    protected void attachSearchViewActivityDrawer(FloatingSearchView searchView){
        if(callback != null){
            callback.onAttachSearchViewToDrawer(searchView);
        }
    }

//    public abstract boolean onActivityBackPress();
}
