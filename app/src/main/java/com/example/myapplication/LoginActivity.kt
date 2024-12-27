package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var  credentialManager: CredentialManager
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        setContentView(binding.root)

        binding.goToRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Sign in with email and password
        binding.loginButton.setOnClickListener{ view ->
            Log.i("WHAT", "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq")
            val userEmail = binding.loginEmail.text.toString()
            val userPassword = binding.loginPassword.text.toString()

            if (userEmail == "" || userPassword == "")
            {
                Log.e("Authentication", "Email or password field is empty!")
                if(userEmail == "") binding.loginEmail.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner_error)
                if (userPassword == "") binding.loginPassword.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner_error)
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("f", "signInWithEmail:success")
                        val user = auth.currentUser
                        // do stuff with user
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("f", "signInWithEmail:failure", task.exception)

                    }
                }
        }

        // Forgot Password
        binding.forgotPasswordButton.setOnClickListener{
            Log.i("FP", "click!")
            val currentUser = auth.currentUser ?: throw Exception("currentUser is null in forgot Password!")
            val userEmail = currentUser.email.toString()
            Log.i("FP", userEmail)

            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener{ task ->
                if (task.isSuccessful) Log.d("FP", "Email sent.")
            }
        }

        // Sign in with google
        binding.googleSignInButton.setOnClickListener {
            Log.i("Google5", "Entered Function!")
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.web_client_ID))
                .setFilterByAuthorizedAccounts(false)
                //.setAutoSelectEnabled(true)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    handleFailure(e)
                }
            }

        }

        // Sign in with facebook
        binding.facebookSignIn.setOnClickListener {
            binding.buttonFacebookLogin.performClick()
        }
        binding.buttonFacebookLogin.setOnClickListener {
            val callbackManager = CallbackManager.Factory.create()
            binding.buttonFacebookLogin.setReadPermissions("email", "public_profile")
            binding.buttonFacebookLogin.registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d("FacebookLogin", "facebook:onSuccess:$loginResult")
                        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener(this@LoginActivity) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("FacebookLoginSuccess", "signInWithCredential:success")
                                    val user = auth.currentUser
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("FacebookLoginFail", "signInWithCredential:failure", task.exception)
                                }
                            }
                    }

                    override fun onCancel() {
                        Log.d("FacebookLogin", "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d("FacebookLogin", "facebook:onError", error)
                    }
                },
            )

        }

        binding.loginEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.loginEmail.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner_highlighted)
            }
            else {
                binding.loginEmail.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner)
            }
        }
        binding.loginPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.loginPassword.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner_highlighted)
            }
            else {
                binding.loginPassword.background = AppCompatResources.getDrawable(this, R.drawable.rounded_corner)
            }
        }

    }
    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        when {
                            googleIdTokenCredential != null -> {
                                val idToken = googleIdTokenCredential.idToken
                                val fireBaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                auth.signInWithCredential(fireBaseCredential)
                                    .addOnCompleteListener(this) {
                                            task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("GoogleFirebase", "signInWithCredential:success")
                                            val user = auth.currentUser

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("GoogleFirebase", "signInWithCredential:failure", task.exception)

                                        }
                                    }
                            }
                            else -> {
                                // Shouldn't happen.
                                Log.d("GoogleFirebase", "No ID token!")
                            }
                        }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("s", "Received an invalid google id token response", e)
                    }
                }
                else {
                    Log.e("GoogleSignFail", "Unexpected credential")
                }
            }
            else -> {
                Log.e("GoogleSignFail", "Unexpected credential")
            }
        }
    }

    private fun handleFailure(exception: GetCredentialException) {
        Log.e("GoogleFail", exception.message.toString())
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }
}