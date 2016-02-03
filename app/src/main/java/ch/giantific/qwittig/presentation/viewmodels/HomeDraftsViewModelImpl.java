/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 22.01.16.
 */
public class HomeDraftsViewModelImpl extends ListViewModelBaseImpl<Purchase, HomeDraftsViewModel.ViewListener>
        implements HomeDraftsViewModel {

    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_SELECTION_MODE = "STATE_SELECTION_MODE";
    private PurchaseRepository mPurchaseRepo;
    private ArrayList<String> mDraftsSelected;
    private boolean mSelectionModeEnabled;
    private boolean mDeleteSelectedItems;

    public HomeDraftsViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull GroupRepository groupRepo,
                                   @NonNull UserRepository userRepository,
                                   @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, groupRepo, userRepository);

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
    public void updateList() {
        mSubscriptions.add(mGroupRepo.fetchGroupDataAsync(mCurrentGroup)
                .toObservable()
                .flatMap(new Func1<Group, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(Group group) {
                        return mPurchaseRepo.getPurchasesLocalAsync(mCurrentUser, mCurrentGroup, true);
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
                        mView.notifyDataSetChanged();
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
    public int getItemViewType(int position) {
        throw new UnsupportedOperationException("There is only one view type for this view");
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
                if (firstSelected.equals(draft.getDraftId())) {
                    firstPos = i;
                    break;
                }
            }

            final int position = firstPos;
            mView.scrollToPosition(position);
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
            mView.startPurchaseEditActivity(draft);
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
        final String draftId = draft.getDraftId();
        if (mDraftsSelected.contains(draftId)) {
            mDraftsSelected.remove(draftId);
        } else {
            mDraftsSelected.add(draftId);
        }

        mView.notifyItemChanged(position);
    }

    @Override
    public void clearSelection() {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final Purchase draft = mItems.get(i);
            if (isSelected(draft)) {
                mDraftsSelected.remove(draft.getDraftId());

                if (mDeleteSelectedItems) {
                    draft.unpinInBackground();
                    mItems.remove(i);
                    mView.notifyItemRemoved(i);
                } else {
                    mView.notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public boolean isSelected(@NonNull Purchase draft) {
        return mDraftsSelected.contains(draft.getDraftId());
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        updateList();
    }
}
