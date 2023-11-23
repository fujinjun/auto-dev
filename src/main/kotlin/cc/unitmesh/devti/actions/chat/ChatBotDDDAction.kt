package cc.unitmesh.devti.actions.chat

import cc.unitmesh.devti.actions.chat.base.ChatBaseAction
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.chat.ChatContext
import cc.unitmesh.devti.gui.sendToChatPanel
import cc.unitmesh.devti.provider.ContextPrompter
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiMethod
import com.intellij.temporary.getElementToAction

class ChatBotDDDAction : ChatBaseAction() {

    override fun getActionType(): ChatActionType {
        return ChatActionType.DDD

    }

    override fun executeAction(event: AnActionEvent) {
        val project = event.project ?: return
        val document = event.getData(CommonDataKeys.EDITOR)?.document
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val prompter = ContextPrompter.prompter(file?.language?.displayName ?: "")
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val element = getElementToAction(project, editor) ?: return
        if (element !is PsiMethod) {
            return
        }
        var selectedText = element.text.trim()
        selectedText = if (selectedText.startsWith("/*")) selectedText.substring(selectedText.indexOf("*/") + 2) else selectedText
        prompter.initContext(getActionType(), selectedText, file, project, 0, element)

        sendToChatPanel(project) { panel, service ->
            val chatContext = ChatContext(
                    getReplaceableAction(event),
                    "",
                    ""
            )

            service.handlePromptAndResponse(panel, prompter, chatContext)
        }
    }

}
