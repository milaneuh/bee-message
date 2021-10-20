package com.filrouge.beemessage.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import com.filrouge.beemessage.adapters.ChatAdapter
import com.filrouge.beemessage.databinding.ActivityChatBinding
import com.filrouge.beemessage.models.ChatMessageBean
import com.filrouge.beemessage.models.UserBean
import com.filrouge.beemessage.utilities.Constants
import com.filrouge.beemessage.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/***
 * CLASS CHAT ACTIVITY
 * Cette class nous permet de voir l'histoque de message avec un/des
 * utilisateur/s on y pourras envoyer des messages, supprimer des messages
 * ou envoyer des médias/fichier.
 */
class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var receivedUser: UserBean
    private lateinit var chatMessageBeans: MutableList<ChatMessageBean>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var databse: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**On initialize notre preferenceManager**/
        preferenceManager = PreferenceManager(applicationContext)

        setListeners()

        loadReceivedDetails()

        init()

        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessageBeans = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessageBeans,
            getBitmapFromEncodeString(receivedUser.image)!!,
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        binding.chatRecyclerView.adapter = chatAdapter
        databse = FirebaseFirestore.getInstance()
    }

    fun sendMessage() {
        var message: HashMap<String, Any> = HashMap()
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.id)
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.text.toString())
        message.put(Constants.KEY_TIMESTAMP, Date())
        databse.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        binding.inputMessage.text = null
    }

    private fun listenMessages() {
        databse.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
            .addSnapshotListener(eventListener)
        databse.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }


    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener;
        }
        if (value != null) {
            var count = chatMessageBeans.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    var chatMessageBean: ChatMessageBean = ChatMessageBean()
                    chatMessageBean.senderId =
                        documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    chatMessageBean.receiverId =
                        documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    chatMessageBean.message = documentChange.document.getString(Constants.KEY_MESSAGE)!!
                    chatMessageBean.dateTime =
                        getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                    chatMessageBean.dateObject =
                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    chatMessageBeans.add(chatMessageBean)
                }
            }
            chatMessageBeans.sortBy { it.dateObject.time }
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageBeans.size, chatMessageBeans.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageBeans.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun getBitmapFromEncodeString(encodedImage: String): Bitmap? {
        var bytes: ByteArray = Base64.decode(
            encodedImage,
            Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceivedDetails() {
        receivedUser = intent.getSerializableExtra(Constants.KEY_USER) as UserBean
        binding.textName.setText(receivedUser.username)
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("dd MMMM, yy - hh:mm", Locale.getDefault()).format(date)
    }
}