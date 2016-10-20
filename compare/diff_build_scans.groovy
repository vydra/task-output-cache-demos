#!/usr/bin/env groovy
import groovy.json.*
import groovy.xml.MarkupBuilder


if (args.length < 2) {
    println "diff_build_scans.groovy first-build-scan-url second-build-scan-url"
    System.exit(-1)
}

def leftUrl=args[0]
def rightUrl=args[1]

String buildScanId(url) {
    url.split('/').last()
}

URL buildUrl(baseUrl, buildScanId) {
    new URI("https://" + baseUrl + "/scan-data/" + buildScanId).toURL()
}

String baseUrl(url) {
    new URI(url).host
}

URL buildScanDataUrl(url) {
    def buildScanId = buildScanId(url)
    def baseUrl = baseUrl(url)
    def buildScanUrl = buildUrl(baseUrl, buildScanId)
    println "URL = " + url
    println "buildScanId = " + buildScanId
    println "baseUrl = " + baseUrl
    println "buildScanDataUrl = " + buildScanUrl
    return buildScanUrl
}

def fetch(URL url) {
    println "Fetching $url"
    url.text
}

def toJson(String json) {
    new JsonSlurper().parseText(json.toString())
}

def prettyPrint(json) {
    JsonOutput.prettyPrint(json.toString())
}

def clickableFilePath(path) {
    return new URI("file", "", path.toURI().getPath(), null, null).toString()
}

int reportDiffBetween(leftUrl, rightUrl) {

    def leftScan = fetch(buildScanDataUrl(leftUrl))
    def rightScan = fetch(buildScanDataUrl(rightUrl))

    println prettyPrint(leftScan)

    def leftJson = toJson(leftScan)
    def rightJson = toJson(rightScan)
    def leftBuildScanId = leftJson.data.publicId
    def rightBuildScanId = rightJson.data.publicId

    def reportFile = new File("results/compare_${leftBuildScanId}_to_${rightBuildScanId}.html")
    reportFile.parentFile.mkdirs()
    def fw = new FileWriter(reportFile)
    def html = new MarkupBuilder(fw)

    def leftSummary = leftJson.data.summary
    def rightSummary = rightJson.data.summary

    html.doubleQuotes = true
    html.expandEmptyElements = true
    html.omitEmptyAttributes = false
    html.omitNullAttributes = false
    html.html {
        head {
            title ("Comparison between $leftBuildScanId and $rightBuildScanId")
        }
        body {
            table(border: 1, width: "100%", style: "border:1px solid black;") {
                thead {
                    td "Value"
                    td {
                        a href: leftUrl, leftBuildScanId
                    }
                    td { 
                        a href: rightUrl, rightBuildScanId
                    }
                }

                tr {
                    td "Project Name"
                    td leftSummary.rootProjectName
                    td rightSummary.rootProjectName
                }
                tr {
                    td "Gradle version"
                    td leftSummary.gradleVersion
                    td rightSummary.gradleVersion
                }
            }
        }
    }

    fw.close()
    println clickableFilePath(reportFile)
    return 0
}

System.exit(reportDiffBetween(leftUrl, rightUrl))
