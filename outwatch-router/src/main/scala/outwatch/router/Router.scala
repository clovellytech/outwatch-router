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

/**
  * An AppRouter handles parsing of URLs and mapping to pages of the given type.
  * @param siteRoot - The prefix part of a pathname, or the subpath at which your site is applied.
  *                 Usually this is just Root, but your site might need a prefix as in /my_site/[parsed pathname]
  * @param parent - The parent path at which this router is mounted. You can have routers contained in subroots of your site.
  * @param f - a mapping function from a Path to a page P.
  * @tparam P - Your page type, such as a sealed trait root type.
  */
class AppRouter[P](siteRoot: Path, parent: Path, f: Path => P) {
  // Sync from the required page to the window.location
  def routerReducer(state: RouterState[P], action: Action): RouterState[P] = action match {
    case Replace(path) =>
      window.history.pushState("", "", Path(siteRoot, Path(parent, path)).toString)
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
    new AppRouter[P](Root, parent, f.lift.andThen(_.getOrElse(notFound)))

  def createParseSiteRoot[P](notFound: P)(f: PartialFunction[Path, P]): AppRouter[P] =
    createParseSiteRoot[P](Root, notFound)(f)

  /**
    * Automatically determine what siteroot we're using, based on the current URL and expected parent.
    * For example, your site could be deployed at /example/directory/, your router root path could be /names,
    * and the current url could be /example/directory/names/alice. So given the call:
    * createParseSubRoot[Page](Path("/names"), NotFound)(f), the router will work out that the window location
    * prefix must be /example/directory/names, and will handle actions such as Replace(Path("/names/bob"))
    * @param parent the parent for this router, another path perhaps managed by another router
    * @param notFound - the default case page assignment
    * @param f - a router function from Path to instances of your page type
    * @tparam P - your page type
    */
  def createParseSiteRoot[P](parent: Path, notFound: P)(f: PartialFunction[Path, P]): AppRouter[P] = {
    val initUrl = window.location.pathname
    // url is of form /sra/srb/src/pa/pb/pc...
    // so just drop the parent part from the right of the url if it exists.

    val siteRoot = initUrl.lastIndexOf(parent.toString) match {
      case x if x < 1 => Root
      case x => Path(initUrl.substring(0, x))
    }

    val routerFun: Path => P = f.lift.andThen(_.getOrElse(notFound))
    new AppRouter[P](siteRoot, parent, routerFun)
  }
}
