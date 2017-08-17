package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, PostServeAction, ResponseTransformer}
import com.github.tomakehurst.wiremock.http.{Request, Response, ResponseDefinition}
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener
import com.github.tomakehurst.wiremock.stubbing.Scenario

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
 * @author ${parsh.toora}
  */

object WireMockServer {

  var wireMockServer: WireMockServer = null;
  var wireMockListener : SwaggerValidationListener = null
  val validationListener = null

  // start server
  def start = {
    wireMockServer.start()
  }

  // normally this would be the step that catches the contract validation errors
  // but for this example we are checking in the tests explicitly
  // there are alternative approaches
  def stop = {
    //validateContract
    wireMockServer.stop
  }

  // validate contract
  def validateContract = {
    wireMockListener.assertValidationPassed()
    wireMockListener.reset()
  }

  // configure  wiremock and configure validators and configure responses
  def configue(inputPort:String, filePath:String)  = {

    // port and swagger defn
    var port: Int = 8080
    if (inputPort != null)
      port = inputPort.toInt
    var jsonPath = "home"
    if (filePath!=null)
      jsonPath=filePath

    wireMockServer = new WireMockServer(options().port(port))
    wireMockListener =  new SwaggerValidationListener(Source.fromFile(s"$jsonPath/openApi.json").mkString)
    wireMockServer.addMockServiceRequestListener(wireMockListener)
    println ( s"Wiremock Starting on port $port")

    // set mock responses

    // happy path get
    wireMockServer.stubFor(get(urlMatching(".*/it"))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$jsonPath/happy.json").mkString)
          .withStatus(200)))

    // delete where items exist in document
    wireMockServer.stubFor(
      delete(urlMatching(".*/it$"))
        .inScenario("")
        .whenScenarioStateIs("exists")
        .willReturn(
          aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully deleted")
          .withStatus(200))
        .willSetStateTo(Scenario.STARTED)
    )

    // delete when state is no items in document
    wireMockServer.stubFor(
      delete(urlMatching(".*/it$"))
        .inScenario("")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("Nothing to delete")
            .withStatus(500))
    )

    // post matching on body simulating happy path add
    wireMockServer.stubFor(post(urlMatching(".*/it"))
      .inScenario("")
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .withHeader("scenario", absent())
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully added")
          .withStatus(201))
        .willSetStateTo("exists")
    )

    // post similtaing invalid return status
    wireMockServer.stubFor(post(urlMatching(".*/it"))
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .withHeader("scenario", equalTo("bad"))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("Bad scenario")
          .withStatus(301)))


    // examples of other usages
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
}



