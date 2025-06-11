package com.rizkaindah0043.figurepedia.screen

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rizkaindah0043.figurepedia.BuildConfig
import com.rizkaindah0043.figurepedia.R
import com.rizkaindah0043.figurepedia.model.Tokoh
import com.rizkaindah0043.figurepedia.model.User
import com.rizkaindah0043.figurepedia.network.ApiStatus
import com.rizkaindah0043.figurepedia.network.MainViewModel
import com.rizkaindah0043.figurepedia.network.UserDataStore
import com.rizkaindah0043.figurepedia.ui.theme.FigurepediaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())
    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage


    var showDialog by remember { mutableStateOf(false) }
    var showTokohDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var seletedTokoh by remember { mutableStateOf<Tokoh?>(null) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var showGalleryPermissionDeniedToast by remember { mutableStateOf(false) }

    LaunchedEffect(showGalleryPermissionDeniedToast) {
        if (showGalleryPermissionDeniedToast) {
            Toast.makeText(context, "Izin akses galeri ditolak.", Toast.LENGTH_SHORT).show()
            showGalleryPermissionDeniedToast = false
        }
    }
    val imageCropperLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val cropped = getCroppedImage(context.contentResolver, result)
        if (cropped != null) {
            bitmap = cropped
            if (showEditDialog) {
                selectedBitmap = cropped
            }

            if (showTokohDialog || (!showTokohDialog && !showEditDialog)) {
                showTokohDialog = true
            }
        }
    }


    fun launchImageCropper(includeGallery: Boolean) {
        val options = CropImageContractOptions(
            null, CropImageOptions(
                imageSourceIncludeGallery = includeGallery,
                imageSourceIncludeCamera = true,
                fixAspectRatio = true
            )
        )
        imageCropperLauncher.launch(options)
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchImageCropper(true)
        } else {
            showGalleryPermissionDeniedToast = true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        }
                        else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    launchImageCropper(true)
                } else {
                    galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_tokoh)
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(
            viewModel,
            user.email,
            Modifier.padding(innerPadding),
            onDelete = { tokoh ->
                seletedTokoh = tokoh
                showDeleteDialog = true
            },
            onEdit = { tokoh ->
                seletedTokoh = tokoh
                showEditDialog = true
            }
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showTokohDialog) {
            TokohDialog(
                userId = user.email,
                bitmap = bitmap,
                onDismissRequest = { showTokohDialog = false },
                onImageClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchImageCropper(true)
                    } else {
                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            ) { name, country, field ->
                viewModel.saveData(user.email, name, country, field, bitmap!!)
                showTokohDialog = false
            }
        }

        if (showEditDialog && seletedTokoh != null) {
            Log.d("EditDialog", "Menampilkan dialog edit untuk tokoh: ${seletedTokoh!!.name}")
            Log.d("EditDialog", "imageUrl yang diterima: ${seletedTokoh!!.imageUrl}")
            Log.d("EditDialog", "country: ${seletedTokoh!!.country}, field: ${seletedTokoh!!.field}")
            TokohDialog(
                userId = user.email,
                bitmap = selectedBitmap,
                imageUrl = if (selectedBitmap == null) seletedTokoh!!.imageUrl.replace("http", "https") else null,
                onImageClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchImageCropper(true)
                    } else {
                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                },
                nameInitial = seletedTokoh!!.name,
                countryInitial = seletedTokoh!!.country,
                fieldInitial = seletedTokoh!!.field,
                onDismissRequest = { showEditDialog = false },
                onConfirmation = { name, country, field ->
                    viewModel.updateData(
                        userId = user.email,
                        id = seletedTokoh!!.id,
                        name = name,
                        country = country,
                        field = field,
                        bitmap = selectedBitmap
                    )
                    selectedBitmap = null
                    showEditDialog = false
                }
            )
        }


        if (showDeleteDialog) {
            DialogHapus(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    seletedTokoh?.let { viewModel.deleteData(user.email, it.id) }
                    showDeleteDialog = false
                }
            )
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, userId: String, modifier: Modifier = Modifier,
                  onDelete: (Tokoh) -> Unit, onEdit: (Tokoh) -> Unit) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveDta(userId)
    }

    when(status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) { tokoh ->
                    ListItem(
                        tokoh = tokoh,
                        onDelete = { onDelete(tokoh) },
                        onEdit = { onEdit(tokoh) }
                    )
                }
            }
        }
        ApiStatus.FAILED -> {
            Column(
                modifier =Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveDta(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(tokoh: Tokoh, onDelete: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("userId", tokoh.userId).apply()
    val userId = sharedPreferences.getString("userId", "") ?: ""

    val isLoggedIn = user.email.isNotEmpty()
    Box(
        modifier = Modifier.padding(4.dp).border(1.dp, Color.Gray).height(200.dp),
        contentAlignment = Alignment.BottomCenter
    ){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(tokoh.imageUrl.replace("http", "https"))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, tokoh.name),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_img),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tokoh.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tokoh.country,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = tokoh.field,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                if (isLoggedIn && userId == user.email) {
                    Column {
                        IconButton(onClick = { onEdit() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_24),
                                contentDescription = stringResource(R.string.edit),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onDelete() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.hapus),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    FigurepediaTheme {
        MainScreen()
    }
}