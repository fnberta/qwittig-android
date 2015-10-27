/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.CompensationRemindHelper;
import ch.giantific.qwittig.helpers.CompensationSaveHelper;
import ch.giantific.qwittig.helpers.SettlementHelper;
import ch.giantific.qwittig.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.utils.AnimUtils;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
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
        LocalQuery.CompensationLocalQueryListener,
        FABProgressListener {

    private static final String SETTLEMENT_HELPER = "SETTLEMENT_HELPER";
    private static final String BUNDLE_AUTO_START_NEW = "BUNDLE_AUTO_START_NEW";
    private static final String STATE_COMPENSATIONS_LOADING = "STATE_COMPENSATIONS_LOADING";
    private static final String STATE_IS_CALCULATING_NEW = "STATE_IS_CALCULATING_NEW";
    private static final String COMPENSATION_QUERY_HELPER = "COMPENSATION_QUERY_HELPER";
    private static final String COMPENSATION_SAVE_HELPER = "COMPENSATION_SAVE_HELPER_";
    private static final String COMPENSATION_REMIND_HELPER = "COMPENSATION_REMIND_HELPER_";
    private static final String LOG_TAG = FinanceCompensationsUnpaidFragment.class.getSimpleName();
    private TextView mTextViewEmptyTitle;
    private TextView mTextViewEmptySubtitle;
    private FABProgressCircle mFabProgressCircle;
    private FloatingActionButton mFabNew;
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

        if (getArguments() != null) {
            mAutoStartNew = getArguments().getBoolean(BUNDLE_AUTO_START_NEW);
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
        View rootView = inflater.inflate(R.layout.fragment_finance_compensations_unpaid, container, false);
        findBaseViews(rootView);

        mFabNew = (FloatingActionButton) rootView.findViewById(R.id.fab_new_account_balance);
        mFabProgressCircle = (FABProgressCircle) rootView.findViewById(R.id.fab_new_account_balance_circle);
        mTextViewEmptyTitle = (TextView) rootView.findViewById(R.id.tv_empty_view_title);
        mTextViewEmptySubtitle = (TextView) rootView.findViewById(R.id.tv_empty_view_subtitle);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsUnpaidRecyclerAdapter(getActivity(), mCompensations,
                this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mFabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSettlement();
            }
        });
        mFabProgressCircle.attachListener(this);
    }

    @NonNull
    @Override
    protected String getQueryHelperTag() {
        return COMPENSATION_QUERY_HELPER;
    }

    @Override
    protected void onlineQuery() {
        onlineQuery(false);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryCompensationsUnpaid(this);
    }

    @Override
    public void onCompensationsLocalQueried(@NonNull List<ParseObject> compensations) {
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
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        if (mIsCalculatingNew) {
            mFabProgressCircle.beginFinalAnimation();
        } else {
            mRecyclerAdapter.notifyDataSetChanged();
            showMainView();
        }
    }

    @Override
    public void onFABProgressAnimationEnd() {
        mIsCalculatingNew = false;
        mRecyclerAdapter.notifyDataSetChanged();
        toggleEmptyViewVisibility();

        MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_new_settlement));
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mCompensationsAll == null || mCompensationsAll.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTextViewEmptyTitle.setText(R.string.no_compensations);
            if (!ParseUtils.isTestUser(mCurrentUser) && mCurrentGroup != null) {
                mTextViewEmptyTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mTextViewEmptySubtitle.setText(R.string.no_compensations_subhead);
                showFab();
            }
        } else if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTextViewEmptyTitle.setText(R.string.no_compensations);
            mTextViewEmptySubtitle.setText(R.string.no_compensations_subhead_group_has);
            mFabNew.setVisibility(View.INVISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void showFab() {
        if (Utils.isRunningLollipopAndHigher()) {
            if (ViewCompat.isLaidOut(mFabNew)) {
                circularRevealFab();
            } else {
                mFabNew.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        circularRevealFab();
                    }
                });
            }
        } else {
            mFabNew.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealFab() {
        Animator reveal = AnimUtils.getCircularRevealAnimator(mFabNew);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                super.onAnimationStart(animation);
                animation.removeListener(this);

                mFabNew.setVisibility(View.VISIBLE);
            }
        });
        reveal.start();
    }

    private void newSettlement() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            showSettlementErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        if (!mIsCalculatingNew) {
            if (mCompensationsAll.isEmpty()) {
                mIsCalculatingNew = true;
                mFabProgressCircle.show();
                calculateNewSettlementWithHelper();
            } else {
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_finish_old));
            }
        }
    }

    private void calculateNewSettlementWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment settlementHelper = HelperUtils.findHelper(fragmentManager, SETTLEMENT_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (settlementHelper == null) {
            settlementHelper = SettlementHelper.newInstance(false);

            fragmentManager.beginTransaction()
                    .add(settlementHelper, SETTLEMENT_HELPER)
                    .commit();
        }
    }

    /**
     * Removes the retained helper fragment after a new settlement was created.
     *
     * @param result the result returned from the cloud code call
     */
    public void onNewSettlementCreated(Object result) {
        HelperUtils.removeHelper(getFragmentManager(), SETTLEMENT_HELPER);
    }

    /**
     * Passes the {@link ParseException} to the generic error handler, shows the user an error
     * message and removes the retained helper fragment and loading indicators.
     *
     * @param e the {@link ParseException} thrown in the process
     */
    public void onNewSettlementCreationFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showSettlementErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), SETTLEMENT_HELPER);

        mFabProgressCircle.hide();
    }

    private void showSettlementErrorSnackbar(@NonNull String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSettlement();
            }
        });
        snackbar.show();
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
            MessageUtils.showBasicSnackbar(mFabNew, getString(R.string.toast_no_connection));
            return;
        }

        final Compensation compensation = (Compensation) mCompensations.get(position);
        final String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        compensation.setPaid(true);
        saveCompensationWithHelper(compensation);
    }

    private void saveCompensationWithHelper(@NonNull ParseObject compensation) {
        FragmentManager fragmentManager = getFragmentManager();
        String compensationId = compensation.getObjectId();
        Fragment saveHelper = HelperUtils.findHelper(fragmentManager, getSaveHelperTag(compensationId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (saveHelper == null) {
            saveHelper = new CompensationSaveHelper(compensation);

            fragmentManager.beginTransaction()
                    .add(saveHelper, getSaveHelperTag(compensationId))
                    .commit();
        }
    }

    @NonNull
    private String getSaveHelperTag(String compensationId) {
        return COMPENSATION_SAVE_HELPER + compensationId;
    }

    /**
     * Passes the {@link ParseException} to the generic error handler, shows the user an error
     * message and removes the retained helper fragment and loading indicators. Also removes the
     * compensation from the list of loading compensations.
     *
     * @param compensation the compensation which failed to save
     * @param e            the {@link ParseException} thrown in the process
     */
    public void onCompensationSaveFailed(@NonNull ParseObject compensation, ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        MessageUtils.showBasicSnackbar(mFabNew, ParseErrorHandler.getErrorMessage(getActivity(), e));
        String compensationId = compensation.getObjectId();
        HelperUtils.removeHelper(getFragmentManager(), getSaveHelperTag(compensationId));

        // position might have changed
        int compPosition = mCompensations.indexOf(compensation);
        setCompensationLoading((Compensation) compensation, compensationId, compPosition, false);
    }

    /**
     * Removes the retained helper fragment and removes the compensation from the list of loading
     * compensations and also from the main {@link RecyclerView} list as it is now paid.
     *
     * @param compensation the now paid compensation
     */
    public void onCompensationSaved(@NonNull ParseObject compensation) {
        String compensationId = compensation.getObjectId();
        HelperUtils.removeHelper(getFragmentManager(), getSaveHelperTag(compensationId));

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
            MessageUtils.showBasicSnackbar(mFabNew, getString(R.string.toast_no_connection));
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        remindUserWithHelper(CompensationRemindHelper.TYPE_REMIND, compensationId);
    }

    private void remindUserWithHelper(@CompensationRemindHelper.RemindType int remindType,
                                      @NonNull String compensationId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment compensationRemindHelper = HelperUtils.findHelper(fragmentManager,
                getRemindHelperTag(compensationId));

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (compensationRemindHelper == null) {
            compensationRemindHelper = CompensationRemindHelper.newInstance(remindType, compensationId);

            fragmentManager.beginTransaction()
                    .add(compensationRemindHelper, getRemindHelperTag(compensationId))
                    .commit();
        }
    }

    @NonNull
    private String getRemindHelperTag(String compensationId) {
        return COMPENSATION_REMIND_HELPER + compensationId;
    }

    /**
     * Removes the retained helper fragment and displays a confirmation message to the user.
     *
     * @param remindType     the type of the reminder, remind to pay or remind that paid
     * @param compensationId the compensation for which a reminder was sent
     */
    public void onUserReminded(int remindType, @NonNull String compensationId) {
        HelperUtils.removeHelper(getFragmentManager(), getRemindHelperTag(compensationId));

        switch (remindType) {
            case CompensationRemindHelper.TYPE_REMIND: {
                Compensation compensation = setCompensationLoading(compensationId, false);
                if (compensation != null) {
                    User payer = compensation.getPayer();
                    String nickname = payer.getNickname();

                    MessageUtils.showBasicSnackbar(mRecyclerView,
                            getString(R.string.toast_compensation_reminded_user, nickname));
                }
                break;
            }
            case CompensationRemindHelper.TYPE_REMIND_PAID: {
                Compensation compensation = setCompensationLoading(compensationId, false);
                if (compensation != null) {
                    User beneficiary = compensation.getBeneficiary();
                    String nickname = beneficiary.getNickname();
                    MessageUtils.showBasicSnackbar(mRecyclerView,
                            getString(R.string.toast_compensation_reminded_user_paid, nickname));
                }
                break;
            }
        }
    }

    /**
     * Passes the {@link ParseException} to the generic error handler, shows the user an error
     * message and removes the retained helper fragment and loading indicators. Also removes the
     * compensation from the list of loading compensations.
     *
     * @param remindType remind to pay or remind that paid
     * @param e          the {@link ParseException} thrown in the process
     */
    public void onUserRemindFailed(int remindType, ParseException e, @NonNull String compensationId) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        MessageUtils.showBasicSnackbar(mRecyclerView, ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), getRemindHelperTag(compensationId));

        setCompensationLoading(compensationId, false);
    }

    @Override
    public void onRemindPaidButtonClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mFabNew, getString(R.string.toast_no_connection));
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        String compensationId = compensation.getObjectId();
        if (mLoadingCompensations.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        remindUserWithHelper(CompensationRemindHelper.TYPE_REMIND_PAID, compensationId);
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

        MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_not_now_done, beneficiaryName));
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
