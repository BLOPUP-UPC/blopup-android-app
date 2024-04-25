import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import edu.upc.R

@Composable
fun SearchInput(
    searchInput: String,
    onSearchQueryChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    TextField(
        modifier = Modifier.focusRequester(focusRequester),
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
        placeholder = { Text(text = stringResource(R.string.search_hint_text), fontSize = 24.sp ) }
    )
    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }
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