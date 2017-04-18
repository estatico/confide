name := "confide"

organization in ThisBuild := "io.estatico"

moduleName := "confide"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",

  "org.typelevel" %% "macro-compat" % "1.1.1",

  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.typesafe" % "config" % "1.3.1",

  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

// Publish settings

releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
homepage := Some(url("https://github.com/estatico/confide"))
licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
scmInfo := Some(
  ScmInfo(
    url("https://github.com/estatico/confide"),
    "scm:git:git@github.com:estatico/confide.git"
  )
)
developers := List(
  Developer("caryrobbins", "Cary Robbins", "carymrobbins@gmail.com",
    url("http://caryrobbins.com"))
)
