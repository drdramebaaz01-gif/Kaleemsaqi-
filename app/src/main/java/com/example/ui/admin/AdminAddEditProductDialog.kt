package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.data.model.StoreCategories
import com.example.ui.theme.DkBlack
import com.example.ui.theme.DkDiscountRed
import com.example.ui.theme.DkOrange
import com.example.ui.theme.DkWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditProductDialog(
    initialProduct: Product? = null,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: ((String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var code by remember { mutableStateOf(initialProduct?.code ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.takeIf { it > 0 }?.toInt()?.toString() ?: "") }
    var discountStr by remember { mutableStateOf(initialProduct?.discountPercentage?.toString() ?: "0") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "10") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: StoreCategories.NAMES.first()) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var isFlashSale by remember { mutableStateOf(initialProduct?.isFlashSale ?: false) }
    var isTrending by remember { mutableStateOf(initialProduct?.isTrending ?: false) }
    var isBestSeller by remember { mutableStateOf(initialProduct?.isBestSeller ?: false) }
    var isNewArrival by remember { mutableStateOf(initialProduct?.isNewArrival ?: false) }

    // Image list handling
    val imageList = remember { mutableStateListOf<String>().apply { addAll(initialProduct?.images ?: emptyList()) } }
    var newImageUrlInput by remember { mutableStateOf("") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialProduct == null) "Add New Product" else "Edit Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = DkDiscountRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                // Code SKU
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Product Code / SKU * (e.g. DKM-001)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        StoreCategories.NAMES.forEach { catName ->
                            DropdownMenuItem(
                                text = { Text(catName) },
                                onClick = {
                                    selectedCategory = catName
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price and Discount Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (PKR) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                    )

                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_discount_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                    )
                }

                // Stock
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_stock_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                // Images Section
                Text(
                    text = "Product Images (${imageList.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newImageUrlInput,
                        onValueChange = { newImageUrlInput = it },
                        label = { Text("Add Image URL or Web Link") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_image_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                    )

                    Button(
                        onClick = {
                            if (newImageUrlInput.isNotBlank()) {
                                imageList.add(newImageUrlInput.trim())
                                newImageUrlInput = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DkOrange),
                        modifier = Modifier.height(54.dp).testTag("add_image_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                // Quick image samples buttons for convenient mobile store setup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SamplePhotoChip("Watch") {
                        imageList.add("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600")
                    }
                    SamplePhotoChip("Shoes") {
                        imageList.add("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600")
                    }
                    SamplePhotoChip("Shirt") {
                        imageList.add("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600")
                    }
                    SamplePhotoChip("Headphones") {
                        imageList.add("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600")
                    }
                }

                if (imageList.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(imageList) { idx, url ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Image $idx",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { imageList.removeAt(idx) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(bottomStart = 6.dp))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("product_description_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DkOrange)
                )

                // Toggles for Store Promotion
                Text("Promotions & Badges:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                ToggleRow("Flash Sale Deal", isFlashSale) { isFlashSale = it }
                ToggleRow("Trending Product", isTrending) { isTrending = it }
                ToggleRow("Best Seller", isBestSeller) { isBestSeller = it }
                ToggleRow("New Arrival", isNewArrival) { isNewArrival = it }

                // Delete Product Option (for Edit mode)
                if (initialProduct != null && onDeleteProduct != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DkDiscountRed.copy(alpha = 0.12f), contentColor = DkDiscountRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("delete_product_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Product from Store", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Please enter product name."
                        return@Button
                    }
                    if (code.isBlank()) {
                        validationError = "Please enter product code / SKU."
                        return@Button
                    }
                    val priceVal = priceStr.toDoubleOrNull()
                    if (priceVal == null || priceVal <= 0) {
                        validationError = "Please enter a valid price."
                        return@Button
                    }
                    val discountVal = discountStr.toIntOrNull() ?: 0
                    val stockVal = stockStr.toIntOrNull() ?: 0

                    validationError = null
                    val saved = Product(
                        id = initialProduct?.id ?: "",
                        name = name.trim(),
                        code = code.trim().uppercase(),
                        description = description.trim(),
                        price = priceVal,
                        discountPercentage = discountVal.coerceIn(0, 99),
                        category = selectedCategory,
                        stock = stockVal.coerceAtLeast(0),
                        images = imageList.toList(),
                        featured = initialProduct?.featured ?: false,
                        isFlashSale = isFlashSale,
                        isTrending = isTrending,
                        isBestSeller = isBestSeller,
                        isNewArrival = isNewArrival,
                        createdAt = initialProduct?.createdAt,
                        updatedAt = null
                    )
                    onSaveProduct(saved)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DkOrange),
                enabled = !isSubmitting,
                modifier = Modifier.testTag("save_product_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = DkWhite, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (initialProduct == null) "Add Product" else "Update Product", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel")
            }
        }
    )

    // Confirmation dialog before deletion
    if (showDeleteConfirmDialog && initialProduct != null && onDeleteProduct != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${initialProduct.name}\" from DK Mart? This will immediately remove it from all customer screens.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteProduct(initialProduct.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DkDiscountRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_product_button")
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }, shape = RoundedCornerShape(8.dp)) {
                    Text("Keep Product")
                }
            }
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = DkOrange, checkedTrackColor = DkOrange.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun SamplePhotoChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF0F0F2))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text("+ $label", fontSize = 11.sp, color = DkOrange, fontWeight = FontWeight.Bold)
    }
}
