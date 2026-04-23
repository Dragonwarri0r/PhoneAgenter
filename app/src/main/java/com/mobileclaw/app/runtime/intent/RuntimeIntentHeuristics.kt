package com.mobileclaw.app.runtime.intent

import com.mobileclaw.app.runtime.policy.ActionScope

data class RuntimeIntentInference(
    val capabilityId: String,
    val scope: ActionScope,
    val matchedSignals: List<String>,
    val containsSensitiveContent: Boolean,
    val containsBlockedOperation: Boolean,
    val confidence: Double,
)

object RuntimeIntentHeuristics {
    private data class MatchText(
        val raw: String,
        val spaced: String,
        val tokens: Set<String>,
    )

    private val draftSignals = linkedMapOf(
        "draft:compose" to listOf(
            "help me write",
            "help me draft",
            "help me compose",
            "what should i say",
            "what should i send",
            "帮我写",
            "帮我起草",
            "帮我草拟",
            "起草",
            "草拟",
            "润色",
            "改写",
            "重写",
        ),
        "draft:transform" to listOf(
            "summarize",
            "translate",
            "explain",
            "总结",
            "翻译",
            "解释",
        ),
    )

    private val blockedSignals = linkedMapOf(
        "blocked:financial_transfer" to listOf(
            "transfer money",
            "send money",
            "wire money",
            "make a payment",
            "buy now",
            "purchase",
            "place order",
            "转账",
            "打款",
            "付款",
            "支付",
            "购买",
            "下单",
        ),
        "blocked:mass_delete" to listOf(
            "delete all",
            "delete everything",
            "erase everything",
            "remove everything",
            "wipe device",
            "factory reset",
            "删除所有",
            "全部删除",
            "清空全部",
            "恢复出厂",
        ),
        "blocked:security_bypass" to listOf(
            "disable security",
            "turn off security",
            "disable 2fa",
            "turn off 2fa",
            "share password",
            "send password",
            "export passwords",
            "关闭安全",
            "关闭双重验证",
            "分享密码",
            "发送密码",
            "导出密码",
        ),
    )

    private val calendarSignals = linkedMapOf(
        "calendar:read" to listOf(
            "what's on my calendar",
            "what is on my calendar",
            "show my calendar",
            "show my schedule",
            "check my calendar",
            "what do i have today",
            "what do i have tomorrow",
            "today on my calendar",
            "today's schedule",
            "my schedule today",
            "my schedule tomorrow",
            "看看我的日历",
            "我的日程",
            "今天的日程",
            "明天的日程",
            "查看日历",
            "今天有什么安排",
            "明天有什么安排",
        ),
        "calendar:delete" to listOf(
            "delete event",
            "remove event",
            "remove from my calendar",
            "delete from my calendar",
            "cancel meeting",
            "delete meeting",
            "delete calendar event",
            "删除日程",
            "删掉日程",
            "从日历删除",
            "取消这个会议",
            "删除这个事件",
        ),
        "calendar:schedule" to listOf(
            "add to my calendar",
            "add this to my calendar",
            "create event",
            "create meeting",
            "add meeting",
            "add event",
            "schedule meeting",
            "schedule event",
            "schedule with",
            "book meeting",
            "set up meeting",
            "reschedule",
            "move meeting",
            "remind me",
            "安排",
            "添加日程",
            "加入日历",
            "创建日程",
            "创建事件",
            "改期",
            "重新安排",
            "提醒我",
        ),
    )

    private val alarmSignals = linkedMapOf(
        "alarm:set" to listOf(
            "set alarm",
            "wake me up",
            "create alarm",
            "alarm for",
            "set an alarm",
            "设置闹钟",
            "设个闹钟",
            "叫醒我",
            "闹钟提醒",
        ),
        "alarm:show" to listOf(
            "show alarms",
            "open alarms",
            "open clock",
            "show my alarms",
            "查看闹钟",
            "打开闹钟",
            "看下闹钟",
        ),
        "alarm:dismiss" to listOf(
            "dismiss alarm",
            "cancel alarm",
            "turn off alarm",
            "stop alarm",
            "关闭闹钟",
            "取消闹钟",
            "停掉闹钟",
        ),
    )

    private val shareSignals = linkedMapOf(
        "share:external" to listOf(
            "share this",
            "share it",
            "share with",
            "post this",
            "publish this",
            "publish it",
            "post to",
            "publish to",
            "tweet",
            "upload this",
            "分享",
            "发布",
            "转发",
            "发到",
            "分享到",
            "发朋友圈",
        ),
    )

