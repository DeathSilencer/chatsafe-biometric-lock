package com.david.chatsafe

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.david.chatsafe.service.ChatSafeAccessibilityService
import com.david.chatsafe.ui.Screens.HomeScreen
import com.david.chatsafe.ui.Screens.PermissionsScreen
import com.david.chatsafe.ui.Screens.isAccessibilityServiceEnabled
import com.david.chatsafe.ui.theme.ChatSafeTheme
import com.david.chatsafe.ui.theme.CyberBlack

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChatSafeTheme {
                var isAppUnlocked by remember { mutableStateOf(false) }
                var hasPermissions by remember { mutableStateOf(false) }

                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // Verifica permisos actuales
                fun checkPermissions() {
                    val overlay = Settings.canDrawOverlays(context)
                    val accessibility = isAccessibilityServiceEnabled(context, ChatSafeAccessibilityService::class.java)
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val battery = pm.isIgnoringBatteryOptimizations(context.packageName)
                    hasPermissions = overlay && accessibility && battery
                }

                // Cierra la app al ir a segundo plano y pide huella al volver
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START && !isAppUnlocked) {
                            launchAppBiometricAuth(
                                onSuccess = {
                                    isAppUnlocked = true
                                    checkPermissions()
                                },
                                onError = { finish() } // Cierra la app si cancela
                            )
                        } else if (event == Lifecycle.Event.ON_STOP) {
                            // Bloquea la app inmediatamente si se minimiza
                            isAppUnlocked = false
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Renderizado condicional
                if (isAppUnlocked) {
                    if (hasPermissions) {
                        HomeScreen()
                    } else {
                        PermissionsScreen(onAllPermissionsGranted = { hasPermissions = true })
                    }
                } else {
                    // Pantalla de protección negra mientras pide la huella
                    Box(modifier = Modifier.fillMaxSize().background(CyberBlack))
                }
            }
        }
    }

    private fun launchAppBiometricAuth(onSuccess: () -> Unit, onError: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Vibrará automáticamente
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ChatSafe")
            .setSubtitle("Autenticación requerida")
            .setNegativeButtonText("Salir")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}