package com.nesu.kity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.appbar.MaterialToolbar

enum class FileAction { COPY_LINK, DOWNLOAD, DELETE_ITEM }

class FilesScreenFragment : Fragment() {

    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private val sharedFileViewModel: SharedFileViewModel by activityViewModels()
    private var filePreviewIconPaddingSmall: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filePreviewIconPaddingSmall = resources.getDimensionPixelSize(R.dimen.file_preview_icon_padding_small)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_files)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.navigateToUploadScreen()
        }

        filesRecyclerView = view.findViewById(R.id.files_recycler_view)
        filesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        filesAdapter = FilesAdapter(mutableListOf()) { fileItem, action ->
            when (action) {
                FileAction.COPY_LINK -> {
                    val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("File URL", fileItem.fileUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "Link copied: ${fileItem.name}", Toast.LENGTH_SHORT).show()
                }
                FileAction.DOWNLOAD -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, fileItem.fileUrl.toUri())
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                FileAction.DELETE_ITEM -> {
                    showDeleteItemConfirmationDialog(fileItem)
                }
            }
        }
        filesRecyclerView.adapter = filesAdapter

        sharedFileViewModel.uploadedFiles.observe(viewLifecycleOwner) { files ->
            filesAdapter.updateFiles(ArrayList(files ?: emptyList()))
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("requireActivity().addMenuProvider(object : MenuProvider { ... })"))
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_files_screen, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("requireActivity().addMenuProvider(object : MenuProvider { ... })"))
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_history -> {
                showClearHistoryConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showClearHistoryConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_clear_history_title)
            .setMessage(R.string.confirm_clear_history_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                sharedFileViewModel.clearAllFiles()
                Toast.makeText(requireContext(), R.string.history_cleared, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteItemConfirmationDialog(fileItem: FileItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_item_title)
            .setMessage(getString(R.string.confirm_delete_item_message_param, fileItem.name)) // More specific message
            .setPositiveButton(R.string.delete) { _, _ ->
                sharedFileViewModel.removeFileItem(fileItem)
                Toast.makeText(requireContext(), R.string.item_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

class FilesAdapter(
    private var files: MutableList<FileItem>, // Change to MutableList if you directly modify this list, or List if always replacing.
    private val onItemAction: (FileItem, FileAction) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    private var iconPadding: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        if (iconPadding == 0) {
            iconPadding = parent.context.resources.getDimensionPixelSize(R.dimen.file_preview_icon_padding_small)
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_entry, parent, false)
        return FileViewHolder(view, iconPadding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position], onItemAction)
    }

    override fun getItemCount(): Int = files.size

    // When using LiveData, it's often better to submit a new list
    // rather than modifying the adapter's internal list directly.
    fun updateFiles(newFiles: List<FileItem>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged() // For more complex scenarios, consider DiffUtil
    }

    class FileViewHolder(itemView: View, private val iconPadding: Int) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.file_thumbnail_image_view)
        private val nameTextView: TextView = itemView.findViewById(R.id.file_name_text_view)
        private val sizeTextView: TextView = itemView.findViewById(R.id.file_size_text_view)
        private val copyLinkButton: ImageButton = itemView.findViewById(R.id.copy_link_icon_button_item)
        private val downloadButton: ImageButton = itemView.findViewById(R.id.download_icon_button_item)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_icon_button_item)

        fun bind(file: FileItem, onItemAction: (FileItem, FileAction) -> Unit) {
            nameTextView.text = file.name
            sizeTextView.text = file.size

            val context = itemView.context
            val guessedMimeType = guessMimeTypeFromFilename(file.name)

            if (file.thumbnailUrl != null && (guessedMimeType?.startsWith("image/") == true)) {
                thumbnailImageView.setBackgroundColor(Color.TRANSPARENT)
                thumbnailImageView.setPadding(0,0,0,0)
                thumbnailImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                thumbnailImageView.load(file.thumbnailUrl) {
                    placeholder(ContextCompat.getDrawable(context, R.drawable.bg_file_preview_area))
                    error(ContextCompat.getDrawable(context, R.drawable.ic_file_generic_24))
                }
            } else {
                thumbnailImageView.setBackgroundResource(R.drawable.bg_file_preview_area)
                thumbnailImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                thumbnailImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                when {
                    guessedMimeType?.startsWith("video/") == true -> thumbnailImageView.setImageResource(R.drawable.ic_file_video_24)
                    guessedMimeType?.startsWith("audio/") == true -> thumbnailImageView.setImageResource(R.drawable.ic_file_audio_24)
                    guessedMimeType == "application/pdf" || guessedMimeType?.startsWith("text/") == true ||
                            guessedMimeType == "application/msword" || guessedMimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                            guessedMimeType == "application/vnd.ms-excel" || guessedMimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                            guessedMimeType == "application/vnd.ms-powerpoint" || guessedMimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                        -> thumbnailImageView.setImageResource(R.drawable.ic_file_document_24)
                    guessedMimeType == "application/zip" || guessedMimeType == "application/x-rar-compressed" || guessedMimeType == "application/x-7z-compressed" ||
                            guessedMimeType == "application/x-tar" || guessedMimeType == "application/gzip"
                        -> thumbnailImageView.setImageResource(R.drawable.ic_file_archive_24)
                    else -> thumbnailImageView.setImageResource(R.drawable.ic_file_generic_24)
                }
            }

            copyLinkButton.setOnClickListener { onItemAction(file, FileAction.COPY_LINK) }
            downloadButton.setOnClickListener { onItemAction(file, FileAction.DOWNLOAD) }
            deleteButton.setOnClickListener { onItemAction(file, FileAction.DELETE_ITEM) }
        }

        private fun guessMimeTypeFromFilename(filename: String): String? {
            return when (filename.substringAfterLast('.', "").lowercase()) {
                "mp4", "mkv", "webm", "avi", "mov", "flv", "wmv" -> "video/example"
                "mp3", "wav", "ogg", "aac", "m4a", "flac" -> "audio/example"
                "pdf" -> "application/pdf"
                "txt", "log", "md", "rtf", "csv", "xml", "json", "html", "htm" -> "text/plain"
                "doc" -> "application/msword"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "xls" -> "application/vnd.ms-excel"
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "ppt" -> "application/vnd.ms-powerpoint"
                "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                "zip" -> "application/zip"
                "rar" -> "application/x-rar-compressed"
                "7z" -> "application/x-7z-compressed"
                "tar" -> "application/x-tar"
                "gz", "tgz" -> "application/gzip"
                "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> "image/example"
                else -> null
            }
        }
    }
}