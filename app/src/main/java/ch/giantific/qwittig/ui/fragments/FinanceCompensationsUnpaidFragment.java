/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.group.SettlementWorker;
import ch.giantific.qwittig.workerfragments.reminder.CompensationRemindWorker;
import ch.giantific.qwittig.workerfragments.save.CompensationSaveWorker;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link FinanceCompensationsBaseFragment}.
 */
public class FinanceCompensationsUnpaidFragment extends FinanceCompensationsBaseFragment implements
        CompensationsUnpaidRecyclerAdapter.AdapterInteractionListener,
        CompensationRepository.GetCompensationsLocalListener {

    private static final String SETTLEMENT_WORKER = "SETTLEMENT_WORKER";
    private static final String BUNDLE_AUTO_START_NEW = "BUNDLE_AUTO_START_NEW";
    private static final String STATE_COMPENSATIONS_LOADING = "STATE_COMPENSATIONS_LOADING";
    private static final String STATE_IS_CALCULATING_NEW = "STATE_IS_CALCULATING_NEW";
    private static final String COMPENSATION_UNPAID_QUERY_WORKER = "COMPENSATION_UNPAID_QUERY_WORKER";
    private static final String COMPENSATION_SAVE_WORKER = "COMPENSATION_SAVE_WORKER_";
    private static final String COMPENSATION_REMIND_WORKER = "COMPENSATION_REMIND_WORKER_";
    private static final String LOG_TAG = FinanceCompensationsUnpaidFragment.class.getSimpleName();
    private TextView mTextViewEmptyTitle;
    private TextView mTextViewEmptySubtitle;
    private FabProgress mFabProgressNewSettlement;
    private UserRepository mUserRepo;
    private CompensationsUnpaidRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseObject> mCompensations = new ArrayList<>();
    private List<ParseObject> mCompensationsAll;
    private boolean mAutoStartNew;
    private ArrayList<String> mLoadingCompensations;
    private boolean mIsCalculatingNew;
    private Compensation mCompensationChangeAmount;

    public FinanceCompensationsUnpaidFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of {@link FinanceCompensationsUnpaidFragment}.
     *
     * @param autoStartNew whether to automatically start a new settlement
     * @return a new instance of {@link FinanceCompensationsUnpaidFragment}
     */
    @NonNull
    public static FinanceCompensationsUnpaidFragment newInstance(boolean autoStartNew) {
        FinanceCompensationsUnpaidFragment fragment = new FinanceCompensationsUnpaidFragment();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_AUTO_START_NEW, autoStartNew);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserRepo = new ParseUserRepository();

        Bundle args = getArguments();
        if (args != null) {
            mAutoStartNew = args.getBoolean(BUNDLE_AUTO_START_NEW);
        }

        if (savedInstanceState != null) {
            ArrayList<String> compsLoading = savedInstanceState.getStringArrayList(
                    STATE_COMPENSATIONS_LOADING);
            mLoadingCompensations = compsLoading != null ? compsLoading : new ArrayList<String>();
            mIsCalculatingNew = savedInstanceState.getBoolean(STATE_IS_CALCULATING_NEW);
        } else {
            mLoadingCompensations = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_COMPENSATIONS_LOADING, mLoadingCompensations);
        outState.putBoolean(STATE_IS_CALCULATING_NEW, mIsCalculatingNew);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finance_compensations_unpaid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextViewEmptyTitle = (TextView) view.findViewById(R.id.tv_empty_view_title);
        mTextViewEmptySubtitle = (TextView) view.findViewById(R.id.tv_empty_view_subtitle);

        mRecyclerAdapter = new CompensationsUnpaidRecyclerAdapter(getActivity(), mCompensations,
                mCurrentUser, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mFabProgressNewSettlement = (FabProgress) view.findViewById(R.id.fab_new_account_balance);
        mFabProgressNewSettlement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSettlement();
            }
        });
        mFabProgressNewSettlement.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mIsCalculatingNew = false;
                mRecyclerAdapter.notifyDataSetChanged();
                toggleEmptyViewVisibility();

                Snackbar.make(mRecyclerView, R.string.toast_new_settlement,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @NonNull
    @Override
    protected String getQueryWorkerTag() {
        return COMPENSATION_UNPAID_QUERY_WORKER;
    }

    @Override
    protected void onlineQuery() {
        onlineQuery(false);
    }

    @Override
    public void onCompensationsUpdated() {
        super.onCompensationsUpdated();

        WorkerUtils.removeWorker(getFragmentManager(), COMPENSATION_UNPAID_QUERY_WORKER);
        setLoading(false);
    }

    @Override
    protected void updateAdapter() {
        mCompsRepo.getCompensationsLocalUnpaidAsync(mCurrentGroup, this);
    }

    @Override
    public void onCompensationsLocalLoaded(@NonNull List<ParseObject> compensations) {
        mCompensationsAll = compensations;
        updateCompensations(compensations);

        if (mAutoStartNew) {
            newSettlement();
            mAutoStartNew = false;
        }
    }

    private void updateCompensations(@NonNull List<ParseObject> parseObjects) {
        mCompensations.clear();

        if (!parseObjects.isEmpty()) {
            for (ParseObject parseObject : parseObjects) {
                Compensation compensation = (Compensation) parseObject;
                if (compensation.getPayer().getObjectId().equals(mCurrentUser.getObjectId())
                        || compensation.getBeneficiary().getObjectId().equals(mCurrentUser
                        .getObjectId())) {
                    mCompensations.add(compensation);

                    // enable or disable progress bar
                    compensation.setIsLoading(
                            mLoadingCompensations.contains(compensation.getObjectId()));
                }
            }
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrencyWithFallback(mCurrentGroup));
        if (mIsCalculatingNew) {
            mFabProgressNewSettlement.beginProgressFinalAnimation();
        } else {
            mRecyclerAdapter.notifyDataSetChanged();
            showMainView();
        }
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mCompensationsAll == null || mCompensationsAll.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTextViewEmptyTitle.setText(R.string.no_compensations);
            if (!ParseUtils.isTestUser(mCurrentUser) && mCurrentGroup != null) {
                mTextViewEmptyTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mTextViewEmptySubtitle.setText(R.string.no_compensations_subhead);
                mFabProgressNewSettlement.show();
            }
        } else if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTextViewEmptyTitle.setText(R.string.no_compensations);
            mTextViewEmptySubtitle.setText(R.string.no_compensations_subhead_group_has);
            mFabProgressNewSettlement.setVisibility(View.INVISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void newSettlement() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            showErrorSnackbar(getString(R.string.toast_no_connection), getSettlementErrorRetryAction());
            return;
        }

        if (mIsCalculatingNew) {
            return;
        }

        mUserRepo.getUsersLocalAsync(mCurrentGroup, new UserRepository.GetUsersLocalListener() {
            @Override
            public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
                if (users.size() > 1) { // size = 1 would mean current user is the only one in the group
                    if (mCompensationsAll.isEmpty()) {
                        mIsCalculatingNew = true;
                        mFabProgressNewSettlement.startProgress();
                        calculateNewSettlementWithWorker();
                    } else {
                        Snackbar.make(mRecyclerView, R.string.toast_compensation_finish_old,
                                        Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mRecyclerView, R.string.toast_only_user_in_group,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private View.OnClickListener getSettlementErrorRetryAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSettlement();
            }
        };
    }

    private void calculateNewSettlementWithWorker() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment settlementWorker = WorkerUtils.findWorker(fragmentManager, SETTLEMENT_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (settlementWorker == null) {
            settlementWorker = SettlementWorker.newInstance(false);

            fragmentManager.beginTransaction()
                    .add(settlementWorker, SETTLEMENT_WORKER)
                    .commit();
        }
    }

    /**
     * Removes the retained worker fragment after a new settlement was created.
     */
    public void onNewSettlementCreated() {
        WorkerUtils.removeWorker(getFragmentManager(), SETTLEMENT_WORKER);
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained worker fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onNewSettlementCreationFailed(int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(context, errorCode),
                getSettlementErrorRetryAction());
        WorkerUtils.removeWorker(getFragmentManager(), SETTLEMENT_WORKER);

        mFabProgressNewSettlement.stopProgress();
    }

    @Nullable
    private Compensation setCompensationLoading(@NonNull String objectId, boolean isLoading) {
        Compensation compensationLoading = null;

        for (int i = 0, mCompensationsSize = mCompensations.size(); i < mCompensationsSize; i++) {
            Compensation compensation = (Compensation) mCompensations.get(i);
            if (objectId.equals(compensation.getObjectId())) {
                setCompensationLoading(compensation, objectId, i, isLoading);
                compensationLoading = compensation;
            }
        }

        return compensationLoading;
    }

    private void setCompensationLoading(@NonNull Compensation compensation, String objectId, int position,
                                        boolean isLoading) {
        compensation.setIsLoading(isLoading);
        mRecyclerAdapter.notifyItemChanged(position);

        if (isLoading) {
            mLoadingCompensations.add(objectId);
        } else {
            mLoadingCompensations.remove(objectId);
        }
    }

    @Override
    public void onCompensationRowClick(int position) {
        // do nothing at the moment
    }

    @Override
    public void onDoneButtonClick(final int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mFabProgressNewSettlement, R.string.toast_no_connection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        final Compensation compensation = (Compensation) mCompensations.get(position);
        final String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        compensation.setPaid(true);
        saveCompensationWithWorker(compensation);
    }

    private void saveCompensationWithWorker(@NonNull ParseObject compensation) {
        FragmentManager fragmentManager = getFragmentManager();
        String compensationId = compensation.getObjectId();
        Fragment saveWorker = WorkerUtils.findWorker(fragmentManager, getSaveWorkerTag(compensationId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (saveWorker == null) {
            saveWorker = new CompensationSaveWorker(compensation);

            fragmentManager.beginTransaction()
                    .add(saveWorker, getSaveWorkerTag(compensationId))
                    .commit();
        }
    }

    @NonNull
    private String getSaveWorkerTag(String compensationId) {
        return COMPENSATION_SAVE_WORKER + compensationId;
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained worker fragment and loading indicators. Also removes the compensation
     * from the list of loading compensations.
     *
     * @param compensation the compensation which failed to save
     * @param errorCode    the error code of the exception thrown in the process
     */
    public void onCompensationSaveFailed(@NonNull ParseObject compensation, int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        Snackbar.make(mFabProgressNewSettlement, ParseErrorHandler.getErrorMessage(context, errorCode),
                Snackbar.LENGTH_LONG).show();
        String compensationId = compensation.getObjectId();
        WorkerUtils.removeWorker(getFragmentManager(), getSaveWorkerTag(compensationId));

        // position might have changed
        int compPosition = mCompensations.indexOf(compensation);
        setCompensationLoading((Compensation) compensation, compensationId, compPosition, false);
    }

    /**
     * Removes the retained worker fragment and removes the compensation from the list of loading
     * compensations and also from the main {@link RecyclerView} list as it is now paid.
     *
     * @param compensation the now paid compensation
     */
    public void onCompensationSaved(@NonNull ParseObject compensation) {
        String compensationId = compensation.getObjectId();
        WorkerUtils.removeWorker(getFragmentManager(), getSaveWorkerTag(compensationId));

        removeItemFromList(compensation);
        mLoadingCompensations.remove(compensationId);
    }

    private void removeItemFromList(ParseObject compensation) {
        int position = mCompensations.indexOf(compensation);
        mCompensations.remove(position);

        int positionAll = mCompensationsAll.indexOf(compensation);
        mCompensationsAll.remove(positionAll);

        mRecyclerAdapter.notifyItemRemoved(position);
        toggleEmptyViewVisibility();
    }

    @Override
    public void onRemindButtonClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mFabProgressNewSettlement, R.string.toast_no_connection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        remindUserWithWorker(CompensationRemindWorker.TYPE_REMIND, compensationId);
    }

    private void remindUserWithWorker(@CompensationRemindWorker.RemindType int remindType,
                                      @NonNull String compensationId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment compensationRemindWorker = WorkerUtils.findWorker(fragmentManager,
                getRemindWorkerTag(compensationId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (compensationRemindWorker == null) {
            compensationRemindWorker = CompensationRemindWorker.newInstance(remindType, compensationId);

            fragmentManager.beginTransaction()
                    .add(compensationRemindWorker, getRemindWorkerTag(compensationId))
                    .commit();
        }
    }

    @NonNull
    private String getRemindWorkerTag(String compensationId) {
        return COMPENSATION_REMIND_WORKER + compensationId;
    }

    /**
     * Removes the retained worker fragment and displays a confirmation message to the user.
     *
     * @param remindType     the type of the reminder, remind to pay or remind that paid
     * @param compensationId the compensation for which a reminder was sent
     */
    public void onUserReminded(int remindType, @NonNull String compensationId) {
        WorkerUtils.removeWorker(getFragmentManager(), getRemindWorkerTag(compensationId));

        switch (remindType) {
            case CompensationRemindWorker.TYPE_REMIND: {
                Compensation compensation = setCompensationLoading(compensationId, false);
                if (compensation != null) {
                    User payer = compensation.getPayer();
                    String nickname = payer.getNickname();

                    Snackbar.make(mRecyclerView,
                            getString(R.string.toast_compensation_reminded_user, nickname),
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case CompensationRemindWorker.TYPE_REMIND_PAID: {
                Compensation compensation = setCompensationLoading(compensationId, false);
                if (compensation != null) {
                    User beneficiary = compensation.getBeneficiary();
                    String nickname = beneficiary.getNickname();
                    Snackbar.make(mRecyclerView,
                            getString(R.string.toast_compensation_reminded_user_paid, nickname),
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained worker fragment and loading indicators. Also removes the compensation
     * from the list of loading compensations.
     *
     * @param compensationId the object id of the compensation a reminder was sent
     * @param errorCode      the error code of the exception thrown in the process
     */
    public void onUserRemindFailed(@NonNull String compensationId, int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        Snackbar.make(mRecyclerView, ParseErrorHandler.getErrorMessage(context, errorCode),
                Snackbar.LENGTH_LONG).show();
        WorkerUtils.removeWorker(getFragmentManager(), getRemindWorkerTag(compensationId));

        setCompensationLoading(compensationId, false);
    }

    @Override
    public void onRemindPaidButtonClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mFabProgressNewSettlement, R.string.toast_no_connection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        remindUserWithWorker(CompensationRemindWorker.TYPE_REMIND_PAID, compensationId);
    }

    @Override
    public void onNotNowMenuClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        User beneficiary = compensation.getBeneficiary();
        String beneficiaryName = beneficiary.getNickname();
        compensation.deleteEventually();
        removeItemFromList(compensation);

        Snackbar.make(mRecyclerView, getString(R.string.toast_not_now_done, beneficiaryName),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onChangeAmountMenuClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        mCompensationChangeAmount = (Compensation) mCompensations.get(position);
        BigFraction amount = mCompensationChangeAmount.getAmountFraction();
        String currency = mCurrentGroup.getCurrency();
        showChangeAmountDialog(amount, currency);
    }

    private void showChangeAmountDialog(BigFraction amount, String currency) {
        CompensationChangeAmountDialogFragment storeSelectionDialogFragment =
                CompensationChangeAmountDialogFragment.newInstance(amount, currency);
        storeSelectionDialogFragment.show(getFragmentManager(), "change_amount");
    }

    /**
     * Sets the amount of the purchase and updates the list.
     *
     * @param amount the new amount to be set in the compensation
     */
    public void onChangedAmountSet(@NonNull BigFraction amount) {
        mCompensationChangeAmount.setAmountFraction(amount);
        int position = mCompensations.indexOf(mCompensationChangeAmount);
        mRecyclerAdapter.notifyItemChanged(position);
        mCompensationChangeAmount.saveEventually();
    }
}
