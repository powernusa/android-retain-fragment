package com.corso.brad.android_retain_fragment.app;

/**
 * Created by bradcorso on 7/6/15.
 */
public interface MVP {
    interface View {
        void setProgress(Integer x);
        void setProgressVisibile(boolean isVisible);
        void setButtonEnabled(boolean isEnabled);
    }

    interface Presenter {
        void bindView(MVP.View view);
        void unbindView();
        void onDestroy();
        void onButtonCLicked();
    }
}
