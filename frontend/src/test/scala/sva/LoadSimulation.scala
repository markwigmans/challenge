/**
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
class LoadSimulation extends Simulation {

  val scn = scenario("transfers").repeat((Config.transfers / Config.loadUsers).get) {
    randomSwitch(30.0 -> ToMerchant.process, 70.0 -> FromMerchant.process)
  }

  setUp(
    scn.inject(atOnceUsers(Config.loadUsers)).protocols(Config.httpConf)
  )
}

object ToMerchant {
  val process =
    exec(session => {
      val from = Utils.randInt(Config.merchants + 1, Config.merchants + Config.accounts + 1)
      val to = Utils.randInt(1, Config.merchants + 1)
      val amount = Utils.randInt(1, 1000)
      session
        .set("from", from)
        .set("to", to)
        .set("amount", amount)
    })
      .exec(http("transfer").post("transfer").body(StringBody("""{"from" : "${from}","to": "${to}","amount":"${amount}"}""")).asJSON.check(status.is(202)))
}

object FromMerchant {
  val process =
    exec(session => {
      val from = Utils.randInt(1, Config.merchants + 1)
      val to = Utils.randInt(Config.merchants + 1, Config.merchants + Config.accounts + 1)
      val amount = Utils.randInt(1, 1000)
      session
        .set("from", from)
        .set("to", to)
        .set("amount", amount)
    })
      .exec(http("transfer").post("transfer").body(StringBody("""{"from" : "${from}","to": "${to}","amount":"${amount}"}""")).asJSON.check(status.is(202)))
}

