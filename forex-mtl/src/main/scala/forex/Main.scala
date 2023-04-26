package forex

import scala.concurrent.ExecutionContext

import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      cache <- Stream.eval(forex.Cache[F](ec, config.oneFrame))
      module = new Module[F](config, cache)
      serverStream = BlazeServerBuilder[F](ec)
                       .bindHttp(config.http.port, config.http.host)
                       .withHttpApp(module.httpApp)
                       .serve
      cacheStream = cache.refresh
      _ <- serverStream concurrently cacheStream
    } yield ()

}
