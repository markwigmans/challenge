package sva

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  * Created by mawi on 23/07/2016.
  */
class LoadSimulation extends Simulation {

  val scn = scenario("transfers").repeat((Config.transfers / Config.loadUsers).get) {
    randomSwitch(50.0 -> ToMerchant.process, 50.0 -> FromMerchant.process)
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
      val amount = Utils.randInt(1, 10000)
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
      val amount = Utils.randInt(1, 10000)
      session
        .set("from", from)
        .set("to", to)
        .set("amount", amount)
    })
      .exec(http("transfer").post("transfer").body(StringBody("""{"from" : "${from}","to": "${to}","amount":"${amount}"}""")).asJSON.check(status.is(202)))
}

