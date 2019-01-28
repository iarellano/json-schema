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

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;
import org.everit.json.schema.FormatValidator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Base class for date and time format validators
 */
public class TemporalFormatValidator implements FormatValidator {
    final static DateTimeFormatter SECONDS_FRACTION_FORMATTER = new DateTimeFormatterBuilder()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .toFormatter();
    static final String ZONE_OFFSET_PATTERN = "XXX";

    private final DateTimeFormatter formatter;
    private final String formatsAccepted;

    TemporalFormatValidator(DateTimeFormatter formatter, String formatsAccepted) {
        this.formatter = requireNonNull(formatter, "formatter cannot be null");
        this.formatsAccepted = formatsAccepted;
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            formatter.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(format(ResourceBundleThreadLocal.get().getString("format"), subject, formatName(), formatsAccepted));
        }
    }
}
