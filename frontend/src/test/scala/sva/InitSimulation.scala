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

/**
  * Created by mawi on 23/07/2016.
  */
class InitSimulation extends Simulation {

  val initChain =
    exec(http("init simulation").post("reset").check(status.is(200)))

  val accountChain =
    repeat((Config.accounts / Config.initUsers).get) {
      exec(http("create accounts").post("account").body(StringBody("""{}""")).asJSON.check(status.is(202)))
    }

  val merchantChain =
    repeat((Config.merchants / Config.initUsers).get) {
      exec(http("create merchants").post("account").body(StringBody("""{"overdraft" : 100000}""")).asJSON.check(status.is(202)))
    }

  val init = scenario("init").exec(initChain)
  val scn = scenario("create").exec(merchantChain, accountChain)

  setUp(
    init.inject(atOnceUsers(1)).protocols(Config.httpConf)
    , scn.inject(atOnceUsers(Config.initUsers)).protocols(Config.httpConf)
  )
}
