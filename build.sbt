organization in ThisBuild := "io.estatico"

lazy val root = applyDefaultSettings(project.in(file(".")))
  .settings(noPublishSettings)
  .aggregate(core, macros, java8)

lazy val core = confideModule("core")

lazy val macros = confideModule("macros").dependsOn(core).settings(macroSettings)

lazy val java8 = confideModule("java8").dependsOn(core)

lazy val defaultScalacOptions = Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros"
)

lazy val defaultLibraryDependencies = Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.typesafe" % "config" % "1.3.1"
)

lazy val defaultTestDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

lazy val macroSettings = Seq(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
    "org.typelevel" %% "macro-compat" % "1.1.1"
  )
)

lazy val defaultPublishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/estatico/confide")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/estatico/confide"),
      "scm:git:git@github.com:estatico/confide.git"
    )
  ),
  developers := List(
    Developer("caryrobbins", "Cary Robbins", "carymrobbins@gmail.com", url("http://caryrobbins.com"))
  )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
).toSeq

def applyDefaultSettings(project: Project) = project.settings(
  scalacOptions ++= defaultScalacOptions,
  libraryDependencies ++= defaultLibraryDependencies ++ defaultTestDependencies,
  defaultPublishSettings
)

def confideModule(path: String) = {
  // Convert path from lisp-case to camelCase
  val id = path.split("-").reduce(_ + _.capitalize)
  // Convert path from list-case to "Confide with spaces"
  val docName = path.replace('-', ' ')
  // Set default and module-specific settings.
  applyDefaultSettings(Project(id, file(path))).settings(
    name := "Confide " + docName,
    moduleName := "confide-" + path,
    description := "confide" + docName
  )
}
