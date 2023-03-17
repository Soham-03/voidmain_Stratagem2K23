package com.soham.voidmain_stratagem2k23

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.soham.voidmain_stratagem2k23.databinding.ActivityMainBinding
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
        setContentView(binding.root)
    }

    private fun getCallBacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

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
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
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
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this@SignInActivity, "Sign in Successful", Toast.LENGTH_SHORT).show()
                    val user = task.result?.user
                    Global.user = user
                    println(user?.uid)
                    val intent = Intent(this@SignInActivity,MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@SignInActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}