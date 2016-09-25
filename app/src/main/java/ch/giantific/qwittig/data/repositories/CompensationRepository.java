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

import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.queues.CompRemindQueue;
import ch.giantific.qwittig.data.queues.CompRemindQueue.RemindType;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.domain.models.Compensation;
import rx.Observable;
import rx.Single;

/**
 * Created by fabio on 07.07.16.
 */
public class CompensationRepository {

    private final DatabaseReference databaseRef;

    @Inject
    public CompensationRepository(@NonNull FirebaseDatabase database) {
        databaseRef = database.getReference();
    }

    public Observable<RxChildEvent<Compensation>> observeCompensationChildren(@NonNull final String groupId,
                                                                              @NonNull final String currentIdentityId,
                                                                              boolean getPaid) {
        final String pathPaid = getPaid ? Compensation.BASE_PATH_PAID : Compensation.BASE_PATH_UNPAID;
        final Query query = databaseRef.child(Compensation.BASE_PATH).child(pathPaid).orderByChild(Compensation.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Compensation.class)
                .filter(event -> {
                    final Compensation comp = event.getValue();
                    return Objects.equals(comp.getDebtor(), currentIdentityId)
                            || Objects.equals(comp.getCreditor(), currentIdentityId);
                });
    }

    public Observable<Compensation> getCompensations(@NonNull String groupId,
                                                     @NonNull final String currentIdentityId,
                                                     boolean getPaid) {
        final String pathPaid = getPaid ? Compensation.BASE_PATH_PAID : Compensation.BASE_PATH_UNPAID;
        final Query query = databaseRef.child(Compensation.BASE_PATH).child(pathPaid).orderByChild(Compensation.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValueListOnce(query, Compensation.class)
                .filter(compensation -> Objects.equals(compensation.getDebtor(), currentIdentityId)
                        || Objects.equals(compensation.getCreditor(), currentIdentityId));
    }

    public Single<Compensation> confirmAmountAndAccept(@NonNull final String compensationId,
                                                       @NonNull final BigFraction amount,
                                                       final boolean amountChanged) {
        final Query query = databaseRef.child(Compensation.BASE_PATH).child(Compensation.BASE_PATH_UNPAID).child(compensationId);
        return RxFirebaseDatabase.observeValueOnce(query, Compensation.class)
                .doOnSuccess(compensation -> {
                    final Map<String, Object> childUpdates = new HashMap<>();

                    childUpdates.put(Compensation.BASE_PATH + "/" + Compensation.BASE_PATH_UNPAID + "/" + compensationId, null);
                    final Map<String, Object> compMap = compensation.toMap();

                    final Map<String, Object> amountMap = new HashMap<>(2);
                    amountMap.put(Compensation.NUMERATOR, amount.getNumerator().intValue());
                    amountMap.put(Compensation.DENOMINATOR, amount.getDenominator().intValue());
                    compMap.put(Compensation.PATH_AMOUNT, amountMap);

                    compMap.put(Compensation.PATH_PAID, true);
                    compMap.put(Compensation.PATH_PAID_AT, ServerValue.TIMESTAMP);
                    compMap.put(Compensation.PATH_AMOUNT_CHANGED, amountChanged);

                    childUpdates.put(Compensation.BASE_PATH + "/" + Compensation.BASE_PATH_PAID + "/" + compensationId, compMap);
                    databaseRef.updateChildren(childUpdates);
                });

        // TODO: use transaction because server might change compensation at the same time due to recalculation
    }

    public void remindDebtor(@NonNull String compensationId) {
        final CompRemindQueue remind = new CompRemindQueue(compensationId, RemindType.REMIND_DEBTOR);
        databaseRef.child(Constants.PATH_PUSH_QUEUE).push().setValue(remind);
    }
}
