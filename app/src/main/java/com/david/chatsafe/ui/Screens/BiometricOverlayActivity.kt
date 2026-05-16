package com.david.chatsafe.ui.Screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.david.chatsafe.service.ChatSafeAccessibilityService

class BiometricOverlayActivity : FragmentActivity() {

    // Variable para guardar de qué app venimos (Por defecto WhatsApp)
    private var targetPackage: String = "com.whatsapp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Atrapamos el paquete que nos mandó el Service
        targetPackage = intent.getStringExtra("TARGET_PKG") ?: "com.whatsapp"

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
            )
        }

        launchBiometricPrompt()
    }

    private fun launchBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Liberamos el candado
                    ChatSafeAccessibilityService.cancelLock()
                    // Si cancela, lo sacamos al menú de la app
                    escapeToApp()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    // DESBLOQUEO EXITOSO
                    ChatSafeAccessibilityService.unlockSession()
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Vibrará automáticamente
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ChatSafe")
            .setSubtitle("Verifica tu identidad para acceder")
            .setNegativeButtonText("Salir")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // NUEVA FUNCIÓN: En vez de ir al inicio del teléfono, reinicia la app actual
    private fun escapeToApp() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                // Estas banderas borran el historial de la app y la abren desde su pantalla principal
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(launchIntent)
            } else {
                // Si por alguna razón falla, hacemos el escape clásico al inicio
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            finish() // Siempre cerramos la pantalla transparente al final
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Si la actividad se destruye por un gesto de deslizar del sistema (atrás)
        // sin haber completado la huella con éxito, liberamos el candado de inmediato.
        // Al regresar al chat, el servicio lo volverá a bloquear en milisegundos.
        if (!ChatSafeAccessibilityService.isSessionUnlocked) {
            ChatSafeAccessibilityService.cancelLock()
        }
    }
}