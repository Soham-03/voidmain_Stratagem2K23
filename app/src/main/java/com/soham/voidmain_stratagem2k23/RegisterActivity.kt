package com.soham.voidmain_stratagem2k23

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.soham.voidmain_stratagem2k23.Global.user
import com.soham.voidmain_stratagem2k23.databinding.ActivityRegisterBinding
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var db: FirebaseFirestore
    private lateinit var imageUri: Uri
    private lateinit var storageReference: StorageReference
    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityRegisterBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        binding.btnSendOTP.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91"+binding.edtTxtPhoneNumber.text.toString())       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(getCallBacks())       // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,SignInActivity::class.java))
        }

        binding.btnCaptureImage.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,CameraActivity::class.java))
        }

        binding.btnCaptureImage2.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)//Final image size will be less than 1 MB(Optional)
                .crop(1f,1f)
                .maxResultSize(800, 800)  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        setContentView(binding.root)
    }

    private fun uploadImageToStorage(){
        val filePath = storageReference.child("PatientsImages").child(user.uid)
        filePath.putFile(imageUri).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                db.collection("patients").document(user.uid).update("image", it.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this@RegisterActivity, "Registeredd", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity,MainActivity::class.java))
                    }
            }
        }
            .addOnFailureListener{
                println(it.message)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!
                imageUri = fileUri
                uploadImageToStorage()
            }
            else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this@RegisterActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this@RegisterActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
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
                    Toast.makeText(this@RegisterActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@RegisterActivity, "Some Error occurred from our side", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@RegisterActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
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
                    val user = task.result?.user
                    this.user = user!!
                    setDataOnDb(user!!)
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@RegisterActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    //update ui
                }
            }
    }

    private fun setDataOnDb(user:FirebaseUser){
        val patient = hashMapOf<String,Any>()
        binding.apply{
            patient["name"] = edtTxtName.text.toString()
            patient["age"] = edtTxtAge.text.toString().toInt()
            patient["address"] = edtTxtAddress.text.toString()
            patient["phoneNumber"] = edtTxtPhoneNumber.text.toString()
        }
        db.collection("patients").document(user.uid).set(patient)
            .addOnSuccessListener {
                Toast.makeText(this@RegisterActivity, "Registered Successfully", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this@RegisterActivity,MainActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "Failed to Register", Toast.LENGTH_SHORT).show()
            }
    }
}