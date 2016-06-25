package ch.giantific.qwittig.presentation.finance.unpaid.itemmodels;

import android.databinding.Bindable;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.CardTopProgressViewModel;

/**
 * Created by fabio on 25.06.16.
 */
public interface CompsUnpaidCompItemModel extends CompsUnpaidItemModel, CardTopProgressViewModel {

    Compensation getCompensation();

    String getId();

    @Bindable
    boolean isCredit();

    @Bindable
    String getCompAmount();

    BigFraction getCompAmountRaw();

    @Bindable
    String getCompUsername();

    @Bindable
    String getCompUserAvatar();

    @Bindable
    boolean isUserPending();

    void setItemLoading(boolean itemLoading);
}