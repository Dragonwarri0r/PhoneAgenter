package com.mobileclaw.interop.contract

object InteropIds {
    object Surface {
        const val RUNTIME_CALLABLE_BASIC: String = "runtime.callable.basic"
    }

    object Capability {
        const val GENERATE_REPLY: String = "generate.reply"
        const val CALENDAR_READ: String = "calendar.read"
        const val CALENDAR_WRITE: String = "calendar.write"
        const val MESSAGE_SEND: String = "message.send"
        const val EXTERNAL_SHARE: String = "external.share"
    }

    object Scope {
        const val REPLY_GENERATE: String = "reply.generate"
        const val CALENDAR_READ: String = "calendar.read"
        const val CALENDAR_WRITE: String = "calendar.write"
        const val MESSAGE_SEND: String = "message.send"
        const val EXTERNAL_SHARE: String = "external.share"
    }

    object Origin {
        const val EXTERNAL_SHARE: String = "external.share"
    }
}
