# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

# Kotlin / Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.jvm.functions.** { *; }

# OkHttp / Okio
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class okhttp3.internal.** { *; }

# JSON (org.json used throughout)
-keep class org.json.** { *; }

# AndroidX Security Crypto (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }
-keep class androidx.security.** { *; }

# Coil (if used)
-keep class coil3.** { *; }

# Gson (if used)
-keep class com.google.gson.** { *; }

# Keep model classes for serialization
-keep class dev.krinry.jarvis.ai.ModelInfo { *; }
-keep class dev.krinry.jarvis.agent.ActionExecutor$AgentAction { *; }
-keep class dev.krinry.jarvis.agent.UiTreeExtractor$UiNode { *; }

# Keep AccessibilityService and related
-keep class dev.krinry.jarvis.service.AutoAgentService { *; }
-keep class dev.krinry.jarvis.service.FloatingBubbleService { *; }

# Keep Agent Engine and TTS
-keep class dev.krinry.jarvis.agent.AgentLlmEngine { *; }
-keep class dev.krinry.jarvis.agent.AgentTtsManager { *; }

# Keep SecureKeyStore
-keep class dev.krinry.jarvis.security.SecureKeyStore { *; }

# Keep BuildConfig and R
-keep class dev.krinry.jarvis.BuildConfig { *; }
-keep class dev.krinry.jarvis.R { *; }
-keep class dev.krinry.jarvis.R$* { *; }

# Preserve enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}