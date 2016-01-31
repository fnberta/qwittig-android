/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;

import static org.junit.Assert.assertTrue;


/**
 * Created by fabio on 20.01.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class FinanceCompsUnpaidViewModelImplTest {

    private FinanceCompsUnpaidViewModelImpl mViewModel;
    @Mock
    private Bundle mBundle;

    @Before
    public void setUp() throws Exception {
        mViewModel = new FinanceCompsUnpaidViewModelImpl(mBundle,
                new ParseGroupRepository(),
                new ParseUserRepository(),
                new ParseCompensationRepository());
    }
}