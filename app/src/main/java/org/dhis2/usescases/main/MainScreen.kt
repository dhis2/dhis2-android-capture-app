package org.dhis2.usescases.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.MutableLiveData
import org.dhis2.R
import org.dhis2.utils.SampleDevicePreview
import java.util.Random

@SampleDevicePreview
@Composable
private fun PreviewMainScreen() {
    MainScreen()
}

@Composable
fun MainScreen() {
    LazyColumn {
        val primaryServices = getRandomStringsList(size = 2)
        val secondaryServices = getRandomStringsList(size = 4)

        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { Title(text = "Seguimento na Comunidade") }
        item { PrimaryServices(services = primaryServices) }

        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { Title(text = "Serviços Complementares") }

        item { Spacer(modifier = Modifier.height(10.dp)) }
        item { SecondaryServices(services = secondaryServices) }
    }
}

@Composable
private fun PrimaryServices(services: List<String>) {
    /**
     * Explanation: LazyColumn does not directly support nesting another LazyColumn
     * or LazyRow inside it. To prevent errors caused by infinite constraints, we
     * calculate the height of each LazyColumn and LazyVerticalGrid before
     * rendering. This avoids the 'java.lang.IllegalStateException'
     * related to infinite height constraints.
     */
    val lazyGridNumberOfLines = calculateLazyGridNumberOfLines(listSize = services.size)
    val lazyGridHeightValue = calculateLazyGridHeight(numberOfLines = lazyGridNumberOfLines)
    val lazyGridHeight = MutableLiveData(lazyGridHeightValue)

    // Set up a container for the LazyVerticalGrid with specified height
    Box(
        modifier = Modifier
            .height(lazyGridHeight.value!!)
            .background(Color.White)
    ) {
        val numberOfLazyGridColumns = 2
        LazyVerticalGrid(
            columns = GridCells.Fixed(numberOfLazyGridColumns),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp)
        ) {
            items(services.size) { index ->
                // Todo: Get service name from 'services' list based on 'index'
                PrimaryServiceItem(services[index])
            }
        }
    }
}

@Composable
private fun SecondaryServices(services: List<String>) {
    val lazyColumnHeightValue = calculateLazyColumnHeight(numberOfLines = services.size)
    val lazyColumnHeight = MutableLiveData(lazyColumnHeightValue)

    // Set up a container for the LazyColumn with specified height
    Box(modifier = Modifier.height(lazyColumnHeight.value!!)) {
        LazyColumn {
            items(services.size) { index ->
                // Todo: Get service name from appropriate source
                SecondaryServiceItem(services[index])
            }
        }
    }
}

@Composable
private fun SecondaryServiceItem(service: String) {
    //Todo: Get service name here
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.mozambique),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = service,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(text = "35 Eventos")
        }
    }
}

@Composable
private fun PrimaryServiceItem(serviceName: String) {
    //Todo: Get service instance here
    Card(
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(all = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                painter = painterResource(id = R.drawable.mozambique)
            )
            TextServiceQuantity(quantity = "89")

            SimpleLine(color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f))
            TextServiceName(service = serviceName)
            SimpleLine(color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextServiceTimeOfLastUpdate(text = "Há duas horas")
                Icon(
                    tint = Color(0xFF4CAF50),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Refresh,
                )
            }
        }
    }
}

@Composable
private fun TextServiceName(service: String) {
    val fontWeight = FontWeight.SemiBold
    val textModifier = Modifier.padding(top = 4.dp, bottom = 4.dp)

    Text(
        text = service,
        fontWeight = fontWeight,
        modifier = textModifier
    )
}

@Composable
private fun TextServiceTimeOfLastUpdate(text: String) {
    val verticalPadding = 8.dp
    val horizontalPadding = 8.dp
    val textModifier = Modifier.padding(
        horizontal = horizontalPadding,
        vertical = verticalPadding
    )

    Text(
        text = text,
        modifier = textModifier
    )
}

@Composable
private fun TextServiceQuantity(quantity: String) {
    val fontSize = 32.sp
    val fontWeight = FontWeight.Bold

    Text(
        text = quantity,
        fontSize = fontSize,
        fontWeight = fontWeight
    )
}


@Composable
private fun Title(text: String) {
    val fontSize = 24.sp
    val fontWeight = FontWeight.Normal
    val paddingModifier = Modifier.padding(start = 16.dp, end = 16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(paddingModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

@Composable
private fun SimpleLine(color: Color) {
    Box(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(color)
    )
}

private fun getRandomStringsList(size: Int): List<String> {
    val randomStringsList = mutableListOf<String>()

    for (i in 0 until size) {
        val randomIndex = Random().nextInt(strings.size)
        randomStringsList.add(strings[randomIndex])
    }
    return randomStringsList
}

private val strings = listOf(
    "Alice", "Bob", "Charlie", "David", "Ella",
    "Frank", "Grace", "Henry", "Ivy", "Jack",
    "Katherine", "Liam", "Mia", "Noah", "Olivia",
    "Penelope", "Quincy", "Ruby", "Samuel", "Tessa",
    "Uma", "Victor", "Willow", "Xander", "Yara", "Zane"
)

private fun calculateLazyGridNumberOfLines(listSize: Int): Int {
    return (listSize + 1) / 2
}

private fun calculateLazyGridHeight(numberOfLines: Int): Dp {
    // The singleGridItemHeight value is the SUM of the height of ALL COMPOSABLES that
    // are part of a single grid item
    val singleGridItemHeight = 200.dp
    return (numberOfLines * singleGridItemHeight)
}

private fun calculateLazyColumnHeight(numberOfLines: Int): Dp {
    // The singleColumnItemHeight value is the SUM of the height of ALL COMPOSABLES that
    // are part of a single column item
    val singleColumnItemHeight = 64.dp
    return (numberOfLines * singleColumnItemHeight)
}
