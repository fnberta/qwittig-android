/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftsItemModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link DraftsViewModel}.
 */
public class DraftsViewModelImpl extends ListViewModelBaseImpl<DraftsItemModel, DraftsViewModel.ViewListener>
        implements DraftsViewModel {

    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_SELECTION_MODE = "STATE_SELECTION_MODE";
    private final PurchaseRepository mPurchaseRepo;
    private final ArrayList<String> mDraftsSelected;
    private boolean mSelectionModeEnabled;
    private boolean mDeleteSelectedItems;
    private NumberFormat mMoneyFormatter;
    private String mCurrentGroupId;

    public DraftsViewModelImpl(@Nullable Bundle savedState,
                               @NonNull Navigator navigator,
                               @NonNull RxBus<Object> eventBus,
                               @NonNull UserRepository userRepository,
                               @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, eventBus, userRepository);

        mPurchaseRepo = purchaseRepo;

        if (savedState != null) {
            mDraftsSelected = savedState.getStringArrayList(STATE_DRAFTS_SELECTED);
            mSelectionModeEnabled = savedState.getBoolean(STATE_SELECTION_MODE, false);
        } else {
            mDraftsSelected = new ArrayList<>();
        }
    }

    @Override
    protected Class<DraftsItemModel> getItemModelClass() {
        return DraftsItemModel.class;
    }

    @Override
    protected int compareItemModels(DraftsItemModel o1, DraftsItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_DRAFTS_SELECTED, mDraftsSelected);
        outState.putBoolean(STATE_SELECTION_MODE, mSelectionModeEnabled);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        super.setListInteraction(listInteraction);

        if (mSelectionModeEnabled) {
            mView.startSelectionMode();
        }
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(),
                                false, true);

                        mInitialDataLoaded = false;
                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(mCurrentGroupId, groupId)) {
                            mItems.clear();
                        }
                        mCurrentGroupId = groupId;
                        addDataListener(identityId);
                        loadInitialData(identityId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String identityId) {
        setDataListenerSub(mPurchaseRepo.observePurchaseChildren(mCurrentGroupId, identityId, true)
                .filter(new Func1<RxChildEvent<Purchase>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Purchase> purchaseRxChildEvent) {
                        return mInitialDataLoaded;
                    }
                })
                .map(new Func1<RxChildEvent<Purchase>, DraftsItemModel>() {
                    @Override
                    public DraftsItemModel call(RxChildEvent<Purchase> event) {
                        final Purchase draft = event.getValue();
                        return getItemModels(event.getValue(), event.getEventType(),
                                mDraftsSelected.contains(draft.getId()));
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull String identityId) {
        setInitialDataSub(mPurchaseRepo.getPurchases(mCurrentGroupId, identityId, true)
                .map(new Func1<Purchase, DraftsItemModel>() {
                    @Override
                    public DraftsItemModel call(Purchase draft) {
                        return getItemModels(draft, -1, mDraftsSelected.contains(draft.getId()));
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<DraftsItemModel>>() {
                    @Override
                    public void onCompleted() {
                        mInitialDataLoaded = true;
                        setLoading(false);
                        scrollToFirstSelectedItem();
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDataError(e);
                    }

                    @Override
                    public void onNext(List<DraftsItemModel> draftsItemModels) {
                        mItems.addAll(draftsItemModels);
                    }
                })
        );
    }

    @NonNull
    private DraftsItemModel getItemModels(@NonNull Purchase draft, int eventType, boolean isSelected) {
        return new DraftsItemModel(eventType, draft, isSelected, mMoneyFormatter);
    }

    private void scrollToFirstSelectedItem() {
        if (mSelectionModeEnabled) {
            for (int i = 0, size = mItems.size(); i < size; i++) {
                final DraftsItemModel itemModel = mItems.get(i);
                if (itemModel.isSelected()) {
                    mListInteraction.scrollToPosition(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        mView.showMessage(R.string.toast_error_drafts_load);
    }

    @Override
    public void onDeleteSelectedDraftsClick() {
        mDeleteSelectedItems = true;
        mView.stopSelectionMode();
    }

    @Override
    public void onSelectionModeEnded() {
        clearSelection();
        mDeleteSelectedItems = false;
        mSelectionModeEnabled = false;
    }

    @Override
    public void onDraftDeleted(@NonNull String draftId) {
        mView.showMessage(R.string.toast_draft_deleted);
        mItems.removeItemAt(getPositionForId(draftId));
    }

    @Override
    public void onDraftRowClick(@NonNull DraftsItemModel itemModel) {
        if (!mSelectionModeEnabled) {
            mNavigator.startPurchaseEdit(itemModel.getId(), true);
        } else {
            toggleSelection(mItems.indexOf(itemModel));
            mView.setSelectionModeTitle(R.string.cab_title_selected, mDraftsSelected.size());
        }
    }

    @Override
    public boolean onDraftRowLongClick(@NonNull DraftsItemModel itemModel) {
        if (!mSelectionModeEnabled) {
            toggleSelection(mItems.indexOf(itemModel));
            mView.startSelectionMode();
            mView.setSelectionModeTitle(R.string.cab_title_selected, mDraftsSelected.size());
            mSelectionModeEnabled = true;
        }

        return true;
    }

    @Override
    public void toggleSelection(int position) {
        final DraftsItemModel itemModel = getItemAtPosition(position);
        final String id = itemModel.getId();
        if (itemModel.isSelected()) {
            itemModel.setSelected(false);
            mDraftsSelected.remove(id);
        } else {
            itemModel.setSelected(true);
            mDraftsSelected.add(id);
        }

        mListInteraction.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final DraftsItemModel itemModel = mItems.get(i);
            if (itemModel.isSelected()) {
                itemModel.setSelected(false);
                mDraftsSelected.remove(itemModel.getId());

                if (mDeleteSelectedItems) {
                    deleteDraft(itemModel);
                } else {
                    mListInteraction.notifyItemChanged(i);
                }
            }
        }
    }

    private void deleteDraft(@NonNull DraftsItemModel itemModel) {
        mPurchaseRepo.deletePurchase(itemModel.getId(), true);
    }
}
