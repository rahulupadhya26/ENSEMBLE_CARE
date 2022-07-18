package com.app.selfcare.fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*

import android.content.Intent

import android.content.DialogInterface
import android.database.Cursor
import android.provider.MediaStore
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import android.annotation.SuppressLint
import com.app.selfcare.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_journal.*
import java.io.ByteArrayOutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateJournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateJournalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var createdJournalDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_create_journal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        val myFormat = "dd-MMM-yyyy HH:mm:ss" // mention the format you need
        val sdf = SimpleDateFormat(myFormat)
        val cal = Calendar.getInstance()
        val formattedDate = sdf.format(cal.time)
        createdJournalDate = formattedDate
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                txtJournalDate.setText(sdf.format(cal.time).substring(0, 11))
                createdJournalDate = sdf.format(cal.time)
            }

        txtJournalDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnSaveJournal.setOnClickListener {
            if (getText(txtJournalDate).isNotEmpty()) {
                if (edit_txt_journal_title.text.toString().trim().isNotEmpty()) {
                    if (edit_txt_journal.text.toString().trim().isNotEmpty()) {
                        //Call create journal api
                        popBackStack()
                    } else {
                        setEditTextError(edit_txt_journal, "Description cannot be empty!")
                    }
                } else {
                    setEditTextError(edit_txt_journal_title, "Title cannot be empty!")
                }
            } else {
                setEditTextError(txtJournalDate, "Date cannot be empty!")
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateJournalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateJournalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_create_journal"
    }
}