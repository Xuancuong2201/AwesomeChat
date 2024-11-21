package com.example.awesomechat.repository.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.model.Friend
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FriendRepository @Inject constructor(auth: FirebaseAuth, private val db: FirebaseFirestore) :
    InteractFriend {
    override val emailCurrent: String = auth.currentUser?.email.toString()
    override suspend fun getStateFriend(): Flow<List<User>> = callbackFlow {
        val docRef = db.collection(InfoFieldQuery.COLLECTION_FRIEND).whereEqualTo(InfoFieldQuery.KEY_STATE,InfoFieldQuery.KEY_FRIEND).where(Filter.or(
            Filter.equalTo(InfoFieldQuery.KEY_SIDE_A, emailCurrent),
            Filter.equalTo(InfoFieldQuery.KEY_SIDE_B, emailCurrent)))
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull {
                if (it.getString(InfoFieldQuery.KEY_SIDE_A).equals(emailCurrent))
                        it.getString(InfoFieldQuery.KEY_SIDE_B)
                    else
                        it.getString(InfoFieldQuery.KEY_SIDE_A)
                }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val friendList = db.collection(InfoFieldQuery.COLLECTION_USER)
                        .whereIn(InfoFieldQuery.KEY_EMAIL, emailList)
                        .get()
                        .await().mapNotNull { item -> item.toObject(User::class.java) }
                    trySend(friendList).isSuccess

                }
            } else {
                trySend(emptyList()).isFailure
            }
        }
        awaitClose { listenerRegistration.remove() }
    }
    override suspend fun getRequestFriend(): Flow<List<User>> = callbackFlow {
        val docRef = db.collection(InfoFieldQuery.COLLECTION_FRIEND).whereEqualTo(InfoFieldQuery.KEY_SIDE_A, emailCurrent)
            .whereEqualTo(InfoFieldQuery.KEY_STATE, InfoFieldQuery.STATE_REQUEST)
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull { it.getString(InfoFieldQuery.KEY_SIDE_B) }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val userQuerySnapshot = db.collection(InfoFieldQuery.COLLECTION_USER)
                        .whereIn(InfoFieldQuery.KEY_EMAIL, emailList)
                        .get()
                        .await().mapNotNull { item -> item.toObject(User::class.java) }
                    trySend(userQuerySnapshot).isSuccess
                }
            } else
                trySend(emptyList()).isFailure
        }
        awaitClose { listenerRegistration.remove() }
        close()
    }

    override suspend fun getInvitationFriend(): Flow<List<User>> = callbackFlow {
        val docRef = db.collection(InfoFieldQuery.COLLECTION_FRIEND).whereEqualTo(InfoFieldQuery.KEY_SIDE_B, emailCurrent)
            .whereEqualTo(InfoFieldQuery.KEY_STATE, InfoFieldQuery.STATE_REQUEST)
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull { it.getString(InfoFieldQuery.KEY_SIDE_A) }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val userQuerySnapshot = db.collection(InfoFieldQuery.COLLECTION_USER)
                        .whereIn(InfoFieldQuery.KEY_EMAIL, emailList)
                        .get()
                        .await().mapNotNull { item -> item.toObject(User::class.java) }
                    trySend(userQuerySnapshot).isSuccess
                }
            } else
                trySend(emptyList()).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getRemainUser(): Flow<List<User>> = callbackFlow {
        val docRef = db.collection(InfoFieldQuery.COLLECTION_FRIEND)
            .where(Filter.or(
                    Filter.equalTo(InfoFieldQuery.KEY_SIDE_A, emailCurrent),
                    Filter.equalTo(InfoFieldQuery.KEY_SIDE_B, emailCurrent)))

        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            var emailList = snapshot?.documents?.mapNotNull {
                if (it.getString(InfoFieldQuery.KEY_SIDE_A).equals(emailCurrent))
                    it.getString(InfoFieldQuery.KEY_SIDE_B)
                else
                    it.getString(InfoFieldQuery.KEY_SIDE_A)
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                        db.collection(InfoFieldQuery.COLLECTION_USER).addSnapshotListener { snapshotUser, eUser ->
                            if (eUser != null) {
                                close(eUser)
                            }
                            val user =
                                snapshotUser?.documents?.mapNotNull { user -> user.toObject(User::class.java) }
                            if (user.isNullOrEmpty()) {
                                trySend(emptyList()).isSuccess
                            } else {
                                if (!emailList.isNullOrEmpty()) {
                                    emailList = emailList!! + emailCurrent
                                    val userNew = user.filter { it.email !in emailList!! }
                                    trySend(userNew).isSuccess
                                } else{
                                    val userNew = user.filter { it.email !in emailCurrent }
                                    trySend(userNew).isSuccess
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.w("Error", "Error fetching messages", e)
                }
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendRequestFriend(sideB: String) {
        return withContext(Dispatchers.IO) {
            val friend = Friend(emailCurrent, InfoFieldQuery.STATE_REQUEST, sideB)
            db.collection(InfoFieldQuery.COLLECTION_FRIEND)
                .add(friend)
        }
    }

    override suspend fun refuseInvitationFriend(sideA: String) {
        val document = db.collection(InfoFieldQuery.COLLECTION_FRIEND)
            .whereEqualTo(InfoFieldQuery.KEY_SIDE_A, sideA)
            .whereEqualTo(InfoFieldQuery.KEY_STATE, InfoFieldQuery.STATE_REQUEST)
            .whereEqualTo(InfoFieldQuery.KEY_SIDE_B, emailCurrent).get()
        document.addOnCompleteListener {
            if (it.isSuccessful && !it.result.isEmpty) {
                val documentSnapshot = it.result.documents[0]
                val documentId = documentSnapshot.id
                db.collection(InfoFieldQuery.COLLECTION_FRIEND).document(documentId)
                    .delete()
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            }
        }
    }

    override suspend fun acceptInvitationFriend(sideA: String) {
        return withContext(Dispatchers.IO) {
            val document = db.collection(InfoFieldQuery.COLLECTION_FRIEND)
                .whereEqualTo(InfoFieldQuery.KEY_SIDE_A, sideA)
                .whereEqualTo(InfoFieldQuery.KEY_STATE, InfoFieldQuery.STATE_REQUEST)
                .whereEqualTo(InfoFieldQuery.KEY_SIDE_B, emailCurrent).get()
            document.addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documentSnapshot = it.result.documents[0]
                    val documentId = documentSnapshot.id
                    db.collection(InfoFieldQuery.COLLECTION_FRIEND).document(documentId)
                        .update(InfoFieldQuery.KEY_STATE, InfoFieldQuery.KEY_FRIEND)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully update!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }
            }
        }
    }

    override suspend fun cancelRequestFriend(sideB: String) {
        return withContext(Dispatchers.IO) {
            val document = db.collection(InfoFieldQuery.COLLECTION_FRIEND)
                .whereEqualTo(InfoFieldQuery.KEY_SIDE_A, emailCurrent)
                .whereEqualTo(InfoFieldQuery.KEY_STATE, InfoFieldQuery.STATE_REQUEST)
                .whereEqualTo(InfoFieldQuery.KEY_SIDE_B, sideB).get()
            document.addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documentId = it.result.documents[0].id
                    db.collection(InfoFieldQuery.COLLECTION_FRIEND).document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }
            }
        }
    }
}
