/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.morphia.Datastore;
import dev.morphia.EntityListener;
import dev.morphia.annotations.EntityListeners;
import dev.morphia.annotations.PrePersist;

public class ValidatingEntityInterceptor implements EntityListener<Object> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatingEntityInterceptor.class);
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
        LOGGER.info(type.toString());
        // System.err.println(type.toString());
        // TODO Auto-generated method stub
        return true;
    }
}
