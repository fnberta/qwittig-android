/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.scopes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Defines a custom di scope indicating that the injected modules should live as long as a service.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerService {
}
