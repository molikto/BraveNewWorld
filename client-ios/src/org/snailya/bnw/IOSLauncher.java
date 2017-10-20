package org.snailya.bnw;


import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import org.robovm.apple.uikit.UIScreen;
import org.snailya.base.PlatformDependentInfo;

class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        float scale = (float) UIScreen.getMainScreen().getScale();
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        return new IOSApplication(new BraveNewWorldWrapper(new PlatformDependentInfo(scale, null)), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}