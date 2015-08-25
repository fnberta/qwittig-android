package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helper.CompensationRemindHelper;
import ch.giantific.qwittig.helper.CompensationSaveHelper;
import ch.giantific.qwittig.helper.SettlementHelper;
import ch.giantific.qwittig.ui.adapter.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class CompensationsUnpaidFragment extends CompensationsBaseFragment implements
        CompensationsUnpaidRecyclerAdapter.AdapterInteractionListener,
        LocalQuery.CompensationLocalQueryListener,
        FABProgressListener {

    private static final String BUNDLE_AUTO_START_NEW = "auto_start_new";
    private static final String STATE_COMPENSATIONS_LOADING = "state_comps_loading";
    private static final String STATE_IS_CALCULATING_NEW = "state_is_calculating_new";
    private static final String COMPENSATION_SAVE_HELPER = "compensation_save_helper_";
    private static final String LOG_TAG = CompensationsUnpaidFragment.class.getSimpleName();
    private TextView mTextViewEmptyTitle;
    private TextView mTextViewEmptySubtitle;
    private FABProgressCircle mFabProgressCircle;
    private FloatingActionButton mFabNew;
    private CompensationsUnpaidRecyclerAdapter mRecyclerAdapter;
    private List<ParseObject> mCompensations = new ArrayList<>();
    private List<ParseObject> mCompensationsAll;
    private boolean mAutoStartNew;
    private ArrayList<String> mLoadingCompensations;
    private boolean mIsCalculatingNew;
    private Compensation mCompensationChangeAmount;

    public CompensationsUnpaidFragment() {
        // Required empty public constructor
    }

    public static CompensationsUnpaidFragment newInstance(boolean autoStartNew) {
        CompensationsUnpaidFragment fragment = new CompensationsUnpaidFragment();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_AUTO_START_NEW, autoStartNew);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAutoStartNew = getArguments().getBoolean(BUNDLE_AUTO_START_NEW);
        }

        if (savedInstanceState != null) {
            mLoadingCompensations = savedInstanceState.getStringArrayList(
                    STATE_COMPENSATIONS_LOADING);
            mIsCalculatingNew = savedInstanceState.getBoolean(STATE_IS_CALCULATING_NEW);
        } else {
            mLoadingCompensations = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_COMPENSATIONS_LOADING, mLoadingCompensations);
        outState.putBoolean(STATE_IS_CALCULATING_NEW, mIsCalculatingNew);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compensations_unpaid, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_compensations);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_compensations);
        mEmptyView = rootView.findViewById(R.id.tv_empty_view);
        mFabNew = (FloatingActionButton) rootView.findViewById(R.id.fab_new_account_balance);
        mFabProgressCircle = (FABProgressCircle) rootView.findViewById(R.id.fab_new_account_balance_circle);
        mTextViewEmptyTitle = (TextView) rootView.findViewById(R.id.tv_empty_view_title);
        mTextViewEmptySubtitle = (TextView) rootView.findViewById(R.id.tv_empty_view_subtitle);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsUnpaidRecyclerAdapter(getActivity(),
                R.layout.row_compensations_pos, R.layout.row_compensations_neg, mCompensations,
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

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryCompensationsUnpaid(this);
    }

    @Override
    public void onCompensationsLocalQueried(List<ParseObject> compensations) {
        mCompensationsAll = compensations;
        updateCompensations(compensations);

        if (mAutoStartNew) {
            newSettlement();
            mAutoStartNew = false;
        }
    }

    private void updateCompensations(List<ParseObject> parseObjects) {
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
            toggleEmptyViewVisibility();
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
            mTextViewEmptySubtitle.setText(R.string.no_compensations_subhead);
            showFab();
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
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
        Animator reveal = Utils.getCircularRevealAnimator(mFabNew);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
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
        SettlementHelper settlementHelper = findSettlementHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (settlementHelper == null) {
            settlementHelper = SettlementHelper.newInstance(false);

            fragmentManager.beginTransaction()
                    .add(settlementHelper, SettlementHelper.SETTLEMENT_HELPER)
                    .commit();
        }
    }

    private SettlementHelper findSettlementHelper(FragmentManager fragmentManager) {
        return (SettlementHelper) fragmentManager.findFragmentByTag(SettlementHelper.SETTLEMENT_HELPER);
    }

    private void removeSettlementHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        SettlementHelper settlementHelper = findSettlementHelper(fragmentManager);

        if (settlementHelper != null) {
            fragmentManager.beginTransaction().remove(settlementHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper created new settlement
     * @param result object returned from CloudCode
     */
    public void onNewSettlementCreated(Object result) {
        removeSettlementHelper();
    }

    /**
     * Called from activity when helper failed to create a new settlement
     * @param e
     */
    public void onNewSettlementCreationFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showSettlementErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeSettlementHelper();

        mFabProgressCircle.hide();
    }

    private void showSettlementErrorSnackbar(String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSettlement();
            }
        });
        snackbar.show();
    }

    private Compensation setCompensationLoading(String objectId, boolean isLoading) {
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

    private void setCompensationLoading(ParseObject compensation, String objectId, int position,
                                                boolean isLoading) {
        ((Compensation) compensation).setIsLoading(isLoading);
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

    private void saveCompensationWithHelper(ParseObject compensation) {
        FragmentManager fragmentManager = getFragmentManager();
        String compensationId = compensation.getObjectId();
        CompensationSaveHelper saveHelper = findCompensationSaveHelper(fragmentManager,
                compensationId);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (saveHelper == null) {
            saveHelper = new CompensationSaveHelper(compensation);

            fragmentManager.beginTransaction()
                    .add(saveHelper, COMPENSATION_SAVE_HELPER + compensationId)
                    .commit();
        }
    }

    private CompensationSaveHelper findCompensationSaveHelper(FragmentManager fragmentManager,
                                                              String compensationId) {
        return (CompensationSaveHelper) fragmentManager.findFragmentByTag(
                COMPENSATION_SAVE_HELPER + compensationId);
    }

    /**
     * Called from activity when helper successfully saved compensation
     * @param compensation
     */
    public void onCompensationSaved(ParseObject compensation) {
        removeCompensationSaveHelper(compensation.getObjectId());

        // position might have changed
        int compPosition = mCompensations.indexOf(compensation);
        removeItemFromList(compPosition);
        mLoadingCompensations.remove(compensation.getObjectId());
    }

    /**
     * Called from activity when helper failed to save compensation
     * @param compensation
     * @param e
     */
    public void onCompensationSaveFailed(ParseObject compensation, ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        MessageUtils.showBasicSnackbar(mFabNew, ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeCompensationSaveHelper(compensation.getObjectId());

        // position might have changed
        int compPosition = mCompensations.indexOf(compensation);
        setCompensationLoading(compensation, compensation.getObjectId(), compPosition, false);
    }

    private void removeCompensationSaveHelper(String compensationId) {
        FragmentManager fragmentManager = getFragmentManager();
        CompensationSaveHelper compensationSaveHelper = findCompensationSaveHelper(fragmentManager,
                compensationId);

        if (compensationSaveHelper != null) {
            fragmentManager.beginTransaction().remove(compensationSaveHelper).commitAllowingStateLoss();
        }
    }

    private void removeItemFromList(int position) {
        mCompensations.remove(position);
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
                                      String compensationId) {
        FragmentManager fragmentManager = getFragmentManager();
        CompensationRemindHelper compensationRemindHelper = findCompensationRemindHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (compensationRemindHelper == null) {
            compensationRemindHelper = CompensationRemindHelper.newInstance(remindType, compensationId);

            fragmentManager.beginTransaction()
                    .add(compensationRemindHelper, CompensationRemindHelper.COMPENSATION_REMIND_HELPER)
                    .commit();
        }
    }

    private CompensationRemindHelper findCompensationRemindHelper(FragmentManager fragmentManager) {
        return (CompensationRemindHelper) fragmentManager.findFragmentByTag(
                CompensationRemindHelper.COMPENSATION_REMIND_HELPER);
    }

    private void removeCompensationRemindHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        CompensationRemindHelper compensationRemindHelper = findCompensationRemindHelper(fragmentManager);

        if (compensationRemindHelper != null) {
            fragmentManager.beginTransaction().remove(compensationRemindHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper successfully reminded user
     * @param remindType remind to pay or remind that paid
     * @param compensationId
     */
    public void onUserReminded(int remindType, String compensationId) {
        removeCompensationRemindHelper();

        switch (remindType) {
            case CompensationRemindHelper.TYPE_REMIND: {
                Compensation compensation = setCompensationLoading(compensationId, false);

                String nickname = "";
                if (compensation != null) {
                    User payer = compensation.getPayer();
                    nickname = payer.getNickname();
                }
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_reminded_user, nickname));
                break;
            }
            case CompensationRemindHelper.TYPE_REMIND_PAID: {
                Compensation compensation = setCompensationLoading(compensationId, false);

                String nickname = "";
                if (compensation != null) {
                    User beneficiary = compensation.getBeneficiary();
                    nickname = beneficiary.getNickname();
                }
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_reminded_user_paid, nickname));
                break;
            }
        }
    }

    /**
     * Called from activity when helper failed to remind user
     * @param remindType remind to pay or remind that paid
     * @param e
     */
    public void onFailedToRemindUser(int remindType, ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        MessageUtils.showBasicSnackbar(mRecyclerView, ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeCompensationRemindHelper();

        if (!mLoadingCompensations.isEmpty()) {
            for (Iterator<String> iterator = mLoadingCompensations.iterator(); iterator.hasNext(); ) {
                String loadingCompensationId = iterator.next();
                for (int i = 0, compensationsSize = mCompensations.size(); i < compensationsSize; i++) {
                    Compensation compensation = (Compensation) mCompensations.get(i);
                    if (loadingCompensationId.equals(compensation.getObjectId())) {
                        compensation.setIsLoading(false);
                        mRecyclerAdapter.notifyItemChanged(i);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        // TODO: find a way to disable only the specific compensation concerned
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
        removeItemFromList(position);

        MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_not_now_done, beneficiaryName));
    }

    @Override
    public void onChangeAmountMenuClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        mCompensationChangeAmount = (Compensation) mCompensations.get(position);
        BigFraction amount = mCompensationChangeAmount.getAmount();
        String currency = mCurrentGroup.getCurrency();
        mListener.showChangeAmountDialog(amount, currency);
    }

    /**
     * Called from activity when change amount dialog is closed.
     * @param amount the new amount to be set in the compensation
     */
    public void changeAmount(BigFraction amount) {
        mCompensationChangeAmount.setAmount(amount);
        int position = mCompensations.indexOf(mCompensationChangeAmount);
        mRecyclerAdapter.notifyItemChanged(position);
        mCompensationChangeAmount.saveEventually();
    }
}
