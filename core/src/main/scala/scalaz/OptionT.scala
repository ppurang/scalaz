package scalaz

sealed trait OptionT[F[_], A] {
  val runT: F[Option[A]]

  import OptionT._
  import =~~=._

  def *->* : (({type λ[α] = OptionT[F, α]})#λ *->* A) =
    scalaz.*->*.!**->**![({type λ[α] = OptionT[F, α]})#λ, A](this)

  def run(implicit i: F =~~= Identity): Option[A] =
    runT

  def isDefinedT(implicit ftr: Functor[F]): F[Boolean] =
    ftr.fmap((_: Option[A]).isDefined)(runT)

  def isDefined(implicit i: F =~~= Identity): Boolean =
    run.isDefined

  def isEmptyT(implicit ftr: Functor[F]): F[Boolean] =
    ftr.fmap((_: Option[A]).isEmpty)(runT)

  def isEmpty(implicit i: F =~~= Identity): Boolean =
    run.isEmpty

  def getOrElseT(default: => A)(implicit ftr: Functor[F]): F[A] =
    ftr.fmap((_: Option[A]).getOrElse(default))(runT)

  def getOrElse(default: => A)(implicit i: F =~~= Identity): A =
    run.getOrElse(default)

  def existsT(f: A => Boolean)(implicit ftr: Functor[F]): F[Boolean] =
    ftr.fmap((_: Option[A]).exists(f))(runT)

  def exists(f: A => Boolean)(implicit i: F =~~= Identity): Boolean =
    run.exists(f)

  def forallT(f: A => Boolean)(implicit ftr: Functor[F]): F[Boolean] =
    ftr.fmap((_: Option[A]).forall(f))(runT)

  def forall(f: A => Boolean)(implicit i: F =~~= Identity): Boolean =
    run.forall(f)

  def orElseT(a: => Option[A])(implicit ftr: Functor[F]): OptionT[F, A] =
    optionT(ftr.fmap((_: Option[A]).orElse(a))(OptionT.this.runT))

  def orElse(a: => Option[A])(implicit i: F =~~= Identity): Option[A] =
    run.orElse(a)

  def map[B](f: A => B)(implicit ftr: Functor[F]): OptionT[F, B] =
    optionT(ftr.fmap((_: Option[A]) map f)(runT))

  def foreach(f: A => Unit)(implicit e: Each[F]): Unit =
    e.each((_: Option[A]) foreach f)(runT)

  def filter(f: A => Boolean)(implicit ftr: Functor[F]): OptionT[F, A] =
    optionT(ftr.fmap((_: Option[A]).filter(f))(runT))

  def flatMap[B](f: A => OptionT[F, B])(implicit m: Monad[F]): OptionT[F, B] =
    optionT(m.bd((o: Option[A]) => o match {
      case None => m.point(None: Option[B])
      case Some(a) => f(a).runT
    })(runT))

  def mapOption[B](f: Option[A] => Option[B])(implicit ftr: Functor[F]): OptionT[F, B] =
    optionT(ftr.fmap(f)(runT))
}

object OptionT extends OptionTs {
  def apply[F[_], A](r: F[Option[A]]): OptionT[F, A] =
    optionT(r)
}

trait OptionTs {
  type Maybe[A] =
  OptionT[Identity, A]

  def optionT[F[_], A](r: F[Option[A]]): OptionT[F, A] = new OptionT[F, A] {
    val runT = r
  }

  def someT[F[_], A](a: A)(implicit p: Pointed[F]): OptionT[F, A] =
    optionT(p.point(Some(a)))

  def noneT[F[_], A](implicit p: Pointed[F]): OptionT[F, A] =
    optionT(p.point(None))

  def just[A]: A => Maybe[A] =
    someT[Identity, A](_)

  def nothing[A]: Maybe[A] =
    noneT[Identity, A]
}
