package cc.unitmesh.devti.intentions.action

import cc.unitmesh.devti.AutoDevBundle
import cc.unitmesh.devti.custom.document.CustomDocumentationConfig
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.sendToChatWindow
import cc.unitmesh.devti.intentions.action.base.AbstractChatIntention
import cc.unitmesh.devti.intentions.action.base.BasedDocumentationIntention
import cc.unitmesh.devti.provider.LivingDocumentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.temporary.getElementToAction

class RefactorMethodIntention: AbstractChatIntention() {
    override fun getText(): String = AutoDevBundle.message("intentions.chat.code.refactor.name")

    override fun getFamilyName(): String = AutoDevBundle.message("intentions.chat.code.refactor.family.name")
    override fun priority() = 999

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        return LivingDocumentation.forLanguage(file.language) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val psiElement = getElementToAction(project, editor)?:return
        selectElement(psiElement, editor!!)
        val selectedText = editor.selectionModel.selectedText

        val language = file!!.language.displayName
        sendToChatWindow(project, ChatActionType.CHAT) { contentPanel, _ ->
            contentPanel.setInput("give me some suggestion for refactor this method:\n```$language\n$selectedText\n```")
        }
    }


}