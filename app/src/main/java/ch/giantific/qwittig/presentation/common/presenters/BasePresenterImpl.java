package ch.giantific.qwittig.presentation.common.presenters;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by fabio on 28.09.16.
 */
public abstract class BasePresenterImpl<T extends BaseViewListener> implements BasePresenter<T> {

    protected final UserRepository userRepo;
    protected final Navigator navigator;
    protected T view;
    protected CompositeSubscription subscriptions = new CompositeSubscription();

    public BasePresenterImpl(@NonNull Navigator navigator,
                             @NonNull UserRepository userRepo) {
        this.navigator = navigator;
        this.userRepo = userRepo;
    }

    @Override
    public void attachView(@NonNull T view) {
        this.view = view;
    }

    @Override
    public final void onViewVisible() {
        subscriptions.add(userRepo.observeAuthStatus()
                .subscribe(new IndefiniteSubscriber<FirebaseUser>() {
                    @Override
                    public void onNext(FirebaseUser currentUser) {
                        if (currentUser != null) {
                            onUserLoggedIn(currentUser);
                        } else {
                            onUserNotLoggedIn();
                        }
                    }
                })
        );
    }

    @CallSuper
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        // empty default implementation
    }

    @CallSuper
    protected void onUserNotLoggedIn() {
        // empty default implementation
    }

    @Override
    public void onViewGone() {
        if (subscriptions.hasSubscriptions()) {
            subscriptions.clear();
        }
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        view.removeWorker(workerTag);
        view.showMessage(R.string.toast_error_unknown);
    }
}
