package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserRepository
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkDiscountRed
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkWhite

@Composable
fun AdminLoginDialog(
    currentUserEmail: String?,
    onDismiss: () -> Unit,
    onAdminVerified: () -> Unit
) {
    var adminEmail by remember { mutableStateOf(currentUserEmail ?: "") }
    var adminPasscode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isCurrentGoogleUserAdmin = currentUserEmail.equals(UserRepository.BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DkBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = DkOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("DK Mart Admin Login", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Store Management Console", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCurrentGoogleUserAdmin) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DkOrange.copy(alpha = 0.12f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Authenticated with authorized admin account ($currentUserEmail).",
                            color = DkOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = DkDiscountRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Admin Email") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = DkOrange) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                OutlinedTextField(
                    value = adminPasscode,
                    onValueChange = { adminPasscode = it },
                    label = { Text("Admin Security Code / Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DkOrange) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                Text(
                    text = "Authorized store owners have access to add products, modify inventory & change order status.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isVerifying = true
                    errorMessage = null

                    // Verification logic: Authorized bootstrap email or store security credentials
                    val emailMatches = adminEmail.trim().equals(UserRepository.BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true) ||
                            currentUserEmail.equals(UserRepository.BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)

                    val isAuthorized = emailMatches || adminPasscode.trim() == "admin123" || adminPasscode.trim() == "dkmart2026"

                    if (isAuthorized) {
                        isVerifying = false
                        onAdminVerified()
                    } else {
                        isVerifying = false
                        errorMessage = "Access Denied: Invalid admin credentials or unauthorized account."
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DkOrange),
                modifier = Modifier.testTag("admin_login_submit_button")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = DkWhite, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enter Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
