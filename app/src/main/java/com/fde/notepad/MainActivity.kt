package com.fde.notepad

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fde.notepad.Utils.Companion.EDIT_VISIBLE
import com.fde.notepad.Utils.Companion.FILE_VISIBLE
import com.fde.notepad.view.VerticalDragScrollBar

/* *
* 文件下拉项：新建, 新窗口, 打开, 保存, 另存为 | 退出
* 编辑下拉项：撤销 | 剪切, 复制, 粘贴, 删除 | 全选（模拟按键）
* 下拉项可以使用自定义 View ，通过 List<List> 添加分区和选项
* 下拉项操作：必须点击才展开，再次点击才关闭
* 顶部需要有标题
* 右侧滚动条拖动
* 退出时需要弹窗提示保存，且未命名文本需要提示命名（自定义弹窗）
* 选中时需要改变字体颜色
* */

class MainActivity : AppCompatActivity() {
    private lateinit var mFileMenuTitleTxt: TextView
    private lateinit var mEditMenuTitleTxt: TextView
    private var mFilePopupWindow: PopupWindow? = null
    private var mEditPopupWindow: PopupWindow? = null
    private lateinit var mFileRecyclerView: RecyclerView
    private lateinit var mEditRecyclerView: RecyclerView

    private lateinit var mContentTxt: EditText
    private lateinit var mVerticalDragScrollBar: VerticalDragScrollBar
    private var lastHeight = 0

    //    private val handler = Handler(Looper.getMainLooper())
//    private val runnable = object : Runnable {
//        override fun run() {
//            Log.w(TAG, "run~")
//            Log.w(TAG, "mContentText.height = ${mContentTxt.layout.height}")
//            handler.postDelayed(this, 1000)
//        }
//    }
    private val scrollListener = object : ScrollListener {

        override fun scrollYBy(y: Int) {
            val currentScrollY = mContentTxt.scrollY
            val maxScrollY = mContentTxt.layout.height - mContentTxt.height
            if (maxScrollY <= 0) return
            val newScrollY = (currentScrollY + y).coerceIn(0, maxScrollY)
            mContentTxt.scrollBy(0, newScrollY - currentScrollY)
        }

        override fun scrollXBy(x: Int) {
        }
    }

    private var nowVisible = 0
    private var isChanged = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("no title")
        setContentView(R.layout.activity_main)
        mFileMenuTitleTxt = findViewById(R.id.fileMenuTitleTxt)
        mEditMenuTitleTxt = findViewById(R.id.editMenuTitleTxt)
        mContentTxt = findViewById(R.id.content)
        mVerticalDragScrollBar = findViewById(R.id.verticalTouchableScrollBar)
        mVerticalDragScrollBar.scrollListener = scrollListener

        init()
        bindScrollToText()
//        mFileRecyclerView = findViewById(R.id.fileRecyclerView)
//        mEditRecyclerView = findViewById(R.id.editRecyclerView)
//        mContentTxt.setHorizontallyScrolling(true)

        setOnTouchListener(mFileMenuTitleTxt, FILE_VISIBLE)
        setOnTouchListener(mEditMenuTitleTxt, EDIT_VISIBLE)
        setOnHoverListener(mFileMenuTitleTxt, FILE_VISIBLE)
        setOnHoverListener(mEditMenuTitleTxt, EDIT_VISIBLE)

//        editText.layout.height layout.height 返回的是 EditText 中文本内容的总高度，包括所有行的高度，即使这些行超出了屏幕的可视区域。
//            val layout = mContentTxt.layout
//            val height = layout.height EditText 本身的 getHeight() 方法返回的是当前视图在屏幕上的高度。这不包括超出屏幕之外的部分。
//            val lineCount = layout?.lineCount ?: 0
//            val totalHeight = layout?.getLineTop(lineCount) ?: 0 Layout 还提供了一种方法来计算每一行的高度，进而可以求出 EditText 中所有文本行的总高度： getLineTop(lineCount) 返回的是最后一行的底部位置，这就是文本内容的总高度。
//        mContentTxt.scrollY + mContentTxt.height 表示的是当前竖直方向上滚动的像素 + 页面展示的高度，也就是文本内容的总高度
//        mContentTxt.scrollTo(0, 100)  // 滚动到100像素处


//        mContentTxt.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
//            // 处理滚动事件
//            // scrollX 是当前横向滚动的位置
//            // scrollY 是当前纵向滚动的位置
//            // oldScrollX 是之前横向滚动的位置
//            // oldScrollY 是之前纵向滚动的位置
//            Log.w(TAG, "scrollY = $scrollY")
//        }
//        handler.postDelayed(runnable, 1000)
//        mContentTxt.setOnTouchListener { view, motionEvent ->  }

    }

    override fun onDestroy() {
        super.onDestroy()
//        handler.removeCallbacks(runnable)
    }

    fun bindScrollToText() {
        mContentTxt.addTextChangedListener {
            isChanged = true
            title = "*"
            if (mContentTxt.layout != null && lastHeight != mContentTxt.layout.height) {
                mVerticalDragScrollBar.updateData(
                    mContentTxt.scrollY,
                    mContentTxt.width,
                    mContentTxt.height,
                    mContentTxt.layout.height
                )
                mVerticalDragScrollBar.invalidate()
                lastHeight = mContentTxt.layout.height
            }
        }
        mContentTxt.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            mVerticalDragScrollBar.updateData(
                scrollY, mContentTxt.width, mContentTxt.height, mContentTxt.layout.height
            )
            mVerticalDragScrollBar.invalidate()
        }
        mContentTxt.post {
            mVerticalDragScrollBar.updateData(
                mContentTxt.scrollY,
                mContentTxt.width,
                mContentTxt.height,
                mContentTxt.layout.height
            )
            mVerticalDragScrollBar.invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        val fileView = LayoutInflater.from(this).inflate(R.layout.menu_layout, null, false)
        val editView = LayoutInflater.from(this).inflate(R.layout.menu_layout, null, false)
        mFileRecyclerView = fileView.findViewById(R.id.recyclerView)
        mEditRecyclerView = editView.findViewById(R.id.recyclerView)
        mFileRecyclerView.layoutManager = LinearLayoutManager(this)
        mFileRecyclerView.adapter = ItemMenuAdapter(arrayListOf(ItemMenu("Open"), ItemMenu("Exit")))
        mEditRecyclerView.layoutManager = LinearLayoutManager(this)
        mEditRecyclerView.adapter = ItemMenuAdapter(arrayListOf(ItemMenu("copy")))

        mFileRecyclerView.setOnClickListener {
            mFilePopupWindow?.dismiss()
        }

        mFilePopupWindow = PopupWindow(
            fileView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        mEditPopupWindow = PopupWindow(
            editView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        // Set the PopupWindow to be able to respond to touch events.
        // This means that when the user touches the content area of the PopupWindow, these touch events will be handled by the PopupWindow.
        mFilePopupWindow?.isTouchable = true
        // Sets the PopupWindow not to get focus so that the underlying view can receive events.
        mFilePopupWindow?.isFocusable = false
        // Setting the PopupWindow external area to be clickable
        mFilePopupWindow?.isOutsideTouchable = true
//        mFilePopupWindow?.setBackgroundDrawable(null)
        mFilePopupWindow?.setOnDismissListener {
            setInvisible(mFileMenuTitleTxt, mFilePopupWindow, FILE_VISIBLE)
        }

        mEditPopupWindow?.isTouchable = true
        mEditPopupWindow?.isFocusable = false
        mEditPopupWindow?.isOutsideTouchable = true
        mEditPopupWindow?.setOnDismissListener {
            setInvisible(mEditMenuTitleTxt, mEditPopupWindow, EDIT_VISIBLE)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener(title: View, which: Int) {
        title.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (view.isSelected) setAllInvisible()
                    else setVisible(title, which)
                    false
                }

                else -> false
            }
        }
    }

    private fun setOnHoverListener(title: View, which: Int) {
        title.setOnHoverListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    title.isHovered = true
                    if (!title.isSelected && hasVisible()) {
                        setVisible(title, which)
                        Log.w(TAG, "hasVisible")
                    }
                    false
                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    title.isHovered = false
                    false
                }

                else -> false
            }
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.w(TAG, "dispatchTouchEvent")
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            if (!Utils.isTouchPointInView(mFileMenuTitleTxt, x, y)) {
//                setInvisible(mFileMenuTitleTxt, FILE_VISIBLE)
            }
            if (!Utils.isTouchPointInView(mEditMenuTitleTxt, x, y)) {
//                setInvisible(mEditMenuTitleTxt, EDIT_VISIBLE)
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun hasVisible(): Boolean = nowVisible != Utils.ALL_INVISIBLE
    private fun isWhichVisible(which: Int): Boolean = (nowVisible and which) != Utils.ALL_INVISIBLE

    private fun setVisible(title: View, which: Int) {
        setAllInvisible()
        title.isSelected = true
        when (which) {
            FILE_VISIBLE -> showFilePopupWindow()
            EDIT_VISIBLE -> showEditPopupWindow()
        }
        nowVisible = which
    }

    private fun showFilePopupWindow() {
//        mFilePopupWindow?.isFocusable = true
        mFilePopupWindow?.showAsDropDown(mFileMenuTitleTxt)
    }

    private fun showEditPopupWindow() {
        if (mEditPopupWindow == null) mEditPopupWindow = PopupWindow(
            mEditRecyclerView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
//        mEditPopupWindow?.isFocusable = true
        mEditPopupWindow?.showAsDropDown(mEditMenuTitleTxt)
    }

    private fun setInvisible(title: View, popupWindow: PopupWindow?, which: Int) {
        title.isSelected = false
        popupWindow?.dismiss()
        nowVisible = nowVisible and (Utils.ALL_VISIBLE - which)
    }

    private fun setAllInvisible() {
        if (nowVisible == Utils.ALL_INVISIBLE) return
        if (isWhichVisible(FILE_VISIBLE)) {
            setInvisible(mFileMenuTitleTxt, mFilePopupWindow, FILE_VISIBLE)
        }
        if (isWhichVisible(EDIT_VISIBLE)) {
            setInvisible(mEditMenuTitleTxt, mEditPopupWindow, EDIT_VISIBLE)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    companion object {
        private val TAG = "MainActivity"

        interface ScrollListener {
            fun scrollXBy(x: Int)
            fun scrollYBy(y: Int)
        }
    }
}