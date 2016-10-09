package ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Created by fabio on 29.09.16.
 */

public class DraftsViewModel extends BaseObservable
        implements Parcelable, LoadingViewModel, EmptyViewModel {

    public static final String TAG = DraftsViewModel.class.getCanonicalName();
    public static final Creator<DraftsViewModel> CREATOR = new Creator<DraftsViewModel>() {
        @Override
        public DraftsViewModel createFromParcel(Parcel in) {
            return new DraftsViewModel(in);
        }

        @Override
        public DraftsViewModel[] newArray(int size) {
            return new DraftsViewModel[size];
        }
    };
    private final ArrayList<String> draftsSelected;
    private boolean empty;
    private boolean loading;
    private boolean selectionModeEnabled;

    public DraftsViewModel() {
        draftsSelected = new ArrayList<>();
        this.empty = true;
        this.loading = true;
    }

    private DraftsViewModel(Parcel in) {
        draftsSelected = in.createStringArrayList();
        empty = in.readByte() != 0;
        loading = in.readByte() != 0;
        selectionModeEnabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(draftsSelected);
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (selectionModeEnabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
    }

    public boolean isSelectionModeEnabled() {
        return selectionModeEnabled;
    }

    public void setSelectionModeEnabled(boolean selectionModeEnabled) {
        this.selectionModeEnabled = selectionModeEnabled;
    }

    public void addDraftSelected(@NonNull String draftId) {
        draftsSelected.add(draftId);
    }

    public void removeDraftSelected(@NonNull String draftId) {
        draftsSelected.remove(draftId);
    }

    public boolean isDraftSelected(@NonNull Purchase draft) {
        return draftsSelected.contains(draft.getId());
    }
}
