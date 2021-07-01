/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import dev.morphia.query.CriteriaContainer;
import org.seedstack.business.specification.StringSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

import java.util.regex.Pattern;

abstract class MorphiaStringConverter<S extends StringSpecification> implements SpecificationConverter<S, MorphiaTranslationContext<?>, CriteriaContainer> {
    @Override
    public CriteriaContainer convert(S specification, MorphiaTranslationContext<?> context, SpecificationTranslator<MorphiaTranslationContext<?>, CriteriaContainer> translator) {
        if (specification.getExpectedString() == null) {
            return context.pickFieldEnd().doesNotExist();
        } else {
            StringSpecification.Options options = specification.getOptions();
            if (hasNoOption(options) && !isRegex()) {
                // We avoid using equal() because Morphia optimizes it without operator ("someAttr": "someVal")
                // Thus generating an invalid query when trying to negate it ("$not": "someVal")
                return context.pickFieldEnd().not().notEqual(specification.getExpectedString());
            } else {
                return context.pickFieldEnd().equal(buildRegex(options, specification.getExpectedString()));
            }

        }
    }

    private Pattern buildRegex(StringSpecification.Options options, String expectedString) {
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        if (options.isTrimmed() || options.isLeadTrimmed()) {
            sb.append("\\s*");
        }
        sb.append(buildRegexMatchingPart(expectedString));
        if (options.isTrimmed() || options.isTailTrimmed()) {
            sb.append("\\s*");
        }
        sb.append("$");
        return Pattern.compile(sb.toString(), options.isIgnoringCase() ? Pattern.CASE_INSENSITIVE : 0);
    }

    private boolean hasNoOption(StringSpecification.Options options) {
        return !options.isLeadTrimmed() && !options.isTailTrimmed() && !options.isTrimmed() && !options.isIgnoringCase();
    }

    abstract String buildRegexMatchingPart(String value);

    abstract boolean isRegex();
}
