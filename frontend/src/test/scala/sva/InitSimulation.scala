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
  val scn = scenario("create").exec(merchantChain,accountChain)

  setUp(
    init.inject(atOnceUsers(1)).protocols(Config.httpConf),
    scn.inject(atOnceUsers(Config.initUsers)).protocols(Config.httpConf)
  )
}
