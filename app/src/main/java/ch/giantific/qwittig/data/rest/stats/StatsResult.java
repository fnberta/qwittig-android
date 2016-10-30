package ch.giantific.qwittig.data.rest.stats;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsResult {

    @SerializedName("group")
    private GroupStats groupStats;
    @SerializedName("user")
    private UserStats userStats;

    public StatsResult(GroupStats groupStats, UserStats userStats) {
        this.groupStats = groupStats;
        this.userStats = userStats;
    }

    public GroupStats getGroupStats() {
        return groupStats;
    }

    public UserStats getUserStats() {
        return userStats;
    }

    @StringDef({UnitType.DAYS, UnitType.MONTHS, UnitType.YEARS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnitType {
        String DAYS = "days";
        String MONTHS = "months";
        String YEARS = "years";
    }

    public static class GroupStats {
        private Pie pie;
        private Bar bar;

        public GroupStats(Pie pie, Bar bar) {
            this.pie = pie;
            this.bar = bar;
        }

        public Pie getPie() {
            return pie;
        }

        public Bar getBar() {
            return bar;
        }
    }

    public static class UserStats {
        private Pie pie;
        private Bar bar;

        public UserStats(Pie pie, Bar bar) {
            this.pie = pie;
            this.bar = bar;
        }

        public Pie getPie() {
            return pie;
        }

        public Bar getBar() {
            return bar;
        }
    }

    public static class Pie {

        private Map<String, Float> stores;
        private Map<String, IdentityTotal> identities;
        private float total;

        public Pie(Map<String, Float> stores, Map<String, IdentityTotal> identities, float total) {
            this.stores = stores;
            this.identities = identities;
            this.total = total;
        }

        public Map<String, Float> getStores() {
            return stores;
        }

        public Map<String, IdentityTotal> getIdentities() {
            return identities;
        }

        public float getTotal() {
            return total;
        }

        public static class IdentityTotal {
            private String nickname;
            private float total;

            public IdentityTotal(String nickname, float total) {
                this.nickname = nickname;
                this.total = total;
            }

            public String getNickname() {
                return nickname;
            }

            public float getTotal() {
                return total;
            }
        }
    }

    public static class Bar {
        private Map<Long, Float> data;
        private String unit;
        private float average;

        public Bar(Map<Long, Float> data, String unit, float average) {
            this.data = data;
            this.unit = unit;
            this.average = average;
        }

        public Map<Long, Float> getData() {
            return data;
        }

        public String getUnit() {
            return unit;
        }

        public float getAverage() {
            return average;
        }
    }
}
