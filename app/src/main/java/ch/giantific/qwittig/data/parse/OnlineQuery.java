package ch.giantific.qwittig.data.parse;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 31.12.14.
 */
public class OnlineQuery {

    public static final int QUERY_ITEMS_PER_PAGE = 15;
    public static final String DATE_CREATED = "createdAt";
    public static final String DATE_UPDATED = "updatedAt";

    private static final String LOG_TAG = OnlineQuery.class.getSimpleName();

    private OnlineQuery() {
        // Class cannot be instantiated
    }

    public static ParseQuery<ParseObject> getPurchasesQuery() {
        User currentUser = (User) ParseUser.getCurrentUser();

        ParseQuery<ParseObject> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, currentUser);

        ParseQuery<ParseObject> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.USERS_INVOLVED, currentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.setLimit(QUERY_ITEMS_PER_PAGE);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    public static ParseQuery<ParseObject> getCompensationsQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.orderByDescending(DATE_CREATED);
        return query;
    }
}