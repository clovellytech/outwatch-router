package outwatch

import outwatch.dom.VDomModifier

package object router {
  type RouterStore[P] = ProHandler[Action, (Action, RouterState[P])]

  type RouterResolve[P] = PartialFunction[P, VDomModifier]
}
