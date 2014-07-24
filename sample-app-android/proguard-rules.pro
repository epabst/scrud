# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/share/android-studio/data/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
        -dontwarn scala.**
        -dontwarn org.slf4j.**

        # for scala. see also http://proguard.sourceforge.net/manual/examples.html#scala
        -keep class scala.collection.SeqLike { public protected *; } # https://issues.scala-lang.org/browse/SI-5397
        -keep class scala.reflect.ScalaSignature { *; }
        -keep class scala.reflect.ScalaLongSignature { *; }
        -keep class scala.Predef$** { *; }
        -keepclassmembers class * { ** MODULE$; }
        -keep class * implements org.xml.sax.EntityResolver
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
            long eventCount;
            int  workerCounts;
            int  runControl;
            scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
            scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
            int base;
            int sp;
            int runState;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
            int status;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
        }
