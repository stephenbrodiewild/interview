package forex.domain

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

case class OneFrameRate(
  from: Currency,
  to: Currency,
  bid: Float,
  ask: Float,
  price: Float,
  time_stamp: Timestamp
){
  def toRate: Rate = 
    Rate(
      Rate.Pair(
        from,
        to
      ),
      Price(price),
      time_stamp
    )
}

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  )
}
