package com.filrouge.beemessage.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.filrouge.beemessage.databinding.ActivitySignUpBinding
import com.filrouge.beemessage.utilities.Constants
import com.filrouge.beemessage.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var preferenceManager: PreferenceManager
    var encodedImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        /**On initialize notre preferenceManager**/
        preferenceManager = PreferenceManager(applicationContext)

        setContentView(binding.root)

        /**On met en place nos listeners**/
        setListeners()
    }

    private fun setListeners() {
        //Clic -> SignInActivity
        binding.tvAllreadyHaveAnAccount.setOnClickListener {
            onBackPressed()
        }

        //Clic -> Verification des inputs -> Enregistrement sur la bdd -> MainActivity
        binding.btSignup.setOnClickListener() {
            if (isValidSignUpDetails()) {
                Log.w("SIGNINGUP", "isValidSIgnUpDetails(True)")
                signUp()
            }
        }

        //Clic -> choisir image de profil
        binding.layoutImage.setOnClickListener {
            var intent: Intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    //Toast maker
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
    }

    //Fonction d'inscription
    private fun signUp() {
        //Affichage progressBar
        loading(true)
        //Initialisation de la bdd
        var databse = FirebaseFirestore.getInstance()

        //On encrypte le mot de passe
        Log.w("SIGNINGUP", "Hashing password...")
        val passwordArray = binding.passwordRegister.text.toString().trim()
        val hashedPassword =
            BCrypt.withDefaults().hashToString(10, passwordArray.toCharArray())

        //On crée une hashMap avec nos data
        var user: HashMap<String, Any> = HashMap()
        user.put(Constants.KEY_NAME, binding.usernameRegister.text.toString())
        user.put(Constants.KEY_EMAIL, binding.emailRegister.text.toString())
        user.put(Constants.KEY_PASSWORD, hashedPassword)
        user.put(Constants.KEY_IMAGE, encodedImage!!)

        //On ajoute nos data dans la bdd
        databse.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                //Si sucess -> On connecte l'user, et on lance MainActivity
                Log.w("signUp", "Sucess")
                //Désaffichage de la progressBar
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(
                    Constants.KEY_NAME,
                    binding.usernameRegister.text.toString()
                )
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)

                //Intent de MainActivity
                var intent: Intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                //Lancement de l'activity
                startActivity(intent)
            }.addOnFailureListener { exception ->
                loading(false)
                exception.printStackTrace()
            }
    }

    //Fonction qui transforme la bitmap de notre image en Base64
    private fun encodeImage(bitmap: Bitmap): String? {
        var previewWidth: Int = 150
        var previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        var previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        var byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        var byte: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byte, Base64.DEFAULT)
    }

    //Image choisis
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        run {
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    var imageUri: Uri = result.data!!.data!!
                    try {
                        var inputStream: InputStream = contentResolver.openInputStream(imageUri)!!
                        var bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        encodedImage = encodeImage(bitmap)!!
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //Fonction qui vérifie les inputs
    private fun isValidSignUpDetails(): Boolean {
        if (encodedImage == null) {
            showToast("Select profile image")
            Log.w("TESTBT", "Select profile image")
            return false
        } else if (binding.usernameRegister.text.toString().trim(' ').isEmpty()) {
            showToast("Enter a username")
            Log.w("TESTBT", "Enter a username")
            return false
        } else if (binding.emailRegister.text.toString().trim(' ').isEmpty()) {
            showToast("Enter a email")
            Log.w("TESTBT", "Enter a email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailRegister.text.toString())
                .matches()
        ) {
            showToast("Enter a valid email")
            Log.w("TESTBT", "Enter a valid email")
            return false
        } else if (binding.passwordRegister.text.toString().trim(' ').isEmpty()) {
            showToast("Enter a password")
            Log.w("TESTBT", "Enter a password")
            return false
        } else if (binding.passwordConfirmationRegister.text.toString().trim(' ').isEmpty()) {
            showToast("Comfirm your password")
            Log.w("TESTBT", "Confirm your password")
            return false
        } else if (!binding.passwordRegister.text.toString()
                .equals(binding.passwordConfirmationRegister.text.toString())
        ) {
            showToast("Password & comfirm password must be same")
            Log.w("TESTBT", "Password & comfirm password must be same")
            return false
        } else {
            return true
        }
    }

    //Fonction d'affichage de la progressBar
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.btSignup.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btSignup.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}