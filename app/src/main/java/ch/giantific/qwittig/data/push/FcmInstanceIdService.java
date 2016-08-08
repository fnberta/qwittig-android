package ch.giantific.qwittig.data.push;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.push.di.DaggerFcmServiceComponent;
import ch.giantific.qwittig.data.repositories.UserRepository;
import timber.log.Timber;

/**
 * Created by fabio on 28.07.16.
 */
public class FcmInstanceIdService extends FirebaseInstanceIdService {

    @Inject
    UserRepository mUserRepo;

    @Override
    public void onCreate() {
        super.onCreate();

        injectDependencies();
    }

    private void injectDependencies() {
        DaggerFcmServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        final FirebaseUser firebaseUser = mUserRepo.getCurrentUser();
        if (!TextUtils.isEmpty(refreshedToken) && firebaseUser != null) {
            mUserRepo.updateToken(firebaseUser.getUid(), refreshedToken);
        }
    }
}
