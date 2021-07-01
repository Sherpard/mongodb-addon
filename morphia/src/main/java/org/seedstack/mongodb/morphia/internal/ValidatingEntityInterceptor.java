/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import com.mongodb.DBObject;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.mongodb.morphia.AbstractEntityInterceptor;
import org.mongodb.morphia.mapping.Mapper;
import org.seedstack.seed.core.internal.validation.VerboseConstraintViolationException;

class ValidatingEntityInterceptor extends AbstractEntityInterceptor {
    @Inject
    private ValidatorFactory validatorFactory;

    @Override
    public void prePersist(final Object ent, final DBObject dbObj, final Mapper mapper) {
        Set<ConstraintViolation<Object>> result = validatorFactory.getValidator().validate(ent);
        if (!result.isEmpty()) {
            throw new VerboseConstraintViolationException(result);
        }
    }
}
