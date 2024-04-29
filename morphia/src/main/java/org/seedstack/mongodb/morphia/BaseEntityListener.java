/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import java.lang.reflect.Type;

import org.seedstack.business.internal.utils.BusinessUtils;

public abstract class BaseEntityListener<T> implements EntityListener<T> {

    private final Class<T> listenerClass;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BaseEntityListener() {
        Type[] generics = BusinessUtils.resolveGenerics(EntityListener.class, this.getClass());
        this.listenerClass = (Class) generics[0];
    }

    @Override
    public Class<T> getListenerClass() {
        return listenerClass;
    }

}
