package au.org.ala.biocache.service

import au.org.ala.test.spock.EnvironmentEndPoint
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

import javax.imageio.ImageIO
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class AssortedServicesSpec extends spock.lang.Specification {
    private static final String APIKEY_TEST = "9404957c-bd30-44f5-a501-107e0b4e581e"
    private static final String APIKEY_PROD = "b60aab2c-911e-4695-9859-ea9ef428120a"

    @EnvironmentEndPoint(envVariable = "testHostUrl")
    String baseUrl

    RESTClient restClient

    private String apiKey

    def setup() {
//        baseUrl = "https://biocache-test.ala.org.au/ws/"
        if (baseUrl == "https://biocache-test.ala.org.au/ws/"){
            apiKey = APIKEY_TEST
        } else if (baseUrl == "https://biocache.ala.org.au/ws/"){
            apiKey = APIKEY_PROD
        } else {
            println("No apiKey available for downloading, some tests will fail.")
        }
        restClient = new RESTClient(baseUrl, ContentType.JSON)
        println("================================================================")
        println(("Setup preparation for: ${specificationContext.currentIteration.name}"))
     }

    def "Get an specific occurrence 794e1d5b-fc14-4b77-a21a-8e73749dc910"() {
        String path = "occurrences/794e1d5b-fc14-4b77-a21a-8e73749dc910"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
        )
        println("When url is:  ${baseUrl}${path}")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        with (response.data) {
            and: "Total records is 1"
            raw.occurrence.individualCount == "1" // 2% margin error from current production data
            println("and total records is 1")

            and: "scientificName is 'Rhipidura leucophrys'"
            raw.classification.scientificName == "Rhipidura leucophrys"
            println("and scientificName is 'Rhipidura leucophrys'")

            and: "vernacularName is 'Willie Wagtail'"
            raw.classification.vernacularName == "Willie Wagtail"
            println("and vernacularName is 'Willie Wagtail'")

            and: "Unchecked assertions >= 45"
            systemAssertions.unchecked.size() >= 45
            println("and unchecked assertions >= 45")

            and: "Warning assertions >= 3"
            systemAssertions.warning.size() >= 3
            println("and warning assertions >= 3")

            and: "Passed assertions >= 28"
            systemAssertions.passed.size() >= 28
            println("and passed assertions >= 28")
        }
    }

    def "Get Static map for the red kangaroo"() {
        String path = "density/map"
        String queryString = "q=Macropus+rufus"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null
        println("and it's a PNG image")

        and: "It is at least 66KB in size"
        response.data.buf.length >= 66000
        println("and it is at least 66KB in size")
    }

    def "Download Macropus rufus static map"() {
        String path = "mapping/wms/image"
        String queryString = "pradiusmm=1&extents=96.173828125,-47.11468820158343,169.826171875,-2.5694811631203973&scale=on&outline=true&fileName=MyMap.jpg&pcolour=3531FF&outlineColour=0x000000&q=Macropus+rufus&dpi=300&format=jpg&baselayer=world&popacity=1"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        response.contentType == "application/octet-stream"
        println("and it has correct content type")

        and: "It is at least 52KB in size"
        response.data.buf.length >= 52000
        println("and it is at least 52KB in size")

        and: "It's an image"
        ImageIO.read(response.data) != null
        println("and it is an image")
    }

    def "Scatterplot example (Example for Macropus Rufus and environmental layers Temperature - annual mean (Bio01) and Precipitation - annual (bio12))"() {
        String path = "scatterplot"
        String queryString = "q=Macropus%20Agilis&y=el893&x=el874&pointradius=2&height=512&pointcolour=FF0000&width=512"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null
        println("and it is a PNG image")

        and: "It is at least 180KB in size"
        response.data.buf.length >= 18000
        println("and it is at least 180KB in size")
    }

    def "An example showing layers."() {
        String path = "ogc/wms/reflect"
        String queryString = "service=WMS&version=1.1.0&request=GetMap&styles=&format=image/png&layers=ALA:occurrences&transparent=true&CACHE=on&CQL_FILTER=qid:1514883477341&SRS=EPSG%3A3857&ENV=color%253Aff0000%253Bname%253Acircle%253Bsize%253A4%253Buncertainty%253A1%253Bopacity%253A1&BBOX=15393194.842039,-4238280.0037665,15393806.338265,-4237668.5075403&WIDTH=256&HEIGHT=256"

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a PNG image"
        response.contentType == "image/png"
        ImageIO.read(response.data) != null
        println("and it is a PNG image")

        and: "It is at least 180 in size"
        response.data.buf.length >= 180
        println("and it is at least 180 in size")
    }

    def "Download all Acacia abbatiana records  with all the default fields and all record issues specifying a \"testing\" reason for the download."() {
        String path = "occurrences/index/download"
        String queryString = "q=Acacia+abbatiana&reasonTypeId=10&apiKey=" + apiKey

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a ZIP file"
        response.contentType == "application/zip"
        println("and it is a ZIP file")

        and: "It is at least 3.5KB in size"
        response.data.buf.length >= 3500
        println("and the file is at least 3.5KB in size")

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")
        println("and the ZIP file contains the expected files")
    }

    def "Shape File Download for genus Macropus agilis"() {
        String path = "occurrences/index/download"
        String queryString = "q=Macropus+agilis&reasonTypeId=10&fileType=shp&apiKey=" + apiKey

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a ZIP file"
        response.contentType == "application/zip"
        println("and it is a ZIP file")

        and: "It is at least 19000 in size"
        response.data.buf.length >= 19000
        println("and the file is at least 19KB in size")

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")
        files.contains("data.zip")
        files.contains("Shape-README.html")
        println("and the ZIP file contains the expected files")
    }

    def "Disable Record Issues in download"() {
        String path = "occurrences/index/download"
        String queryString = "q=genus:Dugong&reasonTypeId=10&qa=none&apiKey=" + apiKey

        println("When testing url is: ${baseUrl}$path?$queryString ")
        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a ZIP file"
        response.contentType == "application/zip"
        println("and it is a ZIP file")

        and: "It is at least 48KB in size"
        response.data.buf.length >= 48000
        println("and the file size is correct")

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")
        println("and the ZIP file contains the expected files")
    }


    def "Download E.gunnii lats and longs"() {
        String path = "occurrences/index/download"
        String queryString = "q=genus:Dugong&reasonTypeId=10&qa=none&apiKey=" + apiKey

        when: "Get occurrence"
        def response = restClient.get(
                path: path,
                queryString: queryString,
                contentType: "application/octet-stream"
        )
        println("When testing url is: ${baseUrl}$path?$queryString ")

        then: "Status is 200"
        response.status == 200
        println("Then: response status is: ${response.status}")

        and: "It's a ZIP file"
        response.contentType == "application/zip"
        println("and it is a ZIP file")

        and: "It is at least 2.4KB in size"
        response.data.buf.length >= 2400
        println("and the file size is correct")

        and: "Zip file contains the expected files"
        ArrayList<String> files = fileNamesInArchive(response.data)
        !files.isEmpty()
        files.contains("citation.csv")
        files.contains("data.csv")
        files.contains("headings.csv")
        files.contains("README.html")
        println("and the ZIP file contains the expected files")
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
}