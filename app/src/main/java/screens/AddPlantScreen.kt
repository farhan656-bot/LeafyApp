package com.example.leafy.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.utils.StorageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    navController: NavController,
    plantId: Int? = null,
    modifier: Modifier = Modifier
) {
    var plantName by remember { mutableStateOf("") }
    var waterTime by remember { mutableStateOf("") }
    var waterDay by remember { mutableStateOf("") }
    var fertilizerTime by remember { mutableStateOf("") }
    var fertilizerDay by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // ✅ Foto baru dari galeri (Uri content://...)
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    // ✅ Foto yang sudah tersimpan di DB (file://...) saat edit
    var existingImageUri by remember { mutableStateOf<String?>(null) }

    var existingPlant by remember { mutableStateOf<PlantEntity?>(null) }

    val timeOptions = listOf("Pilih Waktu", "1x sehari", "2x sehari", "1x seminggu")
    val dayOptions = listOf("Pilih Hari", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val locationOptions = listOf("pilih Lokasi", "Indoor", "Outdoor", "Balkon")

    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            pickedImageUri = uri
        }

    LaunchedEffect(plantId) {
        if (plantId != null) {
            db.plantDao().getPlantById(plantId)?.let { plant ->
                existingPlant = plant
                plantName = plant.name
                waterTime = plant.waterFrequency.ifBlank { timeOptions[0] }
                waterDay = plant.waterDay.ifBlank { dayOptions[0] }
                fertilizerTime = plant.fertilizerFrequency?.ifBlank { timeOptions[0] } ?: timeOptions[0]
                fertilizerDay = plant.fertilizerDay?.ifBlank { dayOptions[0] } ?: dayOptions[0]
                location = plant.location ?: locationOptions[0]
                notes = plant.notes ?: ""

                existingImageUri = plant.imageUri
                pickedImageUri = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (plantId == null) "Tambah tanaman" else "Edit tanaman",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LeafyGreen)
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
            UploadPhotoButtons(
                pickedImageUri = pickedImageUri,
                existingImageUri = existingImageUri,
                onPickFromGallery = { imagePickerLauncher.launch("image/*") }
            )

            FormLabel(text = "Lokasi (optional)")
            FormDropdown(
                options = locationOptions,
                selectedOption = location.ifEmpty { locationOptions[0] },
                onOptionSelected = { location = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.ChevronRight, "pilih", tint = Color.Black) }
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

                        // ✅ kalau user pilih foto baru, copy ke app storage biar tidak hilang saat app dibuka ulang
                        val savedImagePath: String? =
                            pickedImageUri?.let { StorageUtils.copyUriToAppStorage(context, it) }
                                ?: existingPlant?.imageUri

                        val plant = PlantEntity(
                            id = existingPlant?.id ?: 0,
                            name = plantName,
                            waterFrequency = waterTime,
                            waterDay = waterDay,
                            fertilizerFrequency = fertilizerTime.takeIf { it != "Pilih Waktu" && it.isNotBlank() },
                            fertilizerDay = fertilizerDay.takeIf { it != "Pilih Hari" && it.isNotBlank() },
                            location = location.takeIf { it != "pilih Lokasi" && it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                            imageUri = savedImagePath,
                            lastWatered = existingPlant?.lastWatered
                        )

                        if (existingPlant == null) {
                            db.plantDao().insertPlant(plant)
                            Toast.makeText(context, "Tanaman berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        } else {
                            db.plantDao().updatePlant(plant)
                            Toast.makeText(context, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
                        }

                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (existingPlant == null) "Tambah tanaman" else "Simpan perubahan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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
                if (trailingIcon != null) trailingIcon()
                else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
fun UploadPhotoButtons(
    pickedImageUri: Uri?,
    existingImageUri: String?,
    onPickFromGallery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val previewModel: Any? = pickedImageUri ?: existingImageUri

            if (previewModel != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(previewModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto tanaman",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Image(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            IconButton(
                onClick = onPickFromGallery,
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
        }
    }
}
