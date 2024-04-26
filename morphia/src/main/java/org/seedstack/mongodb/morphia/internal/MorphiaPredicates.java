/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;
import static org.seedstack.shed.reflect.ClassPredicates.classImplements;
import static org.seedstack.shed.reflect.ClassPredicates.classIsInterface;
import static org.seedstack.shed.reflect.ClassPredicates.classModifierIs;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import dev.morphia.EntityListener;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;

class MorphiaPredicates {
    static Predicate<Class<?>> PERSISTED_CLASSES = classIsInterface().negate()
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(elementAnnotatedWith(Entity.class, false)
                    .or(elementAnnotatedWith(Embedded.class, false)));

    static Predicate<Class<?>> ENTITY_LISTENERS = classIsInterface().negate()
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(classImplements(EntityListener.class));
}
