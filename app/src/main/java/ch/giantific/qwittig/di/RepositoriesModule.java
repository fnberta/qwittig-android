/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import ch.giantific.qwittig.data.repositories.FirebaseRemoteConfigRepository;
import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.data.repositories.ParseStatsRepository;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.RemoteConfigRepository;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementations to use for the repository interfaces and how to instantiate them.
 */
@Module
public class RepositoriesModule {

    public RepositoriesModule() {
    }

    @Provides
    Gson providesGson() {
        return new Gson();
    }

    @Provides
    GroupRepository providesGroupRepository() {
        return new ParseGroupRepository();
    }

    @Provides
    PurchaseRepository providesPurchaseRepository(@NonNull SharedPreferences sharedPreferences,
                                                  @NonNull ExchangeRates exchangeRates,
                                                  @NonNull ReceiptOcr receiptOcr,
                                                  @NonNull GcmNetworkManager networkManager) {
        return new ParsePurchaseRepository(sharedPreferences, exchangeRates, receiptOcr,
                networkManager);
    }

    @Provides
    UserRepository providesUserRepository(@NonNull GcmNetworkManager networkManager) {
        return new ParseUserRepository(networkManager);
    }

    @Provides
    CompensationRepository providesCompensationRepository() {
        return new ParseCompensationRepository();
    }

    @Provides
    TaskRepository providesTaskRepository() {
        return new ParseTaskRepository();
    }

    @Provides
    StatsRepository providesStatsRepository(@NonNull Gson gson) {
        return new ParseStatsRepository(gson);
    }

    @Provides
    RemoteConfigRepository providesRemoteConfigRepository(@NonNull FirebaseRemoteConfig remoteConfig) {
        return new FirebaseRemoteConfigRepository(remoteConfig);
    }
}
