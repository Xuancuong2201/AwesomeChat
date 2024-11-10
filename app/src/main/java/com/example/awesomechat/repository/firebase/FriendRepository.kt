package com.example.awesomechat.repository.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.model.Friend
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
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
        val docRef = db.collection("Friend").whereEqualTo("state","friend").where(Filter.or(
            Filter.equalTo("sideA", emailCurrent),
            Filter.equalTo("sideB", emailCurrent)))
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull {
                if (it.getString("sideA").equals(emailCurrent))
                        it.getString("sideB")
                    else
                        it.getString("sideA")
                }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val friendList = db.collection("User")
                        .whereIn("email", emailList)
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
        val docRef = db.collection("Friend").whereEqualTo("sideA", emailCurrent)
            .whereEqualTo("state", "request")
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull { it.getString("sideB") }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val userQuerySnapshot = db.collection("User")
                        .whereIn("email", emailList)
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
        val docRef = db.collection("Friend").whereEqualTo("sideB", emailCurrent)
            .whereEqualTo("state", "request")
        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val emailList = snapshot?.documents?.mapNotNull { it.getString("sideA") }
            if (!emailList.isNullOrEmpty()) {
                launch {
                    val userQuerySnapshot = db.collection("User")
                        .whereIn("email", emailList)
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
        val docRef = db.collection("Friend")
            .where(Filter.or(
                    Filter.equalTo("sideA", emailCurrent),
                    Filter.equalTo("sideB", emailCurrent)))

        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            var emailList = snapshot?.documents?.mapNotNull {
                if (it.getString("sideA").equals(emailCurrent))
                    it.getString("sideB")
                else
                    it.getString("sideA")
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val listenerUser =
                        db.collection("User").addSnapshotListener { snapshotUser, eUser ->
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
                    awaitClose { listenerUser.remove() }
                } catch (e: Exception) {
                    Log.w("Firestore", "Error fetching messages", e)
                }
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendRequestFriend(sideB: String) {
        return withContext(Dispatchers.IO) {
            val friend = Friend(emailCurrent, "request", sideB)
            db.collection("Friend")
                .add(friend)
        }
    }

    override suspend fun refuseInvitationFriend(sideA: String) {
        val document = db.collection("Friend")
            .whereEqualTo("sideA", sideA)
            .whereEqualTo("state", "request")
            .whereEqualTo("sideB", emailCurrent).get()
        document.addOnCompleteListener {
            if (it.isSuccessful && !it.result.isEmpty) {
                val documentSnapshot = it.result.documents[0]
                val documentId = documentSnapshot.id
                db.collection("Friend").document(documentId)
                    .delete()
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            }
        }
    }

    override suspend fun acceptInvitationFriend(sideA: String) {
        return withContext(Dispatchers.IO) {
            val document = db.collection("Friend")
                .whereEqualTo("sideA", sideA)
                .whereEqualTo("state", "request")
                .whereEqualTo("sideB", emailCurrent).get()
            document.addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documentSnapshot = it.result.documents[0]
                    val documentId = documentSnapshot.id
                    db.collection("Friend").document(documentId)
                        .update("state", "friend")
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
            val document = db.collection("Friend")
                .whereEqualTo("sideA", emailCurrent)
                .whereEqualTo("state", "request")
                .whereEqualTo("sideB", sideB).get()
            document.addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documentId = it.result.documents[0].id
                    Log.w("documentID", documentId)
                    db.collection("Friend").document(documentId)
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

@Module
@InstallIn(ViewModelComponent::class)
abstract class FriendModule {
    @Binds
    abstract fun bindInteractFriend(friendRepository: FriendRepository): InteractFriend
}