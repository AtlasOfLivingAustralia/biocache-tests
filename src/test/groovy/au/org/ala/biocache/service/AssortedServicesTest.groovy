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

package au.org.ala.biocache.service

import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Functional tests for different services accross biocache-service
 * @author "Javier Molina <javier-molina at GH>"
 */
//@Slf4j
class AssortedServicesTest extends Specification {

    /* https://github.com/spockframework/spock/issues/491 */
    final static Logger log = LoggerFactory.getLogger(AssortedServicesTest.class)

    @EnvironmentEndPoint
    String baseUrl

    RESTClient restClient

    def setup() {
//        baseUrl = "https://devt.ala.org.au/biocache-service/ws/" //Uncomment and adjust for testing a single method test from the IDE
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        log.info("Test: ${specificationContext.currentIteration.name}")

    }

    def "Get an specific occurrence 794e1d5b-fc14-4b77-a21a-8e73749dc910"() {
        String path = "occurrences/794e1d5b-fc14-4b77-a21a-8e73749dc910"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
        )

        log.info("Testing [${baseUrl}$path] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is 1"
        response.data.raw.occurrence.individualCount == "1" // 2% margin error from current production data

        and: "scientificName is 'Rhipidura leucophrys'"
        response.data.raw.classification.scientificName == "Rhipidura leucophrys"

        and: "vernacularName is 'Willie Wagtail'"
        response.data.raw.classification.vernacularName == "Willie Wagtail"

        and: "Unchecked assertions >= 45"
        response.data.systemAssertions.unchecked.size() >= 45

        and: "Warning assertions >= 3"
        response.data.systemAssertions.warning.size() >= 3

        and: "Passed assertions >= 29"
        response.data.systemAssertions.passed.size() >= 29

    }
}