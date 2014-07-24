package com.github.scrud.sample.android;

import com.github.scrud.android.CrudAndroidApplication;
import com.github.scrud.android.AndroidPlatformDriver;
import com.github.scrud.sample.*;

public class SampleAndroidApplication extends CrudAndroidApplication {
    public SampleAndroidApplication() {
        super(new SampleEntityNavigation(new SampleEntityTypeMap(new AndroidPlatformDriver(R.class))));
    }
}
