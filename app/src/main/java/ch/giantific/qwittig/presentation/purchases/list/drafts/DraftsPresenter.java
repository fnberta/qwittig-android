/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;

/**
 * Provides an implementation of the {@link DraftsContract}.
 */
public class DraftsPresenter extends BasePresenterImpl<DraftsContract.ViewListener>
        implements DraftsContract.Presenter {

    private final DraftsViewModel viewModel;
    private final PurchaseRepository purchaseRepo;
    private NumberFormat moneyFormatter;
    private String currentGroupId;
    private boolean deleteSelectedItems;

    @Inject
    public DraftsPresenter(@NonNull Navigator navigator,
                           @NonNull DraftsViewModel viewModel,
                           @NonNull UserRepository userRepo,
                           @NonNull PurchaseRepository purchaseRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.purchaseRepo = purchaseRepo;
    }

    @Override
    public int compareItemViewModels(@NonNull DraftItemViewModel item1,
                                     @NonNull DraftItemViewModel item2) {
        return item1.compareTo(item2);
    }

    @Override
    public void attachView(@NonNull DraftsContract.ViewListener view) {
        super.attachView(view);

        if (viewModel.isSelectionModeEnabled()) {
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

                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            view.clearItems();
                        }
                        currentGroupId = groupId;
                        addDataListener(identityId, groupId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull String identityId, @NonNull String groupId) {
        final Observable<List<DraftItemViewModel>> initialData = purchaseRepo.getPurchases(groupId, identityId, true)
                .map(draft -> getItemViewModel(draft, EventType.NONE, viewModel.isDraftSelected(draft)))
                .toList()
                .doOnNext(draftItemViewModels -> {
                    view.addItems(draftItemViewModels);
                    viewModel.setEmpty(view.isItemsEmpty());
                    viewModel.setLoading(false);
                    scrollToFirstSelectedItem();
                });
        subscriptions.add(purchaseRepo.observePurchaseChildren(groupId, identityId, true)
                .skipUntil(initialData)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .map(event -> {
                    final Purchase draft = event.getValue();
                    return getItemViewModel(event.getValue(), event.getEventType(),
                            viewModel.isDraftSelected(draft));
                })
                .subscribe(new ChildEventSubscriber<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_drafts_load)))
        );
    }

    @NonNull
    private DraftItemViewModel getItemViewModel(@NonNull Purchase draft, int eventType, boolean isSelected) {
        return new DraftItemViewModel(eventType, draft, isSelected, moneyFormatter);
    }

    private void scrollToFirstSelectedItem() {
        if (viewModel.isSelectionModeEnabled()) {
            for (int i = 0, size = view.getItemCount(); i < size; i++) {
                final DraftItemViewModel itemViewModel = view.getItemAtPosition(i);
                if (itemViewModel.isSelected()) {
                    view.scrollToItemPosition(i);
                    break;
                }
            }
        }
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
        viewModel.setSelectionModeEnabled(false);
    }

    @Override
    public void onDraftDeleted(@NonNull String draftId) {
        view.showMessage(R.string.toast_draft_deleted);
        view.removeItemAtPosition(view.getItemPositionForId(draftId));
    }

    @Override
    public void onDraftRowClick(@NonNull DraftItemViewModel itemViewModel) {
        if (!viewModel.isSelectionModeEnabled()) {
            navigator.startPurchaseEdit(itemViewModel.getId(), true);
        } else {
            toggleSelection(view.getItemPositionForItem(itemViewModel));
            view.setSelectionModeTitle(R.string.cab_title_selected, view.getItemCount());
        }
    }

    @Override
    public boolean onDraftRowLongClick(@NonNull DraftItemViewModel itemViewModel) {
        if (!viewModel.isSelectionModeEnabled()) {
            toggleSelection(view.getItemPositionForItem(itemViewModel));
            view.startSelectionMode();
            view.setSelectionModeTitle(R.string.cab_title_selected, view.getItemCount());
            viewModel.setSelectionModeEnabled(true);
        }

        return true;
    }

    @Override
    public void toggleSelection(int position) {
        final DraftItemViewModel itemViewModel = view.getItemAtPosition(position);
        final String id = itemViewModel.getId();
        if (itemViewModel.isSelected()) {
            itemViewModel.setSelected(false);
            viewModel.addDraftSelected(id);
        } else {
            itemViewModel.setSelected(true);
            viewModel.removeDraftSelected(id);
        }

        view.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = view.getItemCount() - 1; i >= 0; i--) {
            final DraftItemViewModel itemViewModel = view.getItemAtPosition(i);
            if (itemViewModel.isSelected()) {
                itemViewModel.setSelected(false);
                viewModel.removeDraftSelected(itemViewModel.getId());

                if (deleteSelectedItems) {
                    purchaseRepo.deleteDraft(itemViewModel.getId(), itemViewModel.getBuyer());
                } else {
                    view.notifyItemChanged(i);
                }
            }
        }
    }
}
