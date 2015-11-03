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

    def mean_0(xs: Seq[Double]): Double =
      if (xs.isEmpty) throw new ArithmeticException("mean of empty list!")
      else xs.sum / xs.length

    def mean(xs: Seq[Double]): Option[Double] =
      if (xs.isEmpty) None
      else Some(xs.sum / xs.length)

    def variance(xs: Seq[Double]): Option[Double] =
      mean(xs).flatMap(m => mean(xs.map(x => math.pow(x - m, 2))))

    def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
      a.flatMap(a => b.map(b => f(a, b)))
      //a.map(a => b.map(b => f(a, b))).getOrElse(None)
      /*
      if ( a == None || b == None) => None
      else f(a, b)
      */

    def sequence[A](a: List[Option[A]]): Option[List[A]] = a match {
      case Nil => Some(Nil)
      //case h :: t => map2(h,t add  sequence(t))(h :: _)
      case h :: t => h.flatMap(h => sequence(t).map(h :: _))
    }
      //a.foldLeft(Some(Nil))(map2(_, _)(_ :+ _))

    def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] = a match {
      case Nil => Some(Nil)
      case h :: t => f(h).flatMap(hd => traverse(t)(f).map(hd :: _))
    }
  }
}
