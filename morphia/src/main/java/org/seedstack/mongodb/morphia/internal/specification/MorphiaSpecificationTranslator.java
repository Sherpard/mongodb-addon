/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.seedstack.business.specification.Specification;
import org.seedstack.business.spi.BaseSpecificationTranslator;

import dev.morphia.query.filters.Filter;

class MorphiaSpecificationTranslator extends BaseSpecificationTranslator<MorphiaTranslationContext<?>, Filter> {
    @Override
    public <S extends Specification<?>> Filter translate(S specification, MorphiaTranslationContext<?> context) {
        return convert(specification, context);
    }
    
    
}
