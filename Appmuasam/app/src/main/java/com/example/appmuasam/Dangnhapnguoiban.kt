package com.example.appmuasam

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmuasam.databinding.ActivityDangnhapnguoibanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Dangnhapnguoiban : AppCompatActivity() {
    private lateinit var binding: ActivityDangnhapnguoibanBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangnhapnguoibanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.button.setOnClickListener {
            val email = binding.editTextText2.text.toString()
            val password = binding.editTextTextPassword2.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            if (userId != null) {
                                val userRef = database.getReference("Users").child(userId)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val accountType = snapshot.child("accountType").value
                                        if (accountType == "seller") {
                                            Toast.makeText(
                                                this@Dangnhapnguoiban,
                                                "Đăng nhập thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this@Dangnhapnguoiban, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            firebaseAuth.signOut()
                                            Toast.makeText(
                                                this@Dangnhapnguoiban,
                                                "Chỉ người bán mới được phép đăng nhập",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@Dangnhapnguoiban,
                                            "Lỗi: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            } else {
                                Toast.makeText(
                                    this,
                                    "Lỗi: Không thể lấy thông tin người dùng",
                                    Toast.LENGTH_SHORT
                                ).show()
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

        binding.textView2.setOnClickListener {
            val registerIntent = Intent(this, Dangkynguoiban::class.java)
            startActivity(registerIntent)
        }
    }
}
