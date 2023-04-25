package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.domain.{ Rate }
import forex.config.OneFrameConfig
import forex.services.rates.errors.Error

import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.blaze._

import cats.implicits._

import scala.concurrent.ExecutionContext.global
import cats.effect.ConcurrentEffect
import forex.domain.Currency

import forex.http.rates.Protocol._
import org.http4s.Request
import org.http4s.Headers
import org.http4s.Method.GET
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.Header

// NoCache because the OneFrame service is polled for each call to get
class NoCacheInterpreter[F[_]: ConcurrentEffect](config: OneFrameConfig) extends Algebra[F] {

   def callOneFrame(client: Client[F], from: Currency, to: Currency): F[Error Either Rate] = {
    val uri: Uri =
      Uri(authority = Some(Uri.Authority(host = Uri.RegName(config.host), port= Some(config.port))))
        .withPath(f"rates?pair=${from.show}${to.show}")

    val req: Request[F] = Request(
      method = GET,
      uri = uri,
      headers =
        Headers.of(Header("token", config.token))
    )

    implicit val x: EntityDecoder[F, OneFrameGetResponse] = jsonOf[F, OneFrameGetResponse]
    
    client.expect[OneFrameGetResponse](req).attempt.map{
      case Left(err) => Left(Error.OneFrameLookupFailed(err.toString()))
      case Right(resp) => Right(resp.head.toRate)
    }

   }

  override def get(pair: Rate.Pair): F[Error Either Rate] = 
      BlazeClientBuilder[F](global).resource.use{
        client => 
          callOneFrame(client, pair.from, pair.to)
      }
}
