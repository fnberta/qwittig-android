package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
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
        CloudCode.CloudFunctionListener,
        FABProgressListener {

    private static final String BUNDLE_AUTO_START_NEW = "auto_start_new";
    private static final String STATE_COMPENSATIONS_LOADING = "state_comps_loading";
    private static final String STATE_IS_CALCULATING_NEW = "state_is_calculating_new";
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
            mFabNew.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    circularRevealFab();
                }
            });
        } else {
            mFabNew.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealFab() {
        Animator reveal = Utils.getCircularRevealAnimator(mFabNew);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
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
            MessageUtils.showBasicSnackbar(mFabNew, getString(R.string.toast_no_connection));
            return;
        }

        if (!mIsCalculatingNew) {
            if (mCompensationsAll.isEmpty()) {
                mIsCalculatingNew = true;
                mFabProgressCircle.show();
                CloudCode.startNewSettlement(getActivity(), mCurrentGroup, false, this);
            } else {
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_finish_old));
            }
        }
    }

    @Override
    public void onCloudFunctionError(String errorMessage) {
        mFabProgressCircle.hide();
        MessageUtils.showBasicSnackbar(mFabNew, errorMessage);

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
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        switch (cloudFunction) {
            case CloudCode.PUSH_COMPENSATION_REMIND: {
                String compensationId = (String) o;
                Compensation compensation = setCompensationLoading(compensationId, false);

                String nickname = "";
                if (compensation != null) {
                    User payer = (User) compensation.getPayer();
                    nickname = payer.getNickname();
                }
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_reminded_user, nickname));
                break;
            }
            case CloudCode.PUSH_COMPENSATION_REMIND_PAID: {
                String compensationId = (String) o;
                Compensation compensation = setCompensationLoading(compensationId, false);

                String nickname = "";
                if (compensation != null) {
                    User beneficiary = (User) compensation.getBeneficiary();
                    nickname = beneficiary.getNickname();
                }
                MessageUtils.showBasicSnackbar(mRecyclerView,
                        getString(R.string.toast_compensation_reminded_user_paid,
                                nickname));
                break;
            }
            case CloudCode.SETTLEMENT_NEW:
                // do nothing
                break;
        }
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

    private void setCompensationLoading(Compensation compensation, String objectId, int position,
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
        compensation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(getActivity(), e);
                    MessageUtils.showBasicSnackbar(mFabNew,
                            ParseErrorHandler.getErrorMessage(getActivity(), e));

                    // position might have changed
                    int compPosition = mCompensations.indexOf(compensation);
                    setCompensationLoading(compensation, compensationId, compPosition, false);
                    compensation.setPaid(false);
                    return;
                }

                // position might have changed
                int compPosition = mCompensations.indexOf(compensation);
                removeItemFromList(compPosition);
                mLoadingCompensations.remove(compensationId);
            }
        });
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
        CloudCode.pushCompensationRemind(getActivity(), compensationId, mCurrentGroup.getCurrency()
                , this);
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
        CloudCode.pushCompensationRemindPaid(getActivity(), compensationId,
                mCurrentGroup.getCurrency(), this);
    }

    @Override
    public void onNotNowMenuClick(int position) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        Compensation compensation = (Compensation) mCompensations.get(position);
        User beneficiary = (User) compensation.getBeneficiary();
        String beneficiaryName = beneficiary.getNickname();
        compensation.deleteEventually();
        removeItemFromList(position);

        MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_not_now_done, beneficiaryName));
    }
}
