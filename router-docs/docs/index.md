---
layout: docs
title: "Outwatch Router"
section: "home"
---

# Outwatch Router

Route creation strategy was mostly taken from Http4s. To create paths and map them to pages:

```scala mdoc
import outwatch.router._

sealed abstract class Page
case class RootPage() extends Page
case class Login() extends Page
case class Register() extends Page
case class Profile(userId: String) extends Page
case class NotFound() extends Page

def routes: PartialFunction[Path, Page] = {
  case Root => RootPage()
  case Root / "login" => Login()
  case Root / "register" => Register()
  case Root / "profile" / userId => Profile(userId)
  case _ => NotFound()
}

routes(Root)
routes(Root / "login")
routes(Root / "profile" / "saopa98f")

routes(Path("/profile/asd"))
routes(Path("/apsinoasn"))
```

A minimum working usage example:

```scala mdoc
import outwatch._
import outwatch.dsl._

import cats.effect.ExitCode
import monix.bio._
import outwatch.router.AppRouter
import outwatch.router.dsl._

object IntVar {
    def unapply(str: String): Option[Int] = {
      if (!str.isEmpty) str.toIntOption
      else None
    }
  }

object OutwatchApp extends BIOApp {

  sealed trait Page
  case object Home extends Page
  case object SomePage extends Page
  case class UserHome(id: Int) extends Page
  case object NotFound extends Page

  val router = AppRouter.create[Task, Page](NotFound) {
    case Root                       => Home
    case Root / "user" / IntVar(id) => UserHome(id)
    case Root / "some-page"         => SomePage
  }

  def resolver: PartialFunction[Page, VDomModifier] = {
    case Home         => div("hm")
    case SomePage     => div("page1")
    case UserHome(id) => div(s"User id: $id")
    case NotFound     => div("notfound")
  }

  def run(args: List[String]): UIO[ExitCode] = {
    import org.scalajs.dom.document
    val el = document.createElement("div")
    el.setAttribute("id", "#myapp")
    document.body.appendChild(el)
    router.store
      .flatMap(implicit store =>
        OutWatch
          .renderInto(
            el,
            div(
              div(
                cls := "four wide column",
                ul(
                  li(router.link("/")("Home")),
                  li(router.link("/some-page")("SomePage")),
                  li(router.link("/user/0")("User Home")),
                  // simulate not found
                  li(router.link("/about")("About"))
                )
              ),
              router.render(resolver),
              // watch for history events
              router.watch()
            )
          )
      )
      .onErrorHandleWith(ex => UIO(ex.printStackTrace()))
      .as(ExitCode.Success)
  }
}
```

Note that although monix's BIO is used here, any effect that implements `cats.effect.Sync` can be used.
