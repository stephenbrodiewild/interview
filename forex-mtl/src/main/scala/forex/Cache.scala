package forex

import forex.domain.Rate
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.domain.OneFrameRate
import cats.data.{OptionT}
import cats.effect.Timer
import org.http4s.Uri
import org.http4s.Request
import org.http4s.Headers
import org.http4s.Method.GET
import org.http4s.Header
import scala.concurrent.ExecutionContext
import org.http4s.client.blaze.BlazeClientBuilder
import cats.effect.ConcurrentEffect
import forex.http.rates.Protocol
import forex.http.rates.Protocol._
import org.http4s.EntityDecoder
import org.http4s.circe._
import forex.programs.rates.errors._
import fs2._
import forex.domain.Price

class Cache[F[_]: ConcurrentEffect: Timer](private val ref: Ref[F, Map[Rate.Pair, Rate]], val config: OneFrameConfig, val ec: ExecutionContext){

    type RateCache = Map[Rate.Pair, Rate]
    object RateCache{
        def frameRatesToCache(oneFrameRates: List[OneFrameRate]): RateCache = ???
    }

    def refresh: Stream[F, Unit] =
        Stream.repeatEval{
            for {
                newCacheOrError <- callOneFrameService
                _ <- newCacheOrError match {
                    case Left(error) => Sync[F].delay(println(f"Error: $error"))
                    case Right(rates) => ref.set(rates)
                }
            } yield ()
        }.metered(config.refreshInterval)


    private def callOneFrameService: F[Either[Error, RateCache]] = {

        val noParamUri: Uri =
            Uri(authority = Some(Uri.Authority(host = Uri.RegName(config.host), port= Some(config.port))))
                .withPath("rates")

        val paramUri = noParamUri.withMultiValueQueryParams(
            Map(
                "pair" -> Rate.pairs.map{
                    case Rate.Pair(from, to) => from.show+to.show
                }
            )
        )

        println(paramUri)

        val req: Request[F] = Request(
            method = GET,
            uri = paramUri,
            headers = Headers.of(Header("token", config.token))
        )

        implicit val x: EntityDecoder[F, Protocol.OneFrameGetResponse] = jsonOf[F, Protocol.OneFrameGetResponse]

        def oneFrameValueToCache(of: Protocol.OneFrameGetResponse): RateCache = 
            of.map{
                case OneFrameRate(from, to, _, _, price, time_stamp) => 
                    Rate.Pair(from, to) -> Rate(Rate.Pair(from, to), Price(price), time_stamp)
            }.toMap

        BlazeClientBuilder[F](ec).resource.use{
            client => client.expect[Protocol.OneFrameGetResponse](req).attempt.map{
                case Left(err) => Left(Error.RateLookupFailed(err.toString()))
                case Right(value) => Right(oneFrameValueToCache(value))
            }
        }
    }

    def get(pair: Rate.Pair): OptionT[F, Rate] = OptionT(ref.get.map(_.get(pair)))

}

object Cache{
    def apply[F[_]: Timer: ConcurrentEffect](ec: ExecutionContext, config: OneFrameConfig): F[Cache[F]] =
        for {
            ref <- Ref.of[F, Map[Rate.Pair, Rate]](Map.empty)
            cache = new Cache[F](ref, config, ec)
        }  yield cache
}