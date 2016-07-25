package sva

import scala.util.Random

/**
  * Created by mawi on 24/07/2016.
  */
object Utils {

  // set the seed so the result is reproducable
  private val RNG = new Random(1234)

  def randInt(a:Int) = RNG.nextInt(a)
  def randInt(a:Int, b:Int) = RNG.nextInt(b-a) + a
}
