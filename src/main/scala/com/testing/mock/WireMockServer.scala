package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.Admin
import com.github.tomakehurst.wiremock.extension.{Parameters, PostServeAction}
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

import scala.collection.mutable.ListBuffer
import scala.io.Source


/**
 * @author ${parsh.toora}
  */

object WireMockServer {

  var wireMockServer: WireMockServer = null;

  def start = {
    assert(stateCache.errorsList.length==0, stateCache.errorsList.reverse.last)
    wireMockServer.start()
  }

  def stop = {
    assert(stateCache.errorsList.length==0, stateCache.errorsList.reverse.last)
    wireMockServer.stop()
  }

  def configue(inputPort:String)  = {
    var port: Int = 8080
    if (inputPort != null)
      port = inputPort.toInt

    println ( s"running on port $port")

    wireMockServer = new WireMockServer(options().port(port).extensions(new ValidateStateModel()))
  }

  def setUpMockResponses(filePath:String) = {

    var jsonPath = "home"
    if (filePath!=null)
      jsonPath=filePath

    wireMockServer.stubFor(get(urlMatching(".*/happy$"))
      .withPostServeAction("validateState", new ParameterTemplate)
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(Source.fromFile(s"$jsonPath/happy.json").mkString)
          .withStatus(200)))

    wireMockServer.stubFor(get(urlMatching(".*/delete$"))
      .withPostServeAction("validateState", new ParameterTemplate)
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("Successfully deleted")
          .withStatus(200)))

    wireMockServer.stubFor(get(urlMatching(".*/put$"))
      .withPostServeAction("validateState", new ParameterTemplate)
      .willReturn(
        aResponse()
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

  object stateCache {
    var count = 0
    var errorsList = ListBuffer[String]()
  }

  class ValidateStateModel extends PostServeAction {

    override def getName: String = "validateState"

    override def doAction(serveEvent: ServeEvent, admin: Admin, parameters: Parameters) = {

      val action = serveEvent.getRequest.getUrl.split("/").last

      if (action == "get") {
        // ok
      } else if (List("put", "post").contains(action)) {
        stateCache.count = stateCache.count + 1
      } else if (action == "delete" && stateCache.count==0) {
        stateCache.errorsList += "Error in call:" + serveEvent.getRequest.getAbsoluteUrl + " - Cannot delete from empty state ... doh!"
      } else if (action=="delete" && stateCache.count>0) {
        stateCache.count = stateCache.count -1
      }
    }
  }

  class ParameterTemplate extends Serializable {}

}



