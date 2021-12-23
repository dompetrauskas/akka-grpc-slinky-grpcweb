import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker.ExecCmd

scalaVersion in ThisBuild := "2.13.4"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.0"
lazy val akkaGrpcVersion = "1.0.2"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestPlusPlayVersion = "5.0.0"
lazy val scalaJsDomVersion = "1.1.0"
lazy val scalaJsScriptsVersion = "1.1.4"
lazy val slinkyVersion = "0.6.6"
lazy val reactVersion = "16.12.0"
lazy val reactProxyVersion = "1.1.8"

lazy val `akka-grpc-slinky-grpcweb` = (project in file("."))
  .aggregate(
    clientBase,
    clientDev,
    clientProd,
    serverBase,
    serverDev,
    serverProd
  )

lazy val proto =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("proto"))
    .enablePlugins(AkkaGrpcPlugin)
    .settings(
      PB.protoSources in Compile := Seq(
        (baseDirectory in ThisBuild).value / "proto" / "src" / "main" / "protobuf"
      )
    )
    .jsSettings(
      libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
      PB.targets in Compile := Seq(
        scalapb.gen(grpc = false) -> (sourceManaged in Compile).value,
        scalapb.grpcweb.GrpcWebCodeGenerator -> (sourceManaged in Compile).value
      )
    )

lazy val protoJs = proto.js
lazy val protoJVM = proto.jvm

lazy val clientBase =
  project
    .enablePlugins(ScalaJSPlugin)
    .in(file("client/base"))
    .settings(
      libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion,
      libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion,
      scalacOptions += "-Ymacro-annotations"
    )
    .dependsOn(protoJs)

def clientCommonSettings = Seq(
  npmDependencies in Compile += "react" -> reactVersion,
  npmDependencies in Compile += "react-dom" -> reactVersion,
  npmDependencies in Compile += "react-proxy" -> reactProxyVersion,
  npmDevDependencies in Compile += "file-loader" -> "6.2.0",
  npmDevDependencies in Compile += "style-loader" -> "2.0.0",
  npmDevDependencies in Compile += "css-loader" -> "5.0.1",
  npmDevDependencies in Compile += "html-webpack-plugin" -> "4.3.0",
  npmDevDependencies in Compile += "webpack-merge" -> "5.7.3",
  webpackResources := (baseDirectory in ThisBuild).value / "client" / "webpack" * "*",
  webpackConfigFile in Test := Some(
    (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-core.config.js"
  ),
  requireJsDomEnv in Test := true
)

lazy val clientDev =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .in(file("client/dev"))
    .settings(
      clientCommonSettings,
      scalaJSStage := FastOptStage,
      webpackConfigFile in fastOptJS := Some(
        (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-fastopt.config.js"
      ),
      webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()
    )
    .dependsOn(clientBase)

lazy val clientProd =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .in(file("client/prod"))
    .settings(
      clientCommonSettings,
      scalaJSStage := FullOptStage,
      webpackConfigFile in fullOptJS := Some(
        (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-opt.config.js"
      )
    )
    .dependsOn(clientBase)

lazy val serverBase = project
  .enablePlugins(AkkaGrpcPlugin)
  .in(file("server/base"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-pki" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,
      "ch.megard" %% "akka-http-cors" % "0.4.2",
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.1.1" % Test
    )
  )
  .dependsOn(protoJVM)

def serverCommonSettings = Seq(
  Compile / mainClass := Some("com.example.server.Server")
)

lazy val serverDev = project
  .in(file("server/dev"))
  .settings(
    serverCommonSettings
  )
  .dependsOn(serverBase)

lazy val serverProd = project
  .enablePlugins(
    WebScalaJSBundlerPlugin,
    JavaAppPackaging,
    DockerPlugin
  )
  .in(file("server/prod"))
  .settings(
    serverCommonSettings,
    scalaJSProjects := Seq(clientProd),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .settings(
    dockerAliases in Docker += DockerAlias(
      None,
      None,
      "akka-grpc-slinky-grpcweb",
      None
    ),
    packageName in Docker := "akka-grpc-slinky-grpcweb",
    dockerBaseImage := "openjdk:8-alpine",
    dockerCommands := {
      val (stage0, stage1) = dockerCommands.value.splitAt(8)
      val (stage1part1, stage1part2) = stage1.splitAt(5)
      stage0 ++ stage1part1 ++ Seq(
        ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
      ) ++ stage1part2
    },
    dockerExposedPorts ++= Seq(9000)
  )
  .dependsOn(serverBase)

addCommandAlias("startServerDev", "~serverDev/reStart")
addCommandAlias(
  "startClientDev",
  "clientDev/fastOptJS::startWebpackDevServer;~clientDev/fastOptJS"
)
