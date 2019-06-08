package ch.berta.fabio.fabspeeddial;

import android.support.annotation.NonNull;
import android.view.MenuItem;

/**
 * Created by fabio on 06.06.16.
 */
public interface FabMenuClickListener {
    void onFabMenuItemClicked(@NonNull MenuItem menuItem);

    void onFabCompleteClicked();
}
