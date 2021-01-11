package com.example.server

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import com.example.BuildInfo
import com.example.server.TwirlImplicits._

class WebService() extends Directives {

  val route: Route = concat(
    pathSingleSlash {
      get {
        complete(
          if (BuildInfo.environmentMode == "prod") {
            com.example.server.html.indexFullOptJs()
          } else {
            com.example.server.html.indexFastOptJs()
          }
        )
      }
    },
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    },
    path("favicon.ico") {
      complete("")
    }
  )
}
