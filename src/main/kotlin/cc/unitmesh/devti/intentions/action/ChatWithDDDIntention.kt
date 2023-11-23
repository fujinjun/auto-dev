package cc.unitmesh.devti.intentions.action

class ChatWithDDDIntention : NewChatWithCodeIntention() {

    override fun getText() = "DDD Suggestion";
    override fun getPrePrompt() = "Give me some suggestions to make this method match the DDD design:"

    override fun filterSelectedText(selectedText: String): String {
        if (selectedText.startsWith("/*")) {
            return selectedText.substring(selectedText.indexOf("*/") + 2)
        }
        return super.filterSelectedText(selectedText)
    }
}