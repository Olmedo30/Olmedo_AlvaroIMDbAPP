package edu.pmdm.olmedo_lvaroimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignIn extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100; // Código de respuesta para el Intent de Sign In
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient; // Cliente de Sign-In de Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setTitle("Sign in IMDb");
        signInButton = findViewById(R.id.share_button);
        // Personalizar el texto del botón
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Sign in with Google");
        // Configurar las opciones de Google Sign-In para obtener token e email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))  // Este debe ser el web client ID de Firebase
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Listener para el botón de Sign In
        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    // Inicia el flujo de sign-in con Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Retorno de la actividad de Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // Tarea que retorna la cuenta de Google Sign-In
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Maneja el resultado del inicio de sesión
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Obtener el token de ID
            String idToken = account.getIdToken();
            // Crea las credenciales de Firebase con el token de Google
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            // Inicia sesión en Firebase
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso en Firebase
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Log.d("SignIn", "Firebase sign-in successful: " + (user != null ? user.getEmail() : "No user"));
                            // Ir a la siguiente pantalla
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Falló el inicio de sesión en Firebase
                            Log.w("SignIn", "Firebase sign-in failed", task.getException());
                        }
                    });
        } catch (ApiException e) {
            // Falló la obtención de la cuenta de Google
            Log.w("SignIn", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Comprueba si el usuario ya inició sesión anteriormente con Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d("SignIn", "User already signed in with Firebase: " + currentUser.getEmail());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
