val scala3Version = "3.8.2"
val jettyVersion = "12.0.20"
val jdbcVersion = "4.3.+"

lazy val root = project
  .in(file("."))
  .settings(
    name := "requestbin",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-encoding", "utf8"
    ),

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "org.scalatra" %% "scalatra-jakarta" % "3.1.2",
      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
      "org.eclipse.jetty.ee10" % "jetty-ee10-servlet" % jettyVersion,
      "org.eclipse.jetty.ee10" % "jetty-ee10-webapp" % jettyVersion,
      "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0" % "provided",
      "org.scalikejdbc" %% "scalikejdbc" % jdbcVersion,
      "org.scalikejdbc" %% "scalikejdbc-config" % jdbcVersion,
      "org.xerial" % "sqlite-jdbc" % "3.51.2.0",
      "com.h2database" % "h2" % "2.2.+",
      "ch.qos.logback" % "logback-classic" % "1.5.+",
      "commons-io" % "commons-io" % "2.21.0"
    )
  )
