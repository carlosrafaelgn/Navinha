# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
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

# Mais informações sobre o ProGuard:
# http://developer.android.com/intl/pt-br/tools/help/proguard.html
# http://proguard.sourceforge.net/index.html#manual/optimizations.html
# http://proguard.sourceforge.net/manual/optimizations.html

-optimizations !code/simplification/cast,!field/*,!class/merging/*,class/marking/final,method/marking/final,method/inlining/short,method/inlining/unique,code/simplification/variable,code/simplification/arithmetic,code/simplification/field,code/simplification/branch,code/simplification/string,code/simplification/advanced,code/removal/advanced,code/removal/simple,code/removal/variable,code/removal/exception,code/allocation/variable
-optimizationpasses 5
-allowaccessmodification
-overloadaggressively
-repackageclasses ''
-dontskipnonpubliclibraryclasses
-printmapping build/outputs/mapping/release/mapping.txt
-printconfiguration build/outputs/mapping/release/configuration.txt
