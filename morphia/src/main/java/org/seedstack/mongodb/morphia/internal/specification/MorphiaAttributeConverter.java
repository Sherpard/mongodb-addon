/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.seedstack.business.specification.AttributeSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

import dev.morphia.query.filters.Filter;

class MorphiaAttributeConverter
        implements SpecificationConverter<AttributeSpecification<?, ?>, MorphiaTranslationContext<?>, Filter> {
    @Override
    public Filter convert(AttributeSpecification<?, ?> specification, MorphiaTranslationContext<?> context,
            SpecificationTranslator<MorphiaTranslationContext<?>, Filter> translator) {
        context.setProperty(specification.getPath());
        return translator.translate(specification.getValueSpecification(), context);
    }
}
