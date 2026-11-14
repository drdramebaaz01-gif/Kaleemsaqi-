package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StoreCategories
import com.example.data.model.StoreCategory
import com.example.ui.components.ProductCard
import com.example.ui.components.PromotionalBanner
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkDiscountRed
import com.example.ui.theme.DkLightGrey
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkOrangeDark
import com.example.ui.theme.DkWhite
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    products: List<Product>,
    isLoading: Boolean,
    wishlistProductIds: Set<String>,
    cartCount: Int,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onCategoryClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onAdminLoginClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Flash sale countdown simulation (Daraz style)
    var countdownSeconds by remember { mutableIntStateOf(19842) }
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        }
    }
    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val seconds = countdownSeconds % 60

    // Filtered products list
    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.code.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == null ||
                product.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesQuery && matchesCategory
    }

    val flashSaleProducts = products.filter { it.isFlashSale || it.discountPercentage >= 20 }
    val trendingProducts = products.filter { it.isTrending || it.discountPercentage >= 15 }
    val bestSellers = products.filter { it.isBestSeller }
    val newArrivals = products.filter { it.isNewArrival }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Daraz Orange App Bar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DkOrange)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo & App Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DkWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "DK Mart",
                                tint = DkOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "DK Mart",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = DkWhite
                            )
                        }
                    }

                    // Header Action Icons (Wishlist and Cart)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onWishlistClick,
                            modifier = Modifier.testTag("home_wishlist_icon")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (wishlistProductIds.isNotEmpty()) {
                                        Badge(containerColor = DkWhite, contentColor = DkOrange) {
                                            Text(wishlistProductIds.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Wishlist",
                                    tint = DkWhite
                                )
                            }
                        }

                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.testTag("home_cart_icon")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = DkWhite, contentColor = DkOrange) {
                                            Text(cartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = DkWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar with clear button
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar"),
                    placeholder = {
                        Text(
                            "Search on DK Mart (e.g. shoes, watch, shirt)...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DkOrange
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DkWhite,
                        unfocusedContainerColor = DkWhite,
                        focusedBorderColor = DkWhite,
                        unfocusedBorderColor = DkWhite,
                        focusedTextColor = DkBlack,
                        unfocusedTextColor = DkBlack
                    )
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DkOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Promotional Banners Carousel
                item {
                    PromotionalBanner(modifier = Modifier.padding(16.dp))
                }

                // Category Chips / Icons Row
                item {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (selectedCategoryFilter != null) {
                                Text(
                                    text = "Clear filter",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DkOrange,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable { selectedCategoryFilter = null }
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(StoreCategories.ALL) { category ->
                                val isSelected = selectedCategoryFilter == category.name
                                CategoryCircleItem(
                                    category = category,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedCategoryFilter = if (isSelected) null else category.name
                                        onCategoryClick(category.name)
                                    }
                                )
                            }
                        }
                    }
                }

                // Empty products state
                if (products.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = DkOrange,
                                    modifier = Modifier.size(54.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "DK Mart Product Catalog Is Live!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Ready to add products. Store admin can log in using the bottom-right ©️ icon to add products and manage customer orders.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DkOrange)
                                        .clickable(onClick = onAdminLoginClick)
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .testTag("empty_state_admin_button")
                                ) {
                                    Text(
                                        text = "Open Admin Login",
                                        color = DkWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Flash Sale Section (if any flash sale products exist)
                if (flashSaleProducts.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(DkWhite)
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = "Flash Sale",
                                        tint = DkDiscountRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FLASH SALE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = DkDiscountRed
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Countdown timer box
                                    TimerBadge(String.format("%02d", hours))
                                    Text(":", fontWeight = FontWeight.Bold, color = DkBlack, modifier = Modifier.padding(horizontal = 2.dp))
                                    TimerBadge(String.format("%02d", minutes))
                                    Text(":", fontWeight = FontWeight.Bold, color = DkBlack, modifier = Modifier.padding(horizontal = 2.dp))
                                    TimerBadge(String.format("%02d", seconds))
                                }

                                Text(
                                    text = "SHOP MORE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DkOrange
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(flashSaleProducts) { product ->
                                    ProductCard(
                                        product = product,
                                        isWishlisted = wishlistProductIds.contains(product.id),
                                        onClick = { onProductClick(product) },
                                        onAddToCart = { onAddToCart(product) },
                                        onToggleWishlist = { onToggleWishlist(product) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Trending Products Section
                if (trendingProducts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Trending Products",
                            icon = Icons.Default.TrendingUp,
                            subtitle = "Most wanted by shoppers right now"
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(trendingProducts) { product ->
                                ProductCard(
                                    product = product,
                                    isWishlisted = wishlistProductIds.contains(product.id),
                                    onClick = { onProductClick(product) },
                                    onAddToCart = { onAddToCart(product) },
                                    onToggleWishlist = { onToggleWishlist(product) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }

                // All Products Grid (Daraz 2-Column Grid)
                if (filteredProducts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = if (selectedCategoryFilter != null) "$selectedCategoryFilter Products" else "Just For You",
                            icon = Icons.Default.Whatshot,
                            subtitle = "Recommended based on your shopping interest"
                        )
                    }

                    // Chunk in pairs of 2 for a clean 2-column grid in LazyColumn
                    val productChunks = filteredProducts.chunked(2)
                    items(productChunks) { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = chunk[0],
                                    isWishlisted = wishlistProductIds.contains(chunk[0].id),
                                    onClick = { onProductClick(chunk[0]) },
                                    onAddToCart = { onAddToCart(chunk[0]) },
                                    onToggleWishlist = { onToggleWishlist(chunk[0]) }
                                )
                            }

                            if (chunk.size > 1) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(
                                        product = chunk[1],
                                        isWishlisted = wishlistProductIds.contains(chunk[1].id),
                                        onClick = { onProductClick(chunk[1]) },
                                        onAddToCart = { onAddToCart(chunk[1]) },
                                        onToggleWishlist = { onToggleWishlist(chunk[1]) }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerBadge(time: String) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DkBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = DkWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DkOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryCircleItem(
    category: StoreCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSelected) DkOrange else DkWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) DkWhite else DkOrange,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (isSelected) DkOrange else MaterialTheme.colorScheme.onBackground
        )
    }
}
