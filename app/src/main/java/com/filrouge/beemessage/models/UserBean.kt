package com.filrouge.beemessage.models

import java.io.Serializable

class UserBean : Serializable {
    lateinit var username: String
    lateinit var image: String
    lateinit var email: String
    lateinit var token: String
    lateinit var id: String
}