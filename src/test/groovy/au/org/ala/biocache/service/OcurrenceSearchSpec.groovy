package au.org.ala.biocache.service

import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class OcurrenceSearchSpec extends spock.lang.Specification {
    @EnvironmentEndPoint(envVariable = "testHostUrl")
    String baseUrl

    RESTClient restClient
    String path = "occurrences/search"

    def setup() {
//        baseUrl = "https://devt.ala.org.au/biocache-service/ws/" //Uncomment and adjust for testing a single method test from the IDE
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        println("================================================================")
        println(("Setup preparation for: ${specificationContext.currentIteration.name}"))
    }

    def "Search all records, first level structure in response is ok"() {
        when: "Search All Records"
        String queryString = "facets="
        def response = restClient.get(
                path: path,
                queryString: queryString,
        )
        println("When url is:  ${baseUrl}${path}")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "Body contains proper structure"
        with(response.data) {
            pageSize
            println("and response Json contains: pageSize $pageSize")
            startIndex >= 0
            println("and response Json contains: startIndex $startIndex")
            totalRecords >= 73949280 * 0.98 // 2% margin error from current production data
            println("and response Json contains: totalRecords $totalRecords")
            sort
            println("and response Json contains: sort $sort")
            dir
            println("and response Json contains: dir $dir")
            occurrences.size() <= pageSize
            println("and response Json contains $occurrences.size occurrences sets")
            !facetResults
            println("and response Json contains: empty facetResults")
            query == "?q=*%3A*"
            println("and response Json contains: query $query")
            urlParameters
            println("and response Json contains: urlParameters $urlParameters")
            queryTitle == "[all records]"
            println("and response Json contains: queryTitle $queryTitle")
            !activeFacetMap
            println("and response Json contains: empty activeFacetMap")
            !activeFacetObj
            println("and response Json contains: empty activeFacetObj")
        }
    }

    def "Search all records, occurrences data is ok"() {
        when: "Search All Records"
        String queryString = "facets="
        def response = restClient.get(
                path: path,
                queryString: queryString,
        )
        println("When url is:  ${baseUrl}${path}")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "occurrences[0] contains proper structure"
        println("and checking some occurrences[0] fields, all but uuid is optional:")
        with(response.data.occurrences[0]) {
            uuid
            println("occurrences[0] contains uuid: ${uuid}")
            if (occurrenceID) {
                println("occurrences[0] contains occurrenceID: ${occurrenceID}")
            }
            if (taxonConceptID) {
                println("occurrences[0] contains taxonConceptID: ${taxonConceptID}")
            }
            if (scientificName) {
                println("occurrences[0] contains scientificName: ${scientificName}")
            }
            if (dataProviderUid) {
                println("occurrences[0] contains dataProviderUid: ${dataProviderUid}")
            }
            if (dataResourceUid) {
                println("occurrences[0] contains dataResourceUid: ${dataResourceUid}")
            }
        }
    }

    def "Search Taxon Macropus should show all records of genus Macropus and not frogs, plants or any other riff-raff"() {
        String queryString = "q=lsid%3Aurn%3Alsid%3Abiodiversity.org.au%3Aafd.taxon%3Ab1d9bf29-648f-47e6-8544-2c2fbdf632b1&facets=genus"
        when: "Search Taxon Macropus"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "All species occurrences on first page are Macropus"
        response.data.occurrences.findAll {it.scientificName.contains("Macropus")}.size() == response.data.occurrences.size()
        println("and all species occurrences on first page are Macropu")

        and: "All genus are Macropus"
        response.data.facetResults.size() == 1

        with(response.data.facetResults[0]) {
            fieldName == "genus"
            fieldResult.size() == 1
            fieldResult[0].label == "Macropus"
            fieldResult[0].count == response.data.totalRecords
        }
        println("and all genus are Macropus")

        and: "queryTitle is GENUS: Macropus"
        response.data.queryTitle == "<span class='lsid' id='urn:lsid:biodiversity.org.au:afd.taxon:b1d9bf29-648f-47e6-8544-2c2fbdf632b1'>GENUS: Macropus</span>"
        println("and has correct query title: ${response.data.queryTitle}")
    }

    def "Text seach Macropus should show all records of genus Macropus and a scattering of other species with the name macropus in them scattered across the kingdoms. There should be Plantae and Fungi amongst them"() {
        String queryString = "q=text%3AMacropus&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name&facets=genus&facets=kingdom"
        when: "Search Text Macropus"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        with (response.data) {
            and: "Total records is at least 266109"
            totalRecords >= 266109 * 0.98 // 2% margin error from current production data
            println("and total records is at least 266109")

            and: "Some species occurrences on first page are macropus"
            occurrences.findAll { it.scientificName?.toLowerCase()?.contains("macropus") }.size() > 0
            println("and some species occurrences on first page are macropus")

            and: "Has 4 Kingdoms"
            def kingdomFacet = facetResults.find { it.fieldName == "kingdom" }?.fieldResult
            kingdomFacet != null
            kingdomFacet.size() == 4
            println("and has four Kingdoms")

            and: "Contains Kingdom Animalia"
            def animaliaKingdom = kingdomFacet.find { it.label == "Animalia" }
            animaliaKingdom != null
            animaliaKingdom.count >= 264515 * 0.98 // 2% margin error from current production data
            println("and contains kingdom Animalia")

            and: "also has some Fungi Kingdoms"
            def fungiKingdom = kingdomFacet.find { it.label == "Fungi" }
            fungiKingdom != null
            fungiKingdom.count >= 10 * 0.98 // 2% margin error from current production data
            println("and also has some Fungi Kingdoms")

            and: "also has some Plantae Kingdom"
            def plantaeKingdom = kingdomFacet.find { it.label == "Plantae" }
            plantaeKingdom != null
            plantaeKingdom.count >= 327 * 0.98 // 2% margin error from current production data
            println("and also has some Plantae Kingdom")

            and: "queryTitle is text:Macropus"
            queryTitle == "text:Macropus"
            println("and has correct query title: ${queryTitle}")
        }
    }

    def "Search for Raw/Provided Scientific Name 'Osphranter rufus' should turn up assorted red kangaroos"() {
        String queryString = "q=raw_name%3A%22Osphranter%20rufus%22&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name"
        when: "Search for Raw/Provided Scientific Name 'Osphranter rufus'"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        with (response.data) {
            and: "Total records is at least 333"
            totalRecords >= 333 * 0.98 // 2% margin error from current production data
            println("and total records is at least 333")

            and: "All species occurrences on first page are Osphranter rufus"
            occurrences.findAll { it.scientificName?.contains("Osphranter rufus") || it.scientificName?.contains("Macropus") }.size() == response.data.occurrences.size()
            println("and all species occurrences on first page are Osphranter rufus")

            and: "Some vernacular name occurrences on first page are 'Red Kangaroo'"
            occurrences.findAll { it.vernacularName?.contains("Red Kangaroo") }.size() > 0
            println("and some vernacular name occurrences on first page are 'Red Kangaroo'")

            and: "Has 2 Taxons"
            def taxonNameFacet = facetResults.find { it.fieldName == "taxon_name" }?.fieldResult
            taxonNameFacet != null
            taxonNameFacet.size() == 2
            println("and has two Taxons")

            and: "Contains Taxon Macropus"
            def animaliaKingdom = taxonNameFacet.find { it.label == "Macropus" }
            animaliaKingdom != null
            animaliaKingdom.count >= 6 * 0.98 // 2% margin error from current production data
            println("and contains Taxon Macropus")

            and: "also has Taxon Osphranter rufus"
            def fungiKingdom = taxonNameFacet.find { it.label == "Osphranter rufus" }
            fungiKingdom != null
            fungiKingdom.count >= 242 * 0.98 // 2% margin error from current production data
            println("and also has Taxon Osphranter rufus")

            and: "queryTitle is raw_name:\"Osphranter rufus\""
            queryTitle == "raw_name:\"Osphranter rufus\""
            println("and has correct query title: ${queryTitle}")
        }
    }

    def "Search for Raw/Provided Scientific Name 'Acacia dealbata' is ok"() {
        String queryString  = "q=raw_name%3A%22Acacia%20dealbata%22&start=0&pageSize=50&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name"
        when: "Search for Raw/Provided Scientific Name 'Acacia dealbata'"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "Total records is at least 25,719"
        response.data.totalRecords >= 25719 * 0.98 // 2% margin error from current production data
        println("and total records is at least 25,719")

        when: "Drill down on occurrences"
        def subspDealbataOccurrences = response.data.occurrences.findAll {it.scientificName?.contains("Acacia dealbata")}
        then: "An occurrence of 'Acacia dealbata' should be present"
        subspDealbataOccurrences.size() > 0
        println("and an occurrence of 'Acacia dealbata' should be present")

        and: "linked to the correct Taxon ID"
        subspDealbataOccurrences.each {
            String occurrencePath = "occurrence/${it.uuid}"
            def occurrenceResponse = restClient.get(
                    path: occurrencePath
            )
            occurrenceResponse.data.processed.classification.taxonConceptID == "https://id.biodiversity.org.au/taxon/apni/51286863"
        }
        println("and linked to the correct Taxon ID: \"https://id.biodiversity.org.au/taxon/apni/51286863\"")

        and: "Has at least 8 Taxons"
        def taxonNameFacet = response.data.facetResults.find {it.fieldName == "taxon_name"}?.fieldResult
        taxonNameFacet != null
        taxonNameFacet.size() >= 8
        println("and has at least eight Taxons")

        and: "Contains Taxon 'Acacia dealbata subsp. dealbata'"
        def subspDealbata = taxonNameFacet.find {it.label == "Acacia dealbata subsp. dealbata"}
        subspDealbata != null
        subspDealbata.count >= 9877 * 0.98 // 2% margin error from current production data
        println("and contains Taxon 'Acacia dealbata subsp. dealbata'")

        and: "queryTitle is raw_name:\"Acacia dealbata\""
        response.data.queryTitle == "raw_name:\"Acacia dealbata\""
        println("and has correct query title: ${response.data.queryTitle}")
    }

    def "Search for species group Mammals should not have any weird kingdoms, phyla or classes floating about"() {
        String queryString  = "q=species_group%3AMammals&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=class&facets=phylum&facets=kingdom&flimit=10"

        when: "Search for species group Mammals"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        with(response.data) {
            and: "Total records is at least 2,713,131"
            totalRecords >= 2713131 * 0.98 // 2% margin error from current production data
            println("and total records is at least 2,713,131")

            and: "Has 1 Kingdom"
            def kingdomFacet = facetResults.find { it.fieldName == "kingdom" }?.fieldResult
            kingdomFacet != null
            kingdomFacet.size() == 1
            println("and has one Kingdom")

            and: "Contains Kingdom Animalia"
            def animaliaKingdom = kingdomFacet.find { it.label == "Animalia" }
            animaliaKingdom != null
            animaliaKingdom.count >= 2713131 * 0.98 // 2% margin error from current production data
            println("and contains Kingdom Animalia")

            and: "Has 1 Phylum"
            def phylumFacet = facetResults.find { it.fieldName == "phylum" }?.fieldResult
            phylumFacet != null
            phylumFacet.size() == 1
            println("and had one Phylum")

            and: "Contains Phylum Chordata"
            def chordataPhylum = phylumFacet.find { it.label == "Chordata" }
            chordataPhylum != null
            chordataPhylum.count >= 2713131 * 0.98 // 2% margin error from current production data
            println("and contains Phylum Chordata")

            and: "Has 2 Classes"
            def classFacet = facetResults.find { it.fieldName == "class" }?.fieldResult
            classFacet != null
            classFacet.size() == 1
            println("and had two Classes")

            and: "Contains Class Mammalia"
            def mammaliaClass = classFacet.find { it.label == "Mammalia" }
            mammaliaClass != null
            mammaliaClass.count >= 2255099 * 0.98 // 2% margin error from current production data
            println("and contains Class Mammalia")

            and: "queryTitle is Lifeform:Mammals"
            queryTitle == "Lifeform:Mammals"
            println("and has correct query title: ${response.data.queryTitle}")
        }
    }

    def "Search for the Australian Museum Entomology Collection should mostly show class Insecta, kingdom Animalia etc. There will be some errors as stuff like Genus nov. gets mismatched and some molluscs and other oddities."() {
        String queryString = "q=text%3AMacropus&start=0&pageSize=20&sort=first_loaded_date&dir=desc&qc=&facets=taxon_name&facets=genus&facets=kingdom"
        when: "Search Text Macropus"
        def response = restClient.get(
                path: path,
                queryString: queryString
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        with(response.data) {
            and: "Total records is at least 266,109"
            totalRecords >= 266109 * 0.98 // 2% margin error from current production data
            println("and total records is at least 266,109")

            and: "Some species occurrences on first page are macropus"
            occurrences.findAll { it.scientificName?.toLowerCase()?.contains("macropus") }.size() > 0
            println("and some species occurrences on first page are macropus")

            and: "Has 4 Kingdoms"
            def kingdomFacet = facetResults.find { it.fieldName == "kingdom" }?.fieldResult
            kingdomFacet != null
            kingdomFacet.size() == 4
            println("and had four Kingdoms")

            and: "Contains Kingdom Animalia"
            def animaliaKingdom = kingdomFacet.find { it.label == "Animalia" }
            animaliaKingdom != null
            animaliaKingdom.count >= 264515 * 0.98 // 2% margin error from current production data
            println("and contains Kingdom Animalia")

            and: "also has some Fungi Kingdoms"
            def fungiKingdom = kingdomFacet.find { it.label == "Fungi" }
            fungiKingdom != null
            fungiKingdom.count >= 10
            println("and also has some Fungi Kingdoms")

            and: "also has some Plantae Kingdoms"
            def plantaeKingdom = kingdomFacet.find { it.label == "Plantae" }
            plantaeKingdom != null
            plantaeKingdom.count >= 327
            println("and also has some Plantae Kingdoms")

            and: "queryTitle is text:Macropus"
            queryTitle == "text:Macropus"
            println("and has correct query title: ${response.data.queryTitle}")
        }
    }
}
