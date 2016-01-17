/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
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
    GroupRepository providesGroupRepository() {
        return new ParseGroupRepository();
    }

    @Provides
    UserRepository providesUserRepository() {
        return new ParseUserRepository();
    }

    @Provides
    TaskRepository providesTaskRepository() {
        return new ParseTaskRepository();
    }
}
