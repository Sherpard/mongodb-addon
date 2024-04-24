/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.seedstack.business.specification.TrueSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;
import org.seedstack.mongodb.morphia.BaseMorphiaRepository;

import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;

class MorphiaTrueConverter
        implements SpecificationConverter<TrueSpecification<?>, MorphiaTranslationContext<?>, Filter> {
    @Override
    public Filter convert(TrueSpecification<?> specification, MorphiaTranslationContext<?> context,
            SpecificationTranslator<MorphiaTranslationContext<?>, Filter> translator) {
        // Always true
        return Filters.exists(BaseMorphiaRepository.ID_KEY);
    }
}
