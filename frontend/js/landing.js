/**
 * UniAssist AI - Landing Page Logic
 * Interactive query preview box demonstrating live NLP/FAQ retrieval.
 */

document.addEventListener("DOMContentLoaded", () => {
    const demoForm = document.getElementById("landing-demo-form");
    const demoInput = document.getElementById("demo-query-input");
    const demoChatBox = document.getElementById("demo-chat-box");

    if (!demoForm || !demoInput || !demoChatBox) return;

    demoForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const query = demoInput.value.trim();
        if (!query) return;

        // Append student query bubble
        const studentDiv = document.createElement("div");
        studentDiv.className = "d-flex gap-2 justify-content-end";
        studentDiv.innerHTML = `
            <div class="chat-bubble student-bubble py-2 px-3 small">${escapeHtml(query)}</div>
            <span class="chat-avatar student-av" style="width:28px;height:28px;font-size:0.75rem;">S</span>
        `;
        demoChatBox.appendChild(studentDiv);
        demoInput.value = "";

        // Append temporary typing indicator
        const typingDiv = document.createElement("div");
        typingDiv.className = "d-flex gap-2";
        typingDiv.id = "demo-typing";
        typingDiv.innerHTML = `
            <span class="chat-avatar assistant-av" style="width:28px;height:28px;font-size:0.75rem;">AI</span>
            <div class="chat-bubble assistant-bubble py-2 px-3 small">
                <span class="typing-dots">
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                </span>
            </div>
        `;
        demoChatBox.appendChild(typingDiv);
        demoChatBox.scrollTop = demoChatBox.scrollHeight;

        try {
            // Attempt query via API or search fallback
            let responseText = "";
            let intent = "general_query";
            let confidence = "82.5%";
            let source = "University Handbook";

            // If user is authenticated, use full chat, otherwise search FAQs
            if (API.isAuthenticated()) {
                const res = await API.sendMessage(query);
                responseText = res.reply;
                intent = res.intent;
                confidence = Math.round(res.confidence * 100) + "%";
                source = res.source;
            } else {
                const faqs = await API.searchFaqs(query);
                if (faqs && faqs.length > 0) {
                    responseText = faqs[0].answer;
                    intent = faqs[0].category;
                    confidence = "90.0%";
                    source = "FAQ #" + faqs[0].id + " (" + faqs[0].category + ")";
                } else {
                    responseText = "To get full personalized AI chatbot assistance and view your saved chat history, please sign in or register for free.";
                    intent = "registration_prompt";
                    confidence = "100%";
                    source = "UniAssist System";
                }
            }

            typingDiv.remove();

            const aiDiv = document.createElement("div");
            aiDiv.className = "d-flex gap-2";
            aiDiv.innerHTML = `
                <span class="chat-avatar assistant-av" style="width:28px;height:28px;font-size:0.75rem;">AI</span>
                <div class="chat-bubble assistant-bubble py-2 px-3 small">
                    ${escapeHtml(responseText)}
                    <div class="chat-bubble-meta mt-2 pt-1 border-top">
                        <span class="pill-intent">${intent}</span>
                        <span class="pill-score">Conf: ${confidence}</span>
                        <span class="pill-source">${source}</span>
                    </div>
                </div>
            `;
            demoChatBox.appendChild(aiDiv);
            demoChatBox.scrollTop = demoChatBox.scrollHeight;

        } catch (err) {
            typingDiv.remove();
            const errDiv = document.createElement("div");
            errDiv.className = "d-flex gap-2";
            errDiv.innerHTML = `
                <span class="chat-avatar assistant-av" style="width:28px;height:28px;font-size:0.75rem;">AI</span>
                <div class="chat-bubble assistant-bubble py-2 px-3 small text-danger">
                    Please ensure the backend server is running on port 8080.
                </div>
            `;
            demoChatBox.appendChild(errDiv);
        }
    });
});

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}
