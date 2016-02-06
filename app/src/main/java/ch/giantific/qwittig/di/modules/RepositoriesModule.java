/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import ch.giantific.qwittig.data.repositories.ParseApiRepository;
import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParseIdentityRepository;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
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
    ApiRepository providesApiRepository(@NonNull Gson gson) {
        return new ParseApiRepository(gson);
    }

    @Provides
    GroupRepository providesGroupRepository() {
        return new ParseGroupRepository();
    }

    @Provides
    PurchaseRepository providesPurchaseRepository(@NonNull SharedPreferences sharedPreferences,
                                                  @NonNull ExchangeRates exchangeRates) {
        return new ParsePurchaseRepository(sharedPreferences, exchangeRates);
    }

    @Provides
    IdentityRepository providesIdentityRepositors() {
        return new ParseIdentityRepository();
    }

    @Provides
    UserRepository providesUserRepository(@NonNull ApiRepository apiRepository) {
        return new ParseUserRepository(apiRepository);
    }

    @Provides
    CompensationRepository providesCompensationRepository() {
        return new ParseCompensationRepository();
    }

    @Provides
    TaskRepository providesTaskRepository() {
        return new ParseTaskRepository();
    }
}
