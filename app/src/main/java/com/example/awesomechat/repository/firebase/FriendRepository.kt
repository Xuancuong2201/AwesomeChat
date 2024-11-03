package com.example.awesomechat.repository.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.Friend
import com.example.awesomechat.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FriendRepository @Inject constructor(private val auth: FirebaseAuth, private val db: FirebaseFirestore) : InteractFriend {
    override val emailCurrent: String = auth.currentUser?.email.toString()

    override suspend fun getStateFriend(email: String, state: String): List<User> {
        return withContext(Dispatchers.IO) {
            val friendList: MutableList<User> = mutableListOf()
            try {
                val friendQuerySnapshot = db.collection("Friend").whereEqualTo("state", state)
                    .where(
                        Filter.or(
                            Filter.equalTo("sideA", email),
                            Filter.equalTo("sideB", email)
                        )
                    ).get().await()
                if (friendQuerySnapshot.isEmpty) return@withContext friendList
                val emailList: MutableList<String> = mutableListOf()
                friendQuerySnapshot.documents.mapNotNull { document ->
                    if (document.getString("sideB").equals(email)) {
                        emailList.add(document.getString("sideA").toString())
                    } else
                        emailList.add(document.getString("sideB").toString())
                }

                for (it in emailList) {
                    val informationQuerySnapshot =
                        db.collection("User").whereEqualTo("email", it).get().await()
                    if (informationQuerySnapshot.isEmpty) continue
                    val user = informationQuerySnapshot.first().toObject(User::class.java)
                    friendList.add(user)
                }
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            return@withContext friendList
        }
    }

    override suspend fun getRequestFriend(email: String, state: String): List<User> {
        return withContext(Dispatchers.IO) {
            val requestList: MutableList<User> = mutableListOf()
            try {
                val requestQuerySnapshot = db.collection("Friend").whereEqualTo("sideA", email)
                    .whereEqualTo("state", state).get().await()
                if (requestQuerySnapshot.isEmpty) return@withContext requestList
                val emailList: List<String> =
                    requestQuerySnapshot.documents.mapNotNull { document ->
                        document.getString("sideB")
                    }
                for (it in emailList) {
                    val informationQuerySnapshot =
                        db.collection("User").whereEqualTo("email", it).get().await()
                    if (informationQuerySnapshot.isEmpty) continue
                    val user = informationQuerySnapshot.first().toObject(User::class.java)
                    requestList.add(user)
                }
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            return@withContext requestList
        }
    }

    override suspend fun getInvitationFriend(email: String, state: String): List<User> {
        return withContext(Dispatchers.IO) {
            val invitationList: MutableList<User> = mutableListOf()
            try {
                val invitationQuerySnapshot = db.collection("Friend").whereEqualTo("sideB", email)
                    .whereEqualTo("state", state).get().await()
                if (invitationQuerySnapshot.isEmpty) return@withContext invitationList
                val emailList: List<String> =
                    invitationQuerySnapshot.documents.mapNotNull { document ->
                        document.getString("sideA")
                    }
                for (it in emailList) {
                    val informationQuerySnapshot =
                        db.collection("User").whereEqualTo("email", it).get().await()
                    if (informationQuerySnapshot.isEmpty) continue
                    val user = informationQuerySnapshot.first().toObject(User::class.java)
                    invitationList.add(user)
                }
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            return@withContext invitationList
        }
    }

    override suspend fun getAllUser(email: String): MutableList<User> {
        return withContext(Dispatchers.IO) {
            var remainingFriendList: MutableList<User> = mutableListOf()
            try {
                val jobCurrentFriend = async {
                    val result = getStateFriend(email, "friend")
                    result.forEach { it.state = "friend" }
                    return@async result
                }
                val jobRequestFriend = async {
                    val result = getRequestFriend(email, "request")
                    result.forEach { it.state = "request" }
                    return@async result
                }
                val jobInvitation = async {
                    val result = getInvitationFriend(email, "request")
                    result.forEach { it.state = "invitation" }
                    return@async result
                }
                val jobAllUser = async {
                    val allUserList: MutableList<User> = mutableListOf()
                    val queryAllEmail =
                        db.collection("User").get().await()
                    if (queryAllEmail.isEmpty) {
                        null
                    } else {
                        val emailList: List<String> =
                            queryAllEmail.documents.mapNotNull { document -> document.getString("email") }
                        for (it in emailList) {
                            val informationQuerySnapshot =
                                db.collection("User").whereEqualTo("email", it).get().await()
                            if (informationQuerySnapshot.isEmpty) continue

                            val user = informationQuerySnapshot.first().toObject(User::class.java)
                            allUserList.add(user)
                        }
                        allUserList.removeIf { user -> user.email == email }
                        return@async allUserList
                    }
                }
                val sumListHaveState =
                    (jobRequestFriend.await() + jobCurrentFriend.await() + jobInvitation.await()).toMutableList()
                val allUserList = jobAllUser.await()?.toMutableList()
                val filterList = allUserList?.filter { user1 ->
                    user1.email !in sumListHaveState.map { user2 -> user2.email }
                }
                filterList?.forEach { it.state = "none" }
                if (filterList != null) {
                    remainingFriendList =
                        (filterList + jobCurrentFriend.await() + jobRequestFriend.await()).toMutableList()
                }
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            return@withContext remainingFriendList
        }
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
                    val documentSnapshot = it.result.documents[0]
                    val documentId = documentSnapshot.id
                    db.collection("Friend").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }
            }
        }
    }

    override suspend fun getQuantityRequestFriend(): Int {
        return try {
            val documentSnapshot = db.collection("Friend")
                .whereEqualTo("state", "request")
                .whereEqualTo("sideB", emailCurrent)
                .get()
                .await()
            documentSnapshot.size()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class FriendModule{
    @Binds
    abstract fun bindInteractFriend(friendRepository: FriendRepository): InteractFriend
}