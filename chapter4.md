# 関数型デザイン&プログラミング勉強メモ - 例外を使わないエラー処理
## 参照透過性とは(復習)

参照透過性の元では、


  __関数が実行するすべてのことはその戻り値によって表現される__


となる。

### 例

以下のsumは __純粋関数__ で __参照透過性__ がある。

```Scala
def sum(a: Int, b: Int): Int = {
  a + b
}
```

以下のように呼び出すと評価結果は3となる

```Scala
scala> val x = sum(1,2)
x: Int = 3
```

なので、置換モデルにより

```Scala
scala> val x = 3
x: Int = 3
```

とすると等価である。

が、次の関数はsum_printないで標準出力へ出力しているので、

```Scala
def sum_print(a: Int, b: Int): Int = {
  val c = a + b
  println("Hello World!")
  c
}
```

置換モデルを適用すると、

```Scala
scala> val x = sum_print(1,2)
Hello World!
x: Int = 3

scala> val x = 3
x: Int = 3
```

評価値に置き換えても等価にならないので、 __自然な推論__ が不可能となる。この場合の、sum_printは、 __非純粋関数__ で、 __参照透過性がない__ 。

## 例外の問題
### 1.参照透過性が失われる

```Scala
def failingFn(i: Int): Int = {
  val y: Int = throw new Exception("fail!")
  try {
    val x = 42 + 5
    x + y
  } catch { case e: Exception => 43 }
}
```

上記の関数を呼ぶとエラーになる。

```Scala
scala> failingFn(12)
java.lang.Exception: fail!
  at .failingFn(<console>:9)
  ... 33 elided
```

yは参照透過ではない。逆に言えば、yが参照透過ならば、呼び出しの結果「43」が返ってくるはずである。

置換モデルで try の中の y を throw new Exception("fail!")とした場合、

```Scala
def failingfn2(i: Int): Int = {
  try {
    val x = 42 + 5
    x + ((throw new Exception("fail!")): Int)
  } catch { case e: Exception => 43 }
}
```

実行すると、tryの中で例外が発生するため、43が返却される。

```Scala
scala> failingFn2(12)
res1: Int = 43
```

#### 参照透過とそうでないもの

* 参照透過な式の意味は、コンテキストに依存しないため、ローカルな推論が可能
    * 42 + 5という式は、どこに書かれようが47である
* 参照透過でない式の意味は、コンテキストに依存するため、よりグローバルな推論が必要
    * throw new Exception("fail!")は、tryの内側/外側で意味が異なる

### 2.型安全でない
__failingFn, Int => Int__　からは、例外が発生するかはわからない。コンパイラも例外の処理を呼び出し元に強制はしない。つまり、意図的に例外を確認しないと発生するまでわからない。

## 例外に代わる手法

以下のようなリストの平均値を返す関数meanを例にとる。

```
def mean_0(xs: Seq[Double]): Double = {
  if (xs.isEmpty) throw new ArithmeticException("mean of empty list")
  else xs.sum / xs.length
}
```

このmean関数は、一部の入力に対して定義されていない(例外を投げているところ)ため、 __部分関数__ となる。

### 偽の値を返す
* すべてのケースで xs.sum / xs.length を計算し、空の場合は、
Double.NaN(0.0/0.0)を返す
* センチネル値を返す
* nullを返す
* (型によっては)nullを返す

ただし、次の理由から問題がある

* 呼び出し元のエラーチェックがなくてもコンパイルエラーにならないので、エラーが伝播する可能性がある
* 呼び出し元で義の値をチェックするボイラープレートだらけになる
* 多層コードに適用できない
 * def max[A](xs: Seq[A])(greater: (A,A) => Boolean): A のような場合
* すべての引数を一様に扱わなければならない高階関数に引数を渡すのがむずかしくなる

### 未定義の入力に対する返却を引数でとる

以下のようにすると、meanは完全関数になるが、エラー元が未定義のケースに対処する方法を知っている必要がある

```
def mean_1(xs: IndexedSeq[Double], onEmpty: Double): Double =
  if (xs.isEmpty) onEmpty
  else xs.sum / xs.length
```

## Option型

__問題への答えが常にあるとは限らないことを戻り値の型で表す__

__あるかもしれないし、ないかもしれない__

* 定義可能な場合は、Some
* 未定義の場合は、None

meanの場合は、以下のようになる。これでmeanは　__完全関数__ となる。

```Scala
def mean(xs: Seq[Double]): Option[Double] =
  if (xs.isEmpty) None
  else Some(xs.sum / sx.length)
```

Optionを操作する高階関数でエラー処理をパターンとして抽出・共通化することで便利になる。(Exercise 4.1, 4.2)

### リフトによる関数のOption対応
Optionを適用する場合は、コード全体を修正する必要はない。

通常の関数を __リフト__ すれば、Optionに対応する関数に変換可能である。

リフトするための高階関数は以下の通り。

```Scala
def lift[A,B](f: A => B): Option[A] => Option[B] = _.map(f)
```

math.absに適用すると、

```Scala
scala> val abs0 = lift[Double,Double](math.abs)
abs0: Option[Double] => Option[Double] = <function1>

scala> abs0(Option(-1.0))
res1: Option[Double] = Some(1.0)
```

### 自動車保険会社のWebサイトロジック実装の例

```Scala
/**
* 2つの主要項目から年間自動車保険料を計算するための極秘の式
*/
def insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double
```

年齢とスピード違反切符の番号がWebフォームで送信されると、フィールドは文字列なので整数へ変換しないといけない。
この際、文字列が有効でないと __NumberFormatException__ が投げられます。

ここで、toIntの例外ベースのAPIをOptionに変換することで、parseInsuranceRateQuote関数を実装する。

```Scala
def parseInsuranceRateQuote(
  age: String
  numberOfSpeedingTickets: String): Option[Double] ={
    val optAge: Option[Int] = Try(age.toInt)
    val optTickets: Option[Int] = Try(NumberFormatException.toInt)
    insuranceRateQuote(optAge, optTickets)
}

def Try[A](a: => A): Option[A] =
  try Some(a)
  catch { case e: Exception => None}
```

Try関数により、例外ベースのAPIをOptionベースのAPIに変換している。

こうすると、insuranceRateQuoteは、Option[Int]値で引数を受け取れないといけない。
そこで、insuranceRateQuoteをリフトして、Option値のコンテキストに対応させる。(Exercise 4.3)

リフト関数map2を使って、以下のように対応させることができる。

```Scala
def parseInsuranceRateQuote(
  age: String,
  numberOfSpeedingTickets: String): Option[String] = {
   val optAge: Option[Int] = Try { age.toInt }
   val optTickets: Option[Int] = Try { numberOfSpeedingTickets.toInt }
   map2(optAge, optTickets)(insuranceRateQuote)
   }  )
```

Try { age.toInt }は、　Try(age.toInt) と同じである。
こうすると、insuranceRateQuoteを変更する必要はない。

String型の値からなるリスト全体をOption[Int]として、解析したい場合、以下のようにすればよいですが、この方法はリストを２回走査するため非効率である。
* 1回目：StringをそれぞれOption[Int]に変換
* 2回目：Option[Int]値をOption[List[Int]]にまとめる

```Scala
def parseInts(a: List[String]): Option[List[Int]] =
  sequence(a map (i => Try(i.toInt)))
```

これをリスト１回の走査ですむように実装する。(Exercise 4.5)

TODO: traverseの呼び方

## 参考情報
* 書籍:[Scala関数型デザイン＆プログラミング](http://www.amazon.co.jp/Scala%E9%96%A2%E6%95%B0%E5%9E%8B%E3%83%87%E3%82%B6%E3%82%A4%E3%83%B3-%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9F%E3%83%B3%E3%82%B0-%E2%80%95Scalaz%E3%82%B3%E3%83%B3%E3%83%88%E3%83%AA%E3%83%93%E3%83%A5%E3%83%BC%E3%82%BF%E3%83%BC%E3%81%AB%E3%82%88%E3%82%8B%E9%96%A2%E6%95%B0%E5%9E%8B%E5%BE%B9%E5%BA%95%E3%82%AC%E3%82%A4%E3%83%89-impress-gear/dp/4844337769)
* WEB:[関数型の考え方: Either と Option による関数型のエラー処理](https://www.ibm.com/developerworks/jp/java/library/j-ft13/)
