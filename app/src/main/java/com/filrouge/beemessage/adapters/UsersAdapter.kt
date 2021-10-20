package com.filrouge.beemessage.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.filrouge.beemessage.databinding.ItemContainerUserbeanBinding
import com.filrouge.beemessage.listeners.UserListener
import com.filrouge.beemessage.models.UserBean

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private var users:List<UserBean>
    private val userListener:UserListener
    constructor(users:List<UserBean>, userListener: UserListener){
        this.users = users
        this.userListener = userListener
    }

    inner class UserViewHolder : RecyclerView.ViewHolder {

        lateinit var binding: ItemContainerUserbeanBinding

        constructor(itemContainerUserbeanBinding: ItemContainerUserbeanBinding) : super(itemContainerUserbeanBinding.root){
            binding = itemContainerUserbeanBinding
        }

        fun setUserData(userBean: UserBean) {
            binding.textName.text = userBean.username
            binding.textEmail.text = userBean.email
            binding.imageProfile.setImageBitmap(getUserImage(userBean.image))
            binding.root.setOnClickListener {
                userListener.onUserClicked(userBean)
            }
        }
    }
    companion object{
        fun getUserImage(encoder:String): Bitmap? {
        var byte: ByteArray = Base64.decode(encoder,
            Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byte,0,byte.size)

    }}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var itemContainerUserbeanBinding:ItemContainerUserbeanBinding = ItemContainerUserbeanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserbeanBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users.get(position))
    }

    override fun getItemCount(): Int {
        return users.size
    }

}