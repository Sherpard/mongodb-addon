/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import java.util.Arrays;

import org.seedstack.business.specification.OrSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;

class MorphiaOrConverter implements SpecificationConverter<OrSpecification<?>, MorphiaTranslationContext<?>, Filter> {
    @Override
    public Filter convert(OrSpecification<?> specification, MorphiaTranslationContext<?> context,
            SpecificationTranslator<MorphiaTranslationContext<?>, Filter> translator) {

        Filter[] criterias = Arrays.stream(specification
                .getSpecifications())
                .map(spec -> translator.translate(spec, new MorphiaTranslationContext<>(context)))
                .toArray(Filter[]::new);

        return Filters.or(criterias);

    }
}
