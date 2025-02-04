import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingCartScreen()
        }
    }
}

@Composable
fun ShoppingCartScreen() {
    var showSnackbar by remember { mutableStateOf(false) } // State to control Snackbar visibility
    val cartItems = listOf(
        CartItem("Apple", 1.50, 3),
        CartItem("Banana", 0.75, 5),
        CartItem("Orange", 1.20, 2)
    )
    val totalCost = cartItems.sumOf { it.price * it.quantity } // Calculate total cost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // List of Items
        cartItems.forEach { item ->
            CartItemRow(item)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Summary Section
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "$${"%.2f".format(totalCost)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Checkout Button
        Button(
            onClick = { showSnackbar = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Checkout", fontSize = 18.sp)
        }
    }

    // Snackbar
    if (showSnackbar) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter // Align Snackbar at the bottom center
        ) {
            Snackbar(
                action = {
                    Button(
                        onClick = { showSnackbar = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Dismiss", color = Color.Black)
                    }
                }
            ) {
                Text(text = "Ordered")
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Item Name
        Text(
            text = item.name,
            fontSize = 16.sp,
            color = Color.Black
        )

        // Item Price and Quantity
        Text(
            text = "$${"%.2f".format(item.price)} x ${item.quantity}",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

data class CartItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

@Preview(showBackground = true)
@Composable
fun PreviewShoppingCartScreen() {
    ShoppingCartScreen()
}