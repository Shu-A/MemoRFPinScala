sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x, xs) => x + sum(xs)
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x, xs) => x * product(xs)
  }

  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  def tail[A](xs: List[A]): List[A] = xs match {
    case Nil => Nil
    case Cons(x, xs) => xs
  }

  def setHead[A](xs: List[A], a: A): List[A] = xs match {
    case Nil => Nil
    case Cons(x, xs) => Cons(a, xs)
  }

  def drop[A](xs: List[A], n: Int): List[A] = xs match {
    case Nil => Nil
    case Cons(x, xs) => if (n > 1) drop(xs, n - 1) else xs
  }

  /*
  def dropWhile[A](xs: List[A], f: A => Boolean): List[A] = xs match {
    case Nil => Nil
    case Cons(x, xs) => if (f(x)) dropWhile(xs, f) else xs
  }
  */
  def dropWhile[A](xs: List[A], f: A => Boolean): List[A] = xs match {
    case Cons(x, xs) if f(x) => dropWhile(xs, f)
    case _ => xs
  }

  @annotation.tailrec
  def append[A](a1: List[A], a2: List[A]): List[A] = a1 match {
    case Nil => a2
    //case Cons(h, t) => Cons(h, append(t, a2))
    case Cons(h, t) => append(t, Cons(h, a2))
  }
}
