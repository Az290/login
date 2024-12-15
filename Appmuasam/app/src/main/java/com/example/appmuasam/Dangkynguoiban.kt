package com.example.appmuasam

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmuasam.databinding.ActivityDangkynguoibanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Dangkynguoiban : AppCompatActivity() {
    private lateinit var binding: ActivityDangkynguoibanBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangkynguoibanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener {
            val email = binding.editTextText2.text.toString()
            val password = binding.editTextTextPassword2.text.toString()
            val confirmPassword = binding.editTextTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid
                                if (userId != null) {
                                    val database = FirebaseDatabase.getInstance()
                                    val userRef = database.getReference("Users").child(userId)
                                    val userInfo = mapOf(
                                        "email" to email,
                                        "accountType" to "seller" // Loại tài khoản người bán
                                    )

                                    userRef.setValue(userInfo).addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Đăng ký thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this, Dangnhapnguoiban::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Lỗi lưu thông tin: ${dbTask.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                    Toast.makeText(this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textView2.setOnClickListener {
            val intent = Intent(this, Dangnhapnguoiban::class.java)
            startActivity(intent)
        }
    }
}
