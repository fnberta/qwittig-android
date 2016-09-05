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
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftItemModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link DraftsViewModel}.
 */
public class DraftsViewModelImpl extends ListViewModelBaseImpl<DraftItemModel, DraftsViewModel.ViewListener>
        implements DraftsViewModel {

    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_SELECTION_MODE = "STATE_SELECTION_MODE";

    private final PurchaseRepository purchaseRepo;
    private final ArrayList<String> draftsSelected;
    private boolean selectionModeEnabled;
    private boolean deleteSelectedItems;
    private NumberFormat moneyFormatter;
    private String currentGroupId;

    public DraftsViewModelImpl(@Nullable Bundle savedState,
                               @NonNull Navigator navigator,
                               @NonNull RxBus<Object> eventBus,
                               @NonNull UserRepository userRepo,
                               @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.purchaseRepo = purchaseRepo;

        if (savedState != null) {
            draftsSelected = savedState.getStringArrayList(STATE_DRAFTS_SELECTED);
            selectionModeEnabled = savedState.getBoolean(STATE_SELECTION_MODE, false);
        } else {
            draftsSelected = new ArrayList<>();
        }
    }

    @Override
    protected Class<DraftItemModel> getItemModelClass() {
        return DraftItemModel.class;
    }

    @Override
    protected int compareItemModels(DraftItemModel o1, DraftItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_DRAFTS_SELECTED, draftsSelected);
        outState.putBoolean(STATE_SELECTION_MODE, selectionModeEnabled);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        super.setListInteraction(listInteraction);

        if (selectionModeEnabled) {
            view.startSelectionMode();
        }
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        moneyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(),
                                false, true);

                        initialDataLoaded = false;
                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            items.clear();
                        }
                        currentGroupId = groupId;
                        addDataListener(identityId);
                        loadInitialData(identityId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String identityId) {
        setDataListenerSub(purchaseRepo.observePurchaseChildren(currentGroupId, identityId, true)
                .filter(new Func1<RxChildEvent<Purchase>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Purchase> purchaseRxChildEvent) {
                        return initialDataLoaded;
                    }
                })
                .map(new Func1<RxChildEvent<Purchase>, DraftItemModel>() {
                    @Override
                    public DraftItemModel call(RxChildEvent<Purchase> event) {
                        final Purchase draft = event.getValue();
                        return getItemModel(event.getValue(), event.getEventType(),
                                draftsSelected.contains(draft.getId()));
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull String identityId) {
        setInitialDataSub(purchaseRepo.getPurchases(currentGroupId, identityId, true)
                .map(new Func1<Purchase, DraftItemModel>() {
                    @Override
                    public DraftItemModel call(Purchase draft) {
                        return getItemModel(draft, EventType.NONE, draftsSelected.contains(draft.getId()));
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<DraftItemModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        setLoading(false);
                        scrollToFirstSelectedItem();
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDataError(e);
                    }

                    @Override
                    public void onNext(List<DraftItemModel> draftItemModels) {
                        items.addAll(draftItemModels);
                    }
                })
        );
    }

    @NonNull
    private DraftItemModel getItemModel(@NonNull Purchase draft, int eventType, boolean isSelected) {
        return new DraftItemModel(eventType, draft, isSelected, moneyFormatter);
    }

    private void scrollToFirstSelectedItem() {
        if (selectionModeEnabled) {
            for (int i = 0, size = items.size(); i < size; i++) {
                final DraftItemModel itemModel = items.get(i);
                if (itemModel.isSelected()) {
                    listInteraction.scrollToPosition(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_drafts_load);
    }

    @Override
    public void onDeleteSelectedDraftsClick() {
        deleteSelectedItems = true;
        view.stopSelectionMode();
    }

    @Override
    public void onSelectionModeEnded() {
        clearSelection();
        deleteSelectedItems = false;
        selectionModeEnabled = false;
    }

    @Override
    public void onDraftDeleted(@NonNull String draftId) {
        view.showMessage(R.string.toast_draft_deleted);
        items.removeItemAt(getPositionForId(draftId));
    }

    @Override
    public void onDraftRowClick(@NonNull DraftItemModel itemModel) {
        if (!selectionModeEnabled) {
            navigator.startPurchaseEdit(itemModel.getId(), true);
        } else {
            toggleSelection(items.indexOf(itemModel));
            view.setSelectionModeTitle(R.string.cab_title_selected, draftsSelected.size());
        }
    }

    @Override
    public boolean onDraftRowLongClick(@NonNull DraftItemModel itemModel) {
        if (!selectionModeEnabled) {
            toggleSelection(items.indexOf(itemModel));
            view.startSelectionMode();
            view.setSelectionModeTitle(R.string.cab_title_selected, draftsSelected.size());
            selectionModeEnabled = true;
        }

        return true;
    }

    @Override
    public void toggleSelection(int position) {
        final DraftItemModel itemModel = getItemAtPosition(position);
        final String id = itemModel.getId();
        if (itemModel.isSelected()) {
            itemModel.setSelected(false);
            draftsSelected.remove(id);
        } else {
            itemModel.setSelected(true);
            draftsSelected.add(id);
        }

        listInteraction.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = items.size() - 1; i >= 0; i--) {
            final DraftItemModel itemModel = items.get(i);
            if (itemModel.isSelected()) {
                itemModel.setSelected(false);
                draftsSelected.remove(itemModel.getId());

                if (deleteSelectedItems) {
                    deleteDraft(itemModel);
                } else {
                    listInteraction.notifyItemChanged(i);
                }
            }
        }
    }

    private void deleteDraft(@NonNull DraftItemModel itemModel) {
        purchaseRepo.deleteDraft(itemModel.getId(), itemModel.getBuyer());
    }
}
