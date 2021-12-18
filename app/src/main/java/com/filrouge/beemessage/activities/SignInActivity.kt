package com.filrouge.beemessage.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.filrouge.beemessage.databinding.ActivitySignInBinding
import com.filrouge.beemessage.utilities.Constants
import com.filrouge.beemessage.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        /**On initialize notre preferenceManager**/
        preferenceManager = PreferenceManager(applicationContext)

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.tvcreateNewAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.btSignin.setOnClickListener {

            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    private fun signIn() {
        //On active la barre de progrès
        loading(true)

        //On récupère l'instance de la base de données Firestore
        var database = FirebaseFirestore.getInstance()

        //On récupère le documents dans la collection USERS qui a un
        //attribut email correspondant à celui incrit par l'utilisateur
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.emailRegister.text.toString())
            .get()
            .addOnCompleteListener {
                //Si on trouve un utilisateur possédant l'email inscrit:

                //On transforme le password en Array
                val passwordArray = binding.passwordRegister.text.toString().trim()

                it.result!!.documents.map { documentSnapshot ->
                    //Pour chaque documents trouvé:

                    //On compare le mot de passe incrit et le mot de passe encrypté du document
                    var verifyerResult: BCrypt.Result =
                        BCrypt.verifyer().verify(
                            passwordArray.toCharArray(),
                            documentSnapshot.getString(Constants.KEY_PASSWORD)
                        )

                    if (verifyerResult.verified) {

                        //Si on a un mot de passe correspondant:

                        //On enregistre l'utilisateur dans la session
                        var documentSnapshot: DocumentSnapshot = it.result!!.documents.get(0)
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                        preferenceManager.putString(
                            Constants.KEY_NAME,
                            documentSnapshot.getString(Constants.KEY_NAME)!!
                        )
                        preferenceManager.putString(
                            Constants.KEY_IMAGE,
                            documentSnapshot.getString(Constants.KEY_IMAGE)!!
                        )

                        //On démare l'activity MainActivity
                        val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        //Si on ne trouve pas d'utilisateur dans la base de données :

                        //On n'affiche plus la barre de progrès et on montre un message d'erreur
                        loading(false)
                        showToast("Unable to sign in")
                    }
                }
            }
    }

    //Toast maker
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
    }

    //Verifie les inputs
    private fun isValidSignInDetails(): Boolean {
        if (binding.emailRegister.text.toString().trim().isEmpty()) {
            showToast("Enter a email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailRegister.text.toString())
                .matches()
        ) {
            showToast("Enter a valid email")
            return false
        } else if (binding.passwordRegister.text.toString().trim().isEmpty()) {
            showToast("Enter a password")
            return false
        } else {
            return true
        }
    }


    //Fonction d'affichage de la progressBar
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.btSignin.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btSignin.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}