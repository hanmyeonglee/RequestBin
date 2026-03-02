val scala3Version = "3.8.2"

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
      "org.scalatra" %% "scalatra-javax" % "3.1.+",
      "org.eclipse.jetty" % "jetty-server" % "12.1.6",
      "org.eclipse.jetty" % "jetty-servlet" % "11.0.26",
      "jakarta.servlet" % "jakarta.servlet-api" % "5.0.0" % "provided",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.+",
      "org.scalikejdbc" %% "scalikejdbc-config" % "4.3.+",
      "org.xerial" % "sqlite-jdbc" % "3.51.2.0",
      "com.h2database" % "h2" % "2.2.+",
      "ch.qos.logback" % "logback-classic" % "1.5.+"
    )
  )
