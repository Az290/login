package com.example.appmuasam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmuasam.databinding.ActivityDangnhapBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Dangnhap : AppCompatActivity() {
    private lateinit var binding: ActivityDangnhapBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val rcSignIn = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangnhapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginbutton.setOnClickListener {
            val email = binding.loginemail.text.toString()
            val password = binding.loginpassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            if (userId != null) {
                                val database = FirebaseDatabase.getInstance()
                                val userRef = database.getReference("Users").child(userId)

                                userRef.get().addOnSuccessListener { snapshot ->
                                    val accountType = snapshot.child("accountType").value.toString()
                                    if (accountType == "buyer") {
                                        Toast.makeText(
                                            this,
                                            "Đăng nhập thành công",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Chỉ tài khoản Người mua được phép đăng nhập",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        firebaseAuth.signOut()
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Không thể xác thực tài khoản",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Lỗi: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupRedirectText.setOnClickListener {
            val signupIntent = Intent(this, Dangky::class.java)
            startActivity(signupIntent)
        }
        binding.textView12.setOnClickListener {
            val dnnguoiban = Intent(this, Dangnhapnguoiban::class.java)
            startActivity(dnnguoiban)
        }
        binding.logingoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, rcSignIn)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google login failed: ${e.statusCode}")
                Toast.makeText(this, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) {
            Toast.makeText(this, "Token Google không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startMainActivity()
                } else {
                    Log.e("FirebaseAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Xác thực với Firebase thất bại.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

