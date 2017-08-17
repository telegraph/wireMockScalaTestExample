package com.testing.mock


/**
  * Created by toorap on 01/08/2017.
  */
object WireMockDriver {

  def main(args : Array[String]) {

    // passed swagger file, port, canned responses file path
    MyStub.configureStub(args(0), args(1).toInt, args(2))
    MyStub.start

  }
}
