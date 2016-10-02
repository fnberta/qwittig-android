package ch.giantific.qwittig.utils.rxwrapper.android.visibility;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 02.10.16.
 */
public class FabVisibilityOnSubscribe implements Single.OnSubscribe<FloatingActionButton> {

    private FloatingActionButton fab;

    public FabVisibilityOnSubscribe(@NonNull FloatingActionButton fab) {
        this.fab = fab;
    }

    @Override
    public void call(SingleSubscriber<? super FloatingActionButton> singleSubscriber) {
        fab.show(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onShown(FloatingActionButton fab) {
                super.onShown(fab);

                singleSubscriber.onSuccess(fab);
            }

            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);

                singleSubscriber.onSuccess(fab);
            }
        });
    }
}
