package com.testing.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener
import com.github.tomakehurst.wiremock.stubbing.Scenario

import scala.io.Source

/**
 * @author ${parsh.toora}
  *
  * Alternative mechanisms for validating state can use '.withTransformerParameter("", "")
  */

abstract class BaseWireMockServer {

  var wireMockServer: WireMockServer = null;
  private var wireMockListener: SwaggerValidationListener = null

  def configureStub(inputSwaggerFile: String, inputPort: Int, cannedResponsesPath: String): Unit = {
    // port and swagger defn
    var port: Int = 8080
    if (inputPort != null)
      port = inputPort.toInt
    var swaggerFile = "home"
    if (inputSwaggerFile != null)
      swaggerFile = inputSwaggerFile

    wireMockServer = new WireMockServer(options().port(port))
    wireMockListener = new SwaggerValidationListener(Source.fromFile(swaggerFile).mkString)
    wireMockServer.addMockServiceRequestListener(wireMockListener)
    setUpMocks(cannedResponsesPath)

    println(s"Stub configured for swagger api $swaggerFile running on port $port")
  }

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

  protected def setUpMocks(cannedResponsesPath: String): Unit
}



