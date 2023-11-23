package cc.unitmesh.devti.llms.qwen

import cc.unitmesh.devti.llms.LLMProvider
import cc.unitmesh.devti.llms.openai.OpenAIProvider
import cc.unitmesh.devti.settings.AutoDevSettingsState
import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationResult
import com.alibaba.dashscope.aigc.generation.models.QwenParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.MessageManager
import com.alibaba.dashscope.exception.ApiException
import com.alibaba.dashscope.exception.InputRequiredException
import com.alibaba.dashscope.exception.NoApiKeyException
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.SSE
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.time.Duration


@Service(Service.Level.PROJECT)
class QwenLLMProvider(val project: Project) : LLMProvider {
    private val autoDevSettingsState = AutoDevSettingsState.getInstance()

    private val key get() = autoDevSettingsState.customEngineToken
    private var msgManager = MessageManager(10)
    private val gen = Generation()
    private val maxTokenLength: Int
        get() = AutoDevSettingsState.getInstance().fetchMaxTokenLength()

    private var historyMessageLength: Int = 0

    override fun prompt(promptText: String): String {
        val result = callWithMessage(promptText);


        return result?.output?.choices!![0].message.content
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun stream(promptText: String, systemPrompt: String, keepHistory: Boolean): Flow<String> {
        if (!keepHistory) {
            clearMessage()
        }
        return callbackFlow {
            withContext(Dispatchers.IO) {
                streamCallWithMessage(promptText)
                        ?.doOnError{ error ->
                            trySend(error.message ?: "Error occurs")
                        }
                        ?.blockingForEach { response ->
                            if (response.output.choices.isNotEmpty()) {
                                val completion = response.output.choices[0].message
                                if (completion != null && completion.content != null) {
                                    trySend(completion.content)
                                }
                            }
                        }

                close()
            }
        }

    }

    @Throws(NoApiKeyException::class, ApiException::class, InputRequiredException::class)
    fun callWithMessage(promptText: String): GenerationResult? {

        historyMessageLength += promptText.length
        if (historyMessageLength > maxTokenLength) {
            msgManager = MessageManager(10)
        }

        val msg = Message()
        msg.role = "user"
        msg.content = promptText
        msgManager.add(msg)
        val param: QwenParam = QwenParam.builder().model(Generation.Models.QWEN_MAX)
                .apiKey(key)
                .messages(msgManager.get())
                .resultFormat(QwenParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .enableSearch(true)
                .build()
        return gen.call(param)
    }
    @Throws(NoApiKeyException::class, ApiException::class, InputRequiredException::class)
    fun streamCallWithMessage(promptText: String): Flowable<GenerationResult>? {


        historyMessageLength += promptText.length
        if (historyMessageLength > maxTokenLength) {
            msgManager.get().clear()
        }
        val msg = Message()
        msg.role = "user"
        msg.content = promptText
        msgManager.add(msg)
        val param: QwenParam = QwenParam.builder().model(Generation.Models.QWEN_MAX)
                .apiKey(key)
                .messages(msgManager.get())
                .resultFormat(QwenParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .incrementalOutput(true)
                .enableSearch(true)
                .build()
        return gen.streamCall(param)
    }
}