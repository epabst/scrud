package com.github.scrud.sample.android;

import com.github.scrud.android.CrudAndroidApplication;
import com.github.scrud.android.AndroidPlatformDriver;
import com.github.scrud.sample.*;

public class AndroidSampleApplication extends CrudAndroidApplication {
    public AndroidSampleApplication() {
        super(new SampleEntityNavigation(new SampleEntityTypeMap(new AndroidPlatformDriver(R.class))));
    }
}
