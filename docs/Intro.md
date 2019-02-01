Outwatch Router
===

```scala mdoc 
import cats._
import cats.implicits._
import cats.Applicative
import cats.data.Kleisli
import outwatch.router._, Router._

sealed abstract class Page
case class RootPage() extends Page
case class Login() extends Page
case class Register() extends Page
case class Profile(userId: String) extends Page
case class NotFound() extends Page

object Page{
  def root: Page = RootPage()
  def login: Page = Login()
  def register: Page = Register()
  def profile(userId: String): Page = Profile(userId)
  def notFound: Page = NotFound()
}

def routes[F[_]: Applicative]: AppRouter[F, Page] = Kleisli[F, Path, Page] { 
  case Root => Page.root.pure[F]
  case Root / "login" => Page.login.pure[F]
  case Root / "register" => Page.register.pure[F]
  case Root / "profile" / userId => Page.profile(userId).pure[F]
  case _ => Page.notFound.pure[F]
}

val router = routes[Id]

router.run(Root)
router.run(Root / "login")
router.run(Root / "profile" / "saopa98f")

router.run(Path("/profile/asd"))
router.run(Path("/apsinoasn"))
```

