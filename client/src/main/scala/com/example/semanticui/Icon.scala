package com.example.semanticui

import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr

@react object Icon extends ExternalComponent {
  case class Props(
      name: UndefOr[IconName] = js.undefined,
      loading: UndefOr[Boolean] = js.undefined,
      disabled: UndefOr[Boolean] = js.undefined,
      size: UndefOr[Size] = js.undefined
  )
  override val component = SemanticUiReact.Icon
}
