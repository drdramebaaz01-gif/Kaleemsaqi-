package com.example.ui.admin

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.StoreCategories
import com.example.ui.orders.OrderStatusChip
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkDiscountRed
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkSuccessGreen
import com.example.ui.theme.DkWhite
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    products: List<Product>,
    orders: List<Order>,
    isProductActionLoading: Boolean,
    onBackClick: () -> Unit,
    onAddProduct: (Product) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Dialog state for Add/Edit
    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.code.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == null ||
                product.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesQuery && matchesCategory
    }

    val totalRevenue = orders.filter { it.status.lowercase() != "cancelled" }.sumOf { it.totalAmount }
    val pendingOrders = orders.filter { it.status.lowercase() == "pending" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DK Mart Admin", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DkOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("CONSOLE", color = DkWhite, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DkBlack,
                    titleContentColor = DkWhite,
                    navigationIconContentColor = DkWhite
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        productToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = DkOrange,
                    contentColor = DkWhite,
                    modifier = Modifier.testTag("admin_fab_add_product")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Metrics Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricItem(label = "Products", value = products.size.toString(), color = DkOrange)
                    MetricItem(label = "Orders", value = orders.size.toString(), color = DkBlack)
                    MetricItem(label = "Pending", value = pendingOrders.size.toString(), color = DkDiscountRed)
                    MetricItem(label = "Revenue", value = "Rs. ${formatter.format(totalRevenue.toLong())}", color = DkSuccessGreen)
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DkOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = DkOrange
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Products (${products.size})", fontWeight = FontWeight.Bold) },
                    selectedContentColor = DkOrange,
                    unselectedContentColor = Color.Gray,
                    modifier = Modifier.testTag("admin_tab_products")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Orders (${orders.size})", fontWeight = FontWeight.Bold) },
                    selectedContentColor = DkOrange,
                    unselectedContentColor = Color.Gray,
                    modifier = Modifier.testTag("admin_tab_orders")
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Products Management Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search & Filter Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search product name or code...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DkOrange) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("admin_product_search"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                            )
                        }

                        // Category Filter chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selectedCategoryFilter == null) DkOrange else Color(0xFFEFEFEF))
                                        .clickable { selectedCategoryFilter = null }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "All",
                                        color = if (selectedCategoryFilter == null) DkWhite else DkBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            items(StoreCategories.NAMES) { catName ->
                                val isSelected = selectedCategoryFilter == catName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) DkOrange else Color(0xFFEFEFEF))
                                        .clickable { selectedCategoryFilter = if (isSelected) null else catName }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = catName,
                                        color = if (isSelected) DkWhite else DkBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("No products found.", fontWeight = FontWeight.Bold)
                                    Text("Tap the + button to add products to your store.", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredProducts, key = { it.id }) { product ->
                                    AdminProductRowItem(
                                        product = product,
                                        onEdit = {
                                            productToEdit = product
                                            showAddEditDialog = true
                                        },
                                        onDelete = {
                                            onDeleteProduct(product.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Orders Management Tab
                    if (orders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Customer Orders Yet", fontWeight = FontWeight.Bold)
                                Text("When customers place orders via Cash on Delivery, manage them here.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(orders, key = { it.id }) { order ->
                                AdminOrderCard(
                                    order = order,
                                    onStatusSelected = { newStatus ->
                                        onUpdateOrderStatus(order.id, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (showAddEditDialog) {
        AdminAddEditProductDialog(
            initialProduct = productToEdit,
            isSubmitting = isProductActionLoading,
            onDismiss = {
                showAddEditDialog = false
                productToEdit = null
            },
            onSaveProduct = { product ->
                if (product.id.isNotBlank()) {
                    onUpdateProduct(product)
                } else {
                    onAddProduct(product)
                }
                showAddEditDialog = false
                productToEdit = null
            },
            onDeleteProduct = { productId ->
                onDeleteProduct(productId)
                showAddEditDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun AdminProductRowItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_product_item_${product.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                if (product.primaryImage.isNotBlank()) {
                    AsyncImage(
                        model = product.primaryImage,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "${product.category} • SKU: ${product.code}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rs. ${formatter.format(product.price.toLong())} (Stock: ${product.stock})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DkOrange
                    )

                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("admin_edit_${product.id}")) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DkOrange, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("admin_delete_${product.id}")) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DkDiscountRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onStatusSelected: (String) -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    var isStatusMenuExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Pending", "Confirmed", "Processing", "Shipped", "Delivered", "Cancelled")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_${order.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val date = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Recent"
                    Text(date, fontSize = 11.sp, color = Color.Gray)
                }

                // Interactive Order Status Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isStatusMenuExpanded = true }
                            .testTag("order_status_dropdown_${order.id}")
                    ) {
                        OrderStatusChip(status = order.status)
                    }

                    DropdownMenu(
                        expanded = isStatusMenuExpanded,
                        onDismissRequest = { isStatusMenuExpanded = false }
                    ) {
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontWeight = if (order.status == opt) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    isStatusMenuExpanded = false
                                    onStatusSelected(opt)
                                }
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F2))

            // Customer details
            Text("Customer: ${order.customerName} (${order.mobileNumber})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Address: ${order.deliveryAddress}, ${order.city}", fontSize = 11.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(6.dp))

            // Items summary
            order.items.forEach { rawItem ->
                val name = rawItem["name"] as? String ?: "Item"
                val qty = (rawItem["quantity"] as? Number)?.toInt() ?: 1
                val price = (rawItem["price"] as? Number)?.toDouble() ?: 0.0
                Text("• ${qty}x $name (Rs. ${formatter.format(price.toLong() * qty)})", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.paymentMethod, fontSize = 11.sp, color = DkSuccessGreen, fontWeight = FontWeight.Bold)
                Text("Total: Rs. ${formatter.format(order.totalAmount.toLong())}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = DkOrange)
            }
        }
    }
}
