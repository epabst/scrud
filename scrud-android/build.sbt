name := "Scrud Android"

organization := "com.github.epabst.scrud"

version := General.scrudVersion

scalaVersion := "2.9.2"

AndroidKeys.platformName in AndroidKeys.Android := "android-10"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "com.github.epabst.quality" % "annotations-for-quality" % "1.0-SNAPSHOT"

libraryDependencies += "com.github.epabst.scrud" % "scrud-android-res" % General.scrudVersion artifacts(
  Artifact("scrud-android-res"), Artifact("scrud-android-res", "apklib", "apklib"))

libraryDependencies in Runtime += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test"

libraryDependencies += "com.google.android" % "support-v4" % "r6"

libraryDependencies += "org.slf4j" % "slf4j-android" % "1.6.1-RC1"

// Don't include this when running simple unit tests since slf4j-jdk14 should be used then.
fullClasspath in Test ~= { classpath => classpath.filter { entry => !entry.data.getName.contains("slf4j-android") } }

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test"

libraryDependencies += "junit" % "junit" % "4.8.2" % "test"

// Required to make sbt run junit tests
libraryDependencies += "com.novocode" % "junit-interface" % "0.4" % "test"

libraryDependencies += "com.pivotallabs" % "robolectric" % "1.1" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

//todo eliminate easymock as a dependency
libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"
