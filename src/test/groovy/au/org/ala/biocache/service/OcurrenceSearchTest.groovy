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

package  au.org.ala.biocache.service

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import spock.lang.Specification

/**
 * Functional test for Occurrence Search
 * @author "Javier Molina <javier-molina at GH>"
 */
class OcurrenceSearchTest extends Specification {
    //TODO externalise as config

//    String baseUrl = "https://devt.ala.org.au/biocache-service/ws/"
    String baseUrl = "https://biocache-test.ala.org.au/ws/"

    RESTClient restClient = new RESTClient(baseUrl, ContentType.JSON)
    String path = "occurrences/search"

    def "Search all records"() {
        when: "Search All Records"
//        String queryString = "facets=genus&facets=taxon_name&facets=species"
        String queryString = "facets="
        def response = restClient.get(
                path: path,
                queryString: queryString,
        )

        then: "Status is 200"
        response.status == 200

        and: "Body contains proper values"
        response.data.totalRecords >= 73949280
        response.data.status == "OK"
        response.data.occurrences.size() == 10
        !response.data.facetResults
        response.data.query == "?q=*%3A*"
        response.data.queryTitle == "[all records]"

    }

    def "Search Taxon Macropus should show all records of genus Macropus and not frogs, plants or any other riff-raff"() {

        String queryString = "q=lsid%3Aurn%3Alsid%3Abiodiversity.org.au%3Aafd.taxon%3Ab1d9bf29-648f-47e6-8544-2c2fbdf632b1&facets=genus"
        when: "Search Taxon Macropus"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                requestContentType: ContentType.JSON
        )

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 148019"
        response.data.totalRecords >= 148019

        and: "Occurrences species on first page are all Macropus"
        response.data.occurrences.findAll {it.scientificName.contains("Macropus")}.size() == response.data.occurrences.size()

        and: "All genus are Macropus"
        response.data.facetResults.size() == 1
        response.data.facetResults[0].fieldName == "genus"
        response.data.facetResults[0].fieldResult.size() == 1
        response.data.facetResults[0].fieldResult[0].label == "Macropus"
        response.data.facetResults[0].fieldResult[0].count == response.data.totalRecords

        and: "queryTitle is GENUS: Macropus"
        assert response.data.queryTitle == "<span class='lsid' id='urn:lsid:biodiversity.org.au:afd.taxon:b1d9bf29-648f-47e6-8544-2c2fbdf632b1'>GENUS: Macropus</span>"

    }

    def "Text seach Macropus should show all records of genus Macropus and a scattering of other species with the name macropus in them scattered across the kingdoms. There should be Plantae and Fungi amongst them"() {

        String queryString = "q=text%3AMacropus&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name&facets=genus&facets=kingdom"
        when: "Search Text Macropus"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                requestContentType: ContentType.JSON
        )

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 266109"
        response.data.totalRecords >= 266109

        and: "Some occurrences species on first page are macropus"
        response.data.occurrences.findAll {it.scientificName?.toLowerCase()?.contains("macropus")}.size() > 0

        and: "Has several 4 Kingdoms"
        def kingdomFacet = response.data.facetResults.find {it.fieldName == "kingdom"}?.fieldResult
        kingdomFacet != null
        kingdomFacet.size() == 4

        and: "Contains kingdom Animalia"
        def animaliaKingdom = kingdomFacet.find {it.label == "Animalia"}
        animaliaKingdom != null
        animaliaKingdom.count >= 264515

        and: "also has some  Fungi kingdom"
        def fungiKingdom = kingdomFacet.find {it.label == "Fungi"}
        fungiKingdom != null
        fungiKingdom.count >= 10

        and: "also has some  Plantae kingdom"
        def plantaeKingdom = kingdomFacet.find {it.label == "Plantae"}
        plantaeKingdom != null
        plantaeKingdom.count >= 327

        and: "the remaining kingdom is unknown"
        def unknownKingdom = kingdomFacet.find {it.label == ""}
        unknownKingdom != null
        unknownKingdom.count >= 1257

        and: "queryTitle is text:Macropus"
        response.data.queryTitle == "text:Macropus"
    }
}
