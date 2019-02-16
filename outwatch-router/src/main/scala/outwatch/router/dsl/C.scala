package outwatch.router
package dsl

import outwatch.dom.VDomModifier
import outwatch.dom.{dsl => O, _}

object C {
  def a[P](linkHref: String)(attrs: VDomModifier*)(implicit store: RouterStore[P]): BasicVNode =
    O.a(
      O.href := linkHref,
      O.onClick.preventDefault.mapTo(Replace(Path(linkHref))) --> store
    )(attrs)
}