    private val messageSignals = linkedMapOf(
        "message:send" to listOf(
            "send message",
            "send a message",
            "text",
            "dm",
            "email",
            "reply to",
            "respond to",
            "发消息",
            "发短信",
            "发邮件",
            "回复",
            "回消息",
            "联系",
            "告诉",
        ),
    )

    private val sensitiveWriteSignals = linkedMapOf(
        "write:sensitive_store" to listOf(
            "save to notes",
            "save this to",
            "write this to",
            "write into",
            "store in",
            "record in",
            "save contact",
            "存到",
            "写入",
            "记录到",
            "保存到",
            "保存联系人",
        ),
        "write:sensitive_edit" to listOf(
            "update note",
            "edit note",
            "change setting",
            "update settings",
            "modify record",
            "delete note",
            "delete record",
            "更新笔记",
            "编辑笔记",
            "修改设置",
            "更新设置",
            "修改记录",
            "删除记录",
        ),
    )

    private val uiSignals = linkedMapOf(
        "ui:gesture" to listOf(
            "click",
            "tap",
            "press",
            "open app",
            "open the",
            "open settings",
            "select option",
            "select the",
            "submit",
            "fill",
            "toggle",
            "scroll",
            "点击",
            "点按",
            "按下",
            "打开",
            "选择",
            "提交",
            "填写",
            "切换",
            "滚动",
        ),
    )

    private val sensitiveContentSignals = linkedMapOf(
        "content:sensitive_secret" to listOf(
            "password",
            "passcode",
            "otp",
            "verification code",
            "api key",
            "secret",
            "密码",
            "验证码",
            "密钥",
            "口令",
        ),
        "content:sensitive_financial" to listOf(
            "bank account",
            "credit card",
            "debit card",
            "social security",
            "ssn",
            "银行账户",
            "银行卡",
            "信用卡",
            "身份证",
        ),
        "content:sensitive_personal" to listOf(
            "medical record",
            "diagnosis",
            "address",
            "phone number",
            "contact details",
            "病历",
            "诊断",
            "住址",
            "电话号码",
            "联系方式",
        ),
    )

