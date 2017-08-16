package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, PostServeAction, ResponseTransformer}
import com.github.tomakehurst.wiremock.http.{Request, Response, ResponseDefinition}
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener
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

    wireMockServer = new WireMockServer(options().port(port).extensions(new ValidateStateTransitionTransformer()))
    wireMockListener =  new SwaggerValidationListener(Source.fromFile(s"$jsonPath/openApi.json").mkString)
    wireMockServer.addMockServiceRequestListener(wireMockListener)
    println ( s"Wiremock Starting on port $port")

    // set mock responses
    wireMockServer.stubFor(get(urlMatching(".*/it"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "get")
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$jsonPath/happy.json").mkString)
          .withStatus(200)))

    wireMockServer.stubFor(delete(urlMatching(".*/it$"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "delete")
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully deleted")
          .withStatus(200))
    )

    wireMockServer.stubFor(post(urlMatching(".*/it"))
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "post")
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully added")
          .withStatus(201)))

    wireMockServer.stubFor(post(urlMatching(".*/it"))
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .withHeader("scenario", equalTo("bad"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "post")
          .withHeader("Content-Type", "application/json")
          .withBody("Bad scenario")
          .withStatus(301)))

    wireMockServer.stubFor(post(urlMatching(".*/it"))
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .withHeader("scenario", absent())
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "post")
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully added")
          .withStatus(201)))



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


  // simple state transition validator
  object ValidateStateModel {

    var count = 0
    var errorsList = ListBuffer[String]()

    def isValidStateTransition(action: String) : Boolean = {

      if (action == "get") {
        // ok
      } else if (List("put", "post").contains(action)) {
        count = count + 1
      } else if (action == "delete" && count==0) {
        errorsList += "Error: - Cannot delete from empty state ... doh!"
        return false
      } else if (action=="delete" && count>0) {
        count = count -1
      }
      true
    }
  }


  // validate the state transitions
  class ValidateStateTransitionTransformer extends ResponseTransformer {

    override def transform(request :Request, response: Response, files :FileSource, parameters: Parameters) : Response = {

      // if not valid mangle the response else let it be
      if (ValidateStateModel.isValidStateTransition(parameters.getString("action"))==false) {

        Response.Builder.like(response)
          .but()
          .body("Invalid state transition for action:" + parameters.getString("action"))
          .status(500)
          .build();
      } else {
        Response.Builder.like(response).build()
      }
    }

    override def getName() : String = {
      "validateStateTransition"
    }
  }
}



