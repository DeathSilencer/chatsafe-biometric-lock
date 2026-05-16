package com.david.chatsafe.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.david.chatsafe.ui.Screens.BiometricOverlayActivity
import com.david.chatsafe.utils.BlockedUserRepo

class ChatSafeAccessibilityService : AccessibilityService() {

    companion object {
        var isLocked = false
        var isSessionUnlocked = false
        private var instance: ChatSafeAccessibilityService? = null

        fun unlockSession() {
            isSessionUnlocked = true
            isLocked = false
            instance?.sessionHandler?.removeCallbacks(instance?.lockSessionRunnable!!)
        }

        fun cancelLock() {
            isLocked = false
        }
    }

    private val settingsKeywords = listOf(
        "Theme", "Tema", "Nicknames", "Apodos", "Profile", "Perfil", "Block", "Bloquear",
        "Notifications", "Notificaciones", "Group info", "Info. del grupo", "Contact info",
        "Info. del contacto", "Mute", "Silenciar"
    )

    val sessionHandler = Handler(Looper.getMainLooper())

    // PARCHE DE SEGURIDAD: Limpiar la memoria al caducar la sesión
    val lockSessionRunnable = Runnable {
        isSessionUnlocked = false
        lastChatDetected = null // Olvidamos el chat para forzar una nueva verificación
    }

    private var currentPackage: String = ""
    private var lastChatDetected: String? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                // Cierre de sesión inmediato al apagar la pantalla
                isSessionUnlocked = false
                lastChatDetected = null
                sessionHandler.removeCallbacks(lockSessionRunnable)
            } else if (intent?.action == Intent.ACTION_USER_PRESENT) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val pkg = rootNode.packageName?.toString() ?: ""
                    if (pkg == "com.facebook.orca" || pkg == "com.whatsapp" || pkg == "com.instagram.android") {
                        checkForLock(rootNode, pkg)
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, screenFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        try { unregisterReceiver(screenReceiver) } catch (e: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        if (pkg != "com.facebook.orca" && pkg != "com.whatsapp" && pkg != "com.instagram.android") return
        currentPackage = pkg

        // 1. CAPTURA POR CLICK OPTIMIZADA (Bloqueo Instantáneo)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val blockedList = when (pkg) {
                "com.whatsapp" -> BlockedUserRepo.getWhatsAppNames(this)
                "com.instagram.android" -> BlockedUserRepo.getInstagramNames(this)
                else -> BlockedUserRepo.getBlockedNames(this)
            }

            // En lugar de leer el evento plano, escaneamos el interior del contenedor cliqueado
            val clickedNode = event.source
            val matchedName = findNameInClickedNode(clickedNode, blockedList)

            if (matchedName != null) {
                lastChatDetected = matchedName // Guardamos el nombre objetivo en sesión

                if (!isSessionUnlocked) {
                    showOverlay(pkg)
                    return // Interceptamos el flujo aquí mismo
                }
            }
        }

        // 2. ESCANEO DE VENTANA DE RESPALDO
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            if (isLocked) return

            val rootNode = rootInActiveWindow ?: return
            checkForLock(rootNode, pkg)
        }
    }

    // --- LA FUNCIÓN QUE FALTABA ---
    private fun checkForLock(rootNode: AccessibilityNodeInfo, pkg: String) {
        val (nameOnScreen, isInsideSensitiveArea) = quickScan(rootNode, pkg)

        // 1. Si detectamos el objetivo en pantalla y estamos dentro
        if (nameOnScreen != null && isInsideSensitiveArea) {
            lastChatDetected = nameOnScreen
            // Cancelamos la cuenta regresiva porque el usuario está chateando activamente
            sessionHandler.removeCallbacks(lockSessionRunnable)

            if (!isSessionUnlocked) {
                showOverlay(pkg)
            }
        } else {
            // 2. Si salimos del chat (ya no está el nombre visible o regresamos al menú)
            if (isSessionUnlocked) {
                if (!sessionHandler.hasCallbacks(lockSessionRunnable)) {
                    // VENTANA DE GRACIA: 1 SEGUNDO (1000 ms)
                    sessionHandler.postDelayed(lockSessionRunnable, 1000)
                }
            } else {
                lastChatDetected = null
            }
        }
    }

    // NUEVA FUNCIÓN AUXILIAR: Escanea de forma ultra veloz el elemento tocado por el usuario
    private fun findNameInClickedNode(node: AccessibilityNodeInfo?, blockedList: Set<String>): String? {
        if (node == null) return null
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)
        var counter = 0

        // Buscamos solo en la jerarquía del elemento tocado (máximo 30 sub-nodos por velocidad)
        while (!queue.isEmpty() && counter < 30) {
            val current = queue.removeFirst()
            counter++

            val text = current.text?.toString()
            val desc = current.contentDescription?.toString()

            if (text != null) {
                val matched = blockedList.find { text.contains(it, ignoreCase = true) }
                if (matched != null) return matched
            }
            if (desc != null) {
                val matched = blockedList.find { desc.contains(it, ignoreCase = true) }
                if (matched != null) return matched
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun quickScan(rootNode: AccessibilityNodeInfo, pkg: String): Pair<String?, Boolean> {
        var foundBlockedName: String? = null
        var isInsideSensitiveArea = false

        val blockedList = when (pkg) {
            "com.whatsapp" -> BlockedUserRepo.getWhatsAppNames(this)
            "com.instagram.android" -> BlockedUserRepo.getInstagramNames(this)
            else -> BlockedUserRepo.getBlockedNames(this)
        }

        if (blockedList.isEmpty()) return Pair(null, false)

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        var counter = 0

        while (!queue.isEmpty() && counter < 120) {
            val node = queue.removeFirst()
            counter++

            val nodeText = node.text?.toString()
            val nodeDesc = node.contentDescription?.toString()
            val viewId = node.viewIdResourceName ?: ""

            if (foundBlockedName == null) {
                if (nodeText != null && blockedList.any { nodeText.contains(it, ignoreCase = true) }) {
                    foundBlockedName = nodeText
                } else if (nodeDesc != null && blockedList.any { nodeDesc.contains(it, ignoreCase = true) }) {
                    foundBlockedName = nodeDesc
                } else if (viewId.contains("conversation_contact_name") || viewId.contains("thread_title")) {
                    if (nodeText != null && blockedList.any { nodeText.contains(it, ignoreCase = true) }) {
                        foundBlockedName = nodeText
                    }
                }
            }

            if (node.className == "android.widget.EditText") isInsideSensitiveArea = true
            if (nodeText != null && settingsKeywords.any { nodeText.contains(it, ignoreCase = true) }) {
                isInsideSensitiveArea = true
            }

            if (foundBlockedName != null && isInsideSensitiveArea) break

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return Pair(foundBlockedName, isInsideSensitiveArea)
    }

    private fun showOverlay(currentPkg: String) {
        if (isLocked) return
        isLocked = true

        val intent = Intent(this, BiometricOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("TARGET_PKG", currentPkg)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            isLocked = false
        }
    }

    override fun onInterrupt() {}
}