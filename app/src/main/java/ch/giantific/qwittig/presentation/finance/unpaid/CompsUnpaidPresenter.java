package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import timber.log.Timber;

public class CompsUnpaidPresenter extends BasePresenterImpl<CompsUnpaidContract.ViewListener>
        implements CompsUnpaidContract.Presenter {

    private static final String STATE_VIEW_MODEL = CompsUnpaidViewModel.class.getCanonicalName();
    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private final CompsUnpaidViewModel viewModel;
    private final SortedList<CompUnpaidItemViewModel> items;
    private final SortedListCallback<CompUnpaidItemViewModel> listCallback;
    private final CompensationRepository compsRepo;
    private boolean initialDataLoaded;
    private String currentGroupId;
    private String compConfirmingId;
    private String groupCurrency;
    private NumberFormat moneyFormatter;

    public CompsUnpaidPresenter(@Nullable Bundle savedState,
                                @NonNull Navigator navigator,
                                @NonNull UserRepository userRepo,
                                @NonNull CompensationRepository compsRepo) {
        super(savedState, navigator, userRepo);

        this.compsRepo = compsRepo;
        listCallback = new SortedListCallback<CompUnpaidItemViewModel>() {
            @Override
            public int compare(CompUnpaidItemViewModel o1, CompUnpaidItemViewModel o2) {
                return o1.compareTo(o2);
            }
        };
        items = new SortedList<>(CompUnpaidItemViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
            compConfirmingId = savedState.getString(STATE_COMP_CHANGE_AMOUNT);
        } else {
            viewModel = new CompsUnpaidViewModel(true);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
        if (!TextUtils.isEmpty(compConfirmingId)) {
            outState.putString(STATE_COMP_CHANGE_AMOUNT, compConfirmingId);
        }
    }

    @Override
    public CompsUnpaidViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupCurrency = identity.getGroupCurrency();
                        moneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, true, true);

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

    private void addDataListener(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(compsRepo.observeCompensationChildren(groupId, identityId, false)
                .filter(compensationRxChildEvent -> initialDataLoaded)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(new ChildEventSubscriber<>(items, viewModel, e ->
                        view.showMessage(R.string.toast_error_comps_load)))
        );
    }

    private void loadInitialData(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(compsRepo.getCompensations(groupId, identityId, false)
                .takeWhile(compensation -> Objects.equals(compensation.getGroup(), currentGroupId))
                .flatMap(compensation -> getItemViewModel(compensation, EventType.NONE, identityId))
                .toList()
                .subscribe(new Subscriber<List<CompUnpaidItemViewModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        viewModel.setEmpty(getItemCount() == 0);
                        viewModel.setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "failed to load initial unpaid compensations with error:");
                        view.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(List<CompUnpaidItemViewModel> compUnpaidItemViewModels) {
                        items.addAll(compUnpaidItemViewModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompUnpaidItemViewModel> getItemViewModel(@NonNull final Compensation compensation,
                                                                 final int eventType,
                                                                 @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(identity -> new CompUnpaidItemViewModel(eventType, compensation, identity,
                        moneyFormatter, isCredit))
                .toObservable();
    }

    @Override
    public CompUnpaidItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onConfirmButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel) {
        final BigFraction amount = itemViewModel.getAmountFraction();
        compConfirmingId = itemViewModel.getId();
        view.showCompensationAmountConfirmDialog(amount, itemViewModel.getNickname(), groupCurrency);
    }

    @Override
    public void onAmountConfirmed(double confirmedAmount) {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            final CompUnpaidItemViewModel itemViewModel = items.get(i);
            if (itemViewModel.getViewType() != CompUnpaidItemViewModel.ViewType.CREDIT
                    || !Objects.equals(itemViewModel.getId(), compConfirmingId)) {
                continue;
            }

            BigFraction amount = itemViewModel.getAmountFraction();
            boolean amountChanged = false;
            if (Math.abs(amount.doubleValue() - confirmedAmount) >= MoneyUtils.MIN_DIFF) {
                amount = new BigFraction(confirmedAmount);
                amountChanged = true;
            }
            confirmCompensation(itemViewModel, amount, amountChanged);
            return;
        }
    }

    private void confirmCompensation(@NonNull CompUnpaidItemViewModel itemViewModel,
                                     @NonNull BigFraction amount, final boolean amountChanged) {
        subscriptions.add(compsRepo.confirmAmountAndAccept(itemViewModel.getId(), amount, amountChanged)
                .subscribe(new SingleSubscriber<Compensation>() {
                    @Override
                    public void onSuccess(Compensation compensation) {
                        view.showMessage(R.string.toast_compensation_accepted);
                        if (amountChanged) {
                            // new compensations are calculated on the server
                            viewModel.setLoading(true);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to confirm compensation with error:");
                        view.showMessage(R.string.toast_error_comps_paid);
                    }
                })
        );
    }

    @Override
    public void onRemindButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel) {
        final String nickname = itemViewModel.getNickname();
        if (itemViewModel.isPending()) {
            view.showMessage(R.string.toast_remind_pending, nickname);
        } else {
            compsRepo.remindDebtor(itemViewModel.getId());
            view.showMessage(R.string.toast_compensation_reminded_user, nickname);
        }
    }
}
