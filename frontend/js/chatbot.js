/**
 * UniAssist AI - Chatbot Client Implementation
 * Handles conversation sessions, message turns, intent attribution,
 * prompt chips, typing animations, feedback, and copy actions.
 */

let currentConversationId = null;
let feedbackModalInstance = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!API.requireAuth()) return;

    feedbackModalInstance = new bootstrap.Modal(document.getElementById("feedbackModal"));

    // DOM Elements
    const chatForm = document.getElementById("chat-input-form");
    const chatInput = document.getElementById("chat-input-text");
    const newChatBtn = document.getElementById("btn-new-chat");
    const clearChatBtn = document.getElementById("btn-clear-chat");
    const sidebarToggleBtn = document.getElementById("sidebar-toggle-btn");
    const sidebar = document.getElementById("chat-sidebar");
    const promptChipsContainer = document.getElementById("prompt-chips-container");
    const submitFeedbackBtn = document.getElementById("btn-submit-feedback");

    // Load initial list of conversations
    await loadConversations();

    // Check URL parameters (e.g. ?conv=123 or ?ask=Attendance)
    const urlParams = new URLSearchParams(window.location.search);
    const convParam = urlParams.get("conv");
    const askParam = urlParams.get("ask");

    if (convParam) {
        await switchConversation(parseInt(convParam, 10));
    }

    if (askParam) {
        chatInput.value = askParam;
        submitStudentMessage(askParam);
    }

    // New Conversation Button
    if (newChatBtn) {
        newChatBtn.addEventListener("click", () => {
            currentConversationId = null;
            document.getElementById("active-chat-title").textContent = "New Conversation";
            resetChatMessagesToWelcome();
            updateSidebarActiveItem();
        });
    }

    // Clear Current Messages Button
    if (clearChatBtn) {
        clearChatBtn.addEventListener("click", () => {
            if (confirm("Clear messages on screen?")) {
                resetChatMessagesToWelcome();
            }
        });
    }

    // Mobile Sidebar Toggle
    if (sidebarToggleBtn && sidebar) {
        sidebarToggleBtn.addEventListener("click", () => {
            sidebar.classList.toggle("open");
        });
    }

    // Prompt Chips Click Delegation
    if (promptChipsContainer) {
        promptChipsContainer.addEventListener("click", (e) => {
            const chip = e.target.closest(".prompt-chip");
            if (chip) {
                const prompt = chip.getAttribute("data-prompt") || chip.textContent.trim();
                chatInput.value = prompt;
                submitStudentMessage(prompt);
            }
        });
    }

    // Form Submission
    if (chatForm) {
        chatForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const message = chatInput.value.trim();
            if (!message) return;
            submitStudentMessage(message);
        });
    }

    // Submit Feedback Modal Action
    if (submitFeedbackBtn) {
        submitFeedbackBtn.addEventListener("click", async () => {
            const messageId = document.getElementById("feedback-message-id").value;
            const rating = document.getElementById("feedback-rating").value;
            const comments = document.getElementById("feedback-comments").value;

            try {
                await API.sendFeedback(messageId ? parseInt(messageId, 10) : null, rating, comments);
                API.showToast("Feedback submitted successfully. Thank you!", "success");
                feedbackModalInstance.hide();
                document.getElementById("feedback-comments").value = "";
            } catch (err) {
                API.showToast("Failed to record feedback.", "error");
            }
        });
    }
});

/**
 * Loads conversation sessions from backend and renders sidebar items.
 */
