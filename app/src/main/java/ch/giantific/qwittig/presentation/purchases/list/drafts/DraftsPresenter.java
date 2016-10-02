/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;

import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link DraftsContract}.
 */
public class DraftsPresenter extends BasePresenterImpl<DraftsContract.ViewListener>
        implements DraftsContract.Presenter {

    private static final String STATE_VIEW_MODEL = DraftsViewModel.class.getCanonicalName();
    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_SELECTION_MODE = "STATE_SELECTION_MODE";
    private final DraftsViewModel viewModel;
    private final SortedList<DraftItemViewModel> items;
    private final SortedListCallback<DraftItemViewModel> listCallback;
    private final ChildEventSubscriber<DraftItemViewModel, DraftsViewModel> subscriber;
    private final PurchaseRepository purchaseRepo;
    private final ArrayList<String> draftsSelected;
    private ListInteraction listInteraction;
    private boolean initialDataLoaded;
    private boolean selectionModeEnabled;
    private boolean deleteSelectedItems;
    private NumberFormat moneyFormatter;
    private String currentGroupId;

    public DraftsPresenter(@Nullable Bundle savedState,
                           @NonNull Navigator navigator,
                           @NonNull UserRepository userRepo,
                           @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, userRepo);

        this.purchaseRepo = purchaseRepo;

        listCallback = new SortedListCallback<DraftItemViewModel>() {
            @Override
            public int compare(DraftItemViewModel o1, DraftItemViewModel o2) {
                return o1.compareTo(o2);
            }
        };
        items = new SortedList<>(DraftItemViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
            draftsSelected = savedState.getStringArrayList(STATE_DRAFTS_SELECTED);
            selectionModeEnabled = savedState.getBoolean(STATE_SELECTION_MODE, false);
        } else {
            viewModel = new DraftsViewModel(true);
            draftsSelected = new ArrayList<>();
        }

        //noinspection ConstantConditions
        subscriber = new ChildEventSubscriber<>(items, viewModel, e ->
                view.showMessage(R.string.toast_error_drafts_load));
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
        outState.putStringArrayList(STATE_DRAFTS_SELECTED, draftsSelected);
        outState.putBoolean(STATE_SELECTION_MODE, selectionModeEnabled);
    }

    @Override
    public DraftsViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
        listCallback.setListInteraction(listInteraction);

        if (selectionModeEnabled) {
            view.startSelectionMode();
        }
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
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
                        addDataListener(identityId, groupId);
                        loadInitialData(identityId, groupId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String identityId, @NonNull String groupId) {
        subscriptions.add(purchaseRepo.observePurchaseChildren(groupId, identityId, true)
                .filter(childEvent -> initialDataLoaded)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .map(event -> {
                    final Purchase draft = event.getValue();
                    return getItemViewModel(event.getValue(), event.getEventType(),
                            draftsSelected.contains(draft.getId()));
                })
                .subscribe(subscriber)
        );
    }

    private void loadInitialData(@NonNull String identityId, @NonNull String groupId) {
        subscriptions.add(purchaseRepo.getPurchases(groupId, identityId, true)
                .takeWhile(purchase -> Objects.equals(purchase.getGroup(), currentGroupId))
                .map(draft -> getItemViewModel(draft, EventType.NONE, draftsSelected.contains(draft.getId())))
                .toList()
                .subscribe(new Subscriber<List<DraftItemViewModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        viewModel.setEmpty(getItemCount() == 0);
                        viewModel.setLoading(false);
                        scrollToFirstSelectedItem();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "failed to load initial drafts with error:");
                        view.showMessage(R.string.toast_error_drafts_load);
                    }

                    @Override
                    public void onNext(List<DraftItemViewModel> draftItemModels) {
                        items.addAll(draftItemModels);
                    }
                })
        );
    }

    @NonNull
    private DraftItemViewModel getItemViewModel(@NonNull Purchase draft, int eventType, boolean isSelected) {
        return new DraftItemViewModel(eventType, draft, isSelected, moneyFormatter);
    }

    private void scrollToFirstSelectedItem() {
        if (selectionModeEnabled) {
            for (int i = 0, size = items.size(); i < size; i++) {
                final DraftItemViewModel itemViewModel = items.get(i);
                if (itemViewModel.isSelected()) {
                    listInteraction.scrollToPosition(i);
                    break;
                }
            }
        }
    }

    @Override
    public DraftItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
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
        items.removeItemAt(subscriber.getPositionForId(draftId));
    }

    @Override
    public void onDraftRowClick(@NonNull DraftItemViewModel itemViewModel) {
        if (!selectionModeEnabled) {
            navigator.startPurchaseEdit(itemViewModel.getId(), true);
        } else {
            toggleSelection(items.indexOf(itemViewModel));
            view.setSelectionModeTitle(R.string.cab_title_selected, draftsSelected.size());
        }
    }

    @Override
    public boolean onDraftRowLongClick(@NonNull DraftItemViewModel itemViewModel) {
        if (!selectionModeEnabled) {
            toggleSelection(items.indexOf(itemViewModel));
            view.startSelectionMode();
            view.setSelectionModeTitle(R.string.cab_title_selected, draftsSelected.size());
            selectionModeEnabled = true;
        }

        return true;
    }

    @Override
    public void toggleSelection(int position) {
        final DraftItemViewModel itemViewModel = getItemAtPosition(position);
        final String id = itemViewModel.getId();
        if (itemViewModel.isSelected()) {
            itemViewModel.setSelected(false);
            draftsSelected.remove(id);
        } else {
            itemViewModel.setSelected(true);
            draftsSelected.add(id);
        }

        listInteraction.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = items.size() - 1; i >= 0; i--) {
            final DraftItemViewModel itemViewModel = items.get(i);
            if (itemViewModel.isSelected()) {
                itemViewModel.setSelected(false);
                draftsSelected.remove(itemViewModel.getId());

                if (deleteSelectedItems) {
                    deleteDraft(itemViewModel);
                } else {
                    listInteraction.notifyItemChanged(i);
                }
            }
        }
    }

    private void deleteDraft(@NonNull DraftItemViewModel itemViewModel) {
        purchaseRepo.deleteDraft(itemViewModel.getId(), itemViewModel.getBuyer());
    }
}
