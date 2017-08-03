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
class TestBasicGet extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val PORT= "8080"
  val jsonPath = getClass.getResource("").getPath + "/../../../../../src/resources"

  override def beforeAll() {
    WireMockServer.configue(PORT)
    WireMockServer.setUpMockResponses(jsonPath)
  }

  override def afterEach() {
    WireMockServer.stop
  }


  feature("Basic CRUD") {

    scenario("Happy path GET") {

      Given("I have Wiremock running")

        WireMockServer.start

      When("I call GET on the stubbed endpoint")

       val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/happy").mkString

      Then("the it should respond with the correct payload")

        resp should equal (Source.fromFile(s"$jsonPath/happy.json").mkString)
    }


    scenario("Happy path PUT") {

      Given("I have Wiremock running")

      WireMockServer.start

      When("I call PUT on the stubbed endpoint")

        val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/put").mkString

      Then("the it should respond with the correct payload")

        resp should equal("Successfully added")
    }

//    scenario("Happy path DELETE") {
//
//      Given("I have Wiremock running")
//
//      WireMockServer.start
//
//      When("I call DELETE on the stubbed endpoint")
//
//      val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/delete").mkString
//
//      Then("the it should respond with the correct payload")
//
//      resp should equal("Successfully deleted")
//    }


  }
}