async function loadConversations() {
    const listContainer = document.getElementById("conversation-history-list");
    const countBadge = document.getElementById("session-count-badge");

    try {
        const conversations = await API.getConversations();
        if (countBadge) countBadge.textContent = conversations.length;

        if (!conversations || conversations.length === 0) {
            listContainer.innerHTML = `
                <div class="text-center py-4 text-muted small">
                    <i class="bi bi-chat-dots d-block fs-3 mb-1 text-muted" style="opacity:0.5;"></i>
                    No saved chats yet.<br>Ask a question below!
                </div>
            `;
            return;
        }

        listContainer.innerHTML = conversations.map(c => `
            <div class="conversation-item ${currentConversationId === c.id ? 'active' : ''}" data-conv-id="${c.id}">
                <div class="d-flex align-items-center gap-2 text-truncate flex-grow-1" onclick="switchConversation(${c.id})">
                    <i class="bi bi-chat-left-text"></i>
                    <span class="text-truncate">${escapeHtml(c.title)}</span>
                </div>
                <button type="button" class="del-btn" onclick="deleteConversation(${c.id}, event)" title="Delete session">
                    <i class="bi bi-x-lg" style="font-size:0.75rem;"></i>
                </button>
            </div>
        `).join("");

    } catch (err) {
        listContainer.innerHTML = `
            <div class="text-center py-3 text-danger small">
                Failed to load history
            </div>
        `;
    }
}

/**
 * Switches the active conversation transcript.
 */
async function switchConversation(conversationId) {
    currentConversationId = conversationId;
    updateSidebarActiveItem();

    const container = document.getElementById("chat-messages-container");
    container.innerHTML = `
        <div class="text-center py-5 text-muted small">
            <div class="spinner-border spinner-border-sm text-primary mb-2"></div>
            <div>Loading messages...</div>
        </div>
    `;

    try {
        const messages = await API.getConversationMessages(conversationId);
        container.innerHTML = "";

        if (messages.length === 0) {
            resetChatMessagesToWelcome();
            return;
        }

        // Set header title to conversation subject
        const firstStudentMsg = messages.find(m => m.senderType === "STUDENT");
        if (firstStudentMsg) {
            document.getElementById("active-chat-title").textContent = firstStudentMsg.messageText.substring(0, 35) + "...";
        }

        messages.forEach(msg => {
            if (msg.senderType === "STUDENT") {
                appendStudentBubble(msg.messageText, false);
            } else {
                appendAssistantBubble({
                    id: msg.id,
                    reply: msg.messageText,
                    intent: msg.predictedIntent,
                    confidence: msg.modelScore,
                    source: msg.sourceReference,
                    fallback: msg.fallbackUsed,
                    suggestedFollowups: []
                }, false);
            }
        });

        scrollToBottom();

    } catch (err) {
        API.showToast("Could not retrieve conversation history.", "error");
        resetChatMessagesToWelcome();
    }
}

/**
 * Deletes a conversation session.
 */
async function deleteConversation(conversationId, event) {
    if (event) event.stopPropagation();

    if (!confirm("Are you sure you want to delete this chat session?")) return;

    try {
        await API.deleteConversation(conversationId);
        API.showToast("Conversation deleted.", "info");

        if (currentConversationId === conversationId) {
            currentConversationId = null;
            resetChatMessagesToWelcome();
        }

        await loadConversations();
    } catch (err) {
        API.showToast("Failed to delete conversation.", "error");
    }
}

/**
 * Submits student query and manages assistant reply stream.
 */
async function submitStudentMessage(messageText) {
    const chatInput = document.getElementById("chat-input-text");
    const sendBtn = document.getElementById("chat-send-btn");
    chatInput.value = "";

    // 1. Render student bubble
    appendStudentBubble(messageText, true);

    // 2. Render typing indicator
    const typingId = appendTypingIndicator();
    scrollToBottom();

    // Disable input while processing
    chatInput.disabled = true;
    sendBtn.disabled = true;

    try {
        const response = await API.sendMessage(messageText, currentConversationId);

        // Update active conversation ID
        if (response.conversationId) {
            currentConversationId = response.conversationId;
        }

        // Remove typing indicator
        removeTypingIndicator(typingId);

        // 3. Render Assistant Bubble
        appendAssistantBubble({
            id: response.messageId,
            reply: response.reply,
            intent: response.intent,
            confidence: response.confidence,
            source: response.source,
            fallback: response.fallback,
            suggestedFollowups: response.suggestedFollowups || []
        }, true);

        // Refresh conversation sidebar
        loadConversations();

    } catch (err) {
        removeTypingIndicator(typingId);
        appendErrorBubble(err.message || "Failed to communicate with assistant. Please try again.");
    } finally {
        chatInput.disabled = false;
        sendBtn.disabled = false;
        chatInput.focus();
        scrollToBottom();
    }
}

