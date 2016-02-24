/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.parse.ParseFile;

import ch.giantific.qwittig.domain.models.Purchase;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Saves an edited {@link Purchase} object and if there is a new receipt image, deletes the old
 * {@link ParseFile} if there was one.
 * <p/>
 * Subclass of {@link AddPurchaseSaveWorker}.
 */
public class EditPurchaseSaveWorker extends AddPurchaseSaveWorker {

    private ParseFile mReceiptParseFileOld;
    private boolean mDraft;
    private boolean mDeleteOldReceipt;

    public EditPurchaseSaveWorker() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link EditPurchaseSaveWorker} with a {@link Purchase} object, whether
     * the purchase was a draft or an already saved purchase and optionally the old receipt image
     * and the path to the new one as parameters.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link com.parse.ParseObject}.
     * Because the fragment  is retained across configuration changes, there is no risk that the
     * system will recreate it with the default empty constructor.
     *
     * @param purchase            the {@link Purchase} object to save
     * @param receiptImage        the new receipt image
     * @param receiptParseFileOld the old receipt image
     * @param draft               whether we are saving a draft or an already saved purchase
     */
    @SuppressLint("ValidFragment")
    private EditPurchaseSaveWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage,
                                   @Nullable ParseFile receiptParseFileOld,
                                   boolean deleteOldReceipt, boolean draft) {
        super(purchase, receiptImage);

        mDeleteOldReceipt = deleteOldReceipt;
        mReceiptParseFileOld = receiptParseFileOld;
        mDraft = draft;
    }

    /**
     * Attaches a new instance of a {@link EditPurchaseSaveWorker}.
     *
     * @param fm           the fragment manager to use for the transaction
     * @param purchase     the {@link Purchase} object to save
     * @param receiptImage the receipt image to attach to the purchase
     * @return a new instance of a {@link EditPurchaseSaveWorker}
     */
    public static EditPurchaseSaveWorker attach(@NonNull FragmentManager fm,
                                                @NonNull Purchase purchase,
                                                @Nullable byte[] receiptImage,
                                                @Nullable ParseFile receiptParseFileOld,
                                                boolean deleteOldReceipt, boolean draft) {
        EditPurchaseSaveWorker worker = (EditPurchaseSaveWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new EditPurchaseSaveWorker(purchase, receiptImage, receiptParseFileOld,
                    deleteOldReceipt, draft);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    @Override
    Observable<Purchase> getPurchaseObservable(@NonNull final String tag) {
        if (mReceiptImage != null) {
            if (mDraft) {
                return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, mReceiptImage, true).toObservable();
            }

            if (mReceiptParseFileOld != null) {
                return deleteOldReceiptFile()
                        .flatMap(new Func1<String, Single<? extends Purchase>>() {
                            @Override
                            public Single<? extends Purchase> call(String s) {
                                return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, mReceiptImage, false);
                            }
                        })
                        .toObservable();
            }

            return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, mReceiptImage, false).toObservable();
        }

        if (mDraft) {
            return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, null, true).toObservable();
        }

        if (mReceiptParseFileOld != null && mDeleteOldReceipt) {
            return deleteOldReceiptFile().flatMapObservable(new Func1<String, Observable<? extends Purchase>>() {
                @Override
                public Observable<? extends Purchase> call(String s) {
                    return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, null, false).toObservable();
                }
            });
        }

        return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, null, false).toObservable();

    }

    private Single<String> deleteOldReceiptFile() {
        final String fileName = mReceiptParseFileOld.getName();
        return mPurchaseRepo.deleteReceipt(fileName).doOnSuccess(new Action1<String>() {
            @Override
            public void call(String s) {
                mPurchase.removeReceiptParseFile();
            }
        });
    }
}