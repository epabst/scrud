libraryDependencies += "com.github.epabst.triangle" % "triangle" % "0.6-SNAPSHOT"

libraryDependencies += "com.github.epabst.scrud" % "scrud-android-res" % General.projectVersion artifacts(
  Artifact("scrud-android-res"), Artifact("scrud-android-res", "apklib", "apklib"))

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "test"

libraryDependencies += "org.slf4j" % "slf4j-android" % "1.6.1-RC1"

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5" % "test"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.8.1"

libraryDependencies += "junit" % "junit" % "4.8.2" % "test"

//todo eliminate easymock as a dependency
libraryDependencies += "org.easymock" % "easymock" % "2.5.2" % "test"