function appendStudentBubble(text, animate = false) {
    const container = document.getElementById("chat-messages-container");
    const user = API.getCurrentUser();
    const initial = user && user.fullName ? user.fullName.charAt(0) : "S";

    const bubbleWrapper = document.createElement("div");
    bubbleWrapper.className = "chat-bubble-container student";
    bubbleWrapper.innerHTML = `
        <span class="chat-avatar student-av">${initial}</span>
        <div>
            <div class="chat-bubble student-bubble">${escapeHtml(text)}</div>
        </div>
    `;
    container.appendChild(bubbleWrapper);
}

function appendAssistantBubble(data, animate = false) {
    const container = document.getElementById("chat-messages-container");

    const confScore = data.confidence ? Math.round(data.confidence * 100) + "%" : "85%";
    const intentLabel = data.intent || "general_support";
    const sourceLabel = data.source || "University Support Ordinance";

    const bubbleWrapper = document.createElement("div");
    bubbleWrapper.className = "chat-bubble-container assistant";

    let followupsHtml = "";
    if (data.suggestedFollowups && data.suggestedFollowups.length > 0) {
        followupsHtml = `
            <div class="mt-3 pt-2 border-top">
                <span class="text-muted d-block small mb-1" style="font-size:0.75rem;"><i class="bi bi-question-circle me-1"></i>Related Questions:</span>
                <div class="d-flex flex-wrap gap-1">
                    ${data.suggestedFollowups.map(q => `
                        <button type="button" class="btn btn-sm btn-outline-secondary py-0 px-2 rounded-pill small" style="font-size:0.75rem;" onclick="askFollowup('${escapeAttr(q)}')">
                            ${escapeHtml(q)}
                        </button>
                    `).join("")}
                </div>
            </div>
        `;
    }

    bubbleWrapper.innerHTML = `
        <span class="chat-avatar assistant-av">AI</span>
        <div style="max-width: 100%;">
            <div class="chat-bubble assistant-bubble">
                <div class="chat-bubble-text">${formatResponseMarkdown(data.reply)}</div>
                
                ${data.fallback ? `
                    <div class="alert alert-warning py-1 px-2 mt-2 mb-1 small d-flex align-items-center gap-2" style="font-size:0.75rem;">
                        <i class="bi bi-shield-exclamation"></i>
                        <span>This query had lower confidence. Please confirm policy details with your department office.</span>
                    </div>
                ` : ''}

                <div class="chat-bubble-meta">
                    <span class="pill-intent" title="Predicted Intent Class">${intentLabel}</span>
                    <span class="pill-score" title="Calibrated Confidence">${confScore}</span>
                    <span class="pill-source" title="Grounding Source"><i class="bi bi-link-45deg"></i> ${escapeHtml(sourceLabel)}</span>
                    <div class="chat-actions">
                        <button type="button" class="chat-action-btn" onclick="copyBubbleText(this)" title="Copy answer">
                            <i class="bi bi-copy"></i>
                        </button>
                        <button type="button" class="chat-action-btn" onclick="openFeedback(${data.id || 'null'}, 'HELPFUL')" title="Helpful answer">
                            <i class="bi bi-hand-thumbs-up"></i>
                        </button>
                        <button type="button" class="chat-action-btn" onclick="openFeedback(${data.id || 'null'}, 'NOT_HELPFUL')" title="Report issue">
                            <i class="bi bi-hand-thumbs-down"></i>
                        </button>
                    </div>
                </div>

                ${followupsHtml}
            </div>
        </div>
    `;
    container.appendChild(bubbleWrapper);
}

