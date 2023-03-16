package com.example.pdfreaderupload

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pdfreaderupload.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var pdfRef = Firebase.storage.reference
    private val storageRef = Firebase.storage.reference

    private var currentFile: Uri? = null
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        progressDialog = ProgressDialog(this@MainActivity)

        binding!!.fileShow.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "application/pdf"
                pdfLauncher.launch(it)
            }
        }
        binding!!.uploadPDFFile.setOnClickListener {
            if (binding!!.namePDFFile.text.isNotEmpty()) {
                val nameFile = binding!!.namePDFFile.text.toString().trim()
                uploadPDFFileStorage(nameFile)
                progressDialog.setTitle("Upload PDF File")
                progressDialog.setMessage("Loading... ")
                progressDialog.show()

            } else {
                binding!!.namePDFFile.error = "Empty Value!"
            }
        }
        binding!!.btnPDFFileDownload.setOnClickListener {
            if (binding!!.namePDFFileDownload.text.isNotEmpty()){
                val nameDownFile = binding!!.namePDFFileDownload.text.toString().trim()
                val downloadRef = storageRef.child("files/$nameDownFile")
                progressDialog.setTitle("Download PDF File")
                progressDialog.setMessage("Loading... ")
                progressDialog.show()
                val towMEGABYTE: Long = 2048 * 2048
                downloadRef.downloadUrl.addOnSuccessListener {
                    startDownload(it)
                    progressDialog.dismiss()
                    Toast.makeText(this,"Download File, Successfully!",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            } else {
                binding!!.namePDFFileDownload.error = "Empty Value!"
            }

        }
    }
        private var pdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK){
                result?.data?.data?.let {
                    currentFile = it
                    binding!!.fileShow.setImageResource(R.drawable.baseline_picture_as_pdf_24)
                }
            }else{
                Toast.makeText(this,"Canceled!",Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadPDFFileStorage(filename: String){
        try {
            currentFile?.let {
                pdfRef.child("files/${filename}").putFile(it).addOnSuccessListener {
                    Toast.makeText(this,"Upload File, Successfully!",Toast.LENGTH_SHORT).show()
                    binding!!.namePDFFile.text.clear()
                    progressDialog.dismiss()
                }.addOnFailureListener{
                    Toast.makeText(this,"Upload File, Field!",Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }

        }catch (e: Exception){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    private fun startDownload(DataUrl: Uri){
        val request = DownloadManager.Request(DataUrl)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Download")
        request.setDescription("Your File is Downloading...")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"${(System.currentTimeMillis())}")
        val  manger = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manger.enqueue(request)
    }
}