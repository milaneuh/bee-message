package com.filrouge.beemessage.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.filrouge.beemessage.databinding.ActivityMainBinding
import com.filrouge.beemessage.utilities.Constants
import com.filrouge.beemessage.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.HashMap

/***
 * CLASSE MAIN ACTIVITY
 * Cette activité est l'activité principale lorsque l'utilisateur se conecte / lance l'application en étant connecté
 * On y pourras voir notre historiques de messagee
 */

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**Composants Graphique**/
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /**On initialize notre preferenceManager**/
        preferenceManager = PreferenceManager(applicationContext)

        /**On charge les données de l'utilisateur**/
        loadUserDetails()
        /**On charge le token de l'utilisateur**/
        getToken()
        /**On met en place les listeners**/
        setListeners()
    }

    private fun setListeners(){
        /**Fonction nous permettant de mettre en place les listeners**/

        binding.imageLogout.setOnClickListener{
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext,UsersActivity::class.java))

        }
    }
    private fun signOut(){
        /**Fonction nous permettant de nous déconnecter**/

        showToast("Signing out ...")
        //On initialise l'instance de la databse
        var databse  =FirebaseFirestore.getInstance()
        //On récupère le document de l'utilisateur connecté
        var documentReference = databse.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        //On créer une HashMap "update" pour retirer le token de session de notre utilisateur
        var updates:HashMap<String, Any> = HashMap()
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
        //On push la HashMap dans le document de notre utilisateur
        documentReference.update(updates)
            .addOnSuccessListener{
                /**Si ça marche :**/

                //On retire l'utilisateur du preferenceManager
                preferenceManager.clear()
                //On lance l'activité SignInActivity
                startActivity(Intent(applicationContext,SignInActivity::class.java))
                //On finish cette activité
                finish()
            }
            .addOnFailureListener {
                /**Si ça ne marche pas :**/

                showToast("Unable to sign out")
            }
    }
    fun loadUserDetails() {
        /**Fonction nous permettant de charger les données de l'utilisateur connecté grace au preferenceManager**/

        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        var byte: ByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT)
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(byte,0,byte.size)
        binding.imageProfile.setImageBitmap(bitmap)

    }

    //Toast maker
    private fun showToast(message: String) {
        /**Fonction nous permettant de créer des Toast plus facilement**/

        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
    }

    private fun getToken() {
        /**Fonction nous permettant de récupèrer le token de l'utilisateur connecté**/

        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }
    private fun updateToken(token:String){
        /**Fonction nous permettant de update le token quand l'utilisater se connecte**/

        var databse = FirebaseFirestore.getInstance()
        var documentReference = databse.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }
}