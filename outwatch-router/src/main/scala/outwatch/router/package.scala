package outwatch

import outwatch.dom.VDomModifier

package object router {
  type RouterStore = ProHandler[Action, RouterState]

  type RouterResolve = PartialFunction[Path, VDomModifier]
}