function appendTypingIndicator() {
    const container = document.getElementById("chat-messages-container");
    const id = "typing-" + Date.now();

    const typingWrapper = document.createElement("div");
    typingWrapper.className = "chat-bubble-container assistant";
    typingWrapper.id = id;
    typingWrapper.innerHTML = `
        <span class="chat-avatar assistant-av">AI</span>
        <div>
            <div class="chat-bubble assistant-bubble py-2 px-3">
                <span class="typing-dots">
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                </span>
            </div>
        </div>
    `;
    container.appendChild(typingWrapper);
    return id;
}

function removeTypingIndicator(id) {
    const element = document.getElementById(id);
    if (element) element.remove();
}

function appendErrorBubble(message) {
    const container = document.getElementById("chat-messages-container");
    const bubbleWrapper = document.createElement("div");
    bubbleWrapper.className = "chat-bubble-container assistant";
    bubbleWrapper.innerHTML = `
        <span class="chat-avatar assistant-av" style="background:var(--danger);">!</span>
        <div>
            <div class="chat-bubble assistant-bubble text-danger border-danger">
                <strong>Error:</strong> ${escapeHtml(message)}
            </div>
        </div>
    `;
    container.appendChild(bubbleWrapper);
}

function resetChatMessagesToWelcome() {
    const container = document.getElementById("chat-messages-container");
    container.innerHTML = `
        <div class="chat-bubble-container assistant">
            <span class="chat-avatar assistant-av">AI</span>
            <div>
                <div class="chat-bubble assistant-bubble">
                    Hello! I am <strong>UniAssist AI</strong>, your 24/7 University Student Support Assistant. 
                    <br><br>
                    I can answer queries about <strong>attendance policies, examination schedules, fee payments, merit scholarships, course curricula, library hours, hostel facilities,</strong> and department contacts.
                    <br><br>
                    How can I assist you today?
                    <div class="chat-bubble-meta">
                        <span class="pill-intent">greeting</span>
                        <span class="pill-score">Conf: 99.8%</span>
                        <span class="pill-source">UniAssist Welcome Handbook</span>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function askFollowup(promptText) {
    const chatInput = document.getElementById("chat-input-text");
    chatInput.value = promptText;
    submitStudentMessage(promptText);
}

function copyBubbleText(btn) {
    const bubble = btn.closest(".assistant-bubble");
    const textEl = bubble.querySelector(".chat-bubble-text");
    if (!textEl) return;

    navigator.clipboard.writeText(textEl.innerText).then(() => {
        API.showToast("Response copied to clipboard!", "success");
    }).catch(() => {
        API.showToast("Could not copy text.", "warning");
    });
}

function openFeedback(messageId, rating) {
    document.getElementById("feedback-message-id").value = messageId || "";
    document.getElementById("feedback-rating").value = rating;
    document.getElementById("feedbackModalTitle").textContent = rating === "HELPFUL" ? "Rate Response as Helpful" : "Report Issue with Response";
    feedbackModalInstance.show();
}

function updateSidebarActiveItem() {
    document.querySelectorAll(".conversation-item").forEach(item => {
        const id = parseInt(item.getAttribute("data-conv-id"), 10);
        if (id === currentConversationId) {
            item.classList.add("active");
        } else {
            item.classList.remove("active");
        }
    });
}

function scrollToBottom() {
    const container = document.getElementById("chat-messages-container");
    if (container) {
        container.scrollTop = container.scrollHeight;
    }
}

function formatResponseMarkdown(text) {
    if (!text) return "";
    let html = escapeHtml(text);
    // Bold **text**
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    // Bullet lines starting with - or *
    html = html.replace(/(?:^|\n)[-*]\s+(.+)/g, '<br>• $1');
    // Newlines to breaks
    html = html.replace(/\n/g, '<br>');
    return html;
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}

function escapeAttr(text) {
    return (text || "").replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
