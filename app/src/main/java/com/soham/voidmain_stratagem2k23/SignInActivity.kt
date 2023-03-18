package com.soham.voidmain_stratagem2k23

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.soham.voidmain_stratagem2k23.databinding.ActivitySigninBinding
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        binding.btnSendOTP.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91"+binding.edtTxtPhoneNumber.text.toString())       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(getCallBacks())          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        auth.useAppLanguage()

        binding.txtCreateAccount.setOnClickListener {
            startActivity(Intent(this@SignInActivity,RegisterActivity::class.java))
        }

        binding.btnEmergency.setOnClickListener {
            try {
                val my_callIntent = Intent(Intent.ACTION_DIAL)
                my_callIntent.data = Uri.parse("tel:+9188888888")
                //here the word 'tel' is important for making a call...
                startActivity(my_callIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    applicationContext,
                    "Error in your phone call" + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContentView(binding.root)
    }

    private fun getCallBacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@SignInActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@SignInActivity, "Some Error occurred from our side", Toast.LENGTH_SHORT).show()
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Toast.makeText(this@SignInActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
                storedVerificationId = verificationId
                resendToken = token
                binding.btnSignIn.setOnClickListener {
                    val credential = PhoneAuthProvider.getCredential(verificationId, binding.edtTxtOTP.text.toString())
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }
        return callbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@SignInActivity, "Sign in Successful", Toast.LENGTH_SHORT).show()
                    val user = task.result?.user
                    Global.user = user
                    println(user?.uid)
                    val intent = Intent(this@SignInActivity,MainActivity::class.java)
                    startActivity(intent)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@SignInActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}