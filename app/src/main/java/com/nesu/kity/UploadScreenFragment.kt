package com.nesu.kity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class UploadScreenFragment : Fragment(), ProgressRequestBody.ProgressListener {

    private lateinit var selectFileTriggerLayout: LinearLayout
    private lateinit var selectFileButtonLabel: TextView
    private lateinit var uploadButton: MaterialButton
    private lateinit var resultTextView: TextView
    private lateinit var previewImageView: ImageView
    private lateinit var userhashEditText: TextInputEditText
    private lateinit var copyLinkButton: ImageButton
    private lateinit var rememberUserhashSwitch: SwitchMaterial
    private lateinit var uploadResultLayout: LinearLayout
    private lateinit var progressBar: LinearProgressIndicator

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var selectedFileSize: Long = 0L
    private var lastUploadedUrl: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var selectFileLauncher: ActivityResultLauncher<Intent>
    private val sharedFileViewModel: SharedFileViewModel by activityViewModels()
    private var filePreviewIconPadding: Int = 0

    companion object {
        const val PREFS_NAME = "KityPrefs"
        const val KEY_USERHASH = "userhash"
        const val KEY_LAST_UPLOAD_URL = "last_upload_url"
        const val KEY_REMEMBER_USERHASH = "remember_userhash"
        private const val MAX_FILE_SIZE_BYTES = 200L * 1024L * 1024L // 200 MB
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        filePreviewIconPadding = resources.getDimensionPixelSize(R.dimen.file_preview_icon_padding)

        selectFileTriggerLayout = view.findViewById(R.id.select_file_trigger_layout_fragment)
        selectFileButtonLabel = view.findViewById(R.id.select_file_button_label_fragment)
        uploadButton = view.findViewById(R.id.upload_button_fragment)
        previewImageView = view.findViewById(R.id.preview_image_view_fragment)
        userhashEditText = view.findViewById(R.id.userhash_edit_text_fragment)
        rememberUserhashSwitch = view.findViewById(R.id.remember_userhash_switch_fragment)
        uploadResultLayout = view.findViewById(R.id.upload_result_layout_fragment)
        resultTextView = view.findViewById(R.id.result_text_view_fragment)
        copyLinkButton = view.findViewById(R.id.copy_link_button_fragment)
        progressBar = view.findViewById(R.id.upload_progress_bar_fragment)

        loadSavedData()

        selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val fileDetails = getFileNameAndSize(uri)
                    if (fileDetails.second > MAX_FILE_SIZE_BYTES) {
                        Log.w("UploadScreenFragment", "File too large (Max: ${Formatter.formatFileSize(requireContext(), MAX_FILE_SIZE_BYTES)}). Selected: ${Formatter.formatFileSize(requireContext(), fileDetails.second)}")
                        clearSelectedFile()
                        return@let
                    }
                    selectedFileUri = uri
                    selectedFileName = fileDetails.first
                    selectedFileSize = fileDetails.second
                    selectFileButtonLabel.text = selectedFileName
                    uploadResultLayout.visibility = View.GONE
                    selectFileTriggerLayout.isEnabled = true
                    uploadButton.isEnabled = true

                    val mimeType = requireActivity().contentResolver.getType(uri)
                    if (mimeType?.startsWith("image/") == true) {
                        previewImageView.setBackgroundColor(Color.TRANSPARENT)
                        previewImageView.setPadding(0, 0, 0, 0)
                        previewImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        previewImageView.load(uri) {
                            placeholder(ContextCompat.getDrawable(requireContext(),R.drawable.bg_file_preview_area))
                            error(ContextCompat.getDrawable(requireContext(),R.drawable.ic_file_generic_24))
                        }
                    } else {
                        previewImageView.setBackgroundResource(R.drawable.bg_file_preview_area)
                        previewImageView.setPadding(filePreviewIconPadding,filePreviewIconPadding,filePreviewIconPadding,filePreviewIconPadding)
                        previewImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        when {
                            mimeType?.startsWith("video/")==true -> previewImageView.setImageResource(R.drawable.ic_file_video_24)
                            mimeType?.startsWith("audio/")==true -> previewImageView.setImageResource(R.drawable.ic_file_audio_24)
                            mimeType=="application/pdf"||mimeType?.startsWith("text/")==true||mimeType=="application/msword"||mimeType=="application/vnd.openxmlformats-officedocument.wordprocessingml.document"||mimeType=="application/vnd.ms-excel"||mimeType=="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"||mimeType=="application/vnd.ms-powerpoint"||mimeType=="application/vnd.openxmlformats-officedocument.presentationml.presentation" -> previewImageView.setImageResource(R.drawable.ic_file_document_24)
                            mimeType=="application/zip"||mimeType=="application/x-rar-compressed"||mimeType=="application/x-7z-compressed"||mimeType=="application/x-tar"||mimeType=="application/gzip" -> previewImageView.setImageResource(R.drawable.ic_file_archive_24)
                            else -> previewImageView.setImageResource(R.drawable.ic_file_generic_24)
                        }
                    }
                }
            }
        }
        selectFileTriggerLayout.setOnClickListener { if (selectFileTriggerLayout.isEnabled) openFilePicker() }
        uploadButton.setOnClickListener {
            selectedFileUri?.let { uri ->
                val userhash=userhashEditText.text.toString().trim()
                if(rememberUserhashSwitch.isChecked) saveUserhash(userhash)
                else sharedPreferences.edit{ remove(KEY_USERHASH) }
                uploadFile(uri,userhash.ifEmpty{null},selectedFileName?:"unknown_file",selectedFileSize)
            } ?: run {
                Log.w("UploadScreenFragment", "Upload attempt with no file selected (select_file_uri is null).")
            }
        }
        copyLinkButton.setOnClickListener {
            resultTextView.text?.toString()?.takeIf{it.startsWith("http")}?.let{urlToCopy->
                val clipboard=requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip=ClipData.newPlainText("Uploaded URL",urlToCopy)
                clipboard.setPrimaryClip(clip)
                Log.i("UploadScreenFragment", "Link copied to clipboard: $urlToCopy")
                selectFileTriggerLayout.isEnabled = true
            }
        }
        rememberUserhashSwitch.setOnCheckedChangeListener {_,isChecked->
            sharedPreferences.edit{putBoolean(KEY_REMEMBER_USERHASH,isChecked)}
            if(!isChecked){sharedPreferences.edit{remove(KEY_USERHASH)};userhashEditText.setText("")}else{saveUserhash(userhashEditText.text.toString().trim())}
        }
    }

    private fun clearSelectedFile() {
        selectedFileUri=null;selectedFileName=null;selectedFileSize=0L
        selectFileButtonLabel.text=getString(R.string.file_picker_button_text)
        previewImageView.setBackgroundResource(R.drawable.bg_file_preview_area)
        previewImageView.setImageResource(R.drawable.ic_file_generic_24)
        previewImageView.setPadding(filePreviewIconPadding,filePreviewIconPadding,filePreviewIconPadding,filePreviewIconPadding)
        previewImageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
        uploadResultLayout.visibility=View.GONE
        uploadButton.isEnabled=false
        selectFileTriggerLayout.isEnabled=true
    }

    private fun loadSavedData() {
        val shouldRemember=sharedPreferences.getBoolean(KEY_REMEMBER_USERHASH,false)
        rememberUserhashSwitch.isChecked=shouldRemember
        if(shouldRemember)userhashEditText.setText(sharedPreferences.getString(KEY_USERHASH,""))
        else userhashEditText.setText("")
        lastUploadedUrl=null
        updateUploadResultDisplay()
        clearSelectedFile()
    }

    private fun saveUserhash(userhash:String){
        if(rememberUserhashSwitch.isChecked) sharedPreferences.edit{putString(KEY_USERHASH,userhash)}
    }

    private fun updateUploadResultDisplay(){
        if(!lastUploadedUrl.isNullOrEmpty()&&lastUploadedUrl!!.startsWith("http")){
            resultTextView.text=lastUploadedUrl
            uploadResultLayout.visibility=View.VISIBLE
        }else{
            resultTextView.text=""
            uploadResultLayout.visibility=View.GONE
        }
    }

    private fun openFilePicker(){
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*"}
        selectFileLauncher.launch(intent)
    }

    private fun isImageUri(uri:Uri):Boolean{
        val mimeType=requireActivity().contentResolver.getType(uri)
        return mimeType?.startsWith("image/")==true
    }

    private fun getFileNameAndSize(uri:Uri):Pair<String,Long>{
        var name="Unknown";var size=0L
        try{
            requireActivity().contentResolver.query(uri,null,null,null,null)?.use{cursor->
                if(cursor.moveToFirst()){
                    val nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(nameIndex!=-1&&cursor.getString(nameIndex)!=null)name=cursor.getString(nameIndex)
                    val sizeIndex=cursor.getColumnIndex(OpenableColumns.SIZE)
                    if(sizeIndex!=-1&&cursor.getType(sizeIndex)!=Cursor.FIELD_TYPE_NULL)size=cursor.getLong(sizeIndex)
                }
            }
            if((name=="Unknown"||name.isEmpty()||size==0L)&&uri.scheme=="file"){
                uri.path?.let{path->val file=File(path);if(file.exists()){if(name=="Unknown"||name.isEmpty())name=file.name;if(size==0L)size=file.length()}}
            }
        }catch(e:Exception){Log.e("UploadScreenFragment","Error getting file name/size for URI: $uri",e)}
        return (name.ifEmpty{"upload_${System.currentTimeMillis()}"}) to size
    }

    private fun uploadFile(fileUri:Uri,userhash:String?,originalFileName:String,originalFileSize:Long){
        Log.i("UploadScreenFragment", "Upload starting for: $originalFileName")

        uploadButton.isEnabled=false
        selectFileTriggerLayout.isEnabled=false
        progressBar.isIndeterminate=false
        progressBar.max=100
        progressBar.progress=0
        progressBar.visibility=View.VISIBLE
        uploadResultLayout.visibility=View.GONE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            var actualFileToUpload:File?=null
            try{
                val tempFileName=originalFileName.ifEmpty{"temp_upload_${System.currentTimeMillis()}"}
                val inputStream:InputStream?=requireActivity().contentResolver.openInputStream(fileUri)
                if(inputStream==null){
                    withContext(Dispatchers.Main){
                        Log.e("UploadScreenFragment", "Upload failed: Could not open stream for URI.")
                        resetUiAfterUploadAttempt(false)
                    }
                    return@launch
                }
                actualFileToUpload=File(requireContext().cacheDir,tempFileName)
                FileOutputStream(actualFileToUpload).use{outputStream->inputStream.copyTo(outputStream)}
                inputStream.close()

                val fileMimeType=requireActivity().contentResolver.getType(fileUri)?:"application/octet-stream"
                val progressRequestBody=ProgressRequestBody(
                    file=actualFileToUpload,
                    contentType=fileMimeType.toMediaTypeOrNull(),
                    listener=this@UploadScreenFragment
                )
                val body=MultipartBody.Part.createFormData("fileToUpload",actualFileToUpload.name,progressRequestBody)
                val reqtypeBody="fileupload".toRequestBody("text/plain".toMediaTypeOrNull())
                val userhashBody=userhash?.takeIf{it.isNotEmpty()}?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response=if(userhashBody!=null){
                    CatboxApi.retrofitService.uploadFile(reqtypeBody,userhashBody,body)
                }else{
                    CatboxApi.retrofitService.uploadFileAnonymous(reqtypeBody,body)
                }

                withContext(Dispatchers.Main){
                    if(response.isSuccessful&&response.body()?.startsWith("http")==true){
                        val fileUrl=response.body()!!
                        lastUploadedUrl=fileUrl
                        updateUploadResultDisplay()
                        progressBar.progress=100
                        val newItem=FileItem(UUID.randomUUID().toString(),originalFileName,Formatter.formatFileSize(requireContext(),originalFileSize),fileUrl,if(isImageUri(fileUri))fileUrl else null)
                        sharedFileViewModel.addUploadedFile(newItem)
                        Log.i("UploadScreenFragment", "Upload successful: $fileUrl")
                        launch{delay(1000);if(isAdded)progressBar.visibility=View.GONE}
                    }else{
                        val errorBody=response.errorBody()?.string()?:response.message()?:"Unknown error"
                        Log.e("UploadScreenFragment","Upload failed: Code=${response.code()}, Body='$errorBody'")
                        lastUploadedUrl=null
                        updateUploadResultDisplay()
                        progressBar.visibility=View.GONE
                        resetUiAfterUploadAttempt(false)
                    }
                }
            }catch(e:Exception){
                Log.e("UploadScreenFragment","Upload error or ProgressRequestBody error",e)
                withContext(Dispatchers.Main){
                    Log.e("UploadScreenFragment", "Exception during upload: ${e.message}")
                    lastUploadedUrl=null
                    updateUploadResultDisplay()
                    progressBar.visibility=View.GONE
                    resetUiAfterUploadAttempt(false)
                }
            }finally{
                actualFileToUpload?.delete()
                if(lastUploadedUrl==null){
                    withContext(Dispatchers.Main){
                        resetUiAfterUploadAttempt(false)
                    }
                }
            }
        }
    }

    override fun onProgressUpdate(percentage:Int){
        lifecycleScope.launch(Dispatchers.Main){
            if(isAdded)progressBar.progress=percentage
        }
    }

    override fun onError(e:Exception){
        lifecycleScope.launch(Dispatchers.Main){
            if(isAdded){
                Log.e("UploadScreenFragment","Error reading file for upload: ${e.message}")
                resetUiAfterUploadAttempt(false)
            }
        }
    }

    private fun resetUiAfterUploadAttempt(wasSuccess:Boolean = false){
        if(isAdded&&view!=null){
            if(!wasSuccess){
                clearSelectedFile()
            } else {
                // If it was a success but we still call reset (e.g. maybe after copy link action
                // if we wanted to reset everything), we might need other logic.
                // For now, wasSuccess=true path is not hit in a way that would re-enable selectFileTriggerLayout prematurely.
            }

            if (!wasSuccess && progressBar.visibility == View.VISIBLE) {
                progressBar.visibility = View.GONE
            }
            progressBar.progress=0
        }
    }
}