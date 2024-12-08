-dontobfuscate
-dontnote **

# toastbits
-keep class dev.toastbits.** { *; }

# Okio
-keep class okio.** { *; }

# Ktor
-keep class io.ktor.client.engine.okhttp.** { *; }

# Database
-keep class * implements java.sql.Driver
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

# Kotlinx
-keep, allowoptimization @kotlinx.serialization.Serializable class *
-keep class kotlinx.coroutines.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }

# Other
-dontwarn aQute.bnd.annotation.**
-dontwarn android.**
-dontwarn com.badlogic.gdx.**
-dontwarn com.sshtools.twoslices.impl.**
-dontwarn com.sun.jna.**
-dontwarn games.spooky.gdx.nativefilechooser.desktop.**
-dontwarn javafx.**
-dontwarn javax.annotation.**
-dontwarn javax.microedition.khronos.egl.**
-dontwarn org.apache.tika.io.MappedBufferCleaner
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.controlsfx.**
-dontwarn org.eclipse.swt.**
-dontwarn org.freedesktop.dbus.**
-dontwarn org.openjsse.**
-dontwarn org.osgi.**
