package cc.unitmesh.devti.intentions.action

import cc.unitmesh.devti.AutoDevBundle
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.sendToChatWindow
import cc.unitmesh.devti.intentions.action.base.AbstractChatIntention
import cc.unitmesh.devti.provider.LivingDocumentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.temporary.getElementToAction
import io.ktor.util.reflect.*

class GeneratePumlIntention : AbstractChatIntention() {
    override fun priority(): Int = 999

    override fun getText() = AutoDevBundle.message("intentions.chat.design.puml.name")
    override fun getFamilyName(): String = AutoDevBundle.message("intentions.chat.design.puml.family.name")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        return LivingDocumentation.forLanguage(file.language) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return


        val classPsi = file.children.filterIsInstance<PsiClass>()[0] as PsiClass
        var selectedText = "public class " + classPsi.name + "{"
        classPsi.children.filter { it is PsiField||it is PsiMethod }.forEach {
            selectedText += if (it is PsiField){
                "\n" + it.text
            }else{
                val method = it as PsiMethod
                method.text.substring(0,method.text.indexOf("{")) + "{}"
            }
        }
        selectedText += "}"

        val language = file.language.displayName

        sendToChatWindow(project, ChatActionType.CHAT) { contentPanel, _ ->
            contentPanel.setInput("Generate Puml for this class :\n```$language\n$selectedText\n```")
        }
    }

}