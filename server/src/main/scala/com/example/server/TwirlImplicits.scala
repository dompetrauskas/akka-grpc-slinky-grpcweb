package com.example.server

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.model.MediaTypes.`text/plain`
import akka.http.scaladsl.model.MediaTypes.`text/xml`
import play.twirl.api.Html
import play.twirl.api.Txt
import play.twirl.api.Xml

object TwirlImplicits {

  /** Twirl marshallers for Xml, Html and Txt mediatypes */
  implicit val twirlHtmlMarshaller = twirlMarshaller[Html](`text/html`)
  implicit val twirlTxtMarshaller  = twirlMarshaller[Txt](`text/plain`)
  implicit val twirlXmlMarshaller  = twirlMarshaller[Xml](`text/xml`)

  def twirlMarshaller[A](contentType: MediaType): ToEntityMarshaller[A] = {
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)
  }
}
