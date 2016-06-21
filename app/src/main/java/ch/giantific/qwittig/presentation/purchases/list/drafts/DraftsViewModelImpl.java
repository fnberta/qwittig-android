/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.bus.events.EventDraftDeleted;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link DraftsViewModel}.
 */
public class DraftsViewModelImpl extends ListViewModelBaseImpl<Purchase, DraftsViewModel.ViewListener>
        implements DraftsViewModel {

    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_SELECTION_MODE = "STATE_SELECTION_MODE";
    private final Navigator mNavigator;
    private final PurchaseRepository mPurchaseRepo;
    private final ArrayList<String> mDraftsSelected;
    private boolean mSelectionModeEnabled;
    private boolean mDeleteSelectedItems;

    public DraftsViewModelImpl(@Nullable Bundle savedState,
                               @NonNull Navigator navigator,
                               @NonNull RxBus<Object> eventBus,
                               @NonNull UserRepository userRepository,
                               @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mPurchaseRepo = purchaseRepo;
        if (savedState != null) {
            mItems = new ArrayList<>();
            mDraftsSelected = savedState.getStringArrayList(STATE_DRAFTS_SELECTED);
            mSelectionModeEnabled = savedState.getBoolean(STATE_SELECTION_MODE, false);
        } else {
            mDraftsSelected = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_DRAFTS_SELECTED, mDraftsSelected);
        outState.putBoolean(STATE_SELECTION_MODE, mSelectionModeEnabled);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(Identity identity) {
                        return mPurchaseRepo.getPurchases(identity, true);
                    }
                })
                .subscribe(new Subscriber<Purchase>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        setLoading(false);
                        mListInteraction.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_drafts_load);
                    }

                    @Override
                    public void onNext(Purchase purchase) {
                        mItems.add(purchase);
                    }
                })
        );
    }

    @Override
    public void onReadyForSelectionMode() {
        if (!mSelectionModeEnabled) {
            return;
        }

        mView.startSelectionMode();
        if (!mDraftsSelected.isEmpty()) {
            // scroll to the first selected item
            final String firstSelected = mDraftsSelected.get(0);
            int firstPos = 0;
            for (int i = 0, size = mItems.size(); i < size; i++) {
                final Purchase draft = mItems.get(i);
                if (Objects.equals(firstSelected, draft.getTempId())) {
                    firstPos = i;
                    break;
                }
            }

            final int position = firstPos;
            mListInteraction.scrollToPosition(position);
        }
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
    public void onDraftRowClick(int position) {
        if (!mSelectionModeEnabled) {
            final Purchase draft = getItemAtPosition(position);
            mNavigator.startPurchaseEdit(draft);
        } else {
            toggleSelection(position);
            mView.setSelectionModeTitle(R.string.cab_title_selected, mDraftsSelected.size());
        }
    }

    @Override
    public void onDraftRowLongClick(int position) {
        if (!mSelectionModeEnabled) {
            toggleSelection(position);
            mView.startSelectionMode();
            mView.setSelectionModeTitle(R.string.cab_title_selected, mDraftsSelected.size());
            mSelectionModeEnabled = true;
        }
    }

    @Override
    public void toggleSelection(int position) {
        final Purchase draft = mItems.get(position);
        final String draftId = draft.getTempId();
        if (mDraftsSelected.contains(draftId)) {
            mDraftsSelected.remove(draftId);
        } else {
            mDraftsSelected.add(draftId);
        }

        mListInteraction.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final Purchase draft = mItems.get(i);
            if (isSelected(draft)) {
                mDraftsSelected.remove(draft.getTempId());

                if (mDeleteSelectedItems) {
                    deleteDraft(draft, i);
                } else {
                    mListInteraction.notifyItemChanged(i);
                }
            }
        }
    }

    private void deleteDraft(@NonNull Purchase draft, final int pos) {
        getSubscriptions().add(mPurchaseRepo.deleteDraft(draft)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase value) {
                        mItems.remove(pos);
                        mListInteraction.notifyItemRemoved(pos);
                        mEventBus.post(new EventDraftDeleted());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_draft_delete);
                    }
                })
        );
    }

    @Override
    public boolean isSelected(@NonNull Purchase draft) {
        return mDraftsSelected.contains(draft.getTempId());
    }
}
