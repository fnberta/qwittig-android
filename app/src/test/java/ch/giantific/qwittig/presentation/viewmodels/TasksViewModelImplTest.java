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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModel;
import ch.giantific.qwittig.presentation.tasks.list.TasksViewModelImpl;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem;
import ch.giantific.qwittig.presentation.tasks.list.items.TaskItem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by fabio on 09.01.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TasksViewModelImplTest {

    @Mock
    private Bundle mMockBundle;
    @Mock
    private Task mMockTask;
    @Mock
    private TasksViewModel.ViewListener mMockView;
    @Mock
    private TaskRepository mMockTaskRepo;
    @Mock
    private UserRepository mMockUserRepo;

    private TasksViewModelImpl mViewModel;
    private ArrayList<TasksBaseItem> mTasks;

//    @Before
//    public void setUp() throws Exception {
//        final User currentUser = mMockUserRepo.getCurrentUser();
//        mViewModel = new TasksViewModelImpl(mMockBundle, mMockView, mMockUserRepo, mMockTaskRepo);
//        mTasks = new ArrayList<>();
//        mTasks.add(new TaskItem(view, mMockTask, currentUser.getCurrentIdentity()));
//        mViewModel.setItems(mTasks);
//    }

//    @Test
//    public void onRefresh_shouldShowErrorIfNoConnection() throws Exception {
//        when(mMockView.isNetworkAvailable()).thenReturn(false);
//
//        mViewModel.refreshItems();
//        assertFalse(mViewModel.isRefreshing());
//        verify(mMockView).showMessage(R.string.toast_no_connection);
//    }
//
//    @Test
//    public void onRefresh_shouldRefreshIfConnection() throws Exception {
//        when(mMockView.isNetworkAvailable()).thenReturn(true);
//
//        mViewModel.refreshItems();
//        assertTrue(mViewModel.isRefreshing());
//        verify(mMockView).startUpdateTasksService();
//    }

    @Test
    public void onFabClick_shouldStartAddActivityIfUserIsInGroup() throws Exception {
//        when(mCurrentUser.getCurrentGroup()).thenReturn(new Group());

        mViewModel.onAddTaskFabClick(Mockito.mock(View.class));
        verify(mMockView).startTaskAddScreen();
    }

//    @Test
//    public void onDeadlineSelected_shouldShowAllTasksIfAllSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_all);
//        verify(mMockTaskRepo).getTasks(group, new Date(Long.MAX_VALUE));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowTodayTasksIfTodaySelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_today);
//        verify(mMockTaskRepo).getTasks(group, new Date(1452470400));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisWeekTasksIfThisWeekSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_week);
//        verify(mMockTaskRepo).getTasks(group, new Date(1452470400));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisMonthTasksIfThisMonthSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_month);
//        verify(mMockTaskRepo).getTasks(group, new Date(1454284800));
//    }
//
//    @Test
//    public void onDeadlineSelected_shouldShowThisYearTasksIfThisYearSelected() throws Exception {
//        final Group group = new Group();
//        when(mCurrentUser.getCurrentGroup()).thenReturn(group);
//
//        mViewModel.onDeadlineSelected(R.string.deadline_year);
//        verify(mMockTaskRepo).getTasks(group, new Date(1483228800));
//    }

    @Test
    public void onTaskRowClick_shouldStartDetailsActivity() throws Exception {
        mViewModel.onTaskRowClicked(1);
        verify(mMockView).startTaskDetailsScreen(mMockTask);
    }

    @Test
    public void onDoneClick_shouldRemoveTaskIfOneTime() throws Exception {
        when(mMockTask.getTimeFrame()).thenReturn(Task.TimeFrame.ONE_TIME);

        mViewModel.onDoneButtonClicked(0);
        verify(mMockTask).deleteEventually();
        assertFalse(mTasks.contains(mMockTask));
        verify(mMockView).notifyItemRemoved(0);
    }

    @Test
    public void onDoneClick_shouldUpdateDeadlineAndAddHistoryEventIfNotOneTime() throws Exception {
        when(mMockTask.getTimeFrame()).thenReturn(Task.TimeFrame.MONTHLY);
//        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
//        verify(mMockTask).updateDeadline();
//        verify(mMockTask).rotateIdentities(mCurrentUser);
        verify(mMockTask).saveEventually();
    }

    @Test
    public void onDoneClick_shouldReloadWholeDataSetIfOldOrNewUserResponsibleIsCurrentUser() throws Exception {
        when(mMockTask.getTimeFrame()).thenReturn(Task.TimeFrame.MONTHLY);
//        when(mMockTask.getIdentityResponsible()).thenReturn(mCurrentUser);
//        when(mMockTask.rotateIdentities(mCurrentUser)).thenReturn(mCurrentUser);
//        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
//        verify(mMockTaskRepo).getTasks(new Group(), new Date());
    }

    @Test
    public void onDoneClick_shouldReloadOnlyItemIfNeitherOldOrNewUserResponsibleIsCurrentUser() throws Exception {
        final Identity identity = Mockito.mock(Identity.class);
        when(identity.getObjectId()).thenReturn("someOtherId");
        when(mMockTask.getTimeFrame()).thenReturn(Task.TimeFrame.MONTHLY);
        when(mMockTask.getIdentityResponsible()).thenReturn(identity);
//        when(mMockTask.rotateIdentities(mCurrentUser)).thenReturn(user);
//        when(mCurrentUser.getObjectId()).thenReturn("someId");

        mViewModel.onDoneButtonClicked(0);
        verify(mMockView).notifyItemChanged(0);
    }

    @Test
    public void onRemindUserClick_shouldShowNoConnectionMessageIfNoConnection() throws Exception {
//        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mMockView.isNetworkAvailable()).thenReturn(false);

        mViewModel.onRemindButtonClicked(0);
        verify(mMockView).showMessage(R.string.toast_no_connection, null);
    }

    @Test
    public void onRemindUserClick_shouldReturnIfTaskIsLoading() throws Exception {
//        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mMockView.isNetworkAvailable()).thenReturn(true);
        when(mMockTask.getObjectId()).thenReturn("someId");

        final ArrayList<String> tasksLoading = new ArrayList<>();
        tasksLoading.add("someId");
        mViewModel.setLoadingTasks(tasksLoading);

        mViewModel.onRemindButtonClicked(0);
        verify(mMockTask, never()).setLoading(true);
        assertFalse(tasksLoading.contains("someId"));
        verify(mMockView, never()).notifyItemChanged(0);
        verify(mMockView, never()).loadRemindUserWorker("someId");
    }

    @Test
    public void onRemindUserClick_shouldSetTaskLoadingAndRemindUserIfNotTestUserHasConnectionNotAlreadyLoading() throws Exception {
//        when(mCurrentUser.getUsername()).thenReturn("James Bond");
        when(mMockView.isNetworkAvailable()).thenReturn(true);
        when(mMockTask.getObjectId()).thenReturn("someId");

        final ArrayList<String> tasksLoading = new ArrayList<>();
        tasksLoading.add("someOtherId");
        mViewModel.setLoadingTasks(tasksLoading);

        mViewModel.onRemindButtonClicked(0);
        verify(mMockTask).setLoading(true);
        assertTrue(tasksLoading.contains("someId"));
        verify(mMockView).notifyItemChanged(0);
        verify(mMockView).loadRemindUserWorker("someId");
    }
}