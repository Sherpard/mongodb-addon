/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.seedstack.business.specification.EqualSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;

class MorphiaEqualConverter
        implements SpecificationConverter<EqualSpecification<?>, MorphiaTranslationContext<?>, Filter> {

    @Override
    public Filter convert(EqualSpecification<?> specification,
            MorphiaTranslationContext<?> context,
            SpecificationTranslator<MorphiaTranslationContext<?>, Filter> translator) {
        if (specification.getExpectedValue() == null) {
            return Filters.exists(context.getProperty()).not();
        } else {
            return Filters.eq(context.getProperty(), specification.getExpectedValue());
        }
    }
}
