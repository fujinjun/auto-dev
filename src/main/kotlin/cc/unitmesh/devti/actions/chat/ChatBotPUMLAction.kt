package cc.unitmesh.devti.actions.chat

import cc.unitmesh.devti.actions.chat.base.ChatBaseAction
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.chat.ChatContext
import cc.unitmesh.devti.gui.sendToChatPanel
import cc.unitmesh.devti.provider.ContextPrompter
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.temporary.getElementToAction
import org.jetbrains.uast.util.isInstanceOf

class ChatBotPUMLAction : ChatBaseAction() {

    override fun getActionType(): ChatActionType {
        return ChatActionType.PUML

    }

    override fun executeAction(event: AnActionEvent) {
        val project = event.project ?: return
        val document = event.getData(CommonDataKeys.EDITOR)?.document
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val prompter = ContextPrompter.prompter(file?.language?.displayName ?: "")
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val element = getElementToAction(project, editor) ?: return
        prompter.initContext(getActionType(), getClassStructureText(file), file, project, 0, element)
        sendToChatPanel(project) { panel, service ->
            val chatContext = ChatContext(
                    getReplaceableAction(event),
                    "",
                    ""
            )

            service.handlePromptAndResponse(panel, prompter, chatContext)
        }
    }

    private fun getClassStructureText(file: PsiFile?): String {
        val classElement = file!!.children.filterIsInstance(PsiClass::class.java).first()
        var content = "class ${classElement.name} {\n"
        classElement.allFields.forEach { content += it.text + "\n" }
        classElement.allMethods.filter { it !is ClsMethodImpl }.forEach { content += (it.returnTypeElement?.text ?: "void") + " ${it.name} (){}\n" }
        content += "}"
        return content;
    }
}
