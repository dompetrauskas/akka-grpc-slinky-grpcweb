package com.example.server
import akka.grpc.scaladsl.WebHandler
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object Server extends ServerBase {
  override implicit val corsSettings: CorsSettings =
    WebHandler.defaultCorsSettings
  override val webServiceRoute: Route = {
    val assetsPath = "public/dist"
    concat(
      pathSingleSlash {
        get {
          encodeResponse {
            getFromResource(s"$assetsPath/index.html")
          }
        }
      },
      pathPrefix("assets" / Remaining) { file =>
        // optionally compresses the response with Gzip or Deflate
        // if the client accepts compressed responses
        encodeResponse {
          getFromResource(s"$assetsPath/" + file)
        }
      },
      path("favicon.ico") {
        complete("")
      }
    )
  }
}
