/***
 *
 * MainActivity.java
 *
 * Copyright 2016 Aldo Pedro Rangel Montiel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.crimsonrgames.titanium.htmltester

import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), AddTagDialogFragment.OnAddTagDialogFragmentInteractionListener {
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null
    /**
     * The EditorFragment that will be used to edit the source code
     */
    private var mEditorFragment: EditorFragment? = null
    /**
     * The PreviewFragment that will be used to show the WebView with the contents of the source
     * code
     */
    private var mPreviewFragment: PreviewFragment? = null

    private var mSourceCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mSourceCode = readHTMLFromDefaultFile()
        if (mEditorFragment == null) {
            mEditorFragment = EditorFragment.newInstance(mSourceCode)
        }
        if (mPreviewFragment == null) {
            mPreviewFragment = PreviewFragment.newInstance(mSourceCode)
        }
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mEditorFragment = mSectionsPagerAdapter!!.getRegisteredFragment(0) as EditorFragment
                if (mEditorFragment == null) return
                mSourceCode = mEditorFragment!!.getmSourceCode()
                mPreviewFragment = mSectionsPagerAdapter!!.getRegisteredFragment(1) as PreviewFragment
                if (mPreviewFragment == null) return
                mPreviewFragment!!.setmSourceCode(mSourceCode)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            mEditorFragment = mSectionsPagerAdapter!!.getRegisteredFragment(0) as EditorFragment
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, mEditorFragment!!.getmSourceCode())
            sendIntent.type = "text/html"
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
        }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        mEditorFragment = mSectionsPagerAdapter!!.getRegisteredFragment(0) as EditorFragment
        mPreviewFragment = mSectionsPagerAdapter!!.getRegisteredFragment(1) as PreviewFragment
        when (id) {
            R.id.action_save -> {
                mEditorFragment?.let{
                    mSourceCode = it.getmSourceCode()
                    this.writeHTMLToDefaultFile(mSourceCode!!)
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this.applicationContext, SAVED, duration)
                    toast.show()
                }
            }
            R.id.action_restore_default -> {
                mSourceCode = DEFAULT_HTML_STRING
                mEditorFragment?.let { it.setmSourceCode(mSourceCode)}
                mPreviewFragment?.let{ it.setmSourceCode(mSourceCode)}
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this.applicationContext, RESTORED_TO_DEFAULT_TEXT, duration)
                toast.show()
            }
            R.id.action_return_to_saved -> {
                mSourceCode = this.readHTMLFromDefaultFile()
                mEditorFragment!!.setmSourceCode(mSourceCode)
                mPreviewFragment!!.setmSourceCode(mSourceCode)
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this.applicationContext, RESTORED_TO_LAST_SAVED_FILE, duration)
                toast.show()
            }
            R.id.action_insert_tag -> showAddTagDialog()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun showAddTagDialog() {
        val input = EditText(this)
        input.setSingleLine()

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.title_add_tag)
                .setMessage(R.string.tag_dialog_text)
                .setPositiveButton(R.string.action_insert_Tag){ dialogInterface, i ->
                    mEditorFragment?.let {
                        val tag = input.text.toString()
                        it.insertTextAtCursorPoint(String.format(TAG_PATTERN, tag, tag))
                    }
                }
                .setNegativeButton(R.string.action_cancel){ dialogInterface, i ->
                    dialogInterface.cancel()

                }
                .create()
        dialog.setView(input,20,0,20,0)
        dialog.show()
    }


    private fun writeHTMLToDefaultFile(HTMLString: String): Boolean {

        val file = File(getExternalFilesDir(null), DEFAULT_FILE_NAME)
        try {
            val stream = FileOutputStream(file)
            try {
                stream.write(HTMLString.toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, "Error: " + e.localizedMessage)

            } finally {
                stream.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error: " + e.localizedMessage)

        }

        return true
    }

    private fun readHTMLFromDefaultFile(): String {
        var fileContents: String
        val file = File(getExternalFilesDir(null), DEFAULT_FILE_NAME)
        val length = file.length().toInt()

        val bytes = ByteArray(length)
        try {
            val `in` = FileInputStream(file)
            try {
                `in`.read(bytes)
                fileContents = String(bytes)
            } catch (e: IOException) {
                fileContents = DEFAULT_HTML_STRING
                Log.e(TAG, "Error: " + e.localizedMessage)
            } finally {
                `in`.close()
            }
        } catch (e: IOException) {
            fileContents = DEFAULT_HTML_STRING
            Log.e(TAG, "Error: " + e.localizedMessage)
        }

        Log.d(TAG, fileContents)
        if (fileContents.isEmpty()) fileContents = DEFAULT_HTML_STRING
        return fileContents
    }

    /***
     * Overriden from AddTagDialogFragment.OnAddTagDialogFragmentInteractionListener
     * Receives the input from the AddTagDialogFragment
     * @param tag the input tag
     */
    override fun onFinishTagEditDialog(tag: String) {

        mEditorFragment = mSectionsPagerAdapter!!.getRegisteredFragment(0) as EditorFragment
        mEditorFragment!!.insertTextAtCursorPoint(String.format(TAG_PATTERN, tag, tag))

    }


    /**
     * The Editor Fragment which contains the EditText element that we'll use as the source editor
     */
    class EditorFragment : Fragment() {
        /**
         * The EditText element reference
         */
        private var mEditor: EditText? = null

        /**
         * Convenience storage of the html code
         */
        private var mSourceCode: String? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            mEditor = rootView.findViewById<View>(R.id.editText) as EditText
            setTextToEditor()
            return rootView
        }

        /**
         * Inserts text at the current cursor point of the EditText
         * @param textToAdd the text to add
         * @return if the operation was successful or not
         */
        fun insertTextAtCursorPoint(textToAdd: String): Boolean {
            var success = false
            if (mEditor != null) {
                mEditor!!.text.insert(mEditor!!.selectionStart, textToAdd)
                success = true
            }
            return success
        }

        fun setTextToEditor() {
            if (mEditor != null) {
                mEditor!!.setText(mSourceCode)
            }
        }

        //Getters

        fun getmSourceCode(): String {
            if (mEditor != null) {
                return mEditor!!.text.toString()
            }
            return ""

        }

        //Setters

        fun setmSourceCode(mSourceCode: String?) {
            this.mSourceCode = mSourceCode
            setTextToEditor()
        }

        companion object {

            /**
             * Returns a new instance of this fragment with the initializing the mEditor
             * with the initial Code
             * @param initialCode The code which will initialize the mEditor
             */
            fun newInstance(initialCode: String?): EditorFragment {
                val fragment = EditorFragment()
                fragment.mSourceCode = initialCode
                return fragment
            }
        }
    }

    /**
     * The Preview Fragment which contains the WebView element used to test the HTML
     */
    class PreviewFragment : Fragment() {
        /**
         * The WebView element reference
         */
        private var mWebView: WebView? = null

        /**
         * Convenience storage of the html code
         */
        private var mSourceCode: String? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_html_preview, container, false)
            mWebView = rootView.findViewById<View>(R.id.webView) as WebView
            return rootView
        }

        override fun setUserVisibleHint(visible: Boolean) {
            super.setUserVisibleHint(visible)
            if (visible && isResumed) {
                //Manually call onResume so it loads the HTMLStringAgain
                onResume()
            }
        }

        override fun onResume() {
            super.onResume()
            if (!userVisibleHint || mWebView == null) {
                return
            }
            loadHTMLInWebView()
        }

        private fun loadHTMLInWebView() {
            if (mWebView != null) {
                val settings = mWebView!!.settings
                settings.defaultTextEncodingName = "utf-8"
                mWebView!!.loadData(mSourceCode, "text/html; charset=utf-8", "UTF-8")
            }
        }

        //Getters

        fun getmSourceCode(): String? {
            return mSourceCode
        }

        //Setters

        fun setmSourceCode(mSourceCode: String?) {
            this.mSourceCode = mSourceCode
            loadHTMLInWebView()
        }

        companion object {

            /**
             * Returns a new instance of this fragment with the initializing the mEditor
             * with the initial Code
             * @param initialCode The code which will initialize the mEditor
             */
            fun newInstance(initialCode: String?): PreviewFragment {
                val fragment = PreviewFragment()
                fragment.mSourceCode = initialCode
                return fragment
            }
        }
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        internal var registeredFragments = SparseArray<Fragment>()

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a EditorFragment (defined as a static inner class below).
            if (mEditorFragment == null) {
                mEditorFragment = EditorFragment.newInstance(mSourceCode)
            }
            if (mPreviewFragment == null) {
                mPreviewFragment = PreviewFragment.newInstance(mSourceCode)
            }
            return if (position == 0) mEditorFragment!! else mPreviewFragment!!
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        }

        fun getRegisteredFragment(position: Int): Fragment {
            return registeredFragments.get(position)
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "EDITOR"
                1 -> return "PREVIEW"
            }
            return null
        }
    }

    companion object {


        val TAG = "MainActivity"
        val SAVED = "Saved"
        val RESTORED_TO_DEFAULT_TEXT = "Restored to default text"
        val RESTORED_TO_LAST_SAVED_FILE = "Restored to last saved file"
        val TAG_PATTERN = "<%s> </%s>"
        private val DEFAULT_HTML_STRING = "<html><body><h1>TODO: Write your own code here!!</h1></body></html>"

        private val DEFAULT_FILE_NAME = "test.html"
    }
}
