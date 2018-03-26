# biocache-tests  

**biocache-tests** is a Groovy/Spock project that seeks to gather all functional (integration) tests that will be used to perform a sanity chcek for an environment.

At this stage this project is focused on the [Biocache Service section from the Biocache Infrastructure Testing Plan 2018](https://wiki.ala.org.au/wiki/Biocache-Infrastructure-Testing-Plan-2018)

## Usage
A typical invocation of the tests will look like the line below

 ```gradle  -DtestHostUrl=https://devt.ala.org.au/biocache-service/ws/ -DreferenceHostUrl=https://biocache-test.ala.org.au/ws/ test``` 

Where:
- testHostUr is the host under test
- referenceHostUrl is an additional host/environment that will be used to compare calls run by SideBySideTest suite

## TODO
* Implement a look up to translate ala-hub taxa=XYZ query to biocache 
* Hook to a Jenkins job for a particular environment

