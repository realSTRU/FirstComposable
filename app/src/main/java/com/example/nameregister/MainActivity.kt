package com.example.nameregister

import android.inputmethodservice.Keyboard.Row
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.nameregister.ui.theme.NameRegisterTheme
import java.nio.file.WatchEvent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var personaDb: PersonaDb
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NameRegisterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NameRegisterScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NameRegisterScreen(
    viewModel: PersonaViewModel = hiltViewModel()
)
{
    var name by remember { mutableStateOf("") }
    val personas by viewModel.personas.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.isMessageShownFlow.collectLatest {
            if (it) {
                snackbarHostState.showSnackbar(
                    message = "Persona guardada",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold (
        content = ({
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 50.dp,start = 15.dp, end= 10.dp, bottom = 50.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.nombre,
                    label = {Text("Digite un nombre")},
                    onValueChange = {viewModel.nombre = it})
                OutlinedButton(modifier = Modifier.fillMaxWidth(),onClick = {
                    viewModel.savePersona()
                    viewModel.setMessageShown()
                }) {
                  
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription ="Guardar" )
                    Text("Guardar")
                }
                Text(text = "Lista de personas", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(personas){ persona->
                        Text(text = persona.Nombre)
                    }
                }
            }

        })
    )
}

@Entity(tableName = "Personas")
data class Persona(
    @PrimaryKey
    val PersonaId : Int? = null,
    val Nombre: String = ""

)
@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val personaDb : PersonaDb
) : ViewModel() {
    var nombre by mutableStateOf("")
    private val _isMessageShown = MutableSharedFlow<Boolean>()
    val isMessageShownFlow = _isMessageShown.asSharedFlow()

    fun setMessageShown() {
        viewModelScope.launch {
            _isMessageShown.emit(true)
        }
    }

    val personas:StateFlow<List<Persona>> = personaDb.personaDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun savePersona() {
        viewModelScope.launch {
            val persona = Persona(
                Nombre = nombre
            )
            personaDb.personaDao().save(persona)
            limpiar()
        }
    }

    private fun limpiar() {
        nombre =  ""
    }
}


@Dao
interface PersonaDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(persona : Persona)

    @Query("""
        SELECT * from 
        Personas WHERE  PersonaId = :id LIMIT 1
    """)
    suspend fun find(id: Int): Persona

    @Delete
    suspend fun delete(persona: Persona)

    @Query("""SELECT * FROM Personas""")
    fun getAll() : Flow<List<Persona>>
}

@Database (entities = [Persona::class], version = 2)
abstract class PersonaDb : RoomDatabase()
{
    abstract fun personaDao(): PersonaDao
}

