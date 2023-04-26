package forex

import org.scalatest._
import flatspec._
import matchers._

//import cats.effect.IO
//import forex.domain.Rate
//import forex.domain.Timestamp
//import forex.domain.Currency
//import forex.domain.Price

class CacheSpec extends AnyFlatSpec with should.Matchers {

  //"An insert then get" should "succeed" in {
  //  val rate = Rate(
  //    Rate.Pair(
  //      Currency.AUD,
  //      Currency.USD
  //    ),
  //    Price(1.0),
  //    Timestamp.now
  //  )
//
  //  val program = 
  //    for {
  //      cache <- Cache.apply[IO]
  //      _ <- cache.insert(rate)
  //      retrieved <- cache.get(rate.pair)
  //    } yield retrieved
//
  //    val result = program.unsafeRunSync()
  //    assert(result.isDefined && result.get == rate)
  //}
}
