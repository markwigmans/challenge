package sva

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * Created by mawi on 23/07/2016.
  */
object Config {

  // URL of the System Under Test
  val httpConf = http.baseURL("http://localhost:8080/")

  // Number of accounts
  val accounts = 297000
  val merchants = 3000

  // Number of runs per user during initialisation
  val initUsers = 100
}
