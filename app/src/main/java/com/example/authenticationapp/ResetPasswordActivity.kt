package com.example.authenticationapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.authenticationapp.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Auth
        auth = FirebaseAuth.getInstance()

        //Email Validation
        val emailStream = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe{
            showEmailValidAlert(it)
        }

        //Reset Password
        binding.btnResetPw.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { reset ->
                    if (reset.isSuccessful){
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                            Toast.makeText(this, "ตรวจสอบอีเมลเพื่อรีเซ็ตรหัสผ่าน!", Toast.LENGTH_SHORT).show()
                        }
                    }else {
                        Toast.makeText(this, reset.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //Click
        binding.tvBackLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        if (isNotValid){
            binding.etEmail.error = "อีเมลไม่ถูกต้อง!"
            binding.btnResetPw.isEnabled = false
            binding.btnResetPw.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
        }else {
            binding.etEmail.error = null
            binding.btnResetPw.isEnabled = true
            binding.btnResetPw.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
        }
    }
}