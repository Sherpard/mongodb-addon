/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import java.util.regex.Pattern;

import org.seedstack.business.specification.StringSpecification;
import org.seedstack.business.spi.SpecificationConverter;
import org.seedstack.business.spi.SpecificationTranslator;

import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;

abstract class MorphiaStringConverter<S extends StringSpecification>
        implements SpecificationConverter<S, MorphiaTranslationContext<?>, Filter> {
    @Override
    public Filter convert(S specification, MorphiaTranslationContext<?> context,
            SpecificationTranslator<MorphiaTranslationContext<?>, Filter> translator) {
        if (specification.getExpectedString() == null) {
            return Filters.exists(context.getProperty()).not();
        } else {
            StringSpecification.Options options = specification.getOptions();
            if (hasNoOption(options) && !isRegex()) {
                return Filters.eq(context.getProperty(), specification.getExpectedString());
            } else {
                return Filters.regex(context.getProperty(), buildRegex(options, specification.getExpectedString()));
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
        return !options.isLeadTrimmed() && !options.isTailTrimmed() && !options.isTrimmed()
                && !options.isIgnoringCase();
    }

    abstract String buildRegexMatchingPart(String value);

    abstract boolean isRegex();
}
