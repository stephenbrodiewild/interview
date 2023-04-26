package forex.services.rates

import forex.Cache
import interpreters._
import cats.effect.ConcurrentEffect

object Interpreters {
  def useCache[F[_]: ConcurrentEffect](cache: Cache[F]): Algebra[F] = new UseCache[F](cache)
}
