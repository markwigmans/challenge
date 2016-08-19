/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sva

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

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
  val initUsers = 200
  val rampUpInit = 10 seconds

  /*
    val accounts = 2970
    val merchants = 30
    val initUsers = 2
  */

  val transfers = 1000000
  val loadUsers = 200
  val rampUpLoad = 10 seconds

  /*
    val transfers = 1000
    val loadUsers = 2
  */

}
