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

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.ELSE;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.THEN;

import org.everit.json.schema.event.ConditionalSchemaMatchEvent;
import org.everit.json.schema.event.ConditionalSchemaMismatchEvent;
import org.everit.json.schema.event.ConditionalSchemaValidationEvent;
import org.everit.json.schema.i18n.ResourceBundleThreadLocal;

import java.util.Arrays;

class ConditionalSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    private ConditionalSchema conditionalSchema;

    private ValidationException ifSchemaException;

    ConditionalSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "owner cannot be null");
    }

    @Override
    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        this.conditionalSchema = conditionalSchema;
        if (!conditionalSchema.getIfSchema().isPresent() ||
                (!conditionalSchema.getThenSchema().isPresent() && !conditionalSchema.getElseSchema().isPresent())) {
            return;
        }
        super.visitConditionalSchema(conditionalSchema);
    }

    @Override
    void visitIfSchema(Schema ifSchema) {
        if (conditionalSchema.getIfSchema().isPresent()) {
            ifSchemaException = owner.getFailureOfSchema(ifSchema, subject);
            if (ifSchemaException == null) {
                owner.validationListener.ifSchemaMatch(createMatchEvent(IF));
            } else {
                owner.validationListener.ifSchemaMismatch(createMismatchEvent(IF, ifSchemaException));
            }
        }
    }

    @Override
    void visitThenSchema(Schema thenSchema) {
        if (ifSchemaException == null) {
            ValidationException thenSchemaException = owner.getFailureOfSchema(thenSchema, subject);
            if (thenSchemaException != null) {
                ValidationException failure = new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        ResourceBundleThreadLocal.get().getString("conditional.then"),
                        Arrays.asList(thenSchemaException),
                        "then",
                        conditionalSchema.getSchemaLocation());

                owner.validationListener.thenSchemaMismatch(createMismatchEvent(THEN, thenSchemaException));
                owner.failure(failure);
            } else {
                owner.validationListener.thenSchemaMatch(createMatchEvent(THEN));
            }
        }
    }

    @Override
    void visitElseSchema(Schema elseSchema) {
        if (ifSchemaException != null) {
            ValidationException elseSchemaException = owner.getFailureOfSchema(elseSchema, subject);
            if (elseSchemaException != null) {
                ValidationException failure = new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        ResourceBundleThreadLocal.get().getString("conditional.else"),
                        Arrays.asList(ifSchemaException, elseSchemaException),
                        "else",
                        conditionalSchema.getSchemaLocation());
                owner.validationListener.elseSchemaMismatch(createMismatchEvent(ELSE, elseSchemaException));
                owner.failure(failure);
            } else {
                owner.validationListener.elseSchemaMatch(createMatchEvent(ELSE));
            }
        }
    }

    private ConditionalSchemaMatchEvent createMatchEvent(ConditionalSchemaValidationEvent.Keyword keyword) {
        return new ConditionalSchemaMatchEvent(conditionalSchema, subject, keyword);
    }

    private ConditionalSchemaMismatchEvent createMismatchEvent(ConditionalSchemaValidationEvent.Keyword keyword,
            ValidationException failure) {
        return new ConditionalSchemaMismatchEvent(conditionalSchema, subject, keyword, failure);
    }

}
