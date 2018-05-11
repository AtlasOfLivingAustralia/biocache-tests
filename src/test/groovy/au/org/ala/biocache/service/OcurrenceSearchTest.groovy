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

import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Functional test for Occurrence Search
 * @author "Javier Molina <javier-molina at GH>"
 */
//@Slf4j
class OcurrenceSearchTest extends Specification {

    /* https://github.com/spockframework/spock/issues/491 */
    final static Logger log = LoggerFactory.getLogger(OcurrenceSearchTest.class)

    @EnvironmentEndPoint
    String baseUrl

    RESTClient restClient
    String path = "occurrences/search"

    def setup() {
//        baseUrl = "https://devt.ala.org.au/biocache-service/ws/" //Uncomment and adjust for testing a single method test from the IDE
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        log.info("Test: ${specificationContext.currentIteration.name}")

    }

    def "Search all records"() {
        when: "Search All Records"
        String queryString = "facets="
        def response = restClient.get(
                path: path,
                queryString: queryString,
        )

        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Body contains proper values"
        response.data.totalRecords >= 73949280 * 0.98 // 2% margin error from current production data
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
                queryString: queryString
        )

        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 148019"
        response.data.totalRecords >= 148019 * 0.98 // 2% margin error from current production data

        and: "All species occurrences on first page are Macropus"
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
                queryString: queryString
        )
        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 266109"
        response.data.totalRecords >= 266109 * 0.98 // 2% margin error from current production data

        and: "Some species occurrences on first page are macropus"
        response.data.occurrences.findAll {it.scientificName?.toLowerCase()?.contains("macropus")}.size() > 0

        and: "Has 4 Kingdoms"
        def kingdomFacet = response.data.facetResults.find {it.fieldName == "kingdom"}?.fieldResult
        kingdomFacet != null
        kingdomFacet.size() == 4

        and: "Contains kingdom Animalia"
        def animaliaKingdom = kingdomFacet.find {it.label == "Animalia"}
        animaliaKingdom != null
        animaliaKingdom.count >= 264515 * 0.98 // 2% margin error from current production data

        and: "also has some  Fungi kingdom"
        def fungiKingdom = kingdomFacet.find {it.label == "Fungi"}
        fungiKingdom != null
        fungiKingdom.count >= 10 * 0.98 // 2% margin error from current production data

        and: "also has some  Plantae kingdom"
        def plantaeKingdom = kingdomFacet.find {it.label == "Plantae"}
        plantaeKingdom != null
        plantaeKingdom.count >= 327 * 0.98 // 2% margin error from current production data

        and: "the remaining kingdom is unknown"
        def unknownKingdom = kingdomFacet.find {it.label == ""}
        unknownKingdom != null
        unknownKingdom.count >= 1257 * 0.98 // 2% margin error from current production data

        and: "queryTitle is text:Macropus"
        response.data.queryTitle == "text:Macropus"
    }

    def "Search for Raw/Provided Scientific Name 'Osphranter rufus' should turn up assorted red kangaroos"() {

        String queryString = "q=raw_name%3A%22Osphranter%20rufus%22&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name"
        when: "Search for Raw/Provided Scientific Name 'Osphranter rufus'"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 333"
        response.data.totalRecords >= 333 * 0.98 // 2% margin error from current production data

        and: "All species occurrences on first page are Osphranter rufus"
        response.data.occurrences.findAll {it.scientificName?.contains("Osphranter rufus") || it.scientificName?.contains("Macropus")}.size() == response.data.occurrences.size()

        and: "Some vernacular name occurrences  on first page are 'Red Kangaroo'"
        response.data.occurrences.findAll {it.vernacularName?.contains("Red Kangaroo")}.size() > 0

        and: "Has 2 Taxon"
        def taxonNameFacet = response.data.facetResults.find {it.fieldName == "taxon_name"}?.fieldResult
        taxonNameFacet != null
        taxonNameFacet.size() == 2

        and: "Contains taxon Macropus"
        def animaliaKingdom = taxonNameFacet.find {it.label == "Macropus"}
        animaliaKingdom != null
        animaliaKingdom.count >= 91 * 0.98 // 2% margin error from current production data

        and: "also has taxon Osphranter rufus"
        def fungiKingdom = taxonNameFacet.find {it.label == "Osphranter rufus"}
        fungiKingdom != null
        fungiKingdom.count >= 242 * 0.98 // 2% margin error from current production data


        and: "queryTitle is raw_name:\"Osphranter rufus\""
        response.data.queryTitle == "raw_name:\"Osphranter rufus\""
    }

    def "Search for Raw/Provided Scientific Name 'Acacia dealbata' should turn up assorted silver wattles"() {

        String queryString  = "q=raw_name%3A%22Acacia%20dealbata%22&start=0&pageSize=50&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name"
        when: "Search for Raw/Provided Scientific Name 'Acacia dealbata'"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 25,719"
        response.data.totalRecords >= 25719 * 0.98 // 2% margin error from current production data

        and: "Some vernacular name occurrences on first page are 'silver wattle'"
        response.data.occurrences.findAll {it.raw_vernacularName?.toLowerCase() =~ /silver.*wattle/}.size() > 0

        when: "Drill down on occurrences"
        def subspDealbataOccurrences = response.data.occurrences.findAll {it.scientificName?.contains("Acacia dealbata subsp. dealbata")}
        then: "An occurrence of 'Acacia dealbata subsp. dealbata' should be present"
        subspDealbataOccurrences.size() > 0

        and: "linked to the correct taxon ID"
        subspDealbataOccurrences.each {
            String occurrencePath = "occurrence/${it.uuid}"
            def occurrenceResponse = restClient.get(
                    path: occurrencePath
            )

            assert occurrenceResponse.data.processed.classification.taxonConceptID == "http://id.biodiversity.org.au/node/apni/2886432"
        }

        and: "Has at least 8 Taxon"
        def taxonNameFacet = response.data.facetResults.find {it.fieldName == "taxon_name"}?.fieldResult
        taxonNameFacet != null
        taxonNameFacet.size() >= 8

        and: "Contains taxon  'Acacia dealbata subsp. dealbata'"
        def subspDealbata = taxonNameFacet.find {it.label == "Acacia dealbata subsp. dealbata"}
        subspDealbata != null
        subspDealbata.count >= 9877 * 0.98 // 2% margin error from current production data

        and: "queryTitle is raw_name:\"Acacia dealbata\""
        response.data.queryTitle == "raw_name:\"Acacia dealbata\""
    }

    def "Search for species group Mammals should not have any weird kingdoms, phyla or classes floating about"() {

        String queryString  = "q=species_group%3AMammals&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=class&facets=phylum&facets=kingdom&flimit=10"

        when: "Search for species group Mammals"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        log.info("Testing [${baseUrl}$path?$queryString] ")

        then: "Status is 200"
        response.status == 200

        and: "Total records is at least 2713131"
        response.data.totalRecords >= 2713131 * 0.98 // 2% margin error from current production data

        and: "Has 1 kingdom"
        def kingdomFacet = response.data.facetResults.find {it.fieldName == "kingdom"}?.fieldResult
        kingdomFacet != null
        kingdomFacet.size() == 1

        and: "Contains kingdom Animalia"
        def animaliaKingdom = kingdomFacet.find {it.label == "Animalia"}
        animaliaKingdom != null
        animaliaKingdom.count >= 2713131 * 0.98 // 2% margin error from current production data

        and: "Has 1 phylum"
        def phylumFacet = response.data.facetResults.find {it.fieldName == "phylum"}?.fieldResult
        phylumFacet != null
        phylumFacet.size() == 1

        and: "Contains phylum Chordata"
        def chordataPhylum = phylumFacet.find {it.label == "Chordata"}
        chordataPhylum != null
        chordataPhylum.count >= 2713131 * 0.98 // 2% margin error from current production data

        and: "Has 2 classes"
        def classFacet = response.data.facetResults.find {it.fieldName == "class"}?.fieldResult
        classFacet != null
        classFacet.size() == 2

        and: "Contains class Mammalia"
        def mammaliaClass = classFacet.find {it.label == "Mammalia"}
        mammaliaClass != null
        mammaliaClass.count >= 2255099 * 0.98 // 2% margin error from current production data

        and: "the remaining class is unknown"
        def unknownKingdom = classFacet.find {it.label == ""}
        unknownKingdom != null
        unknownKingdom.count >= 458032 * 0.98 // 2% margin error from current production data

        and: "queryTitle is Lifeform:Mammals"
        response.data.queryTitle == "Lifeform:Mammals"
    }

