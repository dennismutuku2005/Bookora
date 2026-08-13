package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Message
import com.dennis.bookora.repository.BookRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.dennis.bookora.repository.auth.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val conversationId: String = savedStateHandle["conversationId"] ?: ""

    var messages = mutableStateOf<List<Message>>(emptyList())
        private set

    var isLoading = mutableStateOf(true)
        private set

    var error = mutableStateOf<String?>(null)
        private set

    var otherUserName = mutableStateOf("Chat")
        private set

    var bookTitle = mutableStateOf("")
        private set

    private var listenerRegistration: ListenerRegistration? = null

    init {
        loadConversationInfo()
        listenForMessages()
    }

    private fun loadConversationInfo() {
        viewModelScope.launch {
            try {
                val currentUid = FirebaseAuthManager.currentUser()?.uid ?: ""
                val doc = Firebase.firestore.collection("conversations").document(conversationId).get()
                doc.addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        bookTitle.value = snapshot.getString("bookTitle") ?: ""
                        @Suppress("UNCHECKED_CAST")
                        val names = snapshot.get("participantNames") as? Map<String, String>
                        names?.forEach { (uid, name) ->
                            if (uid != currentUid) {
                                otherUserName.value = name
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun listenForMessages() {
        isLoading.value = true
        val currentUid = FirebaseAuthManager.currentUser()?.uid ?: ""
        
        listenerRegistration = Firebase.firestore
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading.value = false
                if (e != null) {
                    error.value = e.message
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val d = doc.data ?: return@mapNotNull null
                        Message(
                            id = doc.id,
                            conversationId = conversationId,
                            senderId = d["senderId"] as? String ?: "",
                            senderName = d["senderName"] as? String ?: "",
                            text = d["text"] as? String ?: "",
                            timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
                            isMine = (d["senderId"] as? String) == currentUid,
                            read = d["read"] as? Boolean ?: false
                        )
                    }
                    messages.value = list
                }
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repo.sendMessage(conversationId, text.trim())
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
