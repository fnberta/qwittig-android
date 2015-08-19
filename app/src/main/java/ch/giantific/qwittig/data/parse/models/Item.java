package ch.giantific.qwittig.data.parse.models;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Created by fabio on 12.10.14.
 */
@ParseClassName("Item")
public class Item extends ParseObject {

    public static final String CLASS = "Item";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String USERS_INVOLVED = "usersInvolved";

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public void setPrice(Number finalPrice) {
        put(PRICE, finalPrice);
    }

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void setUsersInvolved(List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
    }

    public Item() {
        // A default constructor is required.
    }

    public Item(String name, BigDecimal price, List<ParseUser> usersInvolved) {
        this(name, price);
        setUsersInvolved(usersInvolved);
    }

    public Item(String name, BigDecimal price) {
        setName(name);
        setPrice(price);
        setAccessRights(getCurrentGroup());
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    private Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        return currentUser.getCurrentGroup();
    }

    public double getPriceForeign(float exchangeRate) {
        double price = getPrice();
        if (exchangeRate == 1) {
            return price;
        }

        return price / exchangeRate;
    }

    public void convertPrice(float exchangeRate, boolean toGroupCurrency) {
        double price = getPrice();
        double priceConverted = toGroupCurrency ? price * exchangeRate : price / exchangeRate;
        setPrice(MoneyUtils.roundToFractionDigits(4, priceConverted));
    }

    public List<String> getUsersInvolvedIds() {
        List<String> listIds = new ArrayList<>();
        List<ParseUser> list = getUsersInvolved();
        for (ParseUser user : list) {
            listIds.add(user.getObjectId());
        }
        return listIds;
    }
}
