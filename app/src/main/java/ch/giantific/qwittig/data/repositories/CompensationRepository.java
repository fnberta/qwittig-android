package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.data.queues.CompensationRemind;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.domain.models.Compensation;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by fabio on 07.07.16.
 */
public class CompensationRepository {

    public static final String PATH_REMIND_QUEUE = "queue/remind/compensation/tasks";
    private final DatabaseReference mDatabaseRef;

    @Inject
    public CompensationRepository(@NonNull FirebaseDatabase database) {
        mDatabaseRef = database.getReference();
    }

    public Observable<RxChildEvent<Compensation>> observeCompensationChildren(@NonNull final String groupId,
                                                                              @NonNull final String currentIdentityId,
                                                                              boolean getPaid) {
        final String pathPaid = getPaid ? Compensation.PAID : Compensation.UNPAID;
        final Query query = mDatabaseRef.child(Compensation.PATH).child(pathPaid).orderByChild(Compensation.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Compensation.class)
                .filter(new Func1<RxChildEvent<Compensation>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Compensation> event) {
                        final Compensation comp = event.getValue();
                        return Objects.equals(comp.getDebtor(), currentIdentityId)
                                || Objects.equals(comp.getCreditor(), currentIdentityId);
                    }
                });
    }

    public Observable<Compensation> getCompensations(@NonNull String groupId,
                                                     @NonNull final String currentIdentityId,
                                                     boolean getPaid) {
        final String pathPaid = getPaid ? Compensation.PAID : Compensation.UNPAID;
        final Query query = mDatabaseRef.child(Compensation.PATH).child(pathPaid).orderByChild(Compensation.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValuesOnce(query, Compensation.class)
                .filter(new Func1<Compensation, Boolean>() {
                    @Override
                    public Boolean call(Compensation compensation) {
                        return Objects.equals(compensation.getDebtor(), currentIdentityId)
                                || Objects.equals(compensation.getCreditor(), currentIdentityId);
                    }
                });
    }

    public Single<Compensation> confirmAmountAndAccept(@NonNull final String compensationId,
                                                       @NonNull final BigFraction amount,
                                                       final boolean amountChanged) {
        final Query query = mDatabaseRef.child(Compensation.PATH).child(Compensation.UNPAID).child(compensationId);
        return RxFirebaseDatabase.observeValueOnce(query, Compensation.class)
                .doOnSuccess(new Action1<Compensation>() {
                    @Override
                    public void call(Compensation compensation) {
                        final Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Compensation.PATH + "/" + Compensation.UNPAID + "/" + compensationId, null);
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_CREATED_AT, ServerValue.TIMESTAMP);
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_GROUP, compensation.getGroup());
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_DEBTOR, compensation.getDebtor());
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_CREDITOR, compensation.getCreditor());
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_PAID, true);
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_PAID_AT, ServerValue.TIMESTAMP);
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_AMOUNT + "/" + Compensation.NUMERATOR, amount.getNumerator().intValue());
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_AMOUNT + "/" + Compensation.DENOMINATOR, amount.getDenominator().intValue());
                        childUpdates.put(Compensation.PATH + "/" + Compensation.PAID + "/" + compensationId + "/" + Compensation.PATH_AMOUNT_CHANGED, amountChanged);
                        mDatabaseRef.updateChildren(childUpdates);
                    }
                });

        // TODO: use transaction because server might change compensation at the same time due to recalculation
    }

    public void remindDebtor(@NonNull String compensationId) {
        final CompensationRemind remind = new CompensationRemind(compensationId);
        mDatabaseRef.child(PATH_REMIND_QUEUE).push().setValue(remind);
    }
}