    fun infer(userInput: String): RuntimeIntentInference {
        val matchText = buildMatchText(userInput)

        explicitMarkerOverride(matchText.raw)?.let { return it }

        val draftMatches = collectMatches(matchText, draftSignals)
        if (draftMatches.isNotEmpty()) {
            val sensitiveMatches = collectMatches(matchText, sensitiveContentSignals)
            return inference(
                capabilityId = "generate.reply",
                scope = ActionScope.REPLY_GENERATE,
                matchedSignals = draftMatches + sensitiveMatches,
                containsSensitiveContent = sensitiveMatches.isNotEmpty(),
                containsBlockedOperation = false,
                confidence = 0.78,
            )
        }

        val calendarMatches = collectMatches(matchText, calendarSignals).ifEmpty {
            when {
                looksLikeCalendarRead(matchText) -> listOf("calendar:read")
                looksLikeCalendarDeletion(matchText) -> listOf("calendar:delete")
                looksLikeCalendarScheduling(matchText.raw) -> listOf("calendar:schedule")
                else -> emptyList()
            }
        }
        val alarmMatches = collectMatches(matchText, alarmSignals)
        val shareMatches = collectMatches(matchText, shareSignals)
        val messageMatches = collectMatches(matchText, messageSignals)
        val sensitiveWriteMatches = collectMatches(matchText, sensitiveWriteSignals)
        val uiMatches = collectMatches(matchText, uiSignals)
        val blockedMatches = collectMatches(matchText, blockedSignals)
        val sensitiveMatches = collectMatches(matchText, sensitiveContentSignals)

        val capabilityId = when {
            alarmMatches.any { it == "alarm:set" } -> "alarm.set"
            alarmMatches.any { it == "alarm:dismiss" } -> "alarm.dismiss"
            alarmMatches.any { it == "alarm:show" } -> "alarm.show"
            calendarMatches.any { it == "calendar:delete" } -> "calendar.delete"
            calendarMatches.any { it == "calendar:read" } -> "calendar.read"
            calendarMatches.isNotEmpty() -> "calendar.write"
            shareMatches.isNotEmpty() -> "external.share"
            messageMatches.isNotEmpty() -> "message.send"
            sensitiveWriteMatches.isNotEmpty() -> "sensitive.write"
            uiMatches.isNotEmpty() -> "ui.act"
            else -> "generate.reply"
        }

        val capabilityScope = when (capabilityId) {
            "calendar.read" -> ActionScope.CALENDAR_READ
            "calendar.write" -> ActionScope.CALENDAR_WRITE
            "calendar.delete" -> ActionScope.CALENDAR_DELETE
            "alarm.set" -> ActionScope.ALARM_SET
            "alarm.show" -> ActionScope.ALARM_SHOW
            "alarm.dismiss" -> ActionScope.ALARM_DISMISS
            "external.share" -> ActionScope.EXTERNAL_SHARE
            "message.send" -> ActionScope.MESSAGE_SEND
            "sensitive.write" -> ActionScope.SENSITIVE_WRITE
            "ui.act" -> ActionScope.UI_ACT
            else -> ActionScope.REPLY_GENERATE
        }

        val matchedSignals = buildList {
            addAll(calendarMatches)
            addAll(alarmMatches)
            addAll(shareMatches)
            addAll(messageMatches)
            addAll(sensitiveWriteMatches)
            addAll(uiMatches)
            addAll(blockedMatches)
            addAll(sensitiveMatches)
            if (isEmpty()) add("reply:default_generation")
        }

        return if (blockedMatches.isNotEmpty()) {
            inference(
                capabilityId = capabilityId,
                scope = ActionScope.BLOCKED_OPERATION,
                matchedSignals = matchedSignals,
                containsSensitiveContent = sensitiveMatches.isNotEmpty(),
                containsBlockedOperation = true,
                confidence = 0.94,
            )
        } else {
            val confidence = when (capabilityScope) {
                ActionScope.CALENDAR_WRITE,
                ActionScope.CALENDAR_DELETE,
                ActionScope.CALENDAR_READ,
                ActionScope.ALARM_SET,
                ActionScope.ALARM_SHOW,
                ActionScope.ALARM_DISMISS,
                ActionScope.EXTERNAL_SHARE,
                ActionScope.MESSAGE_SEND,
                ActionScope.SENSITIVE_WRITE,
                ActionScope.UI_ACT,
                -> 0.84

                else -> 0.58
            }
            inference(
                capabilityId = capabilityId,
                scope = capabilityScope,
                matchedSignals = matchedSignals,
                containsSensitiveContent = sensitiveMatches.isNotEmpty(),
                containsBlockedOperation = false,
                confidence = confidence,
            )
        }
    }

