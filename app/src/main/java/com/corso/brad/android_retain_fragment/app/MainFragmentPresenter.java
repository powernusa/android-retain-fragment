package com.corso.brad.android_retain_fragment.app;

import android.util.Log;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bradcorso on 7/6/15.
 */
public class MainFragmentPresenter implements MVP.Presenter{
    private MVP.View view;
    private int totalProgress = 100;
    private Long deltaProgress = 100L;
    private TimeUnit deltaUnit = TimeUnit.MILLISECONDS;
    public Subscription subscription;

    @Override public void bindView(MVP.View view) {
        this.view = view;
        if(subscription != null && !subscription.isUnsubscribed()){
            showProgress(true);
        }
    }

    @Override public void unbindView() {
        this.view = null;
    }

    @Override public void onDestroy() {
        if(subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    @Override public void onButtonCLicked() {
        subscription = Observable.interval(deltaProgress, deltaUnit).take(totalProgress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createSubscriber());
    }

    public void showProgress(boolean show){
        if(view != null) {
            view.setProgressVisibile(show);
            view.setButtonEnabled(!show);
        }
    }

    public void setProgress(int progress){
        if(view != null) {
            view.setProgress(progress);
        }
        Log.v(MainFragmentPresenter.class.getCanonicalName(), "progress: " + progress);
    }

    public Subscriber<Long> createSubscriber(){
        return new Subscriber<Long>() {
            int lastProgress;

            @Override public void onStart() {
                super.onStart();
                setProgress(0);
                showProgress(true);
            }

            @Override public void onNext(Long x) {
                lastProgress = x.intValue() + 1;
                setProgress(lastProgress);
            }

            @Override public void onCompleted() {
                setProgress(100);
                showProgress(false);
            }

            @Override public void onError(Throwable e) {}
        };
    }
}