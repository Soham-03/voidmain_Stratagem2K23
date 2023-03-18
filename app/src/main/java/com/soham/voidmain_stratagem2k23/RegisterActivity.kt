package com.soham.voidmain_stratagem2k23

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.soham.voidmain_stratagem2k23.Global.user
import com.soham.voidmain_stratagem2k23.databinding.ActivityRegisterBinding
import java.io.IOException
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
    private lateinit var detector: FaceDetector
    private var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityRegisterBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val realTimeFdo = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        detector = FaceDetection.getClient(realTimeFdo)

        binding.btnSendOTP.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91"+binding.edtTxtPhoneNumber.text.toString())       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(getCallBacks())       // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
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

        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,SignInActivity::class.java))
        }

        binding.btnCaptureImage.setOnClickListener {
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
                val image = uriToBitmap(fileUri)
                analiseImage(image!!)
            }
            else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this@RegisterActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this@RegisterActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    private fun analiseImage(bitmap: Bitmap) {
        //Make image smaller to do processing faster
        val smallerBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / 10,
            bitmap.height / 10,
            false
        )

        //Input Image for analyzing
        val inputImage = InputImage.fromBitmap(smallerBitmap, 0)
        //start detecting
        detector.process(inputImage)
            .addOnSuccessListener {faces->
                //Task completed

                for (face in faces){
                    val rect = face.boundingBox
                    rect.set(
                        rect.left * 10,
                        rect.top * (10 - 1),
                        rect.right * (10),
                        rect.bottom * 10 + 90
                    )
                    face.trackingId
                }
                println(faces)
                if(faces.isEmpty()) {
                    Toast.makeText(this, "No Face Detected, Please click again", Toast.LENGTH_SHORT)
                        .show()
                    ImagePicker.with(this)
                        .compress(1024)//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(800, 800)
                        .cameraOnly()    //User can only capture image using Camera
                        .createIntent { intent ->
                            startForProfileImageResult.launch(intent)
                        }
                }
                else{
                    Toast.makeText(this, "Face Detected", Toast.LENGTH_SHORT).show()
                    uploadImageToStorage()
                }

//                cropDetectedFace(bitmap, faces)
                println("Face Detected")
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCallBacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@RegisterActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@RegisterActivity, "Some Error occurred from our side", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
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
                    val user = task.result?.user
                    this.user = user!!
                    setDataOnDb(user!!)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@RegisterActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
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