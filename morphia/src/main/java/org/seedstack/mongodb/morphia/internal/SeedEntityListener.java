/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.bson.Document;
import org.seedstack.mongodb.morphia.EntityListener;

import dev.morphia.Datastore;
import dev.morphia.annotations.PostLoad;
import dev.morphia.annotations.PostPersist;
import dev.morphia.annotations.PreLoad;
import dev.morphia.annotations.PrePersist;

@SuppressWarnings("rawtypes")
class SeedEntityListener implements dev.morphia.EntityListener<Object> {
    private final Set<EntityListener> listeners;

    @Inject
    SeedEntityListener(Set<EntityListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    @PostLoad
    public void postLoad(Object entity, Document document, Datastore datastore) {
        Set<EntityListener> affectedListeners = getAffectedListeners(entity);
        for (EntityListener listener : affectedListeners) {
            listener.postLoad(entity, document, datastore);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @PostPersist
    public void postPersist(Object entity, Document document, Datastore datastore) {
        Set<EntityListener> affectedListeners = getAffectedListeners(entity);
        for (EntityListener listener : affectedListeners) {
            listener.postPersist(entity, document, datastore);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @PreLoad
    public void preLoad(Object entity, Document document, Datastore datastore) {
        Set<EntityListener> affectedListeners = getAffectedListeners(entity);
        for (EntityListener listener : affectedListeners) {
            listener.preLoad(entity, document, datastore);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    @PrePersist
    public void prePersist(Object entity, Document document, Datastore datastore) {
        Set<EntityListener> affectedListeners = getAffectedListeners(entity);
        for (EntityListener listener : affectedListeners) {
            listener.prePersist(entity, document, datastore);
        }
    }

    private boolean checkEntityType(Object entity, EntityListener<?> x) {
        return x.getListenerClass().isAssignableFrom(entity.getClass());
    }

    private Set<EntityListener> getAffectedListeners(Object entity) {
        return listeners.stream().filter(x -> checkEntityType(entity, x))
                .collect(Collectors.toSet());

    }

}
