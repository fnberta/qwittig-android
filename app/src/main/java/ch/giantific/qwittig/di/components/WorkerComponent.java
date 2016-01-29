/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.workerfragments.OcrWorker;
import ch.giantific.qwittig.presentation.workerfragments.RatesWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.save.CompensationSaveWorker;
import ch.giantific.qwittig.presentation.workerfragments.save.PurchaseSaveWorker;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {RepositoriesModule.class})
public interface WorkerComponent {

    void inject(CompensationsUpdateWorker compensationsUpdateWorker);

    void inject(PurchaseSaveWorker purchaseSaveWorker);

    void inject(CompensationSaveWorker compensationSaveWorker);

    void inject(PurchasesUpdateWorker purchasesUpdateWorker);

    void inject(RatesWorker ratesWorker);

    void inject(OcrWorker ocrWorker);
}
