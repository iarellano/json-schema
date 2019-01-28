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

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;

interface ReadWriteValidator {

    static ReadWriteValidator createForContext(ReadWriteContext context, ValidationFailureReporter failureReporter) {
        return context == null ? NONE :
                context == ReadWriteContext.READ ? new WriteOnlyValidator(failureReporter) :
                        new ReadOnlyValidator(failureReporter);
    }

    ReadWriteValidator NONE = (schema, subject) -> {
    };

    void validate(Schema schema, Object subject);

}

class ReadOnlyValidator implements ReadWriteValidator {

    private final ValidationFailureReporter failureReporter;

    ReadOnlyValidator(ValidationFailureReporter failureReporter) {
        this.failureReporter = failureReporter;
    }

    @Override public void validate(Schema schema, Object subject) {
        if (schema.isReadOnly() == Boolean.TRUE && subject != null) {
            failureReporter.failure(ResourceBundleThreadLocal.get().getString("read-write.readOnly"), "readOnly");
        }
    }
}

class WriteOnlyValidator implements ReadWriteValidator {

    private final ValidationFailureReporter failureReporter;

    WriteOnlyValidator(ValidationFailureReporter failureReporter) {
        this.failureReporter = failureReporter;
    }

    @Override public void validate(Schema schema, Object subject) {
        if (schema.isWriteOnly() == Boolean.TRUE && subject != null) {
            failureReporter.failure(ResourceBundleThreadLocal.get().getString("read-write.writeOnly"), "writeOnly");
        }
    }
}
