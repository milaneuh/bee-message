package com.filrouge.beemessage.listeners

import com.filrouge.beemessage.models.UserBean

interface UserListener {
    fun onUserClicked(user:UserBean)
}