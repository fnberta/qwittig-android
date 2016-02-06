/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.presentation.finance.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.FinanceCompsUnpaidViewModelImpl;


/**
 * Created by fabio on 20.01.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class FinanceCompsUnpaidViewModelImplTest {

    @Mock
    private Bundle mMockBundle;
    @Mock
    private FinanceCompsUnpaidViewModel.ViewListener mMockView;

    private FinanceCompsUnpaidViewModelImpl mViewModel;

    @Before
    public void setUp() throws Exception {
        mViewModel = new FinanceCompsUnpaidViewModelImpl(mMockBundle, mMockView,
                new ParseGroupRepository(),
                new ParseUserRepository(apiRepo),
                new ParseCompensationRepository());
    }
}