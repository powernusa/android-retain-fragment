package com.corso.brad.android_retain_fragment.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corso.brad.android_retain_fragment.R;
import com.corso.brad.android_retain_fragment.library.ContainerFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends ContainerFragment<MainFragmentPresenter> implements MVP.View
{
    @InjectView(R.id.progressLayout) RelativeLayout progress;
    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.progressText) TextView progressText;
    @InjectView(R.id.button) Button button;
    private MainFragmentPresenter presenter;


    @Override public void setButtonEnabled(boolean isEnabled) {
        button.setEnabled(isEnabled);
    }

    @Override public void setProgress(Integer x){
        progressBar.setProgress(x);
        progressText.setText(x.toString());
    }

    @Override public void setProgressVisibile(boolean isVisible) {
        progress.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick(R.id.button) public void startButtonClicked(){
        presenter.onButtonCLicked();
    }

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(this.getData() == null){
            // Only create if it does not exist already
            this.setData(new MainFragmentPresenter());
        }
        presenter = this.getData();
        presenter.bindView(this);
    }

    @Override
    public void onDestroyView() {
        presenter.unbindView();
        super.onDestroyView();
    }

    @Override
    public void onDestroyByUser() {
        // Only destroy if destroyed by user
        presenter.onDestroy();
        super.onDestroyByUser();
    }
}