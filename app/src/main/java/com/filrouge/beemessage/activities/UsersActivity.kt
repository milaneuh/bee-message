package com.filrouge.beemessage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.filrouge.beemessage.R
import com.filrouge.beemessage.adapters.UsersAdapter
import com.filrouge.beemessage.databinding.ActivityUsersBinding
import com.filrouge.beemessage.listeners.UserListener
import com.filrouge.beemessage.models.UserBean
import com.filrouge.beemessage.utilities.Constants
import com.filrouge.beemessage.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersActivity : AppCompatActivity(), UserListener {
    lateinit var binding: ActivityUsersBinding
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers() {
        loading(true)
        var databse = FirebaseFirestore.getInstance()
        databse.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                var currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null) {
                    var users: MutableList<UserBean> = ArrayList()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result!!) {
                        if (currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }
                        var user: UserBean = UserBean()
                        user.username = queryDocumentSnapshot.getString(Constants.KEY_NAME)!!
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)!!
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)!!
                        if (queryDocumentSnapshot.contains(Constants.KEY_FCM_TOKEN)) {
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)!!
                        }
                        user.id = queryDocumentSnapshot.id
                        users.add(user)
                    }
                    if (users.size > 0) {
                        var usersAdapter: UsersAdapter = UsersAdapter(users, this)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    } else (showErrorMessage())
                } else (showErrorMessage())
            }
    }

    private fun showErrorMessage() {
        binding.tvErrorMessage.text = String.format("%s", "No user avalaible ")
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    //Fonction d'affichage de la progressBar
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onUserClicked(user: UserBean) {
        val intent: Intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}