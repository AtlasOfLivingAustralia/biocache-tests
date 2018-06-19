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

import javax.imageio.ImageIO
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
//        baseUrl = "https://biocache-test.ala.org.au/ws/" //Uncomment and adjust for testing a single method test from the IDE
//        baseUrl = "https://biocache.ala.org.au/ws/" //Uncomment and adjust for testing a single method test from the IDE
//        baseUrl = "https://biocache-clustered.ala.org.au/ws/" //Uncomment and adjust for testing a single method test from the IDE
//        baseUrl = "https://devt.ala.org.au/biocache-service/ws/" //Uncomment and adjust for testing a single method test from the IDE
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        log.info("Test: ${specificationContext.currentIteration.name}")

    }

    private ArrayList<String> fileNamesInArchive(InputStream is) {
        List<String> files = []
        ZipInputStream zi = new ZipInputStream(is)
        ZipEntry entry
        while ((entry = zi.getNextEntry()) != null) {
            files << entry.name
        }
        return files
    }

    def "Get an specific occurrence 794e1d5b-fc14-4b77-a21a-8e73749dc910"() {
        String path = "occurrences/794e1d5b-fc14-4b77-a21a-8e73749dc910"

        log.info("Testing [${baseUrl}$path] ")

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
        )

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

    def "Get Static map for the red kangaroo"() {
        String path = "density/map"
        String queryString = "q=Macropus+rufus"

        log.info("Testing [${baseUrl}$path?$queryString] ")

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null


        and: "It is at least 66KB in size"
        response.data.buf.length >= 66000

    }

    def "Download Macropus rufus static map"() {
        String path = "mapping/wms/image"
        String queryString = "pradiusmm=1&extents=96.173828125,-47.11468820158343,169.826171875,-2.5694811631203973&scale=on&outline=true&fileName=MyMap.jpg&pcolour=3531FF&outlineColour=0x000000&q=Macropus+rufus&dpi=300&format=jpg&baselayer=world&popacity=1"

        log.info("Testing [${baseUrl}$path?$queryString] ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200


        response.contentType == "application/octet-stream"

        and: "It is at least 52KB in size"
        response.data.buf.length >= 52000

        and: "It's an image"
        ImageIO.read(response.data) != null

    }

    def "Scatterplot example (Example for Macropus Rufus and environmental layers Temperature - annual mean (Bio01) and Precipitation - annual (bio12))"() {
        String path = "scatterplot"
        String queryString = "q=Macropus%20Agilis&y=el893&x=el874&pointradius=2&height=512&pointcolour=FF0000&width=512"

        log.info("Testing [${baseUrl}$path?$queryString] ")

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null


        and: "It is at least 21KB in size"
        response.data.buf.length >= 21000

    }

    def "An example showing layers."() {
        String path = "ogc/wms/reflect"
        String queryString = "service=WMS&version=1.1.0&request=GetMap&styles=&format=image/png&layers=ALA:occurrences&transparent=true&CACHE=on&CQL_FILTER=qid:1514883477341&SRS=EPSG%3A3857&ENV=color%253Aff0000%253Bname%253Acircle%253Bsize%253A4%253Buncertainty%253A1%253Bopacity%253A1&BBOX=15393194.842039,-4238280.0037665,15393806.338265,-4237668.5075403&WIDTH=256&HEIGHT=256"

        log.info("Testing [${baseUrl}$path?$queryString] ")

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null


        and: "It is at least 415 in size"
        response.data.buf.length >= 1500

    }

    def "Download all Acacia abbatiana records  with all the default fields and all record issues specifying a \"testing\" reason for the download."() {
        String path = "occurrences/index/download"
        String queryString = "q=Acacia+abbatiana&reasonTypeId=10"


        log.info("Testing [${baseUrl}$path?$queryString] ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a ZIP file"
        response.contentType == "application/zip"

        and: "It is at least 3.5KB in size"
        response.data.buf.length >= 3500

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")

    }

    def "Shape File Download for genus Macropus agilis"() {
        String path = "occurrences/index/download"
        String queryString = "q=Macropus+agilis&reasonTypeId=10&fileType=shp"

        log.info("Testing [${baseUrl}$path?$queryString] ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a ZIP file"
        response.contentType == "application/zip"

        and: "It is at least 1.6MB in size"
        response.data.buf.length >= 1600000

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")
        files.contains("data.zip")
        files.contains("Shape-README.html")

    }

    def "Disable Record Issues in download"() {
        String path = "occurrences/index/download"
        String queryString = "q=genus:Dugong&reasonTypeId=10&qa=none"

        log.info("Testing [${baseUrl}$path?$queryString] ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a ZIP file"
        response.contentType == "application/zip"

        and: "It is at least 48KB in size"
        response.data.buf.length >= 48000

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")

    }


    def "Download E.gunnii lats and longs"() {
        String path = "occurrences/index/download"
        String queryString = "q=genus:Dugong&reasonTypeId=10&qa=none"

        log.info("Testing [${baseUrl}$path?$queryString] ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200

        and: "It's a ZIP file"
        response.contentType == "application/zip"

        and: "It is at least 2.4KB in size"
        response.data.buf.length >= 2400

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")

    }
}