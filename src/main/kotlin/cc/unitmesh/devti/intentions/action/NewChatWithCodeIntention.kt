package cc.unitmesh.devti.intentions.action

import cc.unitmesh.devti.AutoDevBundle
import com.intellij.temporary.getElementToAction
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.sendToChatWindow
import cc.unitmesh.devti.intentions.action.base.AbstractChatIntention
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

open class NewChatWithCodeIntention : AbstractChatIntention() {
    override fun priority(): Int = 999

    var title: String = ""
    override fun getText() = title
    override fun getFamilyName(): String = AutoDevBundle.message("intentions.chat.new.family.name")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        this.title = computeTitle(project, file, getCurrentSelectionAsRange(editor))
        return true
    }

    open fun getPrePrompt(): String {
        return ""
    }

    open fun filterSelectedText(selectedText:String) :String{
        return  selectedText;
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return

        var selectedText = editor.selectionModel.selectedText
        val elementToExplain = getElementToAction(project, editor)

        if (selectedText == null) {
            if (elementToExplain == null) return

            selectElement(elementToExplain, editor)
            selectedText = editor.selectionModel.selectedText
        }

        if (selectedText == null) return

        val language = file.language.displayName

        selectedText = filterSelectedText(selectedText)

        sendToChatWindow(project, ChatActionType.CHAT) { contentPanel, _ ->
            contentPanel.setInput(getPrePrompt() + "\n```$language\n$selectedText\n```")
        }
    }

}