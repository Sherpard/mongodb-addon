/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.mongodb.morphia.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.Collection;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.seedstack.mongodb.morphia.MorphiaDatastore;
class MorphiaModule extends AbstractModule {
    private final Collection<MorphiaDatastore> morphiaDatastoresAnnotation;
    private final Morphia morphia;

    MorphiaModule(Collection<MorphiaDatastore> morphiaDatastoresAnnotation, Morphia morphia) {
        super();
        this.morphiaDatastoresAnnotation = morphiaDatastoresAnnotation;
        this.morphia = morphia;
    }

    @Override
    protected void configure() {
        bind(Morphia.class).toInstance(morphia);
        bind(DatastoreFactory.class);
        if (morphiaDatastoresAnnotation != null && !morphiaDatastoresAnnotation.isEmpty()) {
            for (MorphiaDatastore morphiaDatastore : morphiaDatastoresAnnotation) {
                DatastoreProvider datastoreProvider = new DatastoreProvider(morphiaDatastore);
                requestInjection(datastoreProvider);
                bind(Key.get(Datastore.class, morphiaDatastore)).toProvider(datastoreProvider).in(Scopes.SINGLETON);
            }
        }
        morphia.getMapper().getInterceptors().forEach(this::requestInjection);
    }
}
