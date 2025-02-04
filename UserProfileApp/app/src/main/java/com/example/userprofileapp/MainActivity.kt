import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.userprofileapp.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserProfileScreen()
        }
    }
}

@Composable
fun UserProfileScreen() {
    var isFollowing by remember { mutableStateOf(false) } // State to track follow status
    var showSnackbar by remember { mutableStateOf(false) } // State to control Snackbar visibility
    val username = "brandonbouley"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.selfie),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.FillBounds

            )


            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = username,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bio
            Text(
                text = "Software Developer | Android Enthusiast ",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Follow Button
            Button(
                onClick = {
                    isFollowing = !isFollowing // Toggle follow state
                    showSnackbar = true // Show Snackbar
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Gray else Color.Blue // Change button color based on follow state
                )
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    color = Color.White
                )
            }
        }

        // Snackbar
        if (showSnackbar) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = if (isFollowing) Color.Gray else Color.Blue,
                action = {
                    Button(
                        onClick = { showSnackbar = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Dismiss", color = Color.Black)
                    }
                }
            ) {
                Text(
                    text = if (isFollowing) "Following $username" else "Unfollowed $username",
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserProfileScreen() {
    UserProfileScreen()
}