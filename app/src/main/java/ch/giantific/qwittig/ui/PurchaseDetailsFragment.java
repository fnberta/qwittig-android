package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.PurchaseDetailsRecyclerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;


/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseDetailsFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    private FragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private String mPurchaseId;
    private Purchase mPurchase;
    private PurchaseDetailsRecyclerAdapter mRecyclerAdapter;
    private User mCurrentUser;
    private Group mCurrentGroup;
    private static final String LOG_TAG = PurchaseDetailsFragment.class.getSimpleName();

    public PurchaseDetailsFragment() {
    }

    public static PurchaseDetailsFragment newInstance(String purchaseId) {
        PurchaseDetailsFragment fragment = new PurchaseDetailsFragment();

        Bundle args = new Bundle();
        args.putString(HomePurchasesFragment.INTENT_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPurchaseId = getArguments().getString(HomePurchasesFragment.INTENT_PURCHASE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_purchase_details, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_purchase_details);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_purchase_details);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new PurchaseDetailsRecyclerAdapter(getActivity(),
                R.layout.row_details_item_list);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
        queryData();
    }

    /**
     * Queries data needed to display the purchase. Calls method to query the currentGroup users
     * which will call the method to query the actual purchase in its done callback.
     */
    public void queryData() {
        // user query instead of fetch because fetch would not include data for the pointers
        LocalQuery.queryPurchase(mPurchaseId, this);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        mPurchase = (Purchase) object;

        updateToolbarTitle();
        updateActionBarMenu();

        mRecyclerAdapter.setPurchase(object);
        mRecyclerAdapter.notifyDataSetChanged();

        toggleMainViewVisibility();
        ActivityCompat.startPostponedEnterTransition(getActivity());

        updateReadBy();
    }

    private void updateToolbarTitle() {
        mListener.setToolbarStoreDate(mPurchase.getStore(),
                DateUtils.formatDateShort(mPurchase.getDate()));
    }

    /**
     * Checks if the currentUser is the buyer of the purchase, if yes shows the delete/edit options
     * in the ActionBar. Checks if the purchase has a receipt file, if yes show option to display
     * it in the ActionBar.
     */
    private void updateActionBarMenu() {
        List<ParseUser> usersInvolved = mPurchase.getUsersInvolved();
        boolean allUsersAreValid = true;

        for (ParseUser parseUser : usersInvolved) {
            User user = (User) parseUser;
            if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                allUsersAreValid = false;
                break;
            }
        }

        boolean hasForeignCurrency = !mCurrentGroup.getCurrency().equals(mPurchase.getCurrency());

        if (allUsersAreValid) {
            String buyerId = mPurchase.getBuyer().getObjectId();
            if (buyerId.equals(mCurrentUser.getObjectId())) {
                if (mPurchase.getReceiptParseFile() != null) {
                    mListener.updateActionBarMenu(true, true, hasForeignCurrency);
                } else {
                    mListener.updateActionBarMenu(true, false, hasForeignCurrency);
                }
            } else if (mPurchase.getReceiptParseFile() != null) {
                mListener.updateActionBarMenu(false, true, hasForeignCurrency);
            }
        } else {
            if (mPurchase.getReceiptParseFile() != null) {
                mListener.updateActionBarMenu(false, true, hasForeignCurrency);
            }
        }
    }

    private void toggleMainViewVisibility() {
        boolean purchaseIsNull = mPurchase == null;
        mRecyclerView.setVisibility(purchaseIsNull ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(purchaseIsNull ? View.VISIBLE : View.GONE);
    }

    private void updateReadBy() {
        if (!mPurchase.currentUserHasReadPurchase() &&
                !ParseUtils.isTestUser(ParseUser.getCurrentUser())) {
            mPurchase.addCurrentUserToReadBy();
            mPurchase.saveEventually();
        }
    }

    /**
     * Deletes purchase and all its items.
     */
    public void deletePurchase() {
        if (!ParseUtils.isTestUser(ParseUser.getCurrentUser())) {
            mPurchase.deleteEventually();
            mListener.finishAfterDelete();
        } else {
            mListener.showAccountCreateDialog();
        }
    }

    /**
     * Shows snackbar with exchange rate
     */
    public void showExchangeRate() {
        float exchangeRate = mPurchase.getExchangeRate();
        String message = getString(R.string.toast_exchange_rate_value,
                MoneyUtils.formatMoneyNoSymbol(exchangeRate,
                        MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS));
        MessageUtils.showBasicSnackbar(mRecyclerView, message);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void setToolbarStoreDate(String title, String subtitle);

        void updateActionBarMenu(boolean showEditOptions, boolean hasReceiptFile,
                                 boolean hasForeignCurrency);

        void showAccountCreateDialog();

        void finishAfterDelete();
    }
}
