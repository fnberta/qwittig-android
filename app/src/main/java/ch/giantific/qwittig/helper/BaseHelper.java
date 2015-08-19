package ch.giantific.qwittig.helper;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by fabio on 10.12.14.
 */
public abstract class BaseHelper extends Fragment {

    private static final String LOG_TAG = BaseHelper.class.getSimpleName();

    public BaseHelper() {
        // empty default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }
}
