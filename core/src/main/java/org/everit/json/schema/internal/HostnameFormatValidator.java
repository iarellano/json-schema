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
package org.everit.json.schema.internal;

import org.apache.commons.validator.routines.DomainValidator;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.i18n.ResourceBundleThreadLocal;

import java.util.Optional;

/**
 * Implementation of the "hostname" format value.
 */
public class HostnameFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        return DomainValidator.getInstance(true).isValid(subject) && !subject.contains("_") ?
                Optional.empty() :
                Optional.of(String.format(ResourceBundleThreadLocal.get().getString("format.hostname"), subject));
    }

    @Override
    public String formatName() {
        return "hostname";
    }
}
