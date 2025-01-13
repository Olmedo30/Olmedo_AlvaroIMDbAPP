package edu.pmdm.olmedo_lvaroimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.common.SignInButton;

public class SignIn extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setTitle("Sign in IMDb");

        SignInButton signInButton = findViewById(R.id.share_button);
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Sign in with Google");

        //Configura GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Configura el ActivityResultLauncher para manejar el resultado de la autenticación
        signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
                    handleSignInResult(account);
                }
            }
        });
        //Establece el listener para el botón
        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    //Inicia el flujo de autenticación con Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    //Maneja el resultado de la autenticación de Google
    private void handleSignInResult(GoogleSignInAccount account) {
        if (account != null) {
            String idToken = account.getIdToken();
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Log.d("SignIn", "User signed in: " + (user != null ? user.getEmail() : "No user"));
                            startActivity(new Intent(SignIn.this, MainActivity.class));
                            finish();
                        } else {
                            Log.w("SignIn", "Firebase sign-in failed", task.getException());
                        }
                    });
        } else {
            Log.w("SignIn", "Google sign-in failed: account is null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d("SignIn", "User already signed in: " + currentUser.getEmail());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}