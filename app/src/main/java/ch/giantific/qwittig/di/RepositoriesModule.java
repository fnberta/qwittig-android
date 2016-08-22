/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import com.google.gson.Gson;

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
}
