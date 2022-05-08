package com.example.authenticationapp

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.authenticationapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import java.util.*

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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

        //Click
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            registerUser(email, password)
        }
        binding.tvHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //Fullname Validation
        val nameStream = RxTextView.textChanges(binding.etFullname)
            .skipInitialValue()
            .map { name ->
                name.isEmpty()
            }
        nameStream.subscribe{
            showNameExistAlert(it)
        }

        //Username Validation
        val usernameStream = RxTextView.textChanges(binding.etUsername)
            .skipInitialValue()
            .map { username ->
                username.length < 6
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it, "Username")
        }

        //Password Validation
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { username ->
                username.length < 6
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

        //Confirm Password Validation
        val passwordConfirmStream = io.reactivex.Observable.merge(
            RxTextView.textChanges(binding.etPassword)
                .skipInitialValue()
                .map { password ->
                    password.toString() != binding.etConfirmPassword.text.toString()
                },
            RxTextView.textChanges(binding.etConfirmPassword)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.etPassword.text.toString()
                }
        )
        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }

        //Button Enable True or False
        val invalidFieldsStream = io.reactivex.Observable.combineLatest(
            nameStream,
            emailStream,
            usernameStream,
            passwordStream,
            passwordConfirmStream,
            {nameInvalid: Boolean, emailInvalid: Boolean, usernameInvalid: Boolean, passwordInvalid: Boolean , passwordconfirmInvalid: Boolean ->
               !nameInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid && !passwordconfirmInvalid
            })
        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }else {
                binding.btnRegister.isEnabled = false
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }


    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful){
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "ลงทะเบียนสำเร็จ!", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showNameExistAlert(isNotValid: Boolean){
        binding.etFullname.error = if (isNotValid) "ชื่อไม่สามารถเว้นว่างได้!" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Username")
            binding.etUsername.error = if (isNotValid) "$text ต้องมากกว่า 6 ตัวอักษร!" else null
        else if (text == "Password")
            binding.etPassword.error = if (isNotValid) "$text ต้องมากกว่า 8 ตัวอักษร!" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean){
        binding.etEmail.error = if (isNotValid) "อีเมลไม่ถูกต้อง!" else null
    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean){
        binding.etConfirmPassword.error = if (isNotValid) "รหัสผ่านไม่ตรงกัน!" else null
    }
}