package ch.giantific.qwittig.data.rest;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsResult {
    @SerializedName("pie")
    private PieStats pieStats;
    @SerializedName("bar")
    private BarStats barStats;

    public StatsResult(PieStats pieStats, BarStats barStats) {
        this.pieStats = pieStats;
        this.barStats = barStats;
    }

    public PieStats getPieStats() {
        return pieStats;
    }

    public BarStats getBarStats() {
        return barStats;
    }

    public static class PieStats {

        private Map<String, Float> stores;
        private Map<String, IdentityTotal> identities;
        private float total;

        public PieStats(Map<String, Float> stores, Map<String, IdentityTotal> identities, float total) {
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

    public static class BarStats {
        private Map<Long, Float> data;
        private String unit;
        private float average;

        public BarStats(Map<Long, Float> data, String unit, float average) {
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
