package outwatch.router

import cats.effect.LiftIO
import monix.execution.Scheduler
import org.scalajs.dom.window
import outwatch.util.Store


sealed trait Action
final case class Replace(path: Path) extends Action

final case class RouterState(currentPath: Path)

case class AppRouter() {
  def routerReducer(state: RouterState, action: Action): RouterState = action match {
    case Replace(path) =>
      Path.unapplySeq(path).foreach(p => window.history.replaceState("", "", p.mkString("/")))
      state.copy(currentPath = path)
    case _ => state
  }

  def store[F[_]: LiftIO](implicit S : Scheduler): F[RouterStore] =
    Store.create[RouterState, Action](
      RouterState(Root),
      Store.Reducer.justState(routerReducer _)
    ).to[F]
}
