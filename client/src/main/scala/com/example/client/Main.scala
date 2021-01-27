package com.example.client

import org.scalajs.dom
import slinky.hot
import slinky.web.ReactDOM

import scala.scalajs.LinkingInfo
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("semantic-ui-css/semantic.min.css", JSImport.Namespace)
object SemanticUiCss extends js.Object

object Main {
  val css = SemanticUiCss

  @JSExportTopLevel("main")
  def main(): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    ReactDOM.render(App(), container)
  }
}
