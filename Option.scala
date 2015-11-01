import scala.{Option => _, Either => _, _}

object My {
  sealed trait Option[+A] {

    def map[B](f: A => B): Option[B] = this match {
      case None => None
      case Some(x) => Some(f(x))
    }

    def flatMap[B](f: A => Option[B]): Option[B] =
      map(f).getOrElse(None)

    def getOrElse[B >: A](default: => B): B = this match {
      case None => default
      case Some(x) => x
    }

    def orElse[B >: A](ob: => Option[B]): Option[B] = this match {
      // TODO: Impliment without pattern mathing
      case None => ob
      case Some(x) => Some(x)
    }

    def filter(f: A => Boolean): Option[A] = this match {
      // TODO: Impliment without pattern mathing
      case Some(x) if f(x) => this
      case _ => None
    }
  }
  case class Some[+A](get: A) extends Option[A]
  case object None extends Option[Nothing]

  object Option {

    def mean(xs: Seq[Double]): Option[Double] =
      if (xs.isEmpty) None
      else Some(xs.sum / xs.length)

    def variance(xs: Seq[Double]): Option[Double] =
      mean(xs).flatMap(m => mean(xs.map(x => math.pow(x - m, 2))))
  }
}
