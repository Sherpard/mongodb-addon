/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import static com.google.common.base.Preconditions.checkState;

import dev.morphia.query.CriteriaContainer;
import dev.morphia.query.FieldEnd;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;

public class MorphiaTranslationContext<T> {
    private final Query<T> query;
    private String property;
    private boolean not;

    public MorphiaTranslationContext(Query<T> query) {
        this.query = query;
    }

    public MorphiaTranslationContext(MorphiaTranslationContext<T> source) {
        this.query = source.query;
        this.property = source.property;
        this.not = source.not;
    }

    public FieldEnd<?> pickFieldEnd() {
        FieldEnd<? extends CriteriaContainer> result;

        if (not) {
            result = query.criteria(property).not();
        } else {
            result = query.criteria(property);
        }
        this.property = null;
        this.not = false;
        return result;
    }

    public String getProperty() {
        assertPropertyValue();
        return property;
    }

    public void setProperty(String property) {
        checkState(this.property == null, "A field is already set");
        this.property = property;
    }

    public Query<T> getQuery() {
        return query;
    }

    private void assertPropertyValue() {
        checkState(this.property != null, "No field has been set");

    }
}
