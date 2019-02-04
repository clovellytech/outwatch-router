package outwatch.router

import cats.effect.LiftIO
import monix.execution.Scheduler
import org.scalajs.dom.window
import outwatch.dom._, dsl._
import outwatch.util.Store


sealed trait Action
final case class Replace(path: Path) extends Action

final case class RouterState(path: Path)

object AppRouter {
  def routerReducer(state: RouterState, action: Action): RouterState = action match {
    case Replace(path) =>
      Path.unapplySeq(path).foreach(p => window.history.replaceState("", "", p.mkString("/")))
      state.copy(path = path)
    case _ => state
  }

  def store[F[_]: LiftIO](implicit S : Scheduler): F[RouterStore] =
    Store.create[RouterState, Action](
      RouterState(Root),
      Store.Reducer.justState(routerReducer _)
    ).to[F]

  def render(resolver: RouterResolve)(implicit store: RouterStore): VDomModifier =
    div(store.map(state => resolver(state.path)))
}
