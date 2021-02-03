package com.example

import slinky.readwrite.Writer

import scala.scalajs.js

package object semanticui {
  sealed trait Size
  object Size {
    case object mini    extends Size
    case object tiny    extends Size
    case object small   extends Size
    case object medium  extends Size
    case object large   extends Size
    case object big     extends Size
    case object huge    extends Size
    case object massive extends Size
    implicit val writer: Writer[Size] = _.toString.asInstanceOf[js.Object]
  }

  sealed trait IconName
  object IconName {
    case object home  extends IconName
    case object inbox extends IconName
    implicit val writer: Writer[IconName] = _.toString.asInstanceOf[js.Object]
  }
}
