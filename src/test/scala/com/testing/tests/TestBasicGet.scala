package com.testing.tests


import com.testing.mock.{BaseWireMockServer, MyStub}
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import org.scalatest._
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener.SwaggerValidationException
import com.github.tomakehurst.wiremock.stubbing.Scenario
import org.apache.http.util.EntityUtils

import scala.io.Source


/**
  * Created by toorap on 01/08/2017.
  * Example usage of Code as Contract
  */
class TestBasicGet extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val PORT= 8080
  val jsonPath = getClass.getResource("").getPath + "/../../../../../src/resources"
  val swaggerSchema = jsonPath+"/openApi.json"
  val url = s"http://localhost:$PORT/resource/it";


  override def beforeAll() {
    MyStub.configureStub(swaggerSchema, PORT, jsonPath)
  }

  override def afterEach() {
    MyStub.stop
  }

  /*
  The test sceanrios
   */
  feature("Basic CRUD for it") {

    scenario("Happy path GET") {

      Given("I have Wiremock running")

        MyStub.start

      When("I call GET on the stubbed endpoint")

        val get = new HttpGet(url)
        get.setHeader("Accept", "text/html")
        val response = (new DefaultHttpClient).execute(get)
        val responseBody = EntityUtils.toString(response.getEntity())

      Then("the it should respond with the correct payload")

        responseBody should equal (Source.fromFile(s"$jsonPath/happy.json").mkString)
        MyStub.validateContract
    }


    scenario("Happy path POST") {

      Given("I have Wiremock running")

        MyStub.start

      When("I call POST on the stubbed endpoint")

        val post = new HttpPost(url)
        post.setHeader("Content-type", "application/json")
        post.setEntity(new StringEntity("{ \"this\": \"this\", \"other\": \"more\" }"))
        val response = (new DefaultHttpClient).execute(post)

      Then("the it should respond with the correct payload")

        response.getStatusLine.getStatusCode should equal(201)
        MyStub.validateContract
    }

    scenario("Happy path DELETE") {

      Given("I have Wiremock running")

      MyStub.start

      When("I call DELETE on the stubbed endpoint")

        val delete = new HttpDelete(url)
        delete.setHeader("Content-type", "application/json")
        val response = (new DefaultHttpClient).execute(delete)
        val responseBody = EntityUtils.toString(response.getEntity())

      Then("the it should respond with the correct payload")

        responseBody should equal("Successfully deleted")
        MyStub.validateContract
    }

    scenario("Illogical state transtion for DELETE") {

      Given("I have Wiremock running and I have deleted it")

      MyStub.start

      When("I call DELETE on the stubbed endpoint")

        val delete = new HttpDelete(url)
        delete.setHeader("Content-type", "application/json")
        val response = (new DefaultHttpClient).execute(delete)

      Then("the it should return an error")

        response.getStatusLine.getStatusCode should equal (404)

    }

    scenario("Bad contract POST on request") {

      Given("I have Wiremock running")

      MyStub.start

      When("I call POST on the stubbed endpoint with in invalid payload")

        val post = new HttpPost(url)
        post.setHeader("Content-type", "application/json")
        post.setEntity(new StringEntity("{ \"this\": \"this\", \"other\": 1 }"))
        val response = (new DefaultHttpClient).execute(post)

      Then("it should respond with an error")

        var exception: Exception = null
        try {
          MyStub.validateContract
        } catch {
         case ex: SwaggerValidationException =>
            exception = ex
        }
        exception should not equal (null)
    }

    scenario("Bad contract POST on stub") {

      Given("I have Wiremock running")

      MyStub.start

      When("I call POST on the stubbed endpoint where the stub returns the wrong status")

      val post = new HttpPost(url)
      post.setHeader("Content-type", "application/json")
      post.setHeader("scenario", "bad")
      post.setEntity(new StringEntity("{ \"this\": \"this\", \"other\": \"more\" }"))
      val response = (new DefaultHttpClient).execute(post)

      Then("it should respond with an error")

      var exception: Exception = null
      try {
        MyStub.validateContract
      } catch {
        case ex: SwaggerValidationException =>
          exception = ex
      }
      response.getStatusLine.getStatusCode should equal(301)
      exception should not equal (null)
    }


  }
}
