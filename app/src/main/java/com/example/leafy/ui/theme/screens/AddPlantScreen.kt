package com.example.leafy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(navController: NavController, modifier: Modifier = Modifier) {

    var plantName by remember { mutableStateOf("") }
    var waterTime by remember { mutableStateOf("") }
    var waterDay by remember { mutableStateOf("") }
    var fertilizerTime by remember { mutableStateOf("") }
    var fertilizerDay by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val timeOptions = listOf("Pilih Waktu", "1x sehari", "2x sehari", "1x seminggu")
    val dayOptions = listOf("Pilih Hari", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val locationOptions = listOf("pilih Lokasi", "Indoor", "Outdoor", "Balkon")

    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah tanaman",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Aksi kembali
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LeafyGreen
                )
            )
        },
        containerColor = LeafyGreen
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            FormLabel(text = "Nama Tanaman")
            FormTextField(
                value = plantName,
                onValueChange = { plantName = it },
                placeholder = "Contoh: Lidah Mertua"
            )

            FormLabel(text = "Jadwal Siram")
            Row(Modifier.fillMaxWidth()) {
                FormDropdown(
                    options = timeOptions,
                    selectedOption = waterTime.ifEmpty { timeOptions[0] },
                    onOptionSelected = { waterTime = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FormDropdown(
                    options = dayOptions,
                    selectedOption = waterDay.ifEmpty { dayOptions[0] },
                    onOptionSelected = { waterDay = it },
                    modifier = Modifier.weight(1f)
                )
            }

            FormLabel(text = "Jadwal Pupuk (optional)")
            Row(Modifier.fillMaxWidth()) {
                FormDropdown(
                    options = timeOptions,
                    selectedOption = fertilizerTime.ifEmpty { timeOptions[0] },
                    onOptionSelected = { fertilizerTime = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FormDropdown(
                    options = dayOptions,
                    selectedOption = fertilizerDay.ifEmpty { dayOptions[0] },
                    onOptionSelected = { fertilizerDay = it },
                    modifier = Modifier.weight(1f)
                )
            }

            FormLabel(text = "Upload foto tanaman")
            UploadPhotoButtons()

            FormLabel(text = "Lokasi (optional)")
            FormDropdown(
                options = locationOptions,
                selectedOption = location.ifEmpty { locationOptions[0] },
                onOptionSelected = { location = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.ChevronRight, "pilih", tint = Color.Black)
                }
            )

            FormLabel(text = "Catatan tambahan")
            FormTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Contoh: Ganti pot setiap 6 bulan",
                singleLine = false,
                minLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (plantName.isBlank()) {
                            Toast.makeText(context, "Nama tanaman wajib diisi", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // Bersihkan nilai yang masih placeholder
                        val cleanWaterTime = if (waterTime == timeOptions[0]) "" else waterTime
                        val cleanWaterDay  = if (waterDay  == dayOptions[0]) "" else waterDay

                        val wateringInfo = if (cleanWaterTime.isNotBlank() || cleanWaterDay.isNotBlank()) {
                            "Siraman: ${cleanWaterTime.ifBlank { "-" }} ${cleanWaterDay.ifBlank { "" }}".trim()
                        } else {
                            "Siraman: -"
                        }

                        val cleanFertilizerTime = if (fertilizerTime == timeOptions[0]) "" else fertilizerTime
                        val cleanFertilizerDay  = if (fertilizerDay  == dayOptions[0]) "" else fertilizerDay

                        val fertilizerInfo = if (cleanFertilizerTime.isNotBlank() || cleanFertilizerDay.isNotBlank()) {
                            " | Pupuk: ${cleanFertilizerTime.ifBlank { "-" }} ${cleanFertilizerDay.ifBlank { "" }}".trim()
                        } else {
                            ""
                        }


                        val scheduleText = wateringInfo + fertilizerInfo

                        val plant = PlantEntity(
                            name = plantName,
                            schedule = scheduleText,
                            lastWatered = "Belum pernah",
                            imageUri = null
                        )

                        db.plantDao().insertPlant(plant)
                        Toast.makeText(context, "Tanaman berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Tambah tanaman", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = LeafyGreen,
            unfocusedBorderColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = LeafyGreen
        ),
        singleLine = singleLine,
        minLines = minLines
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                if (trailingIcon != null) {
                    trailingIcon()
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = LeafyGreen,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },

                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun UploadPhotoButtons() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* TODO: Aksi upload galeri */ },
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = "Upload dari Galeri",
                    modifier = Modifier.size(40.dp),
                    tint = Color.DarkGray
                )
            }
            IconButton(
                onClick = { /* TODO: Aksi buka kamera */ },
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Ambil Foto",
                    modifier = Modifier.size(40.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }
}
