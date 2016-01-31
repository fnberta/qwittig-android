/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by fabio on 09.01.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TasksViewModelImplTest {

    private TasksViewModelImpl mViewModel;
    @Mock
    private Bundle mBundle;
    @Mock
    private Task mTask;
    @Mock
    private TasksViewModel.ViewListener mView;
    @Mock
    private User mCurrentUser;
    @Mock
    private TaskRepository mTaskRepo;
    @Mock
    private GroupRepository mGroupRepo;
    @Mock
    private UserRepository mUserRepo;
    private ArrayList<Task> mTasks;

    @Before
    public void setUp() throws Exception {
        mViewModel = new TasksViewModelImpl(mBundle, mGroupRepo, mUserRepo, mTaskRepo);
        mTasks = new ArrayList<>();
        mTasks.add(mTask);
        mViewModel.setItems(mTasks);
    }

    @Test
    public void onRefresh_shouldShowErrorIfNoConnection() throws Exception {
        when(mView.isNetworkAvailable()).thenReturn(false);

        mViewModel.refreshItems();
        assertFalse(mViewModel.isRefreshing());
        verify(mView).showMessage(R.string.toast_no_connection);
    }

    @Test
    public void onRefresh_shouldRefreshIfConnection() throws Exception {
        when(mView.isNetworkAvailable()).thenReturn(true);

        mViewModel.refreshItems();
        assertTrue(mViewModel.isRefreshing());
        verify(mView).loadUpdateTasksWorker();
    }

    @Test
    public void onFabClick_shouldShowDialogIfUserIsNotInGroup() throws Exception {
        when(mCurrentUser.getCurrentGroup()).thenReturn(null);

        mViewModel.onAddTaskFabClick(Mockito.mock(View.class));
        verify(mView).showCreateGroupDialog(R.string.dialog_group_create_tasks);
    }

    @Test
    public void onFabClick_shouldStartAddActivityIfUserIsInGroup() throws Exception {
        when(mCurrentUser.getCurrentGroup()).thenReturn(new Group());

        mViewModel.onAddTaskFabClick(Mockito.mock(View.class));
        verify(mView).startTaskAddActivity();
    }

//    @Test
//    public void onDeadlineSelected_shouldShowAllTasksIfAllSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_all);
//        verify(mTaskRepo).getTasksLocalAsync(group, new Date(Long.MAX_VALUE));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowTodayTasksIfTodaySelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_today);
//        verify(mTaskRepo).getTasksLocalAsync(group, new Date(1452470400));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisWeekTasksIfThisWeekSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_week);
//        verify(mTaskRepo).getTasksLocalAsync(group, new Date(1452470400));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisMonthTasksIfThisMonthSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_month);
//        verify(mTaskRepo).getTasksLocalAsync(group, new Date(1454284800));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisYearTasksIfThisYearSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_year);
//        verify(mTaskRepo).getTasksLocalAsync(group, new Date(1483228800));
//    }

    @Test
    public void onTaskRowClick_shouldStartDetailsActivity() throws Exception {
        mViewModel.onTaskRowClicked(1);
        verify(mView).startTaskDetailsActivity(mTask);
    }

    @Test
    public void onDoneClick_shouldRemoveTaskIfOneTime() throws Exception {
        when(mTask.getTimeFrame()).thenReturn(Task.TIME_FRAME_ONE_TIME);

        mViewModel.onDoneButtonClicked(0);
        verify(mTask).deleteEventually();
        assertFalse(mTasks.contains(mTask));
        verify(mView).notifyItemRemoved(0);
    }

    @Test
    public void onDoneClick_shouldUpdateDeadlineAndAddHistoryEventIfNotOneTime() throws Exception {
        when(mTask.getTimeFrame()).thenReturn(Task.TIME_FRAME_MONTHLY);
        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
        verify(mTask).updateDeadline();
        verify(mTask).addHistoryEvent(mCurrentUser);
        verify(mTask).saveEventually();
    }

    @Test
    public void onDoneClick_shouldReloadWholeDataSetIfOldOrNewUserResponsibleIsCurrentUser() throws Exception {
        when(mTask.getTimeFrame()).thenReturn(Task.TIME_FRAME_MONTHLY);
        when(mTask.getUserResponsible()).thenReturn(mCurrentUser);
        when(mTask.addHistoryEvent(mCurrentUser)).thenReturn(mCurrentUser);
        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
        verify(mTaskRepo).getTasksLocalAsync(new Group(), new Date());
    }

    @Test
    public void onDoneClick_shouldReloadOnlyItemIfNeitherOldOrNewUserResponsibleIsCurrentUser() throws Exception {
        final User user = Mockito.mock(User.class);
        when(user.getObjectId()).thenReturn("someOtherId");
        when(mTask.getTimeFrame()).thenReturn(Task.TIME_FRAME_MONTHLY);
        when(mTask.getUserResponsible()).thenReturn(user);
        when(mTask.addHistoryEvent(mCurrentUser)).thenReturn(user);
        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
        verify(mView).notifyItemChanged(0);
    }

    @Test
    public void onRemindUserClick_shouldShowNoConnectionMessageIfNoConnection() throws Exception {
        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mView.isNetworkAvailable()).thenReturn(false);

        mViewModel.onRemindButtonClicked(0);
        verify(mView).showMessage(R.string.toast_no_connection, null);
    }

    @Test
    public void onRemindUserClick_shouldReturnIfTaskIsLoading() throws Exception {
        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mView.isNetworkAvailable()).thenReturn(true);
        when(mTask.getObjectId()).thenReturn("someId");

        final ArrayList<String> tasksLoading = new ArrayList<>();
        tasksLoading.add("someId");
        mViewModel.setLoadingTasks(tasksLoading);

        mViewModel.onRemindButtonClicked(0);
        verify(mTask, never()).setLoading(true);
        assertFalse(tasksLoading.contains("someId"));
        verify(mView, never()).notifyItemChanged(0);
        verify(mView, never()).loadRemindUserWorker("someId");
    }

    @Test
    public void onRemindUserClick_shouldSetTaskLoadingAndRemindUserIfNotTestUserHasConnectionNotAlreadyLoading() throws Exception {
        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mView.isNetworkAvailable()).thenReturn(true);
        when(mTask.getObjectId()).thenReturn("someId");

        final ArrayList<String> tasksLoading = new ArrayList<>();
        tasksLoading.add("someOtherId");
        mViewModel.setLoadingTasks(tasksLoading);

        mViewModel.onRemindButtonClicked(0);
        verify(mTask).setLoading(true);
        assertTrue(tasksLoading.contains("someId"));
        verify(mView).notifyItemChanged(0);
        verify(mView).loadRemindUserWorker("someId");
    }
}