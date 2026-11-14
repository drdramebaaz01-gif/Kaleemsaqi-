package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminLoginDialog
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOut
import com.example.ui.cart.CartScreen
import com.example.ui.category.CategoryScreen
import com.example.ui.checkout.CheckoutScreen
import com.example.ui.components.DkBottomNavBar
import com.example.ui.components.MainTab
import com.example.ui.home.HomeScreen
import com.example.ui.orders.OrderHistoryScreen
import com.example.ui.product.ProductDetailScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.wishlist.WishlistScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    // Observe Auth State
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Auto sign-in attempt on startup
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = {},
                scope = coroutineScope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        // Authenticated Session
        val mainViewModel: MainViewModel = viewModel()

        LaunchedEffect(user.uid) {
            mainViewModel.onUserAuthenticated(user)
        }

        DkMartMainApp(
            viewModel = mainViewModel,
            currentUser = user,
            onSignOut = {
                signOut(
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun DkMartMainApp(
    viewModel: MainViewModel,
    currentUser: FirebaseUser,
    onSignOut: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val customerOrders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isSubmittingOrder by viewModel.isSubmittingOrder.collectAsStateWithLifecycle()
    val isProductActionLoading by viewModel.isProductActionLoading.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    val wishlistProductIds = remember(wishlistItems) {
        wishlistItems.map { it.productId }.toSet()
    }

    // Toast notification handler
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    // Back handling for sub-screens
    BackHandler(enabled = currentScreen !is Screen.Home) {
        when (currentScreen) {
            Screen.Checkout -> viewModel.navigateTo(Screen.Cart)
            is Screen.ProductDetail -> viewModel.navigateTo(Screen.Home)
            Screen.Wishlist -> viewModel.navigateTo(Screen.Home)
            Screen.AdminDashboard -> viewModel.navigateTo(Screen.Home)
            else -> viewModel.selectTab(MainTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen !is Screen.Checkout && currentScreen !is Screen.ProductDetail && currentScreen !is Screen.AdminDashboard) {
                DkBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    cartItemCount = cartItems.sumOf { it.quantity },
                    onAdminIconClicked = { showAdminLoginDialog = true }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                Screen.Home -> {
                    HomeScreen(
                        products = products,
                        isLoading = false,
                        wishlistProductIds = wishlistProductIds,
                        cartCount = cartItems.sumOf { it.quantity },
                        onProductClick = { product -> viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                        onToggleWishlist = { product -> viewModel.toggleWishlist(product) },
                        onCategoryClick = { categoryName -> viewModel.navigateTo(Screen.Categories(categoryName)) },
                        onCartClick = { viewModel.selectTab(MainTab.CART) },
                        onWishlistClick = { viewModel.navigateTo(Screen.Wishlist) },
                        onAdminLoginClick = { showAdminLoginDialog = true }
                    )
                }

                is Screen.Categories -> {
                    CategoryScreen(
                        products = products,
                        wishlistProductIds = wishlistProductIds,
                        initialCategory = screen.category,
                        onProductClick = { product -> viewModel.navigateTo(Screen.ProductDetail(product)) },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                        onToggleWishlist = { product -> viewModel.toggleWishlist(product) }
                    )
                }

                Screen.Cart -> {
                    CartScreen(
                        cartItems = cartItems,
                        isLoading = false,
                        onUpdateQuantity = { item, qty -> viewModel.updateCartQuantity(item, qty) },
                        onRemoveItem = { item -> viewModel.removeFromCart(item) },
                        onCheckout = { viewModel.navigateTo(Screen.Checkout) },
                        onStartShopping = { viewModel.selectTab(MainTab.HOME) }
                    )
                }

                Screen.Checkout -> {
                    CheckoutScreen(
                        items = cartItems,
                        isSubmitting = isSubmittingOrder,
                        onBackClick = { viewModel.navigateTo(Screen.Cart) },
                        onPlaceOrder = { name, mobile, whatsapp, city, address ->
                            viewModel.placeOrder(
                                fullName = name,
                                mobileNumber = mobile,
                                whatsappNumber = whatsapp,
                                city = city,
                                deliveryAddress = address,
                                itemsToOrder = cartItems
                            )
                        }
                    )
                }

                Screen.Orders -> {
                    OrderHistoryScreen(
                        orders = customerOrders,
                        isLoading = false
                    )
                }

                Screen.Profile -> {
                    ProfileScreen(
                        userEmail = currentUser.email ?: "",
                        userName = currentUser.displayName ?: "",
                        isAdmin = isAdmin,
                        orderCount = customerOrders.size,
                        wishlistCount = wishlistItems.size,
                        onOrdersClick = { viewModel.selectTab(MainTab.ORDERS) },
                        onWishlistClick = { viewModel.navigateTo(Screen.Wishlist) },
                        onAdminDashboardClick = { viewModel.navigateTo(Screen.AdminDashboard) },
                        onSignOutClick = onSignOut
                    )
                }

                Screen.Wishlist -> {
                    WishlistScreen(
                        items = wishlistItems,
                        isLoading = false,
                        onBackClick = { viewModel.navigateTo(Screen.Home) },
                        onAddToCart = { wishItem ->
                            val product = products.firstOrNull { it.id == wishItem.productId }
                            if (product != null) {
                                viewModel.addToCart(product)
                            }
                        },
                        onRemoveFromWishlist = { wishItem ->
                            viewModel.removeFromWishlist(wishItem)
                        }
                    )
                }

                is Screen.ProductDetail -> {
                    ProductDetailScreen(
                        product = screen.product,
                        isWishlisted = wishlistProductIds.contains(screen.product.id),
                        onBackClick = { viewModel.navigateTo(Screen.Home) },
                        onAddToCart = { qty -> viewModel.addToCart(screen.product, qty) },
                        onBuyNow = { qty ->
                            // Quick buy: add item to cart and proceed directly to checkout
                            viewModel.addToCart(screen.product, qty)
                            viewModel.navigateTo(Screen.Checkout)
                        },
                        onToggleWishlist = { viewModel.toggleWishlist(screen.product) }
                    )
                }

                Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        products = products,
                        orders = allOrders,
                        isProductActionLoading = isProductActionLoading,
                        onBackClick = { viewModel.navigateTo(Screen.Home) },
                        onAddProduct = { newProd -> viewModel.addProduct(newProd) },
                        onUpdateProduct = { updatedProd -> viewModel.updateProduct(updatedProd) },
                        onDeleteProduct = { prodId -> viewModel.deleteProduct(prodId) },
                        onUpdateOrderStatus = { orderId, newStatus -> viewModel.updateOrderStatus(orderId, newStatus) }
                    )
                }
            }
        }
    }

    // Hidden Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            currentUserEmail = currentUser.email,
            onDismiss = { showAdminLoginDialog = false },
            onAdminVerified = {
                showAdminLoginDialog = false
                viewModel.verifyAdminAccess()
            }
        )
    }
}
