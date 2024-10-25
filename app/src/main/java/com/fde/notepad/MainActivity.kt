package com.fde.notepad

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fde.notepad.menu.ItemMenu
import com.fde.notepad.menu.ItemMenuAdapter
import com.fde.notepad.provider.FileUtils
import com.fde.notepad.provider.Utils
import com.fde.notepad.provider.Utils.Companion.EDIT_VISIBLE
import com.fde.notepad.provider.Utils.Companion.FILE_VISIBLE
import com.fde.notepad.view.VerticalDragScrollBar
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter


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
    private lateinit var mFilePopupWindow: PopupWindow
    private lateinit var mEditPopupWindow: PopupWindow
    private lateinit var mFileRecyclerView: RecyclerView
    private lateinit var mEditRecyclerView: RecyclerView
    private var mUri: Uri? = null
    private var mDocPath: String? = null

    private lateinit var mContentTxt: EditText
    private lateinit var content: String
    private lateinit var mVerticalDragScrollBar: VerticalDragScrollBar

    private lateinit var openLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var saveLauncher: ActivityResultLauncher<String>

    private var mTitle = "no title"
    private var lastHeight = 0
    private var nowVisible = 0
    private var isChanged = false
    private var shouldFinish = false
    private var shouldClear = false

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


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFileMenuTitleTxt = findViewById(R.id.fileMenuTitleTxt)
        mEditMenuTitleTxt = findViewById(R.id.editMenuTitleTxt)
        mContentTxt = findViewById(R.id.content)
        mVerticalDragScrollBar = findViewById(R.id.verticalTouchableScrollBar)
        mVerticalDragScrollBar.scrollListener = scrollListener

        init()
        bindScrollToText()
        registerLauncher()
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


    var mLength = 0
    private fun registerLauncher() {
        openLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.apply {
                mUri = uri
                contentResolver.openInputStream(this)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val text = reader.readText()
                        mContentTxt.setText(text)
                    }
                    mLength = mContentTxt.text.toString().toByteArray().size
                    saveTitle(Utils.getTitle(uri.path))
//                    Log.w(TAG, "uri = $mUri")
//                    val filePath = uri.path
//                    Log.w(TAG, "uri.path = ${uri.path}")
//                    Log.w(TAG, "fileName = ${filePath?.substring(filePath.lastIndexOf("/") + 1)}")
//                    val fileName = filePath?.substring(filePath.lastIndexOf("/") + 1)
//                    title = fileName
                }
            }
        }

        saveLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    mUri = uri
                    isChanged = false
                    saveTitle(Utils.getTitle(uri.path))
//                    Log.w(TAG, "mUri = $mUri")
                    tryClear()
                    tryFinish()
