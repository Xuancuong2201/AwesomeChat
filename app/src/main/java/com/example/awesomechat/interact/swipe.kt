package com.example.awesomechat.interact

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView

class Swipe( context: Context) : ItemTouchHelper.Callback() {
    private val limitScrollX= dipToPx(context)
    private var currentScrollX=0
    private var currentScrollXWhenInActive =0
    private var initXWhenInActive =0f
    private var firstInActive = false
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int
    {
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(0,swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
    {
        return false
    }
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
    {
        if(actionState== ACTION_STATE_SWIPE){
            if(dX ==0f){
                currentScrollX = viewHolder.itemView.scrollX
                firstInActive = true
            }
            if (isCurrentlyActive){
                var scrollOffset = currentScrollX+(-dX).toInt()
                if( scrollOffset > limitScrollX){
                    scrollOffset=limitScrollX
                }
                else if(scrollOffset<0){
                    scrollOffset=0
                }
                viewHolder.itemView.scrollTo(scrollOffset,0)
            }
            else{
                if(firstInActive){
                    firstInActive = false
                    currentScrollXWhenInActive = viewHolder.itemView.scrollX
                    initXWhenInActive = dX
                }
                if(viewHolder.itemView.scrollX < limitScrollX){
                    viewHolder.itemView.scrollTo((currentScrollXWhenInActive *dX /initXWhenInActive).toInt(),0)
                }
            }

        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if(viewHolder.itemView.scrollX > limitScrollX){
            viewHolder.itemView.scrollTo(limitScrollX,0)
        }
        else if (viewHolder.itemView.scrollX<0){
            viewHolder.itemView.scrollTo(0,0)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return Integer.MAX_VALUE.toFloat()
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Integer.MAX_VALUE.toFloat()
    }
    //Convert to Px
    private fun dipToPx( context: Context):Int {
        return (85f * context.resources.displayMetrics.density).toInt()
    }
}