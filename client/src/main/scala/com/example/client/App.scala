package com.example.client

import com.example.service.ServiceGrpcWeb
import scalapb.grpc.Channels
import scalapb.grpcweb.Metadata
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.web.html._

@react class App extends StatelessComponent {
  type Props = Unit

  def render() = {
    Fragment(
      h1("Hello world!"),
      Unary(),
      Stream(cancel = false),
      Stream(cancel = true)
    )
  }
}

object App {

  val serviceStub: ServiceGrpcWeb.Service[Metadata] = ServiceGrpcWeb.stub(Channels.grpcwebChannel(""))
}
