name := "Scrud Android Tests"

organization := "com.github.epabst.scrud"

version := General.scrudVersion

AndroidKeys.keyalias in AndroidKeys.Android := "change-me"

AndroidKeys.versionCode := 0

scalaVersion := "2.8.1"

AndroidKeys.platformName in AndroidKeys.Android := "android-10"

AndroidKeys.useProguard in AndroidKeys.Android := true

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "junit" % "junit" % "4.5" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"
