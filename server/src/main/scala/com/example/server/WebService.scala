package com.example.server

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import com.example.BuildInfo
import com.example.server.TwirlImplicits._
import com.example.server.WebService._

class WebService() extends Directives {

  val route: Route = concat(
    pathSingleSlash {
      get {
        complete(
          if (BuildInfo.environmentMode.equalsIgnoreCase("development")) {
            com.example.server.html.indexFastOptJs()
          } else {
            com.example.server.html.indexFullOptJs()
          }
        )
      }
    },
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource(s"$AssetsPath/" + file)
      }
    },
    path("favicon.ico") {
      complete("")
    }
  )
}

object WebService {
  val AssetsPath: String = if (BuildInfo.environmentMode.equalsIgnoreCase("production")) {
    "public/dist"
  } else {
    "public"
  }
}
