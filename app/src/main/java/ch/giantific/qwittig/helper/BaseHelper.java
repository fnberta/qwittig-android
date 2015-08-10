package ch.giantific.qwittig.helper;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.leakcanary.RefWatcher;

import java.util.List;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;

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
