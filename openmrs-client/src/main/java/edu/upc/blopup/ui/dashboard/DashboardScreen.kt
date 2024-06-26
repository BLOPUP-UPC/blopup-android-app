package edu.upc.blopup.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
fun DashboardScreen(navigateToSearchPatientScreen: () -> Unit, navigateToCreatePatientScreen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            DashboardCard(
                Modifier
                    .weight(1f)
                    .clickable(onClick = {navigateToSearchPatientScreen()}),
                R.mipmap.ico_search_patients,
                R.string.dashboard_search_icon_label,
            )
            Spacer(modifier = Modifier.padding(10.dp))
            DashboardCard(
                Modifier
                    .weight(1f)
                    .clickable(onClick = { navigateToCreatePatientScreen() }),
                R.mipmap.ico_add_patient,
                R.string.action_register_patient,
            )
        }
    }
}

@Composable
fun DashboardCard(modifier: Modifier, icon: Int, label: Int ) {
    OutlinedCard(
        modifier,
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    )
    {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 25.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(modifier = Modifier.size(80.dp),  painter = painterResource(icon), contentDescription = "Search icon")
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = stringResource(label), color = colorResource(R.color.dark_grey_for_stroke), fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        }
    }
}


@Preview
@Composable
fun DashboardPreview() {
    DashboardScreen({}, {})
}