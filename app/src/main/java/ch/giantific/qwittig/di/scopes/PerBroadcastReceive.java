/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.scopes;

import javax.inject.Scope;

/**
 * Defines a custom di scope indicating that the injected modules should live as long as an
 * onReceive() broadcast call.
 */
@Scope
public @interface PerBroadcastReceive {
}
