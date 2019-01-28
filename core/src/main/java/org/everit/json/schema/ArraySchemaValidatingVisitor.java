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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;
import org.json.JSONArray;

class ArraySchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    private JSONArray arraySubject;

    private ArraySchema arraySchema;

    private int subjectLength;

    public ArraySchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "owner cannot be null");
    }

    @Override void visitArraySchema(ArraySchema arraySchema) {
        if (owner.passesTypeCheck(JSONArray.class, arraySchema.requiresArray(), arraySchema.isNullable())) {
            this.arraySubject = (JSONArray) subject;
            this.subjectLength = arraySubject.length();
            this.arraySchema = arraySchema;
            super.visitArraySchema(arraySchema);
        }
    }

    @Override void visitMinItems(Integer minItems) {
        if (minItems != null && subjectLength < minItems) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("array.minItems"), minItems, subjectLength), "minItems");
        }
    }

    @Override void visitMaxItems(Integer maxItems) {
        if (maxItems != null && maxItems < subjectLength) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("array.maxItems"), maxItems, subjectLength), "maxItems");
        }
    }

    @Override void visitUniqueItems(boolean uniqueItems) {
        if (!uniqueItems || subjectLength == 0) {
            return;
        }
        Collection<Object> uniques = new ArrayList<Object>(subjectLength);
        for (int i = 0; i < subjectLength; ++i) {
            Object item = arraySubject.get(i);
            for (Object contained : uniques) {
                if (ObjectComparator.deepEquals(contained, item)) {
                    owner.failure(ResourceBundleThreadLocal.get().getString("array.uniqueItems"), "uniqueItems");
                    return;
                }
            }
            uniques.add(item);
        }
    }

    @Override void visitAllItemSchema(Schema allItemSchema) {
        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subjectLength), allItemSchema);
        }
    }

    @Override void visitItemSchema(int index, Schema itemSchema) {
        if (index >= subjectLength) {
            return;
        }
        Object subject = arraySubject.get(index);
        String idx = String.valueOf(index);
        ifFails(itemSchema, subject)
                .map(exc -> exc.prepend(idx))
                .ifPresent(owner::failure);
    }

    @Override void visitAdditionalItems(boolean additionalItems) {
        List<Schema> itemSchemas = arraySchema.getItemSchemas();
        int itemSchemaCount = itemSchemas == null ? 0 : itemSchemas.size();
        if (itemSchemas != null && !additionalItems && subjectLength > itemSchemaCount) {
            owner.failure(format(ResourceBundleThreadLocal.get().getString("array.additionalItems"), itemSchemaCount, subjectLength), "items");
        }
    }

    @Override void visitSchemaOfAdditionalItems(Schema schemaOfAdditionalItems) {
        if (schemaOfAdditionalItems == null) {
            return;
        }
        int validationFrom = Math.min(subjectLength, arraySchema.getItemSchemas().size());
        validateItemsAgainstSchema(IntStream.range(validationFrom, subjectLength), schemaOfAdditionalItems);
    }

    private void validateItemsAgainstSchema(IntStream indices, Schema schema) {
        validateItemsAgainstSchema(indices, i -> schema);
    }

    private void validateItemsAgainstSchema(IntStream indices, IntFunction<Schema> schemaForIndex) {
        for (int i : indices.toArray()) {
            String copyOfI = String.valueOf(i); // i is not effectively final so we copy it
            ifFails(schemaForIndex.apply(i), arraySubject.get(i))
                    .map(exc -> exc.prepend(copyOfI))
                    .ifPresent(owner::failure);
        }
    }

    private Optional<ValidationException> ifFails(Schema schema, Object input) {
        return Optional.ofNullable(owner.getFailureOfSchema(schema, input));
    }

    @Override void visitContainedItemSchema(Schema containedItemSchema) {
        if (containedItemSchema == null) {
            return;
        }
        for (int i = 0; i < arraySubject.length(); i++) {
            Optional<ValidationException> exception = ifFails(containedItemSchema, arraySubject.get(i));
            if (!exception.isPresent()) {
                return;
            }
        }
        owner.failure(ResourceBundleThreadLocal.get().getString("array.contains"), "contains");
    }
}
