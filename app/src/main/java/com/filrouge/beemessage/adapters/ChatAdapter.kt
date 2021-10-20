package com.filrouge.beemessage.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.filrouge.beemessage.databinding.ItemContainerReceivedMessageBinding
import com.filrouge.beemessage.databinding.ItemContainerSentMessageBinding
import com.filrouge.beemessage.models.ChatMessageBean

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private val chatMessageBean: MutableList<ChatMessageBean>
    private val receiverProfileImage: Bitmap
    private val senderId: String

    constructor(
        chatMessageBean: MutableList<ChatMessageBean>,
        receiverProfileImage: Bitmap,
        senderId: String
    ) {
        this.chatMessageBean = chatMessageBean
        this.receiverProfileImage = receiverProfileImage
        this.senderId = senderId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.w("ViewType", viewType.toString())
        if (viewType == VIEW_TYPE_SENT) {
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceiveMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessageBean.get(position))
        } else {
            (holder as ReceiveMessageViewHolder).setData(
                chatMessageBean.get(position),
                receiverProfileImage
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessageBean.size
    }

    override fun getItemViewType(position: Int): Int {
        if (chatMessageBean.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }


    companion object {
        /**Ceci sont des valeurs et des classe static, elles se chargeront lorsque l'application se lance**/

        //Les view type nous permet de différencier si un message est reçu ou envoyer.
        // Ce qui nous permettera de les afficher différement.
        val VIEW_TYPE_SENT: Int = 1
        val VIEW_TYPE_RECEIVED: Int = 2

        /**
         * ViewHolder des messages envoyés.
         * Si un message possède un viewType égale à 1
         * alors le recyclerView utilisera ce ViewHolder
         */
        class SentMessageViewHolder : RecyclerView.ViewHolder {
            //Ce binding nous permet de lier le ViewHolder avec le
            //fichier item_container_sent_message.xml qui définira
            //l'affichage de ce dernier
            private val binding: ItemContainerSentMessageBinding

            //Constructeur de notre classe
            constructor(itemContainerSentMessageBinding: ItemContainerSentMessageBinding) : super(
                itemContainerSentMessageBinding.root
            ) {
                binding = itemContainerSentMessageBinding
            }

            //Fonction nosu permettant de afficher les données de notre
            //message grâce à notre binding
            fun setData(chatMessageBean: ChatMessageBean) {
                binding.textMessage.text = chatMessageBean.message
                binding.textDateTime.text = chatMessageBean.dateTime
            }
        }

        class ReceiveMessageViewHolder : RecyclerView.ViewHolder {
            private val binding: ItemContainerReceivedMessageBinding

            constructor(itemContainerReceivedMessageBinding: ItemContainerReceivedMessageBinding) : super(
                itemContainerReceivedMessageBinding.root
            ) {
                binding = itemContainerReceivedMessageBinding
            }

            fun setData(chatMessageBean: ChatMessageBean, receivedImageProfile: Bitmap) {
                binding.textMessage.text = chatMessageBean.message
                binding.textDateTime.text = chatMessageBean.dateTime
                binding.imageProfile.setImageBitmap(receivedImageProfile)
            }
        }
    }

}