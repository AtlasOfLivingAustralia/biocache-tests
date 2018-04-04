/*
 * Copyright (C) 2018 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.test

import groovyx.net.http.ContentType

/**
 *
 * @author "Javier Molina <javier-molina at GH>"
 */
class ContentTypeUtil {
    /**
     * Converts a string of the form "application/json", etc to a {@link ContentType}
     * @param input the content type as string
     * @return a {@link ContentType} instance or null if not known content type found.
     */
    static ContentType contentTypeFromString(String input) {
        switch (input) {
            case "application/json":
            case "application/javascript":
            case "text/javascript":
                return ContentType.JSON
            case "application/octet-stream":
                return ContentType.BINARY
            case "application/xml":
            case "text/xml":
            case "application/xhtml+xml":
            case "application/atom+xml":
                return ContentType.XML
            case "text/plain":
                return ContentType.TEXT

            default:
                return null
        }
    }
}
