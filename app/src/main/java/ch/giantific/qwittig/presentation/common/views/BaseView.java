package ch.giantific.qwittig.presentation.common.views;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.MessageAction;

/**
 * Created by fabio on 28.09.16.
 */

public interface BaseView {

    boolean isNetworkAvailable();

    void showMessage(@StringRes int resId);

    void showMessage(@StringRes int resId, @NonNull Object... args);

    void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action);

    void removeWorker(@NonNull String workerTag);
}