    private fun explicitMarkerOverride(rawInput: String): RuntimeIntentInference? {
        return when {
            "[blocked]" in rawInput -> inference(
                capabilityId = "generate.reply",
                scope = ActionScope.BLOCKED_OPERATION,
                matchedSignals = listOf("debug:blocked_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = true,
                confidence = 0.99,
            )

            "[calendar]" in rawInput -> inference(
                capabilityId = "calendar.write",
                scope = ActionScope.CALENDAR_WRITE,
                matchedSignals = listOf("debug:calendar_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[calendar_read]" in rawInput -> inference(
                capabilityId = "calendar.read",
                scope = ActionScope.CALENDAR_READ,
                matchedSignals = listOf("debug:calendar_read_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[calendar_delete]" in rawInput -> inference(
                capabilityId = "calendar.delete",
                scope = ActionScope.CALENDAR_DELETE,
                matchedSignals = listOf("debug:calendar_delete_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[alarm_set]" in rawInput -> inference(
                capabilityId = "alarm.set",
                scope = ActionScope.ALARM_SET,
                matchedSignals = listOf("debug:alarm_set_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[alarm_show]" in rawInput -> inference(
                capabilityId = "alarm.show",
                scope = ActionScope.ALARM_SHOW,
                matchedSignals = listOf("debug:alarm_show_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[alarm_dismiss]" in rawInput -> inference(
                capabilityId = "alarm.dismiss",
                scope = ActionScope.ALARM_DISMISS,
                matchedSignals = listOf("debug:alarm_dismiss_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[share]" in rawInput -> inference(
                capabilityId = "external.share",
                scope = ActionScope.EXTERNAL_SHARE,
                matchedSignals = listOf("debug:share_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[message]" in rawInput -> inference(
                capabilityId = "message.send",
                scope = ActionScope.MESSAGE_SEND,
                matchedSignals = listOf("debug:message_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[write]" in rawInput -> inference(
                capabilityId = "sensitive.write",
                scope = ActionScope.SENSITIVE_WRITE,
                matchedSignals = listOf("debug:write_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            "[ui]" in rawInput -> inference(
                capabilityId = "ui.act",
                scope = ActionScope.UI_ACT,
                matchedSignals = listOf("debug:ui_marker"),
                containsSensitiveContent = false,
                containsBlockedOperation = false,
                confidence = 0.99,
            )

            else -> null
        }
    }

    private fun buildMatchText(userInput: String): MatchText {
        val raw = userInput.lowercase()
        val spaced = raw.replace(Regex("""[^\p{L}\p{N}]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return MatchText(
            raw = raw,
            spaced = spaced,
            tokens = spaced.split(' ').filter { it.isNotBlank() }.toSet(),
        )
    }

    private fun collectMatches(
        matchText: MatchText,
        rules: Map<String, List<String>>,
    ): List<String> {
        return rules.mapNotNull { (signal, phrases) ->
            signal.takeIf { phrases.any { phrase -> containsPhrase(matchText, phrase) } }
        }
    }

    private fun containsPhrase(
        matchText: MatchText,
        phrase: String,
    ): Boolean {
        val normalizedPhrase = phrase.lowercase().trim()
        return when {
            normalizedPhrase.isBlank() -> false
            normalizedPhrase.any { it.isWhitespace() } -> {
                normalizedPhrase in matchText.raw || normalizedPhrase in matchText.spaced
            }
            normalizedPhrase.all { it.isLetterOrDigit() } -> {
                normalizedPhrase in matchText.raw || normalizedPhrase in matchText.tokens
            }
            else -> normalizedPhrase in matchText.raw
        }
    }

    private fun looksLikeCalendarScheduling(rawInput: String): Boolean {
        return listOf(
            Regex("""\b(add|schedule|book|set up)\b.+\b(to my calendar|calendar|meeting|event)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(remind me)\b.+\b(today|tomorrow|tonight|next)\b""", RegexOption.IGNORE_CASE),
            Regex("""(加入日历|安排.+(会议|日程)|创建.+(会议|日程)|提醒我.+(今天|明天|下午|晚上))"""),
        ).any { it.containsMatchIn(rawInput) }
    }

    private fun looksLikeCalendarRead(matchText: MatchText): Boolean {
        val raw = matchText.raw
        val mentionsCalendar = listOf(
            "calendar",
            "schedule",
            "agenda",
            "日历",
            "日程",
            "安排",
        ).any(raw::contains)
        val mentionsLookup = listOf(
            "what",
            "show",
            "check",
            "read",
            "look up",
            "list",
            "查看",
            "看看",
            "查",
            "查下",
            "查一下",
            "看下",
            "看一下",
            "有什么",
        ).any(raw::contains)
        val mentionsWindow = listOf(
            "today",
            "tomorrow",
            "this week",
            "tonight",
            "afternoon",
            "今天",
            "明天",
            "本周",
            "今晚",
            "下午",
        ).any(raw::contains)
        val hasMutationVerb = listOf(
            "add",
            "create",
            "schedule",
            "book",
            "set up",
            "remove",
            "delete",
            "cancel",
            "安排",
            "添加",
            "创建",
            "加入日历",
            "删除",
            "删掉",
            "取消",
            "改期",
            "重新安排",
        ).any(raw::contains)
        return mentionsCalendar && !hasMutationVerb && (mentionsLookup || mentionsWindow)
    }

    private fun looksLikeCalendarDeletion(matchText: MatchText): Boolean {
        val raw = matchText.raw
        val deleteVerb = listOf(
            "delete",
            "remove",
            "cancel",
            "删掉",
            "删除",
            "取消",
        ).any(raw::contains)
        val calendarContext = listOf(
            "calendar",
            "event",
            "meeting",
            "schedule",
            "日历",
            "日程",
            "事件",
            "会议",
        ).any(raw::contains)
        return deleteVerb && calendarContext
    }

    private fun inference(
        capabilityId: String,
        scope: ActionScope,
        matchedSignals: List<String>,
        containsSensitiveContent: Boolean,
        containsBlockedOperation: Boolean,
        confidence: Double,
    ): RuntimeIntentInference {
        return RuntimeIntentInference(
            capabilityId = capabilityId,
            scope = scope,
            matchedSignals = matchedSignals.distinct(),
            containsSensitiveContent = containsSensitiveContent,
            containsBlockedOperation = containsBlockedOperation,
            confidence = confidence,
        )
    }
}
