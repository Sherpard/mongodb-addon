package org.seedstack.mongodb.morphia.internal;

import java.util.Collection;

import dev.morphia.Datastore;
import dev.morphia.DatastoreImpl;
import dev.morphia.EntityListener;

class DatastoreWrapper extends DatastoreImpl {

    public DatastoreWrapper(Datastore datastore, Collection<EntityListener<?>> extraListeners) {
        super((DatastoreImpl) datastore);
        // TODO: Find a way to wrap mapper instead of adding an interceptor
        extraListeners.forEach(listener -> getMapper().addInterceptor(listener));

    }

}
