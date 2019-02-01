Outwatch Router
===============

```scala
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
// router: AppRouter[Id, Page] = Kleisli(<function1>)

router.run(Root)
// res0: Id[Page] = RootPage()
router.run(Root / "login")
// res1: Id[Page] = Login()
router.run(Root / "profile" / "saopa98f")
// res2: Id[Page] = Profile("saopa98f")

router.run(Path("/profile/asd"))
// res3: Id[Page] = Profile("asd")
router.run(Path("/apsinoasn"))
// res4: Id[Page] = NotFound()
```

