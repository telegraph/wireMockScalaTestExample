package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.WireMock._

import scala.io.Source


/**
 * @author ${parsh.toora}
 *         GET http://host:port/__admin/requests will get log
 *         DELETE http://host:port/__admin/requests will delete calls
 *         GET http://localhost:8080/__admin/requests/unmatched will show unmatched
 */
object WireMockServer {

  var wireMockServer: WireMockServer = null;

  def start = {
    wireMockServer.start()
  }

  def stop = {
    wireMockServer.start()
  }

  def setUpMockResponses(filePath:String) = {

    var jsonPath = "home"
    if (filePath!=null)
      jsonPath=filePath


    wireMockServer.stubFor(get(urlMatching(".*/happy$"))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$jsonPath/happy.json").mkString)
          .withStatus(200)))

    wireMockServer.stubFor(get(urlMatching(".*/sad"))
      .willReturn(
        aResponse()
          .withStatus(500)))

    wireMockServer.stubFor(get(urlMatching(".*/redirect"))
      .willReturn(
        temporaryRedirect("/resource/other")))


    wireMockServer.stubFor(get(urlMatching(".*/wait5Secs$"))
      .willReturn(
        aResponse()
          .withFixedDelay(5000)
          .withHeader("Content-Type", "application/json")
          .withBody("{ \"first\":\"Waitied\", \"second\":\"5\" }")
          .withStatus(200)))
  }

  def configue(inputPort:String)  = {
    var port: Int = 8080
    if (inputPort != null)
      port = inputPort.toInt

    println ( s"running on port $port")

    wireMockServer = new WireMockServer(options().port(port))
  }
}