//    def "Search for the Australian Museum Entomology Collection should mostly show class Insecta, kingdom Animalia etc. There will be some errors as stuff like Genus nov. gets mismatched and some molluscs and other oddities."() {
//
//        String queryString = "q=text%3AMacropus&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name&facets=genus&facets=kingdom"
//        when: "Search Text Macropus"
//        def response = restClient.get(
//                path: path,
//                queryString: queryString
//        )
//
//        then: "Status is 200"
//        response.status == 200
//
//        and: "Total records is at least 266109"
//        response.data.totalRecords >= 266109 * 0.98 // 2% margin error from current production data
//
//        and: "Some species occurrences on first page are macropus"
//        response.data.occurrences.findAll {it.scientificName?.toLowerCase()?.contains("macropus")}.size() > 0
//
//        and: "Has 4 Kingdoms"
//        def kingdomFacet = response.data.facetResults.find {it.fieldName == "kingdom"}?.fieldResult
//        kingdomFacet != null
//        kingdomFacet.size() == 4
//
//        and: "Contains kingdom Animalia"
//        def animaliaKingdom = kingdomFacet.find {it.label == "Animalia"}
//        animaliaKingdom != null
//        animaliaKingdom.count >= 264515 * 0.98 // 2% margin error from current production data
//
//        and: "also has some  Fungi kingdom"
//        def fungiKingdom = kingdomFacet.find {it.label == "Fungi"}
//        fungiKingdom != null
//        fungiKingdom.count >= 10
//
//        and: "also has some  Plantae kingdom"
//        def plantaeKingdom = kingdomFacet.find {it.label == "Plantae"}
//        plantaeKingdom != null
//        plantaeKingdom.count >= 327
//
//        and: "the remaining kingdom is unknown"
//        def unknownKingdom = kingdomFacet.find {it.label == ""}
//        unknownKingdom != null
//        unknownKingdom.count >= 1257
//
//        and: "queryTitle is text:Macropus"
//        response.data.queryTitle == "text:Macropus"
//    }

}
