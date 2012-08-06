import sbt._

object General {
  val scrudVersion = "0.3-alpha8-SNAPSHOT"
  val projectVersion = scrudVersion

  lazy val androidLibrarySettings = Defaults.defaultSettings ++ AndroidBase.settings

  lazy val androidAppSettings =
    Defaults.defaultSettings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++
    Nil
}

object build extends Build {
  lazy val main = Project("scrud-android-parent", file("."), settings =
      Defaults.defaultSettings).aggregate(scrud, sample, tests)

  lazy val scrud = Project (
    "scrud-android",
    file("scrud-android"),
    settings = General.androidLibrarySettings
  )

  lazy val sample: Project = Project (
    "scrud-android-sample",
    file("sample-app"),
    settings = General.androidAppSettings
  ).dependsOn(scrud)

  lazy val tests: Project = Project (
    "scrud-android-tests",
    file("scrud-android/tests"),
    settings = Defaults.defaultSettings ++ AndroidTest.settings
  ) dependsOn(scrud)
}
