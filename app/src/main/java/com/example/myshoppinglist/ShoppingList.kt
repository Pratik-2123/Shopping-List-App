package com.example.myshoppinglist

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint.Align
import android.icu.text.AlphabeticIndex.Bucket.LabelType
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

data class ShoppingItems(
    var id : Int,
    var name : String,
    var quantity : Int,
    var isEditing : Boolean = false,
    var address: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(
    locationUtils : LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context : Context,
    address : String
) {
    var sItem by remember { mutableStateOf(listOf<ShoppingItems>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }



    val requestpermissionlauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // i Have access to the location
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                // Ask for the permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )


                if(rationaleRequired) {
                    Toast.makeText(context,
                        "Location permission is required for this feature to work", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context,
                        "Location permission is required. Please enable it in Android settings", Toast.LENGTH_LONG).show()
                }
            }

        }
    )

    Column {
        Row {
            Text(
                text = "Shopping List",
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                textAlign = TextAlign.Center
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            items(sItem) {
                    item ->
                if(item.isEditing) {
                    ShoppingitemEditor(
                        item = item,
                        onEditComplete = {
                                editedName, editedQuantity ->
                            sItem = sItem.map { it.copy(isEditing = false) }
                            val editedItem = sItem.find { it.id == item.id }
                            editedItem ?.let {
                                it.name = editedName
                                it.quantity = editedQuantity
                                it.address = address
                            }
                        }
                    )
                } else {
                    shoppingListItem(item = item, onEditClick = {
                        // finding out which item we are editing and changing its value and the name
                        sItem = sItem.map { it.copy(isEditing = it.id == item.id) }
                    },
                        onDeleteClick = {
                            sItem = sItem - item
                        }
                    )
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp, end = 30.dp), // Assuming you want it within a full-screen layout
        contentAlignment = Alignment.BottomEnd
    ) {
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(60.dp) // Adjust size as needed
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(16.dp) // Optional padding for visual spacing
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add item",
                tint = Color.White // Optional tint for contrast
            )
        }
    }


    if(showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(onClick = {
                        showDialog = false
                        itemName = ""
                    }) {
                        Text(text = "Cancel")
                    }
                    Button(onClick = {
                        if(itemName.isNotBlank()) {
                            val newItem = ShoppingItems(
                                id = sItem.size+1,
                                name = itemName,
                                quantity = itemQuantity.toInt(),
                                address = address
                            )
                            sItem = sItem + newItem
                            showDialog = false
                            itemName = ""
                        }
                    }) {
                        Text(text = "Add")
                    }
                }
            },
            title = {
                    Text(text = "Add Shopping Items", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(10.dp))
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.padding(top = 16.dp))
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )

                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)) {
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationsceeen") {
                                this.launchSingleTop
                            }
                        } else {
                            requestpermissionlauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }) {
                            Text(text = "Address")
                    }
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ShoppingitemEditor(item: ShoppingItems, onEditComplete: (String, Int) -> Unit) {
    var editedItemname by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity) }
    var isEditing by remember { mutableStateOf(item.isEditing) }

    Row(
        modifier = Modifier
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ){  
        Column {
            OutlinedTextField(
               value = editedItemname,
               onValueChange = {editedItemname = it},
               singleLine = true,
               modifier = Modifier
                   .padding(10.dp)
            )
            OutlinedTextField(
                value = editedQuantity.toString(),
                onValueChange = {
                    editedQuantity = it.toInt()
                                },
                singleLine = true,
                modifier = Modifier
                    .padding(10.dp)
            )
            Button(onClick = {
                isEditing = false
                onEditComplete(editedItemname, editedQuantity.toInt() ?: 1)
            },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Save")
            }
        }
    }
}



@Composable
fun shoppingListItem(
    item : ShoppingItems,
    onEditClick : () -> Unit,
    onDeleteClick : () -> Unit
) {
    val checkedState = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                BorderStroke(
                    width = 2.dp,
                    MaterialTheme.colorScheme.primary,
                ), shape = RoundedCornerShape(20)
            ),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column (
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ){
            Row {
                Text(text = item.name, modifier = Modifier.padding(10.dp))
                Text(text = "Qty:${item.quantity}", modifier = Modifier.padding(10.dp))
            }
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null )
                Text(text = item.address)
            }
        }

        Row (modifier = Modifier.padding(10.dp)){
            IconButton(onClick = onEditClick ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null
                )
            }
            IconButton(onClick = onDeleteClick ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        }
    }
}

