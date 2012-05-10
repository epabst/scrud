import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
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
  lazy val main = Project("scrud-android-parent", file(".")).aggregate(scrud, sample, tests)

  lazy val scrud = Project (
    "scrud-android",
    file("scrud-android"),
    settings = General.fullAndroidSettings ++ Seq(
      libraryDependencies += "com.github.epabst.triangle" % "triangle" % "0.6-SNAPSHOT",
      libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test",
      libraryDependencies += "org.slf4j" % "slf4j-android" % "1.6.1-RC1",
      libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test",
      libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.8.1",
      libraryDependencies += "junit" % "junit" % "4.8.2" % "test",
      //todo eliminate easymock as a dependency
      libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"
    )
  )

  lazy val sample: Project = Project (
    "scrud-android-sample",
    file("sample-app"),
    settings = General.fullAndroidSettings ++ Seq(
      libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test",
      libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test",
      libraryDependencies += "junit" % "junit" % "4.8.2" % "test",
      //todo eliminate easymock as a dependency
      libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"
    )
  ).dependsOn(scrud)

  lazy val tests: Project = Project (
    "scrud-android-tests",
    file("scrud-android/tests"),
    settings = General.settings ++
               AndroidTest.settings ++
               General.proguardSettings ++ Seq (
      name := "Scrud Android Tests"
    )
  ) dependsOn(scrud)
}
