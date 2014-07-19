package com.github.scrud.android

class CrudActivityForRobolectric extends CrudActivity {
  override final lazy val commandContext: AndroidCommandContextForRobolectric = new AndroidCommandContextForRobolectric(androidApplication, this)

  override def androidApplication: CrudAndroidApplicationForRobolectric =
    super.androidApplication.asInstanceOf[CrudAndroidApplicationForRobolectric]

  //make it public for testing
  override def onPause() {
    super.onPause()
  }

  //make it public for testing
  override def onResume() {
    super.onResume()
  }
}
