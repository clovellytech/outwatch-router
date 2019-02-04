package outwatch.router

import cats.effect.LiftIO
import monix.execution.Scheduler
import org.scalajs.dom.window
import outwatch.dom._, dsl._
import outwatch.util.Store

sealed trait Action
final case class Replace(path: Path) extends Action

final case class RouterState[P](page: P)

class AppRouter[F[_]: LiftIO, P](f: Path => P) {
  def routerReducer(state: RouterState[P], action: Action): RouterState[P] = action match {
    case Replace(path) =>
      println(s"Going to $path")
      Path.unapplySeq(path).foreach(p => window.history.replaceState("", "", p.mkString("/")))
      state.copy(page = f(path))
    case _ => state
  }

  def store(implicit S : Scheduler): F[RouterStore[P]] = {
    val startingPath = Path(window.location.pathname)

    Store.create[RouterState[P], Action](
      RouterState(f(startingPath)),
      Store.Reducer.justState(routerReducer _)
    ).to[F]
  }
}


object AppRouter{
  def render[P](resolver: RouterResolve[P])(implicit store: RouterStore[P]): VDomModifier =
    div(store.map(state => resolver(state.page)))

  def create[F[_]: LiftIO, P](notFound: P)(f: PartialFunction[Path, P]): AppRouter[F, P] =
    new AppRouter[F, P](f.lift.andThen(_.getOrElse(notFound)))
}
