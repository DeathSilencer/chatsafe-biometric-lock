package com.david.chatsafe.ui.Screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.david.chatsafe.R
import com.david.chatsafe.service.ChatSafeAccessibilityService
import com.david.chatsafe.ui.theme.CyberBlack
import com.david.chatsafe.ui.theme.CyberDarkBlue
import com.david.chatsafe.ui.theme.CyberText
import com.david.chatsafe.ui.theme.NeonBlue

@Composable
fun PermissionsScreen(onAllPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ESTADOS
    var isOverlayGranted by remember { mutableStateOf(false) }
    var isAccessibilityGranted by remember { mutableStateOf(false) }

    fun checkPermissions() {
        isOverlayGranted = Settings.canDrawOverlays(context)
        isAccessibilityGranted = isAccessibilityServiceEnabled(context, ChatSafeAccessibilityService::class.java)

        if (isOverlayGranted && isAccessibilityGranted) {
            onAllPermissionsGranted()
        }
    }

    // Chequear cada vez que volvemos a la app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) checkPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberBlack, CyberDarkBlue, Color.Black)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(50))
                    .border(2.dp, NeonBlue, RoundedCornerShape(50))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SYSTEM PERMISSIONS",
                color = NeonBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Grant access to initialize ChatSafe protocols",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // --- PERMISO 1: OVERLAY ---
            CyberPermissionRow(
                title = "Visual Overlay",
                desc = "Required to draw the lock screen over Messenger.",
                icon = Icons.Default.Layers,
                isGranted = isOverlayGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- PERMISO 2: ACCESIBILIDAD ---
            CyberPermissionRow(
                title = "Chat Monitor",
                desc = "Required to detect when Messenger is opened.",
                icon = Icons.Default.AccessibilityNew,
                isGranted = isAccessibilityGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }

        // Botón inferior (Solo decorativo si no están listos, o acción final)
        if (isOverlayGranted && isAccessibilityGranted) {
            Button(
                onClick = onAllPermissionsGranted,
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp)
            ) {
                Text("INITIALIZE SYSTEM", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CyberPermissionRow(
    title: String,
    desc: String,
    icon: ImageVector,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isGranted) NeonBlue else Color.DarkGray
    val iconColor = if (isGranted) NeonBlue else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { if (!isGranted) onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CyberText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(desc, color = Color.Gray, fontSize = 12.sp)
        }
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, null, tint = NeonBlue)
        } else {
            Text("GRANT", color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// Función auxiliar para chequear si el servicio está activo
fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    val myComponentName = android.content.ComponentName(context, service)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(myComponentName.flattenToString(), ignoreCase = true)) {
            return true
        }
    }
    return false
}