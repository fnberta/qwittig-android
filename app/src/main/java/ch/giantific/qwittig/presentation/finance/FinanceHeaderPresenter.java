package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 29.09.16.
 */
public class FinanceHeaderPresenter extends BasePresenterImpl<FinanceHeaderContract.ViewListener>
        implements FinanceHeaderContract.Presenter {

    private final FinanceHeaderViewModel viewModel;
    private NumberFormat moneyFormatter;

    @Inject
    public FinanceHeaderPresenter(@NonNull Navigator navigator,
                                  @NonNull FinanceHeaderViewModel viewModel,
                                  @NonNull UserRepository userRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(userRepo::observeIdentity)
                .doOnNext(identity -> moneyFormatter =
                        MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, true))
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_balance_load);
                    }

                    @Override
                    public void onNext(Identity identity) {
                        final BigFraction balance = identity.getBalanceFraction();
                        viewModel.setBalance(moneyFormatter.format(balance));
                        view.setColorTheme(balance);
                        view.startEnterTransition();
                    }
                })
        );
    }
}
