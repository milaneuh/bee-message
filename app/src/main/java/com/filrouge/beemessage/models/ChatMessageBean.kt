package com.filrouge.beemessage.models

import java.util.*

class ChatMessageBean {
    lateinit var senderId: String
    lateinit var receiverId: String
    lateinit var message: String
    lateinit var dateTime: String

    lateinit var dateObject: Date
}