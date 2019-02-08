package outwatch.router

import cats.effect.LiftIO
import monix.execution.Scheduler
import org.scalajs.dom.window
import outwatch.dom._
import monix.reactive.Observable
import outwatch.util.Store

sealed trait Action
final case class Replace(path: Path) extends Action

final case class RouterState[P](page: P)

class AppRouter[F[_]: LiftIO, P](root: Path, f: Path => P) {
  def routerReducer(state: RouterState[P], action: Action): RouterState[P] = action match {
    case Replace(path) =>
      Path.unapplySeq(Path(root, path)).foreach(p => window.history.replaceState("", "", p.mkString("/")))
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
  def render[P](resolver: RouterResolve[P])(implicit store: RouterStore[P]): Observable[VDomModifier] =
    store.map(state => resolver(state.page))

  def create[F[_]: LiftIO, P](notFound: P)(f: PartialFunction[Path, P]): AppRouter[F, P] =
    create[F, P](Root, notFound)(f)

  def create[F[_]: LiftIO, P](parent: Path, notFound: P)(f: PartialFunction[Path, P]): AppRouter[F, P] =
    new AppRouter[F, P](parent, f.lift.andThen(_.getOrElse(notFound)))

}
