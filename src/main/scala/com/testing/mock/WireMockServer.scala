package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.Admin
import com.github.tomakehurst.wiremock.extension.{Parameters, PostServeAction, ResponseTransformer}
import com.github.tomakehurst.wiremock.http.{Request, Response, ResponseDefinition}
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

import scala.collection.mutable.ListBuffer
import scala.io.Source


/**
 * @author ${parsh.toora}
  */

object WireMockServer {

  var wireMockServer: WireMockServer = null;

  def start = {
    wireMockServer.start()
  }

  def stop = {
    wireMockServer.stop()
  }

  def configue(inputPort:String)  = {
    var port: Int = 8080
    if (inputPort != null)
      port = inputPort.toInt

    println ( s"running on port $port")

    wireMockServer = new WireMockServer(options().port(port).extensions(new ValidateStateTransitionTransformer()))
  }

  def setUpMockResponses(filePath:String) = {

    var jsonPath = "home"
    if (filePath!=null)
      jsonPath=filePath

    wireMockServer.stubFor(get(urlMatching(".*/happy$"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "happy")
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$jsonPath/happy.json").mkString)
          .withStatus(200)))

    wireMockServer.stubFor(get(urlMatching(".*/delete$"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "delete")
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully deleted")
          .withStatus(200)))

    wireMockServer.stubFor(post(urlMatching(".*/post$"))
      .withRequestBody(matchingJsonPath("$.this"))
      .withRequestBody(matchingJsonPath("$.other"))
      .willReturn(
        aResponse()
          .withTransformerParameter("action", "post")
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully added")
          .withStatus(201)))

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


  class ValidateStateTransitionTransformer extends ResponseTransformer {

    override def transform(request :Request, response: Response, files :FileSource, parameters: Parameters) : Response = {

      if (ValidateStateModel.isValidStateTransition(parameters.getString("action"))==false) {

        Response.Builder.like(response)
          .but()
          .body("Invalid state transition")
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



