package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber2;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;
import rx.SingleSubscriber;
import timber.log.Timber;

public class CompsUnpaidPresenter extends BasePresenterImpl<CompsUnpaidContract.ViewListener>
        implements CompsUnpaidContract.Presenter {

    private final CompsUnpaidViewModel viewModel;
    private final CompensationRepository compsRepo;
    private NumberFormat moneyFormatter;
    private String currentGroupId;
    private String groupCurrency;

    @Inject
    public CompsUnpaidPresenter(@NonNull Navigator navigator,
                                @NonNull CompsUnpaidViewModel viewModel,
                                @NonNull UserRepository userRepo,
                                @NonNull CompensationRepository compsRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.compsRepo = compsRepo;
    }

    @Override
    public int compareItemViewModels(@NonNull CompUnpaidItemViewModel item1,
                                     @NonNull CompUnpaidItemViewModel item2) {
        return item1.compareTo(item2);
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

    private void addDataListener(@NonNull final String identityId, @NonNull String groupId) {
        final Observable<List<CompUnpaidItemViewModel>> initialData = compsRepo.getCompensations(groupId, identityId, false)
                .flatMap(compensation -> getItemViewModel(compensation, EventType.NONE, identityId))
                .toList()
                .doOnNext(compUnpaidItemViewModels -> {
                    view.addItems(compUnpaidItemViewModels);
                    viewModel.setEmpty(view.isItemsEmpty());
                    viewModel.setLoading(false);
                });
        subscriptions.add(compsRepo.observeCompensationChildren(groupId, identityId, false)
                .skipUntil(initialData)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(new ChildEventSubscriber2<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_comps_load)))
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
    public void onConfirmButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel) {
        final BigFraction amount = itemViewModel.getAmountFraction();
        viewModel.setConfirmingId(itemViewModel.getId());
        view.showConfirmAmountDialog(amount, itemViewModel.getNickname(), groupCurrency);
    }

    @Override
    public void onAmountConfirmed(double confirmedAmount) {
        final CompUnpaidItemViewModel item = view.getItemForId(viewModel.getConfirmingId());
        BigFraction amount = item.getAmountFraction();
        boolean amountChanged = false;
        if (Math.abs(amount.doubleValue() - confirmedAmount) >= MoneyUtils.MIN_DIFF) {
            amount = new BigFraction(confirmedAmount);
            amountChanged = true;
        }

        confirmCompensation(item, amount, amountChanged);
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
