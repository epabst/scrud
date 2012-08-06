name := "Scrud Android"

organization := "com.github.epabst.scrud"

version := General.scrudVersion

scalaVersion := "2.8.1"

AndroidKeys.platformName in AndroidKeys.Android := "android-10"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "com.github.epabst.triangle" % "triangle" % "0.6-SNAPSHOT"

libraryDependencies += "com.github.epabst.scrud" % "scrud-android-res" % General.projectVersion artifacts(
  Artifact("scrud-android-res"), Artifact("scrud-android-res", "apklib", "apklib"))

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test"

libraryDependencies += "org.slf4j" % "slf4j-android" % "1.6.1-RC1"

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test"

libraryDependencies += "junit" % "junit" % "4.8.2" % "test"

libraryDependencies += "com.pivotallabs" % "robolectric" % "1.0" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.RC1" % "test"

//todo eliminate easymock as a dependency
libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"