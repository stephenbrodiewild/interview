package forex

import forex.http.rates.Protocol._
import org.scalatest._
import flatspec._
import matchers._

import io.circe.parser._

class OneFrameResponseSpec extends AnyFlatSpec with should.Matchers {

  "A singleton response from the open-frame service" should "deserialise correctly" in {
    val filename = "/home/stephen/repos/interview/forex-mtl/src/test/resources/singletonResponse.json"
    val jsonString = scala.io.Source.fromFile(filename).mkString
    val json = parse(jsonString)
    assert(json.isRight)
    println(json.toOption.get)
    val decoded = oneFrameGetResponseDecoder.decodeJson(json.toOption.get)
    println(decoded)
    assert(decoded.toOption.isDefined)
  }
}
