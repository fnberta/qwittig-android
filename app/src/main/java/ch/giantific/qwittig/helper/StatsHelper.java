package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.stats.models.Stats;

/**
 * Created by fabio on 10.12.14.
 */
public class StatsHelper extends BaseHelper {

    @IntDef({TYPE_SPENDING, TYPE_STORES, TYPE_CURRENCIES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatsType {}
    public static final int TYPE_SPENDING = 1;
    public static final int TYPE_STORES = 2;
    public static final int TYPE_CURRENCIES = 3;
    private static final String STATS_TYPE = "STATS_type";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String LOG_TAG = StatsHelper.class.getSimpleName();
    private HelperInteractionListener mListener;
    private int mStatsType;

    public StatsHelper() {
        // empty default constructor
    }

    public static StatsHelper newInstance(@StatsType int statsType, String year, int month) {
        StatsHelper fragment = new StatsHelper();
        Bundle args = new Bundle();
        args.putInt(STATS_TYPE, statsType);
        args.putString(YEAR, year);
        args.putInt(MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String year = "";
        int month = 0;
        Bundle args = getArguments();
        if (args != null) {
            mStatsType = args.getInt(STATS_TYPE);
            year = args.getString(YEAR);
            month = args.getInt(MONTH);
        }

        if (TextUtils.isEmpty(year)) {
            return;
        }

        String groupId = getCurrentGroupId();
        switch (mStatsType) {
            case TYPE_SPENDING:
                calcStatsSpending(groupId, year, month);
                break;
            case TYPE_STORES:
                calcStatsStores(groupId, year, month);
                break;
            case TYPE_CURRENCIES:
                calcStatsCurrencies(groupId, year, month);
                break;
        }
    }

    private String getCurrentGroupId() {
        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();
        return currentGroup.getObjectId();
    }

    private void calcStatsSpending(String groupId, String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(CloudCode.STATS_SPENDING, params, new FunctionCallback<String>() {
            @Override
            public void done(String dataJson, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                if (mListener != null) {
                    Stats stats = parseJson(dataJson);
                    mListener.onStatsCalculated(mStatsType, stats);
                }
            }
        });
    }

    private void calcStatsStores(String groupId, String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(CloudCode.STATS_STORES, params, new FunctionCallback<String>() {
            @Override
            public void done(String dataJson, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                if (mListener != null) {
                    Stats stats = parseJson(dataJson);
                    mListener.onStatsCalculated(mStatsType, stats);                }
            }
        });
    }

    private void calcStatsCurrencies(String groupId, String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(CloudCode.STATS_CURRENCIES, params, new FunctionCallback<String>() {
            @Override
            public void done(String dataJson, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                if (mListener != null) {
                    Stats stats = parseJson(dataJson);
                    mListener.onStatsCalculated(mStatsType, stats);
                }
            }
        });
    }

    @NonNull
    private Map<String, Object> getStatsPushParams(String groupId, String year, int month) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        params.put(CloudCode.PARAM_YEAR, year);
        if (month != 0) {
            params.put(CloudCode.PARAM_MONTH, month - 1);
        }
        return params;
    }

    private Stats parseJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Stats.class);
    }
    

    private void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onFailedToCalculateStats(mStatsType, e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onStatsCalculated(int statsType, Stats stats);

        void onFailedToCalculateStats(int statsType, ParseException e);
    }
}
