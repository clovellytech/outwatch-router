package outwatch.router

import cats.effect.IO
import monix.execution.Scheduler
import org.scalajs.dom.window
import outwatch.dom._
import monix.reactive.Observable
import outwatch.util.Store

sealed trait Action
final case class Replace(path: Path) extends Action

final case class RouterState[P](page: P)

class AppRouter[P](root: Path, f: Path => P) {
  def routerReducer(state: RouterState[P], action: Action): RouterState[P] = action match {
    case Replace(path) =>
      window.history.replaceState("", "", Path(root, path).toString)
      state.copy(page = f(path))
    case _ => state
  }

  def store(implicit S : Scheduler): IO[RouterStore[P]] = {
    val startingPath = Path(window.location.pathname)

    Store.create[Action, RouterState[P]](
      Replace(startingPath),
      RouterState(f(startingPath)),
      Store.Reducer.justState(routerReducer _)
    )
  }
}

object AppRouter{
  def render[P](resolver: RouterResolve[P])(implicit store: RouterStore[P]): Observable[VDomModifier] =
    store.map{ case (_, RouterState(p)) => resolver(p) }

  def create[P](notFound: P)(f: PartialFunction[Path, P]): AppRouter[P] =
    create[P](Root, notFound)(f)

  def create[P](parent: Path, notFound: P)(f: PartialFunction[Path, P]): AppRouter[P] =
    new AppRouter[P](parent, f.lift.andThen(_.getOrElse(notFound)))
}
