package ch.giantific.qwittig.utils.rxwrapper.firebase.callables;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.Callable;

/**
 * Created by fabio on 04.09.16.
 */

public class TaskCallable<T> implements Callable<T> {

    private final Task<T> task;

    public TaskCallable(@NonNull Task<T> task) {
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        return Tasks.await(task);
    }
}
