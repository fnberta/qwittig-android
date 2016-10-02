package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.queues.AssignmentRemindQueue;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.domain.models.Assignment;
import ch.giantific.qwittig.domain.models.Assignment.TimeFrame;
import ch.giantific.qwittig.domain.models.AssignmentHistory;
import ch.giantific.qwittig.utils.DateUtils;
import rx.Observable;
import rx.Single;

/**
 * Created by fabio on 12.07.16.
 */
public class AssignmentRepository {

    private final DatabaseReference databaseRef;

    @Inject
    public AssignmentRepository(@NonNull FirebaseDatabase firebaseDatabase) {
        databaseRef = firebaseDatabase.getReference();
    }

    public Observable<RxChildEvent<Assignment>> observeAssignmentChildren(@NonNull String groupId,
                                                                          @NonNull final String currentIdentityId,
                                                                          @NonNull final Date deadline) {
        final Query query = databaseRef.child(Assignment.BASE_PATH).orderByChild(Assignment.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Assignment.class)
                .filter(childEvent -> {
                    final Assignment assignment = childEvent.getValue();
                    return assignment.getIdentityIds().contains(currentIdentityId) &&
                            assignment.getDeadline() <= deadline.getTime();
                });
    }

    public Observable<Assignment> getAssignments(@NonNull String groupId,
                                                 @NonNull final String currentIdentityId,
                                                 @NonNull final Date deadline) {
        final Query query = databaseRef.child(Assignment.BASE_PATH).orderByChild(Assignment.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValueListOnce(query, Assignment.class)
                .filter(assignment -> assignment.getIdentityIds().contains(currentIdentityId) &&
                        assignment.getDeadline() <= deadline.getTime());
    }

    public Observable<Assignment> observeAssignment(@NonNull String assignmentId) {
        final Query query = databaseRef.child(Assignment.BASE_PATH).child(assignmentId);
        return RxFirebaseDatabase.observeValue(query, Assignment.class);
    }

    public Single<Assignment> getAssignment(@NonNull String assignmentId) {
        final Query query = databaseRef.child(Assignment.BASE_PATH).child(assignmentId);
        return RxFirebaseDatabase.observeValueOnce(query, Assignment.class);
    }

    public void saveAssignment(@NonNull Assignment assignment, @Nullable String assignmentId) {
        final String key = TextUtils.isEmpty(assignmentId)
                ? databaseRef.child(Assignment.BASE_PATH).push().getKey()
                : assignmentId;
        databaseRef.child(Assignment.BASE_PATH).child(key).setValue(assignment);
    }

    public void deleteAssignment(@NonNull String assignmentId) {
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("%s/%s", Assignment.BASE_PATH, assignmentId), null);
        childUpdates.put(String.format("%s/%s", AssignmentHistory.BASE_PATH, assignmentId), null);
        databaseRef.updateChildren(childUpdates);
    }

    public void addHistory(@NonNull AssignmentHistory assignmentHistory,
                           @NonNull String[] identitiesSorted,
                           @NonNull String timeFrame) {
        final Map<String, Object> childUpdates = new HashMap<>();

        // add history event
        final String assignmentId = assignmentHistory.getAssignment();
        final String key = databaseRef.child(AssignmentHistory.BASE_PATH).child(assignmentId).push().getKey();
        childUpdates.put(String.format("%s/%s/%s", AssignmentHistory.BASE_PATH, assignmentId, key), assignmentHistory.toMap());

        // update identities position
        final List<String> identityIds = Arrays.asList(identitiesSorted);
        Collections.rotate(identityIds, -1);
        final Map<String, Integer> identities = new HashMap<>();
        for (int i = 0, size = identityIds.size(); i < size; i++) {
            identities.put(identityIds.get(i), i);
        }
        childUpdates.put(String.format("%s/%s/%s", Assignment.BASE_PATH, assignmentId, Assignment.PATH_IDENTITIES), identities);

        // update deadline
        if (!Objects.equals(timeFrame, TimeFrame.AS_NEEDED)) {
            final Calendar deadline = DateUtils.getCalendarInstanceUTC();
            deadline.setTime(new Date());
            switch (timeFrame) {
                case TimeFrame.DAILY:
                    deadline.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case TimeFrame.WEEKLY:
                    deadline.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case TimeFrame.MONTHLY:
                    deadline.add(Calendar.MONTH, 1);
                    break;
                case TimeFrame.YEARLY:
                    deadline.add(Calendar.YEAR, 1);
                    break;
            }

            DateUtils.resetToMidnight(deadline);
            childUpdates.put(String.format("%s/%s/%s", Assignment.BASE_PATH, assignmentId, Assignment.PATH_DEADLINE), deadline.getTimeInMillis());
        }

        databaseRef.updateChildren(childUpdates);
    }

    public Observable<AssignmentHistory> getAssignmentHistory(@NonNull String assignmentId) {
        final Query query = databaseRef.child(AssignmentHistory.BASE_PATH).child(assignmentId);
        return RxFirebaseDatabase.observeValueListOnce(query, AssignmentHistory.class);
    }

    public void remindResponsible(@NonNull String assignmentId) {
        final AssignmentRemindQueue remind = new AssignmentRemindQueue(assignmentId);
        databaseRef.child(Constants.PATH_PUSH_QUEUE).push().setValue(remind);
    }
}
