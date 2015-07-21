package ch.giantific.qwittig.data.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fabio on 12.10.14.
 */
@ParseClassName("Group")
public class Group extends ParseObject {

    public static final String CLASS = "Group";
    public static final String NAME = "name";
    public static final String CURRENCY = "currency";
    public static final String USERS_INVITED = "usersInvited";
    public static final String ROLE_PREFIX= "groupOf_";

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public void setCurrency(String currency) {
        put(CURRENCY, currency);
    }

    public List<String> getUsersInvited() {
        List<String> usersInvited = getList(USERS_INVITED);
        if (usersInvited == null) {
            return Collections.emptyList();
        }

        return usersInvited;
    }

    public void setUsersInvited(List<String> usersInvited) {
        put(USERS_INVITED, usersInvited);
    }

    public Group() {
        // A default constructor is required.
    }

    public Group(String name) {
        put(NAME, name);
    }

    public Group(String name, String currency) {
        put(NAME, name);
        put(CURRENCY, currency);
    }

    public void addUsersInvited(List<String> usersInvited) {
        addAllUnique(USERS_INVITED, usersInvited);
    }

    public void addUserInvited(String userInvited) {
        add(USERS_INVITED, userInvited);
    }

    public void removeUserInvited(String userInvited) {
        List<String> userList = new ArrayList<>();
        userList.add(userInvited);
        removeUsersInvited(userList);
    }

    public void removeUsersInvited(List<String> usersInvited) {
        removeAll(USERS_INVITED, usersInvited);
    }
}
