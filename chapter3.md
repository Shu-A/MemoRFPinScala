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

#### 単方向リストのデータ共有がうまくいかない場合

以下のようなListの末尾を除く全ての要素で構成されたListを返す関数iinitは、単方向リストが故にリストの最後のConsを操作するために、それより前のConsオブジェクトをすべてコピーする必要が出てくる。

```Scala
def init[A](l: List[A]): List[A]
```

### 高階関数の型推論の改善

高階関数には、無名関数が呼ばれることが多い。dropWhileを以下のように呼ぶ必要があり、第２引数の無名関数の引数の型をアノテーションで指定する必要がある。

```
scala> List.dropWhile(List(1,2,3,4,5), (x: Int) => x < 4)
res13: List[Int] = Cons(4,Cons(5,Nil))

scala> List.dropWhile(List(1,2,3,4,5), {x:Int => x < 4})
res17: List[Int] = Cons(4,Cons(5,Nil))
```

fに無名関数を渡した場合、引数の型はList[A]のAと推論されそうだが、してくれない。
この型推論をさせるためには、次のようにカリー化する必要がある。

```Scala
def dropWhile2[A](l: List[A])(f: A => Boolean): List[A]
```

呼び出すと、次のように無名関数の引数を指定しなくても、引数グループの左から右へ推論してくれる。

```Scala
scala> List.dropWhile2(List(1,2,3,4,5))(x => x < 4)
res19: List[Int] = Cons(4,Cons(5,Nil))
```

## リストの再帰と高階関数の一般化

次のsumとpuroductの実装を見ると以下の共通点がある。
* リストが空の場合に決まった値を返すこと
* リストが空でない場合に要素を結果に追加するための関数があること

```Scala
def sum(ints: List[Int]): Int = ints match {
  case Nil => 0
  case Cons(x, xs) => x + sum(xs)
}

def product(ds: List[Double]): Double = ds match {
  case Nil => 1.0
  case Cons(x, xs) => x * product(xs)
}
```

上のような関数は、次のようなfoldRight関数で一般化できる。

```Scala
def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = as match {
  case Nil => z
  case Cons(x, xs) => f(x, foldRight(xs, z)(f))
}
```

次のように呼ぶとよれぞれ、sumとproductと同じ処理となる。

```Scala
scala> List.foldRight(List(1,2,3,4,5), 0)(_ + _)
res1: Int = 15

scala> List.foldRight(List(1,2,3,4,5), 1)(_ * _)
res2: Int = 120
```

式展開は、次のようになる。
```
f(1, f(2, f(3, f(4, f(5, z)))))
```

次のように呼ぶとわかりやすい。

```Scala
scala> List.foldRight(List("1","2","3","4","5"), "0")(_ + _)
res7: String = 123450
```

foldRightは、(フレームをコールスタックにプッシュしながら)リストを最後まで走査してからでないと畳み込みを開始できない。
