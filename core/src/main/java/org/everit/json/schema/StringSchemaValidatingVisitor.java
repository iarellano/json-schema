/*
 * Original work Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 * Modified work Copyright (c) 2019 Isaias Arellano - isaias.arellano.delgado@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;
import org.everit.json.schema.regexp.Regexp;

public class StringSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private String stringSubject;

    private int stringLength;

    private final ValidatingVisitor owner;

    public StringSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "failureReporter cannot be null");
    }

    @Override void visitStringSchema(StringSchema stringSchema) {
        if (owner.passesTypeCheck(String.class, stringSchema.requireString(), stringSchema.isNullable())) {
            stringSubject = (String) subject;
            stringLength = stringSubject.codePointCount(0, stringSubject.length());
            super.visitStringSchema(stringSchema);
        }
    }

    @Override void visitMinLength(Integer minLength) {
        if (minLength != null && stringLength < minLength.intValue()) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("string.minLength"), minLength, stringLength), "minLength");
        }
    }

    @Override void visitMaxLength(Integer maxLength) {
        if (maxLength != null && stringLength > maxLength.intValue()) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("string.maxLength"), maxLength, stringLength), "maxLength");
        }
    }

    @Override void visitPattern(Regexp pattern) {
        if (pattern != null && pattern.patternMatchingFailure(stringSubject).isPresent()) {
            String message = format(ResourceBundleThreadLocal.get().getString("string.pattern"), subject, pattern.toString());
            owner.failure(message, "pattern");
        }
    }

    @Override void visitFormat(FormatValidator formatValidator) {
        Optional<String> failure = formatValidator.validate(stringSubject);
        if (failure.isPresent()) {
            owner.failure(failure.get(), "format");
        }
    }

}
