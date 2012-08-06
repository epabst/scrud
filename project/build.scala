import sbt._

import Keys._
import AndroidKeys._

object General {
  val scrudVersion = "0.3-alpha8-SNAPSHOT"
  val projectVersion = scrudVersion
  val settings = Seq(
    organization := "com.github.epabst.scrud",
    version := projectVersion,
    versionCode := 0,
    scalaVersion := "2.8.1",
    platformName in Android := "android-10",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository")

  val proguardSettings = Seq (
    useProguard in Android := true
  )

  lazy val minimalAndroidSettings =
    Defaults.defaultSettings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++
    Nil

  lazy val fullAndroidSettings = minimalAndroidSettings ++
    General.settings ++
    proguardSettings ++
    Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.RC1" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project("scrud-android-parent", file("."), settings =
      Defaults.defaultSettings ++ General.settings).aggregate(scrud, sample, tests)

  lazy val scrud = Project (
    "scrud-android",
    file("scrud-android"),
    settings = Defaults.defaultSettings ++ General.settings ++ AndroidBase.settings
  )

  lazy val sample: Project = Project (
    "scrud-android-sample",
    file("sample-app"),
    settings = General.minimalAndroidSettings
  ).dependsOn(scrud)

  lazy val tests: Project = Project (
    "scrud-android-tests",
    file("scrud-android/tests"),
    settings = Defaults.defaultSettings ++
               General.settings ++
               AndroidTest.settings ++
               General.proguardSettings ++ Seq (
      name := "Scrud Android Tests"
    )
  ) dependsOn(scrud)
}
