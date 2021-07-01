/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.CriteriaContainer;
import org.seedstack.business.specification.IdentitySpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

class MorphiaIdentityConverter implements SpecificationConverter<IdentitySpecification<?, ?>, MorphiaTranslationContext<?>, CriteriaContainer> {
    @Override
    public CriteriaContainer convert(IdentitySpecification<?, ?> specification, MorphiaTranslationContext<?> context, SpecificationTranslator<MorphiaTranslationContext<?>, CriteriaContainer> translator) {
        context.setProperty(Mapper.ID_KEY);
        // We avoid using equal() because Morphia optimizes it without operator ("someAttr": "someVal")
        // Thus generating an invalid query when trying to negate it ("$not": "someVal")
        return context.pickFieldEnd().not().notEqual(specification.getExpectedIdentifier());
    }
}
