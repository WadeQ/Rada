# Only what THIS app actually needs — not a generic template. Firebase, Play Billing, Google
# Play services and AndroidX Compose all ship their own consumer ProGuard rules bundled in
# their AARs, which the Android Gradle Plugin merges in automatically; duplicating rules for
# them here would just fight the shrinker's obfuscation for no benefit.
#
# Verified before writing this: the app never uses Firestore's toObject()/reflection-based
# model mapping (every read is doc.getString/getDouble/etc, every write is a plain Map), no
# Gson/Moshi/kotlinx.serialization, no Class.forName, no Parcelable data models — the usual
# sources of "obfuscation silently broke my app" don't apply here. Two things do:

# 1. ReportType round-trips through Firestore as a string (type.name written, ReportType.valueOf
#    read back) and that data outlives any single app version. Direct calls to valueOf()/values()
#    are normally reachable to R8 on their own, but this is the one place a persisted string
#    depends on it staying correct, so it's kept explicitly rather than trusted to inference.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 2. androidx.car.app (Android Auto) is a newer, less common library than the rest of the stack;
#    keep its model classes (Screen, Template, Row, etc. and their builders) rather than assume
#    its consumer rules cover everything — the Android Auto surface is already hard enough to
#    test without also wondering whether R8 renamed something in it.
-keep class androidx.car.app.** { *; }
-keep interface androidx.car.app.** { *; }

# Real, de-obfuscatable line numbers in Crashlytics stack traces instead of "Unknown Source".
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# See res/values/keep.xml for why Google Sign-In's client ID strings are protected from
# *resource* shrinking (a separate setting from this file, currently off — see build.gradle.kts).