//                    Log.w(TAG, "uri = $uri")
//                    Log.w(TAG, "path = ${uri.path}")
//                    if (shouldClear) {
//                        mUri = null
//                        mContentTxt.text.clear()
//                    } else if (shouldFinish) {
//                        finish()
//                    }
                }
            }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mContentTxt.post {
            if (mContentTxt.layout != null) {
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
        mContentTxt.selectAll()
//        mContentTxt.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                // 确保 layout 不为 null
//                mContentTxt.layout?.let { layout ->
//                    mVerticalDragScrollBar.updateData(
//                        mContentTxt.scrollY,
//                        mContentTxt.width,
//                        mContentTxt.height,
//                        layout.height
//                    )
//                    mVerticalDragScrollBar.invalidate()
//                    lastHeight = layout.height
//                }
//                // 移除监听器，避免重复调用
//                mContentTxt.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        handler.removeCallbacks(runnable)
    }

    fun bindScrollToText() {
        mContentTxt.addTextChangedListener {
            isChanged = true
            title = "*$mTitle"
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
        saveTitle(getString(R.string.untitled))
        mDocPath = intent.getStringExtra("docPath")
        mUri = intent.data
//        Log.w(TAG, "mDocPath = $mDocPath")
//        Log.w(TAG, "mUri = $mUri")
        if (mDocPath != null) {
            mContentTxt.setText(FileUtils.readTextFromPath(mDocPath))
            saveTitle(mDocPath!!)
        } else if (mUri != null) {
            mContentTxt.setText(FileUtils.readTextFromUri(mUri, this))
//            saveTitle(mUri.path)
        }
        val fileView = LayoutInflater.from(this).inflate(R.layout.menu_layout, null, false)
        val editView = LayoutInflater.from(this).inflate(R.layout.menu_layout, null, false)
        mFileRecyclerView = fileView.findViewById(R.id.recyclerView)
        mEditRecyclerView = editView.findViewById(R.id.recyclerView)
        mFileRecyclerView.layoutManager = LinearLayoutManager(this)
        mFileRecyclerView.adapter =
            ItemMenuAdapter(
                arrayListOf(
                    ItemMenu(
                        getString(R.string._new),
                        getString(R.string.hotkey_new)
                    ) {
                        mFilePopupWindow.dismiss()
                        new()
                    },
                    ItemMenu(
                        getString(R.string.new_window),
                        getString(R.string.hotkey_new_window)
                    ) {
                        mFilePopupWindow.dismiss()
                        newWindow()
                    },
                    ItemMenu(getString(R.string.open), getString(R.string.hotkey_open)) {
                        mFilePopupWindow.dismiss()
                        open()
                    },
                    ItemMenu(getString(R.string.save), getString(R.string.hotkey_save)) {
                        lifecycleScope.launch {
                            save()
                        }
                        mFilePopupWindow.dismiss()
                    },
                    ItemMenu(getString(R.string.exit)) {
                        mFilePopupWindow.dismiss()
                        exit()
                    })
            )
        mEditRecyclerView.layoutManager = LinearLayoutManager(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        mEditRecyclerView.adapter = ItemMenuAdapter(
            arrayListOf(
                ItemMenu(getString(R.string.undo), getString(R.string.hotkey_undo)) {
                    mEditPopupWindow.dismiss()
                },
                ItemMenu(getString(R.string.cut), getString(R.string.hotkey_cut)) {
                    mEditPopupWindow.dismiss()
                },
                ItemMenu(getString(R.string.copy), getString(R.string.hotkey_copy)) {
                    mEditPopupWindow.dismiss()
                    val editableText = mContentTxt.text
                    var start = mContentTxt.selectionStart
                    var end = mContentTxt.selectionEnd
                    if (start > end) {
                        start = start + end
                        end = start - end
                        start = start - end
                    }
                    val charSequence = editableText.subSequence(start, end)
                    val clip = ClipData.newPlainText("Note", charSequence)
                    clipboard.setPrimaryClip(clip)
                },
                ItemMenu(getString(R.string.paste), getString(R.string.hotkey_paste)) {
                    mEditPopupWindow.dismiss()
                    val clip = clipboard.primaryClip as ClipData
                    if (clip.itemCount > 0) {
                        val s = clip.getItemAt(0).coerceToText(this).toString()
                        val editableText = mContentTxt.text
                        var start = mContentTxt.selectionStart
                        var end = mContentTxt.selectionEnd
                        if (start > end) {
                            start = start + end
                            end = start - end
                            start = start - end
                        }
                        editableText.replace(start, end, s)
                    }
                },
                ItemMenu(getString(R.string.delete), getString(R.string.hotkey_delete)) {
                    mEditPopupWindow.dismiss()
                },
                ItemMenu(getString(R.string.select_all), getString(R.string.hotkey_select_all)) {
                    mEditPopupWindow.dismiss()
                    mContentTxt.setSelection(0, mContentTxt.text.length)
                }
            )
        )

        mFileRecyclerView.setOnClickListener {
            mFilePopupWindow.dismiss()
        }

        mFilePopupWindow = PopupWindow(
            fileView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )

        mEditPopupWindow = PopupWindow(
            editView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )
        // Set the PopupWindow to be able to respond to touch events.
        // This means that when the user touches the content area of the PopupWindow, these touch events will be handled by the PopupWindow.
        mFilePopupWindow.isTouchable = true
        // Sets the PopupWindow not to get focus so that the underlying view can receive events.
        mFilePopupWindow.isFocusable = false
        // Setting the PopupWindow external area to be clickable
        mFilePopupWindow.isOutsideTouchable = true
        // mFilePopupWindow?.setBackgroundDrawable(null)
        mFilePopupWindow.setOnDismissListener {
            setInvisible(mFileMenuTitleTxt, mFilePopupWindow, FILE_VISIBLE)
        }

        mEditPopupWindow.isTouchable = true
        mEditPopupWindow.isFocusable = false
        mEditPopupWindow.isOutsideTouchable = true
        mEditPopupWindow.setOnDismissListener {
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

    fun open() {
        openLauncher.launch(arrayOf("text/plain"))
    }

    fun save() {
        Log.w(TAG, "save()")
        if (!isChanged) return
        content = mContentTxt.text.toString()
        Log.w(TAG, "content = $content")
        if (mUri == null && mDocPath == null) {
            saveLauncher.launch("$mTitle.txt")
        } else {
            try {
//                var outputStream: OutputStream? = null
                if (mUri != null) {
//                    contentResolver.openOutputStream(mUri!!)?.use { outputStream ->
//                        OutputStreamWriter(outputStream).use { writer ->
//                            writer.write("") // 清空文件内容
//                        }
//                        outputStream.flush()
//                    }
//                    Log.w(TAG, "mLength = $mLength")
//                    Log.w(TAG, "mUri != null")
//                    Log.w(TAG, "mUri = $mUri")
                    FileUtils.clearFileContent(mUri, mLength, this)
                    FileUtils.writeTextToUri(mUri, content, this)
//                    outputStream = contentResolver.openOutputStream(mUri!!)
                }
                else {
//                    Log.w(TAG, "mDocPath = $mDocPath")
//                    FileUtils.clearFileContent()
                    FileUtils.writeTextToPath(mDocPath, content, this)
//                    outputStream = FileOutputStream(mDocPath, false)
                }
//                Log.w(TAG, "uri = $mUri")
//                Log.w(TAG, "mDocPath = $mDocPath")
//                OutputStreamWriter(outputStream).use { writer ->
//                    writer.write(content)
//                }
//                outputStream?.flush()
//                outputStream?.close()
                isChanged = false
                title = mTitle
                tryClear()
                tryFinish()
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun new() {
        shouldClear = true
        if (isChanged) {
            createDialog()
        }
        saveTitle(getString(R.string.untitled))
    }

    private fun createDialog() {
        val message = getString(R.string.dialog_message, mTitle)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(message)
            .setPositiveButton(getString(R.string.discard)) { _, _ ->
                tryClear()
                tryFinish()
            }
            .setNegativeButton(getString(R.string.save)) { _, _ ->
                save()
            }
            .setNeutralButton(getString(R.string.cancel), null).create().show()
    }

    fun newWindow() {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        when (keyCode) {
            KeyEvent.KEYCODE_S -> {
                if (event?.isCtrlPressed == true) save()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun exit() {
        if (!isChanged) finish()
        shouldFinish = true
        createDialog()
    }

    fun tryClear() {
        if (shouldClear) {
            mUri = null
            mContentTxt.text.clear()
            shouldClear = false
        }
    }

    fun tryFinish() {
        if (shouldFinish) {
            finish()
            shouldFinish = false
        }
    }

    fun saveTitle(title: String) {
        mTitle = title
        this.title = title
    }

    fun onWindowDismissed(finishTask: Boolean, suppressWindowTransition: Boolean) = exit()

    private fun showFilePopupWindow() = mFilePopupWindow.showAsDropDown(mFileMenuTitleTxt)

    private fun showEditPopupWindow() = mEditPopupWindow.showAsDropDown(mEditMenuTitleTxt)

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

    companion object {
        private val TAG = "MainActivity"

        interface ScrollListener {
            fun scrollXBy(x: Int)
            fun scrollYBy(y: Int)
        }
    }
}