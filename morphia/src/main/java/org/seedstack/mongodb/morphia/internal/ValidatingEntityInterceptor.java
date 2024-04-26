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

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import org.bson.Document;
import org.seedstack.seed.core.internal.validation.VerboseConstraintViolationException;

import dev.morphia.Datastore;
import dev.morphia.EntityListener;
import dev.morphia.annotations.PrePersist;

class ValidatingEntityInterceptor implements EntityListener<Object> {

    @Inject
    private ValidatorFactory validatorFactory;

    @Override
    @PrePersist
    public void prePersist(Object entity, Document document, Datastore datastore) {

        Set<ConstraintViolation<Object>> result = validatorFactory.getValidator().validate(entity);
        if (!result.isEmpty()) {
            throw new VerboseConstraintViolationException(result);
        }

    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return PrePersist.class.equals(type);
    }
}
