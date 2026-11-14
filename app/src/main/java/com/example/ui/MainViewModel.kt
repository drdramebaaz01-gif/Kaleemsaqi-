package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.WishlistItem
import com.example.data.repository.CartRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WishlistRepository
import com.example.ui.components.MainTab
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data class Categories(val category: String? = null) : Screen
    data object Cart : Screen
    data object Checkout : Screen
    data object Orders : Screen
    data object Profile : Screen
    data object Wishlist : Screen
    data class ProductDetail(val product: Product) : Screen
    data object AdminDashboard : Screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepo = ProductRepository(application)
    private val orderRepo = OrderRepository(application)
    private val cartRepo = CartRepository(application)
    private val wishlistRepo = WishlistRepository(application)
    private val userRepo = UserRepository(application)

    // Current Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    // Auth & User Profile
    val currentUser: StateFlow<FirebaseUser?> = MutableStateFlow(Firebase.auth.currentUser).asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // Real-time products stream
    val products: StateFlow<List<Product>> = productRepo.observeProducts()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Cart items stream
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Real-time Wishlist items stream
    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems.asStateFlow()

    // Real-time Customer Orders stream
    private val _customerOrders = MutableStateFlow<List<Order>>(emptyList())
    val customerOrders: StateFlow<List<Order>> = _customerOrders.asStateFlow()

    // Real-time All Orders stream (for Admin)
    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    // Loading & Feedback Flags
    private val _isSubmittingOrder = MutableStateFlow(false)
    val isSubmittingOrder: StateFlow<Boolean> = _isSubmittingOrder.asStateFlow()

    private val _isProductActionLoading = MutableStateFlow(false)
    val isProductActionLoading: StateFlow<Boolean> = _isProductActionLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun onUserAuthenticated(user: FirebaseUser) {
        viewModelScope.launch {
            // Check admin status
            val adminStatus = userRepo.isUserAdmin(user.email, user.uid)
            _isAdmin.value = adminStatus

            // Sync user profile
            userRepo.syncUserProfile(user.uid, user.displayName ?: "DK Mart Shopper", user.email ?: "")

            // Start listening to customer cart
            launch {
                cartRepo.observeCart(user.uid).catch { emit(emptyList()) }.collect {
                    _cartItems.value = it
                }
            }

            // Start listening to customer wishlist
            launch {
                wishlistRepo.observeWishlist(user.uid).catch { emit(emptyList()) }.collect {
                    _wishlistItems.value = it
                }
            }

            // Start listening to customer orders
            launch {
                orderRepo.observeCustomerOrders(user.uid).catch { emit(emptyList()) }.collect {
                    _customerOrders.value = it
                }
            }

            // If Admin, also observe all orders
            if (adminStatus) {
                launch {
                    orderRepo.observeAllOrders().catch { emit(emptyList()) }.collect {
                        _allOrders.value = it
                    }
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        when (screen) {
            Screen.Home -> _selectedTab.value = MainTab.HOME
            is Screen.Categories -> _selectedTab.value = MainTab.CATEGORIES
            Screen.Cart -> _selectedTab.value = MainTab.CART
            Screen.Orders -> _selectedTab.value = MainTab.ORDERS
            Screen.Profile -> _selectedTab.value = MainTab.PROFILE
            else -> {}
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        when (tab) {
            MainTab.HOME -> _currentScreen.value = Screen.Home
            MainTab.CATEGORIES -> _currentScreen.value = Screen.Categories()
            MainTab.CART -> _currentScreen.value = Screen.Cart
            MainTab.ORDERS -> _currentScreen.value = Screen.Orders
            MainTab.PROFILE -> _currentScreen.value = Screen.Profile
        }
    }

    // Cart operations
    fun addToCart(product: Product, quantity: Int = 1) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            val result = cartRepo.addToCart(user.uid, product, quantity)
            if (result.isSuccess) {
                _toastMessage.value = "Added \"${product.name}\" to Cart!"
            } else {
                _toastMessage.value = "Failed to add to cart: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateCartQuantity(item: CartItem, newQuantity: Int) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            cartRepo.updateQuantity(user.uid, item.id, newQuantity)
        }
    }

    fun removeFromCart(item: CartItem) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            cartRepo.removeFromCart(user.uid, item.id)
            _toastMessage.value = "Removed from cart."
        }
    }

    // Wishlist operations
    fun toggleWishlist(product: Product) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            val result = wishlistRepo.toggleWishlist(user.uid, product)
            if (result.isSuccess) {
                val added = result.getOrDefault(false)
                _toastMessage.value = if (added) "Added to Wishlist!" else "Removed from Wishlist."
            }
        }
    }

    fun removeFromWishlist(item: WishlistItem) {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            wishlistRepo.removeFromWishlist(user.uid, item.id)
        }
    }

    // Checkout operation (COD)
    fun placeOrder(
        fullName: String,
        mobileNumber: String,
        whatsappNumber: String,
        city: String,
        deliveryAddress: String,
        itemsToOrder: List<CartItem>
    ) {
        val user = Firebase.auth.currentUser ?: return
        if (_isSubmittingOrder.value) return // Prevent duplicate submission
        _isSubmittingOrder.value = true

        viewModelScope.launch {
            val subtotal = itemsToOrder.sumOf { it.subtotal }
            val deliveryFee = if (subtotal >= 2500 || subtotal == 0.0) 0.0 else 199.0
            val grandTotal = subtotal + deliveryFee

            val itemsPayload = itemsToOrder.map {
                mapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "price" to it.finalUnitPrice,
                    "quantity" to it.quantity,
                    "image" to it.image
                )
            }

            val result = orderRepo.placeOrder(
                userId = user.uid,
                customerName = fullName,
                mobileNumber = mobileNumber,
                whatsappNumber = whatsappNumber,
                city = city,
                deliveryAddress = deliveryAddress,
                items = itemsPayload,
                totalAmount = grandTotal
            )

            _isSubmittingOrder.value = false

            if (result.isSuccess) {
                // Clear cart on successful order placement
                cartRepo.clearCart(user.uid)
                _toastMessage.value = "🎉 Order Placed Successfully via Cash on Delivery!"
                navigateTo(Screen.Orders)
            } else {
                _toastMessage.value = "Order placement failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    // Admin Operations
    fun addProduct(product: Product) {
        _isProductActionLoading.value = true
        viewModelScope.launch {
            val res = productRepo.addProduct(product)
            _isProductActionLoading.value = false
            if (res.isSuccess) {
                _toastMessage.value = "✅ Product \"${product.name}\" Added to Store!"
            } else {
                _toastMessage.value = "Error adding product: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateProduct(product: Product) {
        _isProductActionLoading.value = true
        viewModelScope.launch {
            val res = productRepo.updateProduct(product)
            _isProductActionLoading.value = false
            if (res.isSuccess) {
                _toastMessage.value = "✅ Product \"${product.name}\" Updated!"
            } else {
                _toastMessage.value = "Error updating product: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteProduct(productId: String) {
        _isProductActionLoading.value = true
        viewModelScope.launch {
            val res = productRepo.deleteProduct(productId)
            _isProductActionLoading.value = false
            if (res.isSuccess) {
                _toastMessage.value = "🗑️ Product Deleted from Store!"
            } else {
                _toastMessage.value = "Error deleting product: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            val res = orderRepo.updateOrderStatus(orderId, newStatus)
            if (res.isSuccess) {
                _toastMessage.value = "Order status updated to $newStatus."
            } else {
                _toastMessage.value = "Failed to update order status."
            }
        }
    }

    fun verifyAdminAccess() {
        _isAdmin.value = true
        navigateTo(Screen.AdminDashboard)
        // Ensure all orders are observed
        viewModelScope.launch {
            orderRepo.observeAllOrders().catch { emit(emptyList()) }.collect {
                _allOrders.value = it
            }
        }
    }
}
