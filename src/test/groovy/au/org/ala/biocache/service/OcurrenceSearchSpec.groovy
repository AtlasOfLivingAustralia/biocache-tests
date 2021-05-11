package au.org.ala.biocache.service

import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class OcurrenceSearchSpec extends spock.lang.Specification {
    @EnvironmentEndPoint
    String baseUrl

    RESTClient restClient
    String path = "occurrences/search"

    def setup() {
//        baseUrl = "https://devt.ala.org.au/biocache-service/ws/" //Uncomment and adjust for testing a single method test from the IDE
        baseUrl = "https://biocache-test.ala.org.au/ws/"
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        println(("Setup preparation for: ${specificationContext.currentIteration.name}"))
    }

    def "Search all records"() {
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
}
