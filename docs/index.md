---
layout: home
title:  "Outwatch Router"
section: "home"
---

Outwatch Router
===

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
