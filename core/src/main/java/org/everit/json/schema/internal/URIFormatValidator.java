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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.everit.json.schema.i18n.ResourceBundleThreadLocal;
import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "uri" format value.
 */
public class URIFormatValidator implements FormatValidator {

    private final boolean protocolRelativeURIPermitted;

    public URIFormatValidator() {
        this(true);
    }

    public URIFormatValidator(boolean protocolRelativeURIPermitted) {
        this.protocolRelativeURIPermitted = protocolRelativeURIPermitted;
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            if (subject != null) {
                URI uri = new URI(subject);
                if (hasProtocol(uri) || (protocolRelativeURIPermitted && isProtocolRelativeURI(subject)))
                    return Optional.empty();
            }
        } catch (URISyntaxException e) {
            // Nothing To Do
        }
        return Optional.of(String.format(ResourceBundleThreadLocal.get().getString("format.uri"), subject));
    }

    /**
     * @deprecated use {@code Optional.of(String.format("[%s] is not a valid URI", subject))} instead
     */
    @Deprecated
    protected Optional<String> failure(String subject) {
        return Optional.of(String.format(ResourceBundleThreadLocal.get().getString("format.uri"), subject));
    }

    private boolean isProtocolRelativeURI(String subject) {
        return subject.startsWith("//");
    }

    private boolean hasProtocol(URI uri) {
        return uri.getScheme() != null;
    }

    @Override public String formatName() {
        return "uri";
    }
}
