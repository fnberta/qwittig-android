package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract.PurchaseResult;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentitiesItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditGenericItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditHeaderItemViewModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.rxwrapper.android.transitions.TransitionEvent;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Created by fabio on 26.09.16.
 */
public class PurchaseAddPresenter extends BasePresenterImpl<PurchaseAddEditContract.ViewListener>
        implements PurchaseAddEditContract.Presenter {

    private static final int FIXED_ROWS_COUNT = 6;
    protected final PurchaseAddEditViewModel viewModel;
    protected final PurchaseRepository purchaseRepo;
    protected final NumberFormat exchangeRateFormatter;
    protected final DateFormat dateFormatter;
    final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    protected NumberFormat moneyFormatter;
    protected Identity currentIdentity;
    protected List<Identity> identities;

    @Inject
    public PurchaseAddPresenter(@NonNull Navigator navigator,
                                @NonNull PurchaseAddEditViewModel viewModel,
                                @NonNull UserRepository userRepo,
                                @NonNull GroupRepository groupRepo,
                                @NonNull PurchaseRepository purchaseRepo,
                                @NonNull RemoteConfigHelper configHelper) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
        this.purchaseRepo = purchaseRepo;
        this.configHelper = configHelper;

        dateFormatter = DateUtils.getDateFormatter(false);
        exchangeRateFormatter = MoneyUtils.getExchangeRateFormatter();

        if (viewModel.getItemCount() == 0) {
            // first start, set some default values for view model
            initDefaultValues();
        } else {
            // not first start, currency is set, initialise formatter
            moneyFormatter = MoneyUtils.getMoneyFormatter(viewModel.getCurrency(), false, true);
        }
    }

    private void initDefaultValues() {
        final List<String> currencies = Arrays.asList(configHelper.getSupportedCurrencyCodes());
        viewModel.setSupportedCurrencies(currencies);
        final Date date = new Date();
        viewModel.setDate(date, dateFormatter.format(date));

        viewModel.addItem(new PurchaseAddEditHeaderItemViewModel(R.string.header_purchase));
        viewModel.addItem(PurchaseAddEditGenericItemViewModel.createNewDateInstance());
        viewModel.addItem(PurchaseAddEditGenericItemViewModel.createNewStoreInstance());
        viewModel.addItem(new PurchaseAddEditHeaderItemViewModel(R.string.header_articles));
        viewModel.addItem(PurchaseAddEditGenericItemViewModel.createNewAddRowInstance());
        viewModel.addItem(PurchaseAddEditGenericItemViewModel.createNewTotalInstance());
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        loadPurchase(currentUser);
    }

    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        subscriptions.add(getInitialChain(currentUser)
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> value) {
                        // fill with one article row if articles empty
                        if (viewModel.getItemCount() == FIXED_ROWS_COUNT) {
                            addArticleOnFirstRun();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load group identities with error:");
                        view.showMessage(R.string.toast_error_purchase_add_edit_load);
                    }
                })
        );
    }

    @NonNull
    protected final Single<List<Identity>> getInitialChain(@NonNull FirebaseUser currentUser) {
        return view.getEnterTransition()
                .filter(transitionEvent -> transitionEvent.getEventType() == TransitionEvent.EventType.END)
                .toSingle()
                .flatMap(transitionEvent -> view.showFab())
                .flatMap(fab -> userRepo.getUser(currentUser.getUid()))
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> {
                    currentIdentity = identity;
                    // if first run, set initial currency to group currency, create money formatter
                    // and set initial total and my share
                    if (TextUtils.isEmpty(viewModel.getCurrency())) {
                        setCurrencyOnFirstRun(identity.getGroupCurrency());
                    }
                })
                .flatMapObservable(identity -> groupRepo.getGroupIdentities(identity.getGroup(), true))
                .toSortedList()
                .toSingle()
                .doOnSuccess(identities -> this.identities = identities);
    }

    private void setCurrencyOnFirstRun(@NonNull String groupCurrency) {
        viewModel.setCurrency(groupCurrency, true);
        moneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, false, true);

        final String formatted = moneyFormatter.format(0);
        viewModel.setTotal(0, formatted);
        viewModel.setMyShare(formatted);
    }

    private void addArticleOnFirstRun() {
        final PurchaseAddEditArticleItemViewModel articleItem =
                new PurchaseAddEditArticleItemViewModel(getArticleIdentities());
        // addItemAtPosition right above 'addItemAtPosition article' row
        viewModel.addItemAtPosition(4, articleItem);
        view.notifyItemAdded(4);
    }

    final PurchaseAddEditArticleIdentityItemViewModel[] getArticleIdentities() {
        final Set<String> identitiesIds = new HashSet<>(identities.size());
        for (Identity identity : identities) {
            identitiesIds.add(identity.getId());
        }

        return getArticleIdentities(identitiesIds);
    }

    protected final PurchaseAddEditArticleIdentityItemViewModel[] getArticleIdentities(@NonNull Set<String> identities) {
        final int size = this.identities.size();
        final PurchaseAddEditArticleIdentityItemViewModel[] articleIdentities =
                new PurchaseAddEditArticleIdentityItemViewModel[size];
        for (int i = 0; i < size; i++) {
            final Identity identity = this.identities.get(i);
            final String id = identity.getId();
            articleIdentities[i] = new PurchaseAddEditArticleIdentityItemViewModel(id,
                    identity.getNickname(), identity.getAvatar(), identities.contains(id));
        }

        return articleIdentities;
    }

    @Override
    public void onDateClick(View view) {
        this.view.showDatePickerDialog();
    }

    @Override
    public void onDateSet(@NonNull Date date) {
        viewModel.setDate(date, dateFormatter.format(date));
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View v, int position, long id) {
        final String newCurrency = (String) parent.getItemAtPosition(position);
        final String currency = viewModel.getCurrency();
        if (TextUtils.isEmpty(currency) || Objects.equals(newCurrency, currency)) {
            return;
        }

        moneyFormatter = MoneyUtils.getMoneyFormatter(newCurrency, false, true);
        viewModel.setCurrency(newCurrency, false);
        viewModel.setExchangeRateAvailable(false);
        view.loadFetchExchangeRatesWorker(currentIdentity.getGroupCurrency(), newCurrency);

        // TODO: once we support currencies with other than 2 decimal values, update items, total and myShare
    }

    @Override
    public void onAddRowClick(@NonNull BasePurchaseAddEditItemViewModel itemViewModel) {
        final int position = viewModel.getPositionForItem(itemViewModel);
        final PurchaseAddEditArticleItemViewModel articleItem =
                new PurchaseAddEditArticleItemViewModel(getArticleIdentities());
        viewModel.addItemAtPosition(position, articleItem);
        view.notifyItemAdded(position);
        view.scrollToPosition(position + 1);
    }

    @Override
    public void onArticlePriceChanged(PurchaseAddEditArticleItemViewModel itemViewModel,
                                      CharSequence price) {
        final String priceString = price.toString();
        itemViewModel.setPrice(priceString, MoneyUtils.parsePrice(priceString, moneyFormatter));

        if (currentIdentity != null) {
            viewModel.updateTotalAndMyShare(currentIdentity.getId(), moneyFormatter);
        }
    }

    @Override
    public void onArticlePriceFocusChange(PurchaseAddEditArticleItemViewModel itemViewModel,
                                          boolean hasFocus) {
        if (!hasFocus) {
            final double priceParsed = itemViewModel.getPriceParsed();
            itemViewModel.setPrice(moneyFormatter.format(priceParsed), priceParsed);
        }
    }

    @Override
    public void onToggleIdentitiesClick(@NonNull PurchaseAddEditArticleItemViewModel itemViewModel) {
        final int insertPos = viewModel.getPositionForItem(itemViewModel) + 1;
        if (viewModel.getItemCount() < insertPos) {
            expandIdentityRow(insertPos, itemViewModel);
            collapseOtherIdentityRows(insertPos);
        } else if (viewModel.getItemAtPosition(insertPos).getViewType() == ViewType.IDENTITIES) {
            viewModel.removeItemAtPosition(insertPos);
            view.notifyItemRemoved(insertPos);
        } else {
            expandIdentityRow(insertPos, itemViewModel);
            collapseOtherIdentityRows(insertPos);
        }
    }

    private void expandIdentityRow(int pos, @NonNull BasePurchaseAddEditItemViewModel parentItem) {
        final PurchaseAddEditArticleItemViewModel articleItem =
                (PurchaseAddEditArticleItemViewModel) parentItem;

        viewModel.addItemAtPosition(pos, new PurchaseAddEditArticleIdentitiesItemViewModel(articleItem.getIdentities()));
        view.notifyItemAdded(pos);
    }

    private void collapseOtherIdentityRows(int insertPos) {
        int pos = 0;
        for (Iterator<BasePurchaseAddEditItemViewModel> iterator = viewModel.getItemsIterator(); iterator.hasNext(); ) {
            final int viewType = iterator.next().getViewType();
            if (viewType == ViewType.IDENTITIES && pos != insertPos) {
                iterator.remove();
                view.notifyItemRemoved(pos);
            }

            pos++;
        }
    }

    @Override
    public void onArticleRowIdentityClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel identityViewModel) {
        // toggle identity selection state
        identityViewModel.setSelected(!identityViewModel.isSelected());

        final int identityRowPos = viewModel.getOpenIdentityRowPosition();
        // notify selection state changed
        view.notifyItemIdentityChanged(identityRowPos, identityViewModel);

        // load and set appropriate toggle image
        final PurchaseAddEditArticleItemViewModel articleItem =
                (PurchaseAddEditArticleItemViewModel) viewModel.getItemAtPosition(identityRowPos - 1);
        articleItem.notifyPropertyChanged(BR.identities);

        // update total and my share
        viewModel.updateTotalAndMyShare(currentIdentity.getId(), moneyFormatter);
    }

    @Override
    public boolean onArticleRowIdentityLongClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel identityViewModel) {
        // toggle identity selection state
        identityViewModel.setSelected(!identityViewModel.isSelected());

        for (int i = 0, size = viewModel.getItemCount(); i < size; i++) {
            final BasePurchaseAddEditItemViewModel itemViewModel = viewModel.getItemAtPosition(i);
            final int viewType = itemViewModel.getViewType();
            switch (viewType) {
                case ViewType.IDENTITIES:
                    // notify selection state changed
                    view.notifyItemIdentityChanged(i, identityViewModel);
                    break;
                case ViewType.ARTICLE:
                    // load and set appropriate toggle image for all articles
                    final PurchaseAddEditArticleItemViewModel articleItem =
                            (PurchaseAddEditArticleItemViewModel) itemViewModel;
                    articleItem.toggleIdentity(identityViewModel);
                    articleItem.notifyPropertyChanged(BR.identities);
                    break;
            }
        }

        // update total and my share
        viewModel.updateTotalAndMyShare(currentIdentity.getId(), moneyFormatter);

        return true;
    }

    @Override
    public void onArticleDismiss(int position) {
        viewModel.removeItemAtPosition(position);
        // if identity row was open, removeItem it as well
        if (viewModel.getItemAtPosition(position).getViewType() == ViewType.IDENTITIES) {
            viewModel.removeItemAtPosition(position);
            view.notifyItemRangeRemoved(position, 2);
        } else {
            view.notifyItemRemoved(position);
        }

        viewModel.updateTotalAndMyShare(currentIdentity.getId(), moneyFormatter);
    }

    @Override
    public void onAddEditNoteMenuClick() {
        view.showAddEditNoteDialog(viewModel.getNote());
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        if (!Objects.equals(viewModel.getNote(), note)) {
            view.showMessage(TextUtils.isEmpty(viewModel.getNote())
                             ? R.string.toast_note_added
                             : R.string.toast_note_edited);
            viewModel.setNote(note);
            view.reloadOptionsMenu();
        }
    }

    @Override
    public void onDeleteNote() {
        viewModel.setNote("");
        view.reloadOptionsMenu();
        view.showMessage(R.string.toast_note_deleted);
    }

    @Override
    public void onAddEditReceiptImageMenuClick() {
        view.captureImage();
    }

    @Override
    public void onReceiptImageTaken(@NonNull String receiptImagePath) {
        view.showMessage(TextUtils.isEmpty(viewModel.getReceipt())
                         ? R.string.toast_receipt_added
                         : R.string.toast_receipt_changed);
        viewModel.setReceipt(receiptImagePath);
        view.reloadOptionsMenu();
    }

    @Override
    public void onReceiptImageTakeFailed() {
        view.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        deleteReceiptImage();
        viewModel.setReceipt("");

        view.showMessage(R.string.toast_receipt_deleted);
        view.reloadOptionsMenu();
    }

    private void deleteReceiptImage() {
        final File receipt = new File(viewModel.getReceipt());
        if (!receipt.delete()) {
            Timber.w("failed to delete receipt file");
        }
    }

    @Override
    public void onExchangeRateClick(View view) {
        this.view.showManualExchangeRateSelectorDialog(((TextView) view).getText().toString());
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull final String workerTag) {
        subscriptions.add(single
                .subscribe(new SingleSubscriber<Float>() {
                    @Override
                    public void onSuccess(Float exchangeRate) {
                        view.removeWorker(workerTag);
                        viewModel.setExchangeRateAvailable(true);
                        viewModel.setExchangeRate(exchangeRate,
                                exchangeRateFormatter.format(exchangeRate));
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        view.showMessageWithAction(R.string.toast_error_exchange_rate,
                                new MessageAction(R.string.action_retry) {
                                    @Override
                                    public void onClick(View v) {
                                        view.loadFetchExchangeRatesWorker(currentIdentity.getGroupCurrency(),
                                                viewModel.getCurrency());
                                    }
                                });
                    }
                })
        );
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        final BigDecimal roundedExchangeRate = MoneyUtils.roundExchangeRate(exchangeRate);
        final double doubleValue = roundedExchangeRate.doubleValue();
        viewModel.setExchangeRate(doubleValue, exchangeRateFormatter.format(doubleValue));
        viewModel.setExchangeRateAvailable(true);
    }

    @Override
    public void onSavePurchaseClick(View v) {
        if (!viewModel.isExchangeRateAvailable()) {
            view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!Objects.equals(viewModel.getCurrency(), currentIdentity.getGroupCurrency())
                && viewModel.getExchangeRate() == 1) {
            view.showMessageWithAction(R.string.toast_exchange_no_data,
                    new MessageAction(R.string.action_purchase_save_draft) {
                        @Override
                        public void onClick(View v) {
                            onSaveAsDraftMenuClick();
                        }
                    });
            return;
        }

        if (TextUtils.isEmpty(viewModel.getStore())) {
            view.showMessage(R.string.toast_store_empty);
            return;
        }

        if (viewModel.getItemCount() <= FIXED_ROWS_COUNT) {
            view.showMessage(R.string.toast_min_one_article);
            return;
        }

        if (!viewModel.isItemsValid()) {
            return;
        }

        startPurchaseSave(false);
    }

    @Override
    public void onSaveAsDraftMenuClick() {
        if (!viewModel.isExchangeRateAvailable()) {
            view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        startPurchaseSave(true);
    }

    private void startPurchaseSave(final boolean asDraft) {
        final Purchase purchase = getPurchase(asDraft);
        savePurchase(purchase, asDraft);
        onPurchaseSaved(asDraft);
    }

    @NonNull
    private Purchase getPurchase(final boolean isDraft) {
        final List<Article> articles = viewModel.getArticlesFromItems(moneyFormatter);
        final List<String> purchaseIdentities = new ArrayList<>();
        for (Article article : articles) {
            for (String identityId : article.getIdentitiesIds()) {
                if (!purchaseIdentities.contains(identityId)) {
                    purchaseIdentities.add(identityId);
                }
            }
        }

        final int fractionDigits = MoneyUtils.getFractionDigits(viewModel.getCurrency());
        return createPurchase(purchaseIdentities, articles, fractionDigits, isDraft);
    }

    @NonNull
    protected Purchase createPurchase(@NonNull List<String> purchaseIdentities,
                                      @NonNull List<Article> purchaseArticles,
                                      int fractionDigits, boolean isDraft) {
        final double total = convertRoundTotal(fractionDigits);
        return new Purchase(currentIdentity.getGroup(), currentIdentity.getId(),
                viewModel.getDate(), viewModel.getStore(), total, viewModel.getCurrency(),
                viewModel.getExchangeRate(), viewModel.getReceipt(), viewModel.getNote(), isDraft,
                null, purchaseIdentities, purchaseArticles);
    }

    final double convertRoundTotal(int fractionDigits) {
        final double total = viewModel.getTotal();
        final double exchangeRate = viewModel.getExchangeRate();
        if (exchangeRate == 1) {
            return new BigDecimal(total).setScale(fractionDigits,
                    BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        final double totalConverted = total * exchangeRate;
        return new BigDecimal(totalConverted).setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS,
                BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    protected void savePurchase(@NonNull Purchase purchase, boolean asDraft) {
        if (asDraft) {
            purchaseRepo.saveDraft(purchase, null);
        } else {
            purchaseRepo.savePurchase(purchase, null, currentIdentity.getUser(), false);
        }
    }

    protected void onPurchaseSaved(boolean asDraft) {
        navigator.finish(asDraft ? PurchaseResult.PURCHASE_DRAFT : PurchaseResult.PURCHASE_SAVED);
    }

    @Override
    public void onExitClick() {
        view.showPurchaseDiscardDialog();
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(PurchaseResult.PURCHASE_DISCARDED);
    }
}
