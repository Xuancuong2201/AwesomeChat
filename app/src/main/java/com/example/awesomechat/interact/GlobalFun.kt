package com.example.awesomechat.interact


import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.awesomechat.R
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class InteractData {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        fun convertToList(users: List<User>): MutableList<Any> {
            if (users.isEmpty()) return mutableListOf()
            else {
                val groupedUsers =
                    users.groupBy { getFirstCharacterBeforeLastSpace(it.name).uppercaseChar() }
                        .toSortedMap()
                val mixedList: MutableList<Any> = mutableListOf<Any>().apply {
                    groupedUsers.forEach { (initial, userList) ->
                        add(initial)
                        addAll(userList)
                    }
                }
                return mixedList
            }
        }

        fun adjustList(list: List<DetailMessage>): List<DetailMessage> {
            for (i in 0..list.size step 1) {
                if (i + 1 < list.size) {
                    list[i].show = list[i + 1].sentby != list[i].sentby
                }
            }
            return list
        }

        fun isValidDateFormat(date: String): Boolean {
            return try {
                val format = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                sdf.parse(date)
                true
            } catch (e: Exception) {
                false
            }

        }

        fun containsNumber(input: String): Boolean {
            val regex = Regex("[0-9]")
            return regex.containsMatchIn(input)
        }

        fun isValidEmail(email: String): Boolean {
            val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
            return emailPattern.matcher(email).matches()
        }

        fun isNumberPhone(phone: String): Boolean {

            return phone.length < 8 || phone.length > 12
        }

        fun isValidPassword(password: String): Boolean {
            return password.isNotEmpty() && password.length > 8 &&
                    password.any { it.isLowerCase() } &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isDigit() } &&
                    password.all { it.isLetterOrDigit() }
        }

        private fun getFirstCharacterBeforeLastSpace(name: String): Char {
            return when (name.lastIndexOf(' ')) {
                -1 -> name[0]
                else -> name[name.lastIndexOf(' ') + 1]
            }
        }
    }
}

@BindingAdapter("getFontFamily")
fun getFortFamily(view: TextView, status: Boolean): String {
    return if (status)
        "@font/light"
    else
        "@font/lato_black"
}

@BindingAdapter("showDate")
fun showDate(view: TextView, date: Date?) {
    val currentTime = Date()
    val timeDifference = currentTime.time - date!!.time
    val differenceInDays = timeDifference / (1000 * 60 * 60)
    if (differenceInDays > 48) {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        view.text = simpleDateFormat.format(date)
    } else if (differenceInDays in 25..47) {
        view.text = view.context.getString(R.string.yesterday)
    } else {
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        view.text = simpleDateFormat.format(date)
    }
}

@BindingAdapter("showDateDetails")
fun showDateDetails(view: TextView, date: Date?) {
    val currentTime = Date()
    val timeDifference = currentTime.time - date!!.time
    val differenceInDays = timeDifference / (1000 * 60 * 60)
    if (differenceInDays > 48) {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        view.text = simpleDateFormat.format(date)
    } else if (differenceInDays in 25..47) {
        view.text = view.context.getString(R.string.yesterday)
    } else {
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        view.text = simpleDateFormat.format(date)
    }
}
@BindingAdapter("isValidInput")
fun setButtonState(view: LinearLayout, isValid: Boolean) {
    val color = if (isValid) {
        ContextCompat.getColor(view.context, R.color.primary_color)
    } else {
        ContextCompat.getColor(view.context, R.color.no_focus)
    }
    view.setBackgroundColor(color)
}

@BindingAdapter("checkMess")
fun checkMess(view: View, type: String) {
    view.visibility = if (type == "mess") View.VISIBLE else View.GONE
}

@BindingAdapter("checkMultiImage")
fun checkMultiImage(view: View, type: String) {
    view.visibility = if (type == "multi image") View.VISIBLE else View.GONE
}

@BindingAdapter("checkImage")
fun checkImage(view: View, type: String) {
    view.visibility = if (type == "image") View.VISIBLE else View.GONE
}


@BindingAdapter("loadImageMessage")
fun loadImageMessage(view: ImageView, message: DetailMessage) {
    if (message.type != "mess") {
        Glide.with(view.context)
            .load(message.content)
            .into(view)
    }
}

@BindingAdapter("imageUrlPerson")
fun loadImagePerson(view: ImageView, url: String?) {
    Glide.with(view.context)
        .load(url)
        .apply(RequestOptions.circleCropTransform())
        .into(view)
}

@BindingAdapter("imageUrlBackground")
fun loadImageBackground(view: ImageView, url: String?) {
    Glide.with(view.context)
        .load(url)
        .into(view)
}

