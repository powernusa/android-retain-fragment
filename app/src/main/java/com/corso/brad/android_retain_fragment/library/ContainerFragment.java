package com.corso.brad.android_retain_fragment.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

/**
 * Created by bradcorso on 6/27/15.
 */
public abstract class ContainerFragment<C> extends Fragment
{
    private String tag = getClass().getCanonicalName();
    private RetainFragment<C> retainFragment;
    protected boolean destroyedBySystem;

    public ContainerFragment() {}

    public C getData(){ return retainFragment.data; }

    public void setData(C data){ retainFragment.data = data; }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Find or Create a RetainFragment to hold the component
        retainFragment = RetainFragment.findOrCreate(getSupportFragmentManager(), tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        destroyedBySystem = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        destroyedBySystem = true;
    }

    @Override
    public void onDestroy() {
        if(destroyedBySystem) onDestroyBySystem(); else onDestroyByUser();
        super.onDestroy();
    }

    // Activity destroyed By User. Perform cleanup of retain fragment.
    public void onDestroyByUser(){
        retainFragment.remove(getSupportFragmentManager());
        retainFragment.data = null;
        retainFragment = null;
    }

    // Activity destroyed by System. Subclasses can override this if needed.
    public void onDestroyBySystem(){}

    public FragmentManager getSupportFragmentManager(){
        return getActivity().getSupportFragmentManager();
    }
}