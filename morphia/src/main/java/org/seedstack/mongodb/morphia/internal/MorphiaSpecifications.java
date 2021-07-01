/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import org.kametic.specifications.Specification;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.seedstack.seed.core.internal.utils.SpecificationBuilder;

import java.lang.reflect.Modifier;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;
import static org.seedstack.shed.reflect.ClassPredicates.classIsInterface;
import static org.seedstack.shed.reflect.ClassPredicates.classModifierIs;

class MorphiaSpecifications {
    static Specification<Class<?>> PERSISTED_CLASSES = new SpecificationBuilder<>(classIsInterface().negate()
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(elementAnnotatedWith(Entity.class, false)
                    .or(elementAnnotatedWith(Embedded.class, false))
            )).build();
}
