package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
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

    private static final String STATE_VIEW_MODEL = PurchaseAddEditViewModel.class.getCanonicalName();
    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_FETCHING_RATES = "STATE_FETCHING_RATES";
    private static final int FIXED_ROWS_COUNT = 6;
    protected final PurchaseAddEditViewModel viewModel;
    protected final PurchaseRepository purchaseRepo;
    protected final RemoteConfigHelper configHelper;
    protected final ArrayList<BasePurchaseAddEditItemViewModel> items;
    protected final NumberFormat exchangeRateFormatter;
    protected final DateFormat dateFormatter;
    private final GroupRepository groupRepo;
    protected List<Identity> identities;
    protected ListInteraction listInteraction;
    protected NumberFormat moneyFormatter;
    protected Identity currentIdentity;
    private boolean fetchingExchangeRates;

    public PurchaseAddPresenter(@Nullable Bundle savedState,
                                @NonNull Navigator navigator,
                                @NonNull UserRepository userRepo,
                                @NonNull GroupRepository groupRepo,
                                @NonNull PurchaseRepository purchaseRepo,
                                @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, userRepo);

        this.groupRepo = groupRepo;
        this.purchaseRepo = purchaseRepo;
        this.configHelper = configHelper;

        dateFormatter = DateUtils.getDateFormatter(false);
        exchangeRateFormatter = MoneyUtils.getExchangeRateFormatter();

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
            items = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            fetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
            //noinspection ConstantConditions
            moneyFormatter = MoneyUtils.getMoneyFormatter(viewModel.getCurrency(), false, true);
        } else {
            final List<String> currencies = Arrays.asList(configHelper.getSupportedCurrencyCodes());
            final Date date = new Date();
            viewModel = new PurchaseAddEditViewModel(currencies, false, date, dateFormatter.format(date));
            items = new ArrayList<>();
            initFixedRows();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
        outState.putParcelableArrayList(STATE_ROW_ITEMS, items);
        outState.putBoolean(STATE_FETCHING_RATES, fetchingExchangeRates);
    }

    private void initFixedRows() {
        items.add(new PurchaseAddEditHeaderItemViewModel(R.string.header_purchase));
        items.add(PurchaseAddEditGenericItemViewModel.createNewDateInstance());
        items.add(PurchaseAddEditGenericItemViewModel.createNewStoreInstance());
        items.add(new PurchaseAddEditHeaderItemViewModel(R.string.header_articles));
        items.add(PurchaseAddEditGenericItemViewModel.createNewAddRowInstance());
        items.add(PurchaseAddEditGenericItemViewModel.createNewTotalInstance());
    }

    @Override
    public PurchaseAddEditViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
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
                        // fill with one article row on first start
                        addArticleOnFirstRun();
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
                    setCurrencyOnFirstRun(identity.getGroupCurrency());
                })
                .flatMapObservable(identity -> groupRepo.getGroupIdentities(identity.getGroup(), true))
                .toSortedList()
                .toSingle()
                .doOnSuccess(identities -> this.identities = identities);
    }

    private void setCurrencyOnFirstRun(@NonNull String groupCurrency) {
        if (TextUtils.isEmpty(viewModel.getCurrency())) {
            viewModel.setCurrency(groupCurrency, true);
            moneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, false, true);

            final String formatted = moneyFormatter.format(0);
            viewModel.setTotal(0, formatted);
            viewModel.setMyShare(formatted);
        }
    }

    private void addArticleOnFirstRun() {
        if (getItemCount() == FIXED_ROWS_COUNT) {
            final PurchaseAddEditArticleItemViewModel articleItem =
                    new PurchaseAddEditArticleItemViewModel(getArticleIdentities());
            // add right above 'add article' row
            items.add(4, articleItem);
            listInteraction.notifyItemInserted(4);
        }
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
    public BasePurchaseAddEditItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
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
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final String newCurrency = (String) parent.getItemAtPosition(position);
        final String currency = viewModel.getCurrency();
        if (TextUtils.isEmpty(currency) || Objects.equals(newCurrency, currency)) {
            return;
        }

        moneyFormatter = MoneyUtils.getMoneyFormatter(newCurrency, false, true);
        // update my share currency field
        viewModel.setCurrency(newCurrency, false);
        // get new exchange rate
        this.view.loadFetchExchangeRatesWorker(currentIdentity.getGroupCurrency(), newCurrency);

        // TODO: once we support currencies with other than 2 decimal values, update items, total and myShare
    }

    @Override
    public void onAddRowClick(@NonNull BasePurchaseAddEditItemViewModel itemViewModel) {
        final int position = items.indexOf(itemViewModel);

        final PurchaseAddEditArticleItemViewModel articleItem =
                new PurchaseAddEditArticleItemViewModel(getArticleIdentities());
        items.add(position, articleItem);
        listInteraction.notifyItemInserted(position);
        listInteraction.scrollToPosition(position + 1);
    }

    @Override
    public void onArticleNameChanged(PurchaseAddEditArticleItemViewModel itemViewModel,
                                     CharSequence name) {
        itemViewModel.setName(name.toString());
    }

    @Override
    public void onArticlePriceChanged(PurchaseAddEditArticleItemViewModel itemViewModel,
                                      CharSequence price) {
        final String priceString = price.toString();
        itemViewModel.setPrice(priceString, MoneyUtils.parsePrice(priceString, moneyFormatter));

        if (currentIdentity != null) {
            updateTotalAndMyShare();
        }
    }

    private void updateTotalAndMyShare() {
        double total = 0;
        double myShare = 0;
        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            final double itemPrice = articleItem.getPriceParsed();

            // update total price
            total += itemPrice;

            // update my share
            final PurchaseAddEditArticleIdentityItemViewModel[] articleIdentities =
                    articleItem.getIdentities();
            int selectedCount = 0;
            boolean currentIdentityInvolved = false;
            for (PurchaseAddEditArticleIdentityItemViewModel articleIdentity : articleIdentities) {
                if (!articleIdentity.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (Objects.equals(articleIdentity.getIdentityId(), currentIdentity.getId())) {
                    currentIdentityInvolved = true;
                }
            }
            if (currentIdentityInvolved) {
                myShare += (itemPrice / selectedCount);
            }
        }

        viewModel.setTotal(total, moneyFormatter.format(total));
        viewModel.setMyShare(moneyFormatter.format(myShare));
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
        final int insertPos = items.indexOf(itemViewModel) + 1;
        if (items.size() < insertPos) {
            expandArticleRow(insertPos, itemViewModel);
        } else if (getItemAtPosition(insertPos).getViewType() == ViewType.IDENTITIES) {
            collapseArticleRow(insertPos);
        } else {
            expandArticleRow(insertPos, itemViewModel);
        }
    }

    private void expandArticleRow(int pos, @NonNull BasePurchaseAddEditItemViewModel parentItem) {
        final PurchaseAddEditArticleItemViewModel articleItem =
                (PurchaseAddEditArticleItemViewModel) parentItem;
        items.add(pos, new PurchaseAddEditArticleIdentitiesItemViewModel(articleItem.getIdentities()));
        listInteraction.notifyItemInserted(pos);

        collapseOtherItemRows(pos);
    }

    private void collapseArticleRow(int pos) {
        items.remove(pos);
        listInteraction.notifyItemRemoved(pos);
    }

    private void collapseOtherItemRows(int insertPos) {
        int pos = 0;
        for (Iterator<BasePurchaseAddEditItemViewModel> iterator =
             items.iterator(); iterator.hasNext(); ) {
            final int type = iterator.next().getViewType();
            if (type == ViewType.IDENTITIES && pos != insertPos) {
                iterator.remove();
                listInteraction.notifyItemRemoved(pos);
            }

            pos++;
        }
    }

    @Override
    public void onArticleRowIdentityClick() {
        for (int i = 0, mItemsSize = items.size(); i < mItemsSize; i++) {
            final BasePurchaseAddEditItemViewModel itemViewModel = items.get(i);
            if (itemViewModel.getViewType() != ViewType.IDENTITIES) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) getItemAtPosition(i - 1);
            articleItem.notifyPropertyChanged(BR.identities);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onArticleRowIdentityLongClick(@NonNull PurchaseAddEditArticleIdentityItemViewModel userClicked) {
        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            articleItem.toggleIdentity(userClicked);
            articleItem.notifyPropertyChanged(BR.identities);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onArticleDismiss(int position) {
        items.remove(position);
        if (getItemAtPosition(position).getViewType() != ViewType.IDENTITIES) {
            listInteraction.notifyItemRemoved(position);
        } else {
            items.remove(position);
            listInteraction.notifyItemRangeRemoved(position, 2);
        }

        updateTotalAndMyShare();
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
                        fetchingExchangeRates = false;

                        viewModel.setExchangeRate(exchangeRate,
                                exchangeRateFormatter.format(exchangeRate));
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        fetchingExchangeRates = false;

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
    }

    @Override
    public void onSavePurchaseClick(View view) {
        if (fetchingExchangeRates) {
            this.view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!Objects.equals(viewModel.getCurrency(), currentIdentity.getGroupCurrency())
                && viewModel.getExchangeRate() == 1) {
            this.view.showMessageWithAction(R.string.toast_exchange_no_data,
                    new MessageAction(R.string.action_purchase_save_draft) {
                        @Override
                        public void onClick(View v) {
                            onSaveAsDraftMenuClick();
                        }
                    });
            return;
        }

        if (TextUtils.isEmpty(viewModel.getStore())) {
            this.view.showMessage(R.string.toast_store_empty);
            return;
        }

        if (!validateItems()) {
            return;
        }

        startPurchaseSave(false);
    }

    @Override
    public void onSaveAsDraftMenuClick() {
        if (fetchingExchangeRates) {
            view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        startPurchaseSave(true);
    }

    private boolean validateItems() {
        boolean hasItem = false;
        boolean allValid = true;
        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            hasItem = true;
            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            if (!articleItem.isInputValid()) {
                allValid = false;
            }
        }

        if (!hasItem) {
            view.showMessage(R.string.toast_min_one_article);
            return false;
        }

        return allValid;
    }

    private void startPurchaseSave(final boolean asDraft) {
        final Purchase purchase = getPurchase(asDraft);
        savePurchase(purchase, asDraft);
        onPurchaseSaved(asDraft);
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

    @NonNull
    private Purchase getPurchase(final boolean isDraft) {
        final List<String> purchaseIdentities = new ArrayList<>();
        final List<Article> purchaseArticles = new ArrayList<>();
        final int fractionDigits = MoneyUtils.getFractionDigits(viewModel.getCurrency());

        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            final String itemName = articleItem.getName();
            final String name = itemName != null ? itemName : "";
            final String price = articleItem.getPrice();

            final List<String> identities = new ArrayList<>();
            for (PurchaseAddEditArticleIdentityItemViewModel row : articleItem.getIdentities()) {
                if (row.isSelected()) {
                    final String identityId = row.getIdentityId();
                    identities.add(identityId);
                    if (!purchaseIdentities.contains(identityId)) {
                        purchaseIdentities.add(identityId);
                    }
                }
            }

            final double priceRounded = convertRoundItemPrice(price, fractionDigits);
            final Article article = new Article(name, priceRounded, identities);
            purchaseArticles.add(article);
        }

        return createPurchase(purchaseIdentities, purchaseArticles, fractionDigits, isDraft);
    }

    private double convertRoundItemPrice(@NonNull String priceText, int fractionDigits) {
        final double exchangeRate = viewModel.getExchangeRate();
        try {
            final double price = new BigDecimal(priceText)
                    .setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            if (exchangeRate == 1) {
                return price;
            }

            return new BigDecimal(price * exchangeRate)
                    .setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        } catch (NumberFormatException e) {
            try {
                final double parsed = moneyFormatter.parse(priceText).doubleValue();
                if (exchangeRate == 1) {
                    return new BigDecimal(parsed)
                            .setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                }

                return new BigDecimal(parsed * exchangeRate)
                        .setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
            } catch (ParseException e1) {
                return 0;
            }
        }
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

    @Override
    public void onExitClick() {
        view.showPurchaseDiscardDialog();
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(PurchaseResult.PURCHASE_DISCARDED);
    }
}
