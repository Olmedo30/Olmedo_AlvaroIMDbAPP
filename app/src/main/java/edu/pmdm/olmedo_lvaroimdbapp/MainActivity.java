package edu.pmdm.olmedo_lvaroimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import edu.pmdm.olmedo_lvaroimdbapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //Configuración del Header en NavigationView
        NavigationView navigationView = binding.navView; // Obtén la referencia al NavigationView
        Button logOutButton = navigationView.getHeaderView(0).findViewById(R.id.buttonLogOut);

        //Referencias a la foto y texto del Header
        ImageView profileImageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        TextView emailTextView = navigationView.getHeaderView(0).findViewById(R.id.textView);
        TextView nameTextView = navigationView.getHeaderView(0).findViewById(R.id.nametextView);

        //Obtenemos el usuario logueado en Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            //Asigna el correo al TextView
            if (user.getEmail() != null) {
                nameTextView.setText(user.getDisplayName());
                emailTextView.setText(user.getEmail());
            }
            //Carga la foto de perfil con Glide (si existe)
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(profileImageView);
            }
        }

        //Cierra sesión al darle al botón
        logOutButton.setOnClickListener(v -> {
            //Cierra sesión en Firebase
            FirebaseAuth.getInstance().signOut();
            //Cierra sesión de Google
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    //Si la sesión de Google se cierra correctamente
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    startActivity(intent);
                    finish(); // Termina la actividad actual
                } else {
                    //Si la sesión de Google falla
                    task.getException().printStackTrace();
                }
            });
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
