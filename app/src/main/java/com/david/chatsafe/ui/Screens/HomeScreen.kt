package com.david.chatsafe.ui.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.david.chatsafe.R
import com.david.chatsafe.ui.theme.*
import com.david.chatsafe.utils.BlockedUserRepo

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Estados de navegación y diálogos
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Listas separadas
    var messengerList by remember { mutableStateOf(BlockedUserRepo.getBlockedNames(context).toList()) }
    var whatsappList by remember { mutableStateOf(BlockedUserRepo.getWhatsAppNames(context).toList()) }
    var instagramList by remember { mutableStateOf(BlockedUserRepo.getInstagramNames(context).toList()) }

    fun refreshLists() {
        messengerList = BlockedUserRepo.getBlockedNames(context).toList()
        whatsappList = BlockedUserRepo.getWhatsAppNames(context).toList()
        instagramList = BlockedUserRepo.getInstagramNames(context).toList()
    }

    val currentList = when (selectedTabIndex) {
        0 -> messengerList
        1 -> whatsappList
        else -> instagramList
    }

    val currentPlatform = when (selectedTabIndex) {
        0 -> "MESSENGER"
        1 -> "WHATSAPP"
        else -> "INSTAGRAM"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(CyberBlack, CyberDarkBlue)))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // HEADER
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(50))
                    .border(2.dp, NeonBlue, RoundedCornerShape(50))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CHATSAFE ACTIVE",
                color = NeonBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Vigilancia Multi-Plataforma",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- SELECTOR DE PLATAFORMA (TABS) ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = NeonBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = when (selectedTabIndex) {
                            0 -> NeonBlue
                            1 -> Color(0xFF25D366)
                            else -> Color(0xFFE1306C)
                        }
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("MESSENGER", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("WHATSAPP", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("INSTAGRAM", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTONERA ---
            val actionGradient = when (selectedTabIndex) {
                0 -> listOf(NeonBlue, NeonPurple)
                1 -> listOf(Color(0xFF075E54), Color(0xFF25D366))
                else -> listOf(Color(0xFF833AB4), Color(0xFFF77737))
            }

            CyberActionButton(
                text = "AÑADIR A $currentPlatform",
                icon = Icons.Default.Add,
                gradient = actionGradient,
                onClick = { showAddDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- LISTA DE VIGILANCIA ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "OBJETIVOS $currentPlatform [${currentList.size}]",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentList) { name ->
                    CyberItemRow(name = name, onDelete = {
                        when (selectedTabIndex) {
                            0 -> BlockedUserRepo.removeName(context, name)
                            1 -> BlockedUserRepo.removeWhatsAppName(context, name)
                            else -> BlockedUserRepo.removeInstagramName(context, name)
                        }
                        refreshLists()
                    })
                }
            }
        }

        // --- DIÁLOGOS ---
        if (showAddDialog) {
            CyberInputDialog(
                platform = currentPlatform,
                onDismiss = { showAddDialog = false },
                onConfirm = { name ->
                    if (name.isNotBlank()) {
                        when (selectedTabIndex) {
                            0 -> BlockedUserRepo.addName(context, name)
                            1 -> BlockedUserRepo.addWhatsAppName(context, name)
                            else -> BlockedUserRepo.addInstagramName(context, name)
                        }
                        refreshLists()
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---
@Composable
fun CyberActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun CyberInputDialog(
    platform: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    val warningText = when (platform) {
        "WHATSAPP" -> "Usa el nombre EXACTO como aparece en tu lista de chats de WhatsApp. (incluye emojis y letras diferentes)"
        "INSTAGRAM" -> "Usa el nombre de usuario (username) o nombre exacto del chat en Instagram Direct."
        else -> "Usa el nombre o apodo EXACTO del chat en Messenger (incluye emojis y letras diferentes)."
    }

    val placeholderText = when (platform) {
        "WHATSAPP" -> "Ej: Juan WhatsApp 🐍"
        "INSTAGRAM" -> "Ej: @jessica_rojas"
        else -> "Ej: Jessica Rojas ✨"
    }

    val accentColor = when (platform) {
        "WHATSAPP" -> Color(0xFF25D366)
        "INSTAGRAM" -> Color(0xFFE1306C)
        else -> NeonBlue
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, accentColor),
            colors = CardDefaults.cardColors(containerColor = CyberBlack.copy(alpha = 0.95f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NUEVO OBJETIVO $platform",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = warningText, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholderText, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        cursorColor = accentColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = ErrorRed)
                    }
                    Button(
                        onClick = { onConfirm(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("AÑADIR", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CyberItemRow(name: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A)).border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.Delete, "Borrar", tint = ErrorRed, modifier = Modifier.clickable { onDelete() }.padding(4.dp))
    }
}

fun Modifier.shadow(elevation: androidx.compose.ui.unit.Dp, shape: androidx.compose.ui.graphics.Shape = androidx.compose.ui.graphics.RectangleShape) = this