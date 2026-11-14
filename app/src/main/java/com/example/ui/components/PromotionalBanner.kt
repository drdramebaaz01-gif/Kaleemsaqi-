package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkOrangeDark
import com.example.ui.theme.DkWhite
import kotlinx.coroutines.delay

data class BannerItem(
    val title: String,
    val subtitle: String,
    val badge: String,
    val gradient: List<Color>
)

val BANNERS = listOf(
    BannerItem(
        title = "DK Mart Mega Deals",
        subtitle = "Up to 60% OFF on Electronics & Fashion",
        badge = "FLASH DEALS",
        gradient = listOf(DkOrange, DkOrangeDark)
    ),
    BannerItem(
        title = "Cash On Delivery Available",
        subtitle = "Pay securely when parcel arrives at your door",
        badge = "100% COD",
        gradient = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
    ),
    BannerItem(
        title = "Fast Nationwide Shipping",
        subtitle = "Delivery across Karachi, Lahore, Islamabad & more",
        badge = "EXPRESS",
        gradient = listOf(Color(0xFF1E1E1E), Color(0xFF37474F))
    )
)

@Composable
fun PromotionalBanner(
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentIndex = (currentIndex + 1) % BANNERS.size
        }
    }

    val banner = BANNERS[currentIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(banner.gradient))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 40.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DkWhite.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = banner.badge,
                        color = DkWhite,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = banner.title,
                    color = DkWhite,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = banner.subtitle,
                    color = DkWhite.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Indicator Dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BANNERS.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) DkWhite else DkWhite.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}
