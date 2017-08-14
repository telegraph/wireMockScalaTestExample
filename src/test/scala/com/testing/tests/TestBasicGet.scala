package com.testing.tests

import java.io.File
import java.util

import com.testing.mock.WireMockServer
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
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

       val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/pet/findByStatus?status=available").mkString

      Then("the it should respond with the correct payload")

        resp should equal (Source.fromFile(s"$jsonPath/happy.json").mkString)
    }


    scenario("Happy path POST") {

      Given("I have Wiremock running")

      WireMockServer.start

      When("I call PUT on the stubbed endpoint")

      val url = s"http://localhost:$PORT/resource/post";
      val post = new HttpPost(url)
      post.setHeader("Content-type", "application/json")
      post.setEntity(new StringEntity("{ \"this\": \"data\", \"other\": \"more\" }"))
      val response = (new DefaultHttpClient).execute(post)

      Then("the it should respond with the correct payload")

      response.getStatusLine.getStatusCode should equal(201)
    }

    scenario("Happy path DELETE") {

      Given("I have Wiremock running")

      WireMockServer.start

      When("I call DELETE on the stubbed endpoint")

      val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/delete").mkString

      Then("the it should respond with the correct payload")

      resp should equal("Successfully deleted")
    }

    scenario("Illogical path DELETE") {

      Given("I have Wiremock running")

        WireMockServer.start

      When("I call DELETE on the stubbed endpoint")

        var exception: Exception=null
        try {
          val resp = scala.io.Source.fromURL(s"http://localhost:$PORT/resource/delete").mkString
        } catch {
          case ex: Exception => {
            exception = ex
          }
        }

      Then("the it should return an error")

        exception should not equal (null)

    }


  }
}
