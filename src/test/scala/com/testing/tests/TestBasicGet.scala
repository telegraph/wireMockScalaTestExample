package com.testing.tests

import java.io.File
import java.net.{HttpURLConnection, URL}
import java.util

import com.testing.mock.WireMockServer
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import org.scalatest._
import java.net.{HttpURLConnection, URL}

import com.atlassian.oai.validator.wiremock.SwaggerValidationListener.SwaggerValidationException
import org.apache.http.util.EntityUtils

import scala.io.Source


/**
  * Created by toorap on 01/08/2017.
  * Example usage of Wiremock
  */
class TestBasicGet extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val PORT= "8080"
  val jsonPath = getClass.getResource("").getPath + "/../../../../../src/resources"
  val url = s"http://localhost:$PORT/resource/it";

  override def beforeAll() {
    WireMockServer.configue(PORT, jsonPath)
  }

  override def afterEach() {
    WireMockServer.stop
  }


  feature("Basic CRUD for it") {

    scenario("Happy path GET") {

      Given("I have Wiremock running")

        WireMockServer.start

      When("I call GET on the stubbed endpoint")

        val get = new HttpGet(url)
        get.setHeader("Accept", "text/html")
        val response = (new DefaultHttpClient).execute(get)
        val responseBody = EntityUtils.toString(response.getEntity())

      Then("the it should respond with the correct payload")

        responseBody should equal (Source.fromFile(s"$jsonPath/happy.json").mkString)
    }


    scenario("Happy path POST") {

      Given("I have Wiremock running")

        WireMockServer.start

      When("I call POST on the stubbed endpoint")

        val post = new HttpPost(url)
        post.setHeader("Content-type", "application/json")
        post.setEntity(new StringEntity("{ \"this\": \"this\", \"other\": \"more\" }"))
        val response = (new DefaultHttpClient).execute(post)

      Then("the it should respond with the correct payload")

        response.getStatusLine.getStatusCode should equal(201)
    }

    scenario("Happy path DELETE") {

      Given("I have Wiremock running")

        WireMockServer.start

      When("I call DELETE on the stubbed endpoint")

        val delete = new HttpDelete(url)
        delete.setHeader("Content-type", "application/json")
        val response = (new DefaultHttpClient).execute(delete)
        val responseBody = EntityUtils.toString(response.getEntity())

      Then("the it should respond with the correct payload")

        responseBody should equal("Successfully deleted")
    }

    scenario("Illogical state transtion for DELETE") {

      Given("I have Wiremock running and I have deleted it")

        WireMockServer.start

      When("I call DELETE on the stubbed endpoint")

        val delete = new HttpDelete(url)
        delete.setHeader("Content-type", "application/json")
        val response = (new DefaultHttpClient).execute(delete)
        val responseBody = EntityUtils.toString(response.getEntity())

      Then("the it should return an error")

        responseBody should equal("Invalid state transition for action:delete")

    }

    scenario("Bad contract POST") {

      Given("I have Wiremock running")

      WireMockServer.start

      When("I call POST on the stubbed endpoint")

        val post = new HttpPost(url)
        post.setHeader("Content-type", "application/json")
        post.setEntity(new StringEntity("{ \"this\": \"this\", \"other\": 1 }"))
        val response = (new DefaultHttpClient).execute(post)

      Then("the it should respond with an error")

        var exception: Exception = null
        try {
          WireMockServer.stop
        } catch {
         case ex: SwaggerValidationException =>
            exception = ex
        }
        exception should not equal (null)
        WireMockServer.wireMockListener.reset()
    }


  }
}
