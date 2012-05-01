import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "Scrud Android",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.8.1",
    platformName in Android := "android-10",
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

  val proguardSettings = Seq (
    useProguard in Android := true
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.RC1" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "Scrud Android",
    file("scrud-android"),
    settings = General.fullAndroidSettings ++ Seq(
      libraryDependencies += "com.github.epabst.triangle" % "triangle" % "0.6-SNAPSHOT",
      libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test",
      libraryDependencies += "org.slf4j" % "slf4j-android" % "1.6.1-RC1",
      libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test",
      libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.8.1",
      //todo eliminate easymock as a dependency
      libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test",
      libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"
    )
  )

  lazy val tests = Project (
    "tests",
    file("scrud-android/tests"),
    settings = General.settings ++
               AndroidTest.settings ++
               General.proguardSettings ++ Seq (
      name := "Scrud AndroidTests"
    )
  ) dependsOn main
}
