package com.testing.tests

import java.io.File

import com.testing.mock.WireMockServer
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import org.scalatest._

import scala.io.Source

/**
  * Created by toorap on 01/08/2017.
  * Example usage of Wiremock
  */
class TestBasicGet extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with Matchers {

  val PORT= "8080"
  val jsonPath = getClass.getResource("").getPath + "/../../../../../src/resources"
  override def beforeAll() {
    WireMockServer.configue(PORT)
    WireMockServer.setUpMockResponses(jsonPath)
  }

  override def afterAll() {
    WireMockServer.stop
  }


  feature("Basic GET") {

    scenario("Happy path") {

      Given("I have wiremock running")

      WireMockServer.start

      When("i call GET on the stubbed endpoint")

       val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/happy").mkString

      Then("the it should respond with the correct paylod")

        resp should equal (Source.fromFile(s"$jsonPath/happy.json").mkString)
    }

  }
}
