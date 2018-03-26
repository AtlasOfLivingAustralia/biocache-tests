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
package au.org.ala.test.spock

import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Annotation to pass hosts urls from system environment variables
 * Main ideas taken from https://dzone.com/articles/spock-and-testing-restful-api
 * @author "Javier Molina <javier-molina at GH>"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD])
@ExtensionAnnotation(EnvironmentEndPointExtension)
@interface EnvironmentEndPoint {
    String envVariable() default 'testHostUrl'
}