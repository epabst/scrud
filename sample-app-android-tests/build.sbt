name := "Scrud Android Tests"

organization := "com.github.epabst.scrud"

version := General.scrudVersion

AndroidKeys.keyalias in AndroidKeys.Android := "change-me"

AndroidKeys.versionCode := 0

scalaVersion := "2.9.2"

AndroidKeys.platformName in AndroidKeys.Android := "android-10"

AndroidKeys.useProguard in AndroidKeys.Android := true

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "com.jayway.android.robotium" % "robotium-solo" % "3.4.1"

libraryDependencies in Runtime += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test"
