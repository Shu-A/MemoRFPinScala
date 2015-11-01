//import scala.{Option => _, Either => _, _}

object My {
  sealed trait Option[+A] {
    def map[B](f: A => B): Option[B] = this match {
      case None => None
      case Some(x) => Some(f(x))
      //case Some(x) => println("Not None")
    }
  }
  case class Some[+A](get: A) extends Option[A]
  case object None extends Option[Nothing]

  object Option {

    def mean(xs: Seq[Double]): Option[Double] =
      if (xs.isEmpty) None
      else Some(xs.sum / xs.length)

  }
}
