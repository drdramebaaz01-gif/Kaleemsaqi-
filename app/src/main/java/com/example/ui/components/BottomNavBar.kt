package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkWhite

enum class MainTab {
    HOME,
    CATEGORIES,
    CART,
    ORDERS,
    PROFILE
}

@Composable
fun DkBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    cartItemCount: Int = 0,
    onAdminIconClicked: () -> Unit
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = DkBlack,
            tonalElevation = 6.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == MainTab.HOME,
                onClick = { onTabSelected(MainTab.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DkOrange,
                    selectedTextColor = DkOrange,
                    indicatorColor = DkOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_home")
            )

            NavigationBarItem(
                selected = selectedTab == MainTab.CATEGORIES,
                onClick = { onTabSelected(MainTab.CATEGORIES) },
                icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                label = { Text("Categories", fontSize = 11.sp, fontWeight = if (selectedTab == MainTab.CATEGORIES) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DkOrange,
                    selectedTextColor = DkOrange,
                    indicatorColor = DkOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_categories")
            )

            NavigationBarItem(
                selected = selectedTab == MainTab.CART,
                onClick = { onTabSelected(MainTab.CART) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = DkOrange,
                                    contentColor = DkWhite
                                ) {
                                    Text(text = if (cartItemCount > 99) "99+" else cartItemCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                label = { Text("Cart", fontSize = 11.sp, fontWeight = if (selectedTab == MainTab.CART) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DkOrange,
                    selectedTextColor = DkOrange,
                    indicatorColor = DkOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_cart")
            )

            NavigationBarItem(
                selected = selectedTab == MainTab.ORDERS,
                onClick = { onTabSelected(MainTab.ORDERS) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                label = { Text("Orders", fontSize = 11.sp, fontWeight = if (selectedTab == MainTab.ORDERS) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DkOrange,
                    selectedTextColor = DkOrange,
                    indicatorColor = DkOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_orders")
            )

            NavigationBarItem(
                selected = selectedTab == MainTab.PROFILE,
                onClick = { onTabSelected(MainTab.PROFILE) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                label = { Text("Account", fontSize = 11.sp, fontWeight = if (selectedTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DkOrange,
                    selectedTextColor = DkOrange,
                    indicatorColor = DkOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("tab_profile")
            )
        }

        // Hidden tiny ©️ admin icon in bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(20.dp)
                .clip(CircleShape)
                .clickable(onClick = onAdminIconClicked)
                .testTag("hidden_admin_icon"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "©️",
                fontSize = 11.sp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}
