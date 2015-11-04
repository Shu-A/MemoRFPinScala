# 関数型デザイン&プログラミング勉強メモ - 関数型プログラミングのデータ構造

## 関数型データ構造

関数型データ構造は、本質的に __イミュータブル__

* 純粋関数のみを使って操作される
* データを直接変更しない
* 他の副作用が発生しない

そうすると一つ気になることが。。

```
データを余分にコピーすることになるのか？
```

そうはならない。

単方向リストを使ってこれを調べる。

```Scala
sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Con(x, xs) => x + sum(xs)
  }
}
```

* traitは抽象インターフェースである
* sealedは、トレイとの実装がすべてこのファイルで宣言されなければならないことを意味する
* case object, case classの部分は、Listで使用可能な形式を表す
* [+A]は、型パラメータでジェネリクス
* +記号は、型パラメータが共変(convariant)の意味

## パターンマッチング

```Scala
def sum(ints: List[Int]): Int = ints match {
  case Nil => 0
  case Con(x, xs) => x + sum(xs)
}
```

* パターンマッチングは、switch文を高度にしたようなもの
* matchの前にある、intsはターゲット、または被検査体と呼ばれる
* このターゲットのパターンに対して結果を記載する
* 複数のパターンにマッチする場合、最初にマッチしたケースが適用される

### 例

```Scala
scala> val x = List(1,2,3,4,5) match {
     |   case x :: (2 :: (4 :: _)) => x
     |   case Nil => 42
     |   case x :: (y :: (3 :: (4 :: _))) => x + y
     |   case h :: t => h
     |   case _ => 101
     | }
x: Int = 3
```

3つ目以下はすべてマッチするため3つ目の結果が得られる。

## 関数型データ構造でのデータ共有

データがイミュータブルである場合、リストの要素追加または削除はどうなるか。

* 追加

リストxsの先頭に1を足す場合、(Cons(1, xs))を返す。
この場合、xsはコピーはされず共有される。

* 削除

mylist = Cons(x, xs)から先頭要素を削除したい場合は、xsを返す。

__永続的である関数型データ構造は、データの変更や破壊を避ける必要がない__

### データ共有の効率性

* dropは、リストの先頭からn個の要素を削除する関数
* dropWhileは、述語(与えられた引数の真偽を返すもの。is○○みたいな関数とか)

どちらも以下のようにリストのコピーを作らず返すことで処理を効率化できる。

```Scala
def drop[A](xs: List[A], n: Int): List[A] = xs match {
  case Nil => Nil
  case Cons(x, xs) => if (n > 1) drop(xs, n - 1) else xs
}

def dropWhile[A](xs: List[A], f: A => Boolean): List[A] = xs match {
  case Cons(x, xs) if f(x) => dropWhile(xs, f)
  case _ => xs
}
```
