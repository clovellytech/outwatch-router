package outwatch.router

import cats.data.Kleisli

object Router {
  type AppRouter[F[_], A] = Kleisli[F, Path, A]
  val AppRouter = Kleisli

}
