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

import au.org.ala.test.ContentTypeUtil
import au.org.ala.test.SpreadSheetUtil
import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Consumes a spreadsheet and query the webservice for each
 * row comparing that reference and test system generate the same output
 *
 * It is assumed that each request is idempotent at least to a granularity of a few seconds otherwise the
 * comparisson would not make sense
 * @author "Javier Molina <javier-molina at GH>"
 */
class SideBySideTest extends Specification {


    // System under test
    @EnvironmentEndPoint
    String testUrl //= "https://devt.ala.org.au/biocache-service/ws/"

    // Reference system
    @EnvironmentEndPoint(envVariable = "referenceHostUrl")
    String referenceUrl //= "https://biocache-test.ala.org.au/ws/"

    RESTClient referenceRestClient
    RESTClient testRestClient

    def setup() {
        referenceRestClient = new RESTClient(referenceUrl)
        testRestClient = new RESTClient(testUrl)
    }

    @Shared
    SpreadSheetUtil spreadSheetUtil = new SpreadSheetUtil()

    @Unroll
    def "Compare each call for #description"() {
        when: "Query both systems"
        Map<String, ?> referenceRequestParams =
                [path: path,
                 queryString: queryString]

        if(contentType) {
            referenceRequestParams.contentType = contentType
        }

        // the get call below modifies the original map hence we need to keep a copy for the seconnd call
        Map<String, ?> testRequestParameters = referenceRequestParams.clone()

        def referenceResponse = referenceRestClient.get(
                referenceRequestParams
        )

        def testResponse = testRestClient.get(
                testRequestParameters
        )

        then: "Status is 200 for both responses"
        referenceResponse.status == 200
        testResponse.status == 200

        and: "Data is equal for both systems"
        if(referenceResponse.data instanceof InputStream) {
            referenceResponse.data.getBytes() == testResponse.data.getBytes()
        } else {
            referenceResponse.data == testResponse.data
        }

        where:
        row << spreadSheetUtil.getFirstSheetFromResource("/side_by_side_tests.xlsx")
        data = spreadSheetUtil.getRowData(row)
        description = data[0]
        contentType = ContentTypeUtil.contentTypeFromString(data[1]) //Optional parameter in spreadsheet
        path = data[2]
        queryString = data[3]

    }
}
