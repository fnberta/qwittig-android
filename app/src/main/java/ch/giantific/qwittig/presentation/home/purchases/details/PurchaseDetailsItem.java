/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.annotation.SuppressLint;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Created by fabio on 29.01.16.
 */
public class PurchaseDetailsItem extends BaseObservable
        implements HeaderRowViewModel, PurchaseDetailsItemRowViewModel,
        PurchaseDetailsTotalRowViewModel, PurchaseDetailsMyShareRowViewModel,
        PurchaseDetailsNoteRowViewModel {

    @Type
    private int mType;
    @StringRes
    private int mHeader;
    private List<Identity> mIdentities;
    private Item mItem;
    private String mItemName;
    private String mItemPrice;
    private float mItemAlpha;
    private float mItemUserPercentage;
    private String mPurchaseTotal;
    private String mPurchaseTotalForeign;
    private String mPurchaseMyShare;
    private String mPurchaseMyShareForeign;
    private String mPurchaseNote;

    private PurchaseDetailsItem(@NonNull Item item, @NonNull Identity currentIdentity,
                                @NonNull String currency) {
        mType = Type.ITEM;

        mItem = item;
        mItemName = item.getName();
        mItemPrice = MoneyUtils.formatMoney(item.getPrice(), currency);

        final List<Identity> identities = item.getIdentities();
        if (identities.contains(currentIdentity)) {
            mItemUserPercentage = (1f / identities.size()) * 100;
            mItemAlpha = 1f;
        } else {
            mItemUserPercentage = 0;
            mItemAlpha = DISABLED_ALPHA;
        }
    }

    private PurchaseDetailsItem(@StringRes int header) {
        mType = Type.HEADER;
        mHeader = header;
    }

    private PurchaseDetailsItem(@NonNull List<Identity> identities) {
        mType = Type.USERS_INVOLVED;
        mIdentities = identities;
    }

    private PurchaseDetailsItem(@NonNull String value) {
        mPurchaseNote = value;
        mType = Type.NOTE;
    }

    @SuppressLint("SwitchIntDef")
    private PurchaseDetailsItem(@Type int type, @NonNull String value,
                                @NonNull String foreignValue) {
        mType = type;
        switch (type) {
            case Type.MY_SHARE:
                mPurchaseMyShare = value;
                mPurchaseMyShareForeign = foreignValue;
                break;
            case Type.TOTAL:
                mPurchaseTotal = value;
                mPurchaseTotalForeign = foreignValue;
                break;
        }
    }

    public static PurchaseDetailsItem createHeaderInstance(@StringRes int header) {
        return new PurchaseDetailsItem(header);
    }

    public static PurchaseDetailsItem createUsersInvolvedInstance(@NonNull List<Identity> identities) {
        return new PurchaseDetailsItem(identities);
    }

    public static PurchaseDetailsItem createItemInstance(@NonNull Item item,
                                                         @NonNull Identity currentIdentity,
                                                         @NonNull String currency) {
        return new PurchaseDetailsItem(item, currentIdentity, currency);
    }

    public static PurchaseDetailsItem createTotalInstance(@NonNull String purchaseTotal,
                                                          @NonNull String purchaseTotalForeign) {
        return new PurchaseDetailsItem(Type.TOTAL, purchaseTotal, purchaseTotalForeign);
    }

    public static PurchaseDetailsItem createMyShareInstance(@NonNull String purchaseMyShare,
                                                            @NonNull String purchaseMyShareForeign) {
        return new PurchaseDetailsItem(Type.MY_SHARE, purchaseMyShare, purchaseMyShareForeign);
    }

    public static PurchaseDetailsItem createNoteInstance(@NonNull String purchaseNote) {
        return new PurchaseDetailsItem(purchaseNote);
    }

    @Type
    public int getType() {
        return mType;
    }

    @Override
    @Bindable
    @StringRes
    public int getHeader() {
        return mHeader;
    }

    public List<Identity> getIdentities() {
        return mIdentities;
    }

    public Item getItem() {
        return mItem;
    }

    @Override
    @Bindable
    public String getItemName() {
        return mItemName;
    }

    @Override
    @Bindable
    public String getItemPrice() {
        return mItemPrice;
    }

    @Override
    @Bindable
    public float getItemAlpha() {
        return mItemAlpha;
    }

    @Override
    @Bindable
    public float getItemUserPercentage() {
        return mItemUserPercentage;
    }

    @Override
    @Bindable
    public String getPurchaseTotal() {
        return mPurchaseTotal;
    }

    @Override
    @Bindable
    public String getPurchaseTotalForeign() {
        return mPurchaseTotalForeign;
    }

    @Override
    @Bindable
    public String getPurchaseMyShare() {
        return mPurchaseMyShare;
    }

    @Override
    public String getPurchaseMyShareForeign() {
        return mPurchaseMyShareForeign;
    }

    @Override
    @Bindable
    public String getPurchaseNote() {
        return mPurchaseNote;
    }

    @IntDef({Type.ITEM, Type.HEADER, Type.USERS_INVOLVED, Type.TOTAL, Type.MY_SHARE, Type.NOTE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int HEADER = 0;
        int USERS_INVOLVED = 1;
        int ITEM = 2;
        int TOTAL = 3;
        int MY_SHARE = 4;
        int NOTE = 5;
    }
}
