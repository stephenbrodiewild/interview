package forex.services.rates.interpreters

import forex.services.rates.Algebra
import forex.domain.Rate
import forex.services.rates.errors._

import forex.Cache
import cats.implicits._
import cats.effect.ConcurrentEffect

class UseCache[F[_]: ConcurrentEffect](cache: Cache[F]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = 
    cache.get(pair).value.map{
      case None => Left(Error.OneFrameLookupFailed(pair.toString()))
      case Some(value) => Right(value)
    }
}
