package com.example.shoppingapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoppingapp.ui.theme.ShoppingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShoppingApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Data class for Product
data class Product(
    val name: String,
    val price: String,
    val description: String
)

// Hardcoded list of products
val products = listOf(
    Product("Bowl of Soup", "$5", "This is a bowl of soup."),
    Product("Big bowl of soup", "$10", "This is a pretty big bowl of soup."),
    Product("Huge bowl of soup", "$300", "Just greedy...")
)

// Product List Composable
@Composable
fun ProductList(
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(products) { product ->
            Text(
                text = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductSelected(product) }
                    .padding(16.dp)
            )
        }
    }
}

// Product Details Composable with Conditional Back Button
@Composable
fun ProductDetails(
    product: Product?,
    onBackClicked: () -> Unit, // Callback for back navigation
    isLandscape: Boolean, // Pass orientation information
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Show Back Button only in Portrait Mode
        if (!isLandscape) {
            Button(
                onClick = onBackClicked,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Back to List")
            }
        }

        // Product Details
        if (product != null) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = product.price, style = MaterialTheme.typography.titleMedium)
                Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(text = "Select a product to view details.")
        }
    }
}

// Main Shopping App Composable
@Composable
fun ShoppingApp(modifier: Modifier = Modifier) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // Check the orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Debug log to verify orientation
    //println("Current Orientation: ${if (isLandscape) "Landscape" else "Portrait"}")

    if (isLandscape) {
        // Landscape mode: Show both panes side by side
        Row(modifier = modifier) {
            ProductList(
                products = products,
                onProductSelected = { selectedProduct = it },
                modifier = Modifier.weight(1f)
            )
            ProductDetails(
                product = selectedProduct,
                onBackClicked = { selectedProduct = null },
                isLandscape = isLandscape,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // Portrait mode: Show one pane at a time
        if (selectedProduct == null) {
            ProductList(
                products = products,
                onProductSelected = { selectedProduct = it },
                modifier = modifier
            )
        } else {
            ProductDetails(
                product = selectedProduct,
                onBackClicked = { selectedProduct = null },
                isLandscape = isLandscape,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingAppPreview() {
    ShoppingAppTheme {
        ShoppingApp()
    }
}