package com.testing.mock


/**
  * Created by toorap on 01/08/2017.
  */
object WireMockDriver {

  def main(args : Array[String]) {

    // passed port and json path
    WireMockServer.configue(args(0))
    WireMockServer.setUpMockResponses(args(1))
    WireMockServer.start

  }
}
