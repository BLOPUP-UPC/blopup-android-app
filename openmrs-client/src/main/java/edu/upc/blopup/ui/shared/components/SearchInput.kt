import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import edu.upc.R

@Composable
fun SearchInput(searchInput: String, onSearchQueryChange: (String) -> Unit) {
    TextField(
        value = searchInput, onValueChange = { onSearchQueryChange(it) },
        colors = TextFieldDefaults.colors(
            focusedPlaceholderColor = Color.LightGray,
            unfocusedPlaceholderColor = Color.LightGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = colorResource(R.color.dark_teal),
            unfocusedContainerColor = colorResource(R.color.dark_teal),
        ),
        placeholder = { Text(stringResource(R.string.search_hint_text)) }
    )
}


@Composable
fun SearchOptionIcon(
    onSearchOption: () -> Unit,
    isSearchInput: Boolean,
    onSearchClose: () -> Unit
) {
    when {
        isSearchInput -> {
            IconButton(onClick = { onSearchClose() }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Color.LightGray
                )
            }
        }

        else -> {
            IconButton(onClick = { onSearchOption() }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = colorResource(R.color.white)
                )
            }

        }
    }
}