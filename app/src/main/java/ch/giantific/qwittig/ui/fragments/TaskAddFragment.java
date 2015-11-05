/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.TaskUser;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.ui.adapters.TaskUsersInvolvedRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Provides an interface for the user to add a new {@link Task}. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskAddFragment extends BaseFragment implements
        UserRepository.GetUsersLocalListener,
        TaskUsersInvolvedRecyclerAdapter.AdapterInteractionListener {

    @IntDef({TASK_SAVED, TASK_DISCARDED, TASK_NO_CHANGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskAction {}
    public static final int TASK_SAVED = 0;
    public static final int TASK_DISCARDED = 1;
    public static final int TASK_NO_CHANGES = 2;
    public static final int RESULT_TASK_SAVED = 2;
    public static final int RESULT_TASK_DISCARDED = 3;
    private static final String STATE_DEADLINE_SELECTED = "STATE_DEADLINE_SELECTED";
    private static final String STATE_USERS_INVOLVED = "STATE_USERS_INVOLVED";
    FragmentInteractionListener mListener;
    Spinner mSpinnerTimeFrame;
    TaskUsersInvolvedRecyclerAdapter mUsersRecyclerAdapter;
    Date mDeadlineSelected;
    ArrayList<TaskUser> mUsersInvolved;
    StringResSpinnerAdapter mTimeFrameAdapter;
    @NonNull
    private List<ParseUser> mUsersAvailable = new ArrayList<>();
    private TextView mTextViewDeadline;
    private RecyclerView mRecyclerViewUsers;
    private ItemTouchHelper mUsersItemTouchHelper;
    public TaskAddFragment() {
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();

        if (savedInstanceState != null) {
            mDeadlineSelected = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_DEADLINE_SELECTED));
            ArrayList<TaskUser> usersInvolved = savedInstanceState.getParcelableArrayList(STATE_USERS_INVOLVED);
            mUsersInvolved = usersInvolved != null ? usersInvolved : new ArrayList<TaskUser>();
        } else {
            mDeadlineSelected = new Date();
            mUsersInvolved = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(STATE_DEADLINE_SELECTED, DateUtils.parseDateToLong(mDeadlineSelected));
        outState.putParcelableArrayList(STATE_USERS_INVOLVED, mUsersInvolved);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_add, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextViewDeadline = (TextView) view.findViewById(R.id.tv_task_deadline);
        mTextViewDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        mTextViewDeadline.setText(DateUtils.formatDateLong(mDeadlineSelected));

        mRecyclerViewUsers = (RecyclerView) view.findViewById(R.id.rv_task_users_involved);
        setupUsersInvolvedRecyclerView();

        mSpinnerTimeFrame = (Spinner) view.findViewById(R.id.sp_task_time_frame);
        setupTimeFrameSpinner();

        setupUserList();
    }

    private void showDatePickerDialog() {
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.show(getFragmentManager(), "date_picker");
    }

    /**
     * Sets the deadline for the new task.
     *
     * @param deadline the deadline to set
     */
    public void setDeadline(@NonNull Date deadline) {
        mTextViewDeadline.setText(DateUtils.formatDateLong(deadline));
        mDeadlineSelected = deadline;
    }

    private void setupUsersInvolvedRecyclerView() {
        mRecyclerViewUsers.setHasFixedSize(true);
        mRecyclerViewUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerAdapter = new TaskUsersInvolvedRecyclerAdapter(getActivity(),
                mUsersAvailable, mUsersInvolved, this);
        mRecyclerViewUsers.setAdapter(mUsersRecyclerAdapter);

        mUsersItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
                if (source.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                mUsersRecyclerAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mUsersRecyclerAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        });
        mUsersItemTouchHelper.attachToRecyclerView(mRecyclerViewUsers);
    }

    private void setupTimeFrameSpinner() {
        final int[] timeFrames = new int[]{
                R.string.time_frame_daily,
                R.string.time_frame_weekly,
                R.string.time_frame_monthly,
                R.string.time_frame_yearly,
                R.string.time_frame_as_needed,
                R.string.time_frame_one_time};
        mTimeFrameAdapter = new StringResSpinnerAdapter(getActivity(), R.layout.spinner_item, timeFrames);
        mSpinnerTimeFrame.setAdapter(mTimeFrameAdapter);
        mSpinnerTimeFrame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                int timeFrame = (int) parent.getItemAtPosition(position);
                mTextViewDeadline.setVisibility(timeFrame == R.string.time_frame_as_needed ?
                        View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    void setupUserList() {
        queryUsers();
    }

    final void queryUsers() {
        UserRepository repo = new ParseUserRepository();
        repo.getUsersLocalAsync(mCurrentGroup, this);
    }

    @Override
    public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
        mUsersAvailable.clear();

        if (!users.isEmpty()) {
            final int usersSize = users.size();
            if (mUsersInvolved.isEmpty()) {
                for (int i = 0; i < usersSize; i++) {
                    ParseUser user = users.get(i);
                    mUsersAvailable.add(user);
                    mUsersInvolved.add(new TaskUser(user.getObjectId(), true));
                }
            } else {
                int usersInvolvedSize = mUsersInvolved.size();
                ParseUser[] parseUsers = new ParseUser[usersInvolvedSize];
                List<String> ids = new ArrayList<>(usersInvolvedSize);
                for (int i = 0; i < usersInvolvedSize; i++) {
                    TaskUser taskUser = mUsersInvolved.get(i);
                    ids.add(taskUser.getUserId());
                }

                for (Iterator<ParseUser> iterator = users.iterator(); iterator.hasNext(); ) {
                    ParseUser user = iterator.next();
                    String userId = user.getObjectId();
                    if (ids.contains(userId)) {
                        int pos = ids.indexOf(userId);
                        parseUsers[pos] = user;
                        iterator.remove();
                    }
                }

                Collections.addAll(mUsersAvailable, parseUsers);
                if (!users.isEmpty()) {
                    for (ParseUser user : users) {
                        mUsersAvailable.add(user);
                        mUsersInvolved.add(new TaskUser(user.getObjectId(), false));
                    }
                }
            }

            mRecyclerViewUsers.setMinimumHeight(usersSize * getResources().getDimensionPixelSize(R.dimen.list_avatar_with_text));
        }

        mUsersRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Saves the new {@link Task} object to the online Parse.com database and pins it to the local
     * data store if the user is not a test user and the title is not empty. If it is a one-time
     * task, checks if there is exactly one user involved selected.
     *
     * @param title the title of the new task
     */
    public void saveTask(@NonNull String title) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            MessageUtils.showBasicSnackbar(mRecyclerViewUsers, getString(R.string.error_task_title));
            return;
        }

        String timeFrame = getTimeFrameSelected();
        List<ParseUser> usersInvolved = getUsersInvolved();
        if (timeFrame.equals(Task.TIME_FRAME_ONE_TIME) && usersInvolved.size() > 1) {
            MessageUtils.showBasicSnackbar(mRecyclerViewUsers, getString(R.string.toast_task_max_one_user_one_time));
            return;
        }

        Task task = getTask(title, timeFrame, usersInvolved);
        pinTask(task);
    }

    @NonNull
    Task getTask(@NonNull String title,
                 @NonNull String timeFrame,
                 @NonNull List<ParseUser> usersInvolved) {
        return new Task(mCurrentUser, title, mCurrentGroup, timeFrame, mDeadlineSelected,
                usersInvolved);
    }

    private void pinTask(@NonNull final Task task) {
        task.pinInBackground(Task.PIN_LABEL, new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e == null) {
                    task.saveEventually();
                    finish(TASK_SAVED);
                }
            }
        });
    }

    @Task.TimeFrame
    final String getTimeFrameSelected() {
        int selected = (int) mSpinnerTimeFrame.getSelectedItem();
        switch (selected) {
            case R.string.time_frame_daily:
                return Task.TIME_FRAME_DAILY;
            case R.string.time_frame_weekly:
                return Task.TIME_FRAME_WEEKLY;
            case R.string.time_frame_monthly:
                return Task.TIME_FRAME_MONTHLY;
            case R.string.time_frame_yearly:
                return Task.TIME_FRAME_YEARLY;
            case R.string.time_frame_one_time:
                return Task.TIME_FRAME_ONE_TIME;
            default:
                mDeadlineSelected = null;
                return Task.TIME_FRAME_AS_NEEDED;
        }
    }

    @NonNull
    final List<ParseUser> getUsersInvolved() {
        final List<ParseUser> parseUsers = new ArrayList<>();

        for (int i = 0, usersInvolvedSize = mUsersInvolved.size(); i < usersInvolvedSize; i++) {
            TaskUser taskUser = mUsersInvolved.get(i);
            if (taskUser.isInvolved()) {
                parseUsers.add(mUsersAvailable.get(i));
            }
        }

        return parseUsers;
    }

    /**
     * Sets the activity result depending on the action taken and finishes the hosting
     * {@link Activity}.
     *
     * @param taskAction the task according to which to set the activity result
     */
    public void finish(@TaskAction int taskAction) {
        Activity activity = getActivity();
        switch (taskAction) {
            case TASK_SAVED:
                activity.setResult(RESULT_TASK_SAVED);
                break;
            case TASK_DISCARDED:
                activity.setResult(RESULT_TASK_DISCARDED);
                break;
            case TASK_NO_CHANGES:
                activity.setResult(Activity.RESULT_CANCELED);
                break;
        }
        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void onUsersRowItemClick(int position) {
        TaskUser taskUser = mUsersInvolved.get(position);
        if (taskUser.isInvolved()) {
            if (!userIsLastOneChecked()) {
                taskUser.setIsInvolved(false);
                mUsersRecyclerAdapter.notifyItemChanged(position);
            } else {
                MessageUtils.showBasicSnackbar(mRecyclerViewUsers,
                        getString(R.string.toast_min_one_user));
            }
        } else {
            taskUser.setIsInvolved(true);
            mUsersRecyclerAdapter.notifyItemChanged(position);
        }
    }

    /**
     * Returns whether the user has at least one user involved selected.
     *
     * @return whether a clicked checked user is the only one checked
     */
    private boolean userIsLastOneChecked() {
        int usersInvolvedCount = 0;
        for (TaskUser taskUser : mUsersInvolved) {
            if (taskUser.isInvolved()) {
                usersInvolvedCount++;
            }

            if (usersInvolvedCount > 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        mUsersItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Checks whether the user has made an changes to the data on the screen. If yes shows a
     * dialog that asks if the changes should be discarded. If no, finishes.
     */
    public void checkForChangesAndExit() {
        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            finish(TASK_NO_CHANGES);
        }
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment dialog =
                new DiscardChangesDialogFragment();
        dialog.show(getFragmentManager(), "discard_task_changes");
    }

    boolean changesWereMade() {
        return !TextUtils.isEmpty(mListener.getTaskTitle());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * </p>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Gets the title of task.
         *
         * @return the title of the task
         */
        @NonNull
        String getTaskTitle();

        /**
         * Sets the title of the task.
         *
         * @param title the title to set
         */
        void setTaskTitle(String title);
    }
}
