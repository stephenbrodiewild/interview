package forex.http
package rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import forex.programs.rates.errors

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  type OneFrameGetResponse = List[OneFrameRate]
  
  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val oneFrameRateDecoder: Decoder[OneFrameRate] = 
    deriveConfiguredDecoder[OneFrameRate]

  implicit val currencyDecoder: Decoder[Currency] = 
    Decoder.instance[Currency] {
      hc => Right(Currency.fromString(hc.value.asString.get))
    }

  implicit val oneFrameGetResponseDecoder: Decoder[OneFrameGetResponse] = Decoder.decodeList[OneFrameRate]

  implicit val errorEncoder: Encoder[errors.Error] = deriveConfiguredEncoder[errors.Error]

}
