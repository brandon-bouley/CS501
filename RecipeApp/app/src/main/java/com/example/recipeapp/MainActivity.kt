import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipeapp.R


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeScreen()
        }
    }
}

@Composable
fun RecipeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Recipe Title
        Text(
            text = "Spicy Vodka Pasta Recipe",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Recipe Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pasta),
                contentDescription = "Recipe Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Ingredients Section
        Text(
            text = "Ingredients",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Use Rows for each ingredient
                IngredientRow("1 box", "your favorite pasta")
                IngredientRow("1 can", "tomato paste")
                IngredientRow("2 cloves", "garlic, chopped")
                IngredientRow("1 oz", "vodka")
                IngredientRow("1/2", "white onion, diced")
                IngredientRow("1 cup", "heavy cream")
                IngredientRow("1 tbsp", "olive oil")
                IngredientRow("2 tbsp", "butter")
                IngredientRow("1/2 cup", "pasta water")
                IngredientRow("1 tsp", "red chili flakes")
                IngredientRow("To taste", "salt + pepper")
                IngredientRow("1 cup", "Parmesan cheese, grated")
                IngredientRow("1-2", "basil leaves, chopped")
            }
        }

        // Divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = Color.Gray
        )

        // Directions Section
        Text(
            text = "Directions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("1. Heat a large saucepan with 1 tbsp olive oil, onions, and garlic. Add in tomato paste and once cooked, add in the cream. Mix together until combined.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("2. Cook pasta in salted water - cook it a few minutes before it’s al dente and set aside.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("3. Add in vodka and mix together. Add chili flakes, salt, pepper to the sauce and mix. Add in freshly grated Parmesan cheese & butter. Mix together. Also add in 1/2 cup of the pasta water.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("4. Add in pasta and mix together until all pasta is coated evenly. Plate and garnish with salt, pepper, basil, and freshly grated Parmesan cheese.")
            }
        }
    }
}

@Composable
fun IngredientRow(quantity: String, ingredient: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = quantity,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = ingredient)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecipeScreen() {
    RecipeScreen()
}