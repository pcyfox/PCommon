# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



# >>>>>>>>>>>>>>>>>>>>>>>>  Retrofit2  End >>>>>>>>>>>>>>>>>>>>>>>>
-keep class com.pcommon.lib_network.udp.UDPSocketClient

-keep class io.netty.***
-keep class io.netty.bootstrap**{ *; }
-keep class io.netty.buffer**{ *; }
-keep class io.netty.channel**{ *; }
-keep class io.netty.handler**{ *; }
-keep class io.netty.resolver**{ *; }
-keep class io.netty.channel.**{ *; }
-keep class io.netty.util.**{ *; }
-keep class com.pcommon.lib_network.entity.**{ *; }

