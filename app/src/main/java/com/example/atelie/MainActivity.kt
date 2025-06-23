package com.example.atelie

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

val supabase = createSupabaseClient(
    supabaseUrl = "https://isizirohszlbmspfwvus.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlzaXppcm9oc3psYm1zcGZ3dnVzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA2Mjg1NTMsImV4cCI6MjA2NjIwNDU1M30.QlQW_qWnAVELsxocvw_KZ_Aw5DOcZMPpIXKYdoDDaAk"
) {
    install(Auth) {
        scheme = "atelie"
        host = "login-callback"
        defaultExternalAuthAction = ExternalAuthAction.CustomTabs() // pra abrir o login em Custom Tabs ao invés de navegador externo
    }
    install(Postgrest)
    //install other modules
}

@OptIn(InternalSerializationApi::class)
@Serializable
data class Client(
    val id: Int,
    val name: String,
    val phone: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val created_at: OffsetDateTime
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadFragment(NewOrderFragment())

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_orders -> loadFragment(NewOrderFragment())
                R.id.nav_costumers -> loadFragment(ClientsFragment())
            }
            true
        }

        supabase.handleDeeplinks(intent)


//        lifecycleScope.launch {
//            try {
//                supabase.auth.signInWith(Email) {
//                    email = "anthony.passos@gmail.com"
//                    password = "123456789"
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Log.d("supabase-login", "F")
//            }
//        }
//
//        @OptIn(InternalSerializationApi::class)
//        lifecycleScope.launch {
//            try {
//                val clientes = supabase
//                    .from("clients")
//                    .select()
//                    .decodeList<Client>()
//
//                Log.d("Supabase", clientes.toString())
//
//                println(clientes)
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//                Log.d("Supabase", "Não foi meu chapa")
//            }
//        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}