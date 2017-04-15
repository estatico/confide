name := "confide"

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.11.8", "2.12.1")

lazy val confide = project.in(file("."))
  .settings(
    moduleName := "confide",

    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),

    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-unchecked",
      "-feature",
      "-deprecation",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros"
    ),

    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",

      "org.typelevel" %% "macro-compat" % "1.1.1",

      "com.chuusai" %% "shapeless" % "2.3.2",
      "com.typesafe" % "config" % "1.3.1",

      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  )
