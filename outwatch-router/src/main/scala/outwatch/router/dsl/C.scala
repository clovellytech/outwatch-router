package outwatch.router
package dsl

import outwatch._
import outwatch.dsl._

object C {
  def link[P](linkHref: String)(attrs: VDomModifier*)(implicit store: RouterStore[P]): BasicVNode =
    a(href := linkHref)(
      onClick.preventDefault.useLazy(Replace(Path(linkHref))) --> store.sink,
      attrs,
    )
}
