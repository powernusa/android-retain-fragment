package com.corso.brad.android_retain_fragment.library;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by bradcorso on 6/30/15.
 */
public class RetainFragment<T> extends Fragment {
    public T data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static <T> RetainFragment<T> findOrCreate(FragmentManager fm, String tag) {
        RetainFragment<T> retainFragment = (RetainFragment<T>) fm.findFragmentByTag(tag);

        if(retainFragment == null){
            retainFragment = new RetainFragment<>();
            fm.beginTransaction()
                    .add(retainFragment, tag)
                    .commitAllowingStateLoss();
        }

        return retainFragment;
    }

    public void remove(FragmentManager fm) {
        if(!fm.isDestroyed()){
            fm.beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
            data = null;
        }
    }
}