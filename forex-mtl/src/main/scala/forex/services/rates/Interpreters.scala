package forex.services.rates

import cats.Applicative
import interpreters._
import forex.config.OneFrameConfig
import cats.effect.ConcurrentEffect

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def noCache[F[_]: ConcurrentEffect](config: OneFrameConfig): Algebra[F] = new NoCacheInterpreter[F](config)
}
