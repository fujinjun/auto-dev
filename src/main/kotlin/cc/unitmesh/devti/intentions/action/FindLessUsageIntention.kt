package cc.unitmesh.devti.intentions.action

import cc.unitmesh.devti.AutoDevBundle
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.sendToChatWindow
import cc.unitmesh.devti.intentions.action.base.AbstractChatIntention
import cc.unitmesh.devti.provider.LivingDocumentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import java.nio.file.Path

class FindLessUsageIntention : AbstractChatIntention() {
    override fun priority(): Int = 999

    override fun getText() = AutoDevBundle.message("intentions.chat.code.usage.name")
    override fun getFamilyName(): String = AutoDevBundle.message("intentions.chat.code.usage.family.name")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        return LivingDocumentation.forLanguage(file.language) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val elementFactory = JavaPsiFacade.getElementFactory(project)
        val psiClasses = file.children.filterIsInstance<PsiClass>();
        val path = file.virtualFile.path
        val module = path.substring(0,path.indexOf("/src/main/java"))+"/src/main/java"

        val children = PsiManager.getInstance(project)
                .findDirectory(VirtualFileManager.getInstance().findFileByNioPath(Path.of(module))!!)!!.children;
        val arr = mutableListOf<String>()
        searchAll(children,arr);

        sendToChatWindow(project, ChatActionType.CHAT) { contentPanel, _ ->
            contentPanel.setInput("less usage class is :\n" + arr.joinToString(","))
        }
    }

    private fun searchAll(children: Array<PsiElement>, arr: MutableList<String>) {
        children.forEach {
            if (it is PsiDirectory){
                searchAll(it.children, arr)
            }else{
                if (it is PsiFile){
                    if (it.name.endsWith("Handler.java") || it.name.endsWith("Impl.java")){
                        return;
                    }
                    for (psiClass in it.children.filterIsInstance<PsiClass>()) {

                        val search = ReferencesSearch.search(psiClass)
                        if (search.count() < 3){
                            arr.add(it.name)
                        }
                    }

                }else{
                    println("none")
                }
            }
        }
    }

}