package edu.upc.blopup.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity

@Composable
fun DashboardScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 25.dp).background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            DashboardCard(
                Modifier.weight(1f).clickable(onClick = {
                    context.startActivity(Intent(context, SyncedPatientsActivity::class.java))
                }),
                R.mipmap.ico_search_patients,
                R.string.dashboard_search_icon_label,
            )
            Spacer(modifier = Modifier.padding(10.dp))
            DashboardCard(
                Modifier.weight(1f).clickable(onClick = {
                    context.startActivity(Intent(context, AddEditPatientActivity::class.java))
                }),
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
        colors = CardColors(
            containerColor = Color.White,
            contentColor = colorResource(R.color.dark_grey_for_stroke),
            disabledContainerColor = Color.White,
            disabledContentColor = colorResource(R.color.dark_grey_for_stroke),
        ),
    )
    {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(icon), contentDescription = "Search icon")
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = stringResource(label))
        }
    }
}


@Preview
@Composable
fun DashboardPreview() {
    DashboardScreen()
}