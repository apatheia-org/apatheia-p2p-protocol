ThisBuild / organization := "org.apatheia"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "0.0.9-alpha"

lazy val root = (project in file(".")).settings(
  name := "apatheia-p2p-protocol",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.4.8",

    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.4.8",

    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.4.8",

    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),

    // log4cats+logback
    "org.typelevel" %% "log4cats-slf4j"  % "2.5.0",
    "ch.qos.logback" % "logback-classic" % "1.4.5",

    // test
    "org.scalactic"     %% "scalactic"    % "3.2.15",
    "org.scalatest"     %% "scalatest"    % "3.2.15"   % "test",
    "org.scalatestplus" %% "mockito-4-6"  % "3.2.15.0" % "test",
    "org.mockito"        % "mockito-core" % "4.6.0"    % "test"
  ),
  semanticdbCompilerPlugin := {
    ("org.scalameta" % "semanticdb-scalac" % semanticdbVersion.value)
      .cross(CrossVersion.full)
  },
  semanticdbEnabled := true,                        // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision, // only required for Scala 2.x
  scalacOptions += "-Wunused:imports" // Scala 2.x only, required by `RemoveUnused`
)

// publish to github packages settings
ThisBuild / publishTo := Some(
  "GitHub Adriano Brito's Apache Maven Packages" at "https://maven.pkg.github.com/apatheia-org/apatheia-p2p-protocol"
)
ThisBuild / publishMavenStyle := true
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "adrianobrito",
  System.getenv("GITHUB_TOKEN")
)
