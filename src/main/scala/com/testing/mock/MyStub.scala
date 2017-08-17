package com.testing.mock

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.Scenario

import scala.io.Source

/**
  * Created by toorap on 17/08/2017.
  */
object MyStub extends BaseWireMockServer {


  override var stateTransitions =    Map(
      "delete" -> Map("exists" -> Scenario.STARTED),
      "post" -> Map(Scenario.STARTED -> "exists", "exists" -> "exists"))


  override def setUpMocks(cannedResponsesPath: String): Unit  = {

    // happy path get
    wireMockServer.stubFor(get(urlMatching(".*/it"))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$cannedResponsesPath/happy.json").mkString)
          .withStatus(200)))

    // post matching on body simulating happy path add transitoning state
    stateTransitions("post") foreach {
      case (inState, outState) =>
        wireMockServer.stubFor(post(urlMatching(".*/it"))
          .inScenario("state")
          .whenScenarioStateIs(inState)
          .withRequestBody(matchingJsonPath("$.this"))
          .withRequestBody(matchingJsonPath("$.other"))
          .withHeader("scenario", absent())
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody("Successfully added")
              .withStatus(201))
          .willSetStateTo(outState)
        )
    }

    // delete where items exist in document
    stateTransitions("delete") foreach {
      case (inState, outState) =>
        wireMockServer.stubFor(
        delete(urlMatching(".*/it$"))
        .inScenario("state")
        .whenScenarioStateIs(inState)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("Successfully deleted")
            .withStatus(200))
        .willSetStateTo(outState)
      )
    }

    // post simulating invalid return status
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

    Scenario.inStartedState()
  }

}
