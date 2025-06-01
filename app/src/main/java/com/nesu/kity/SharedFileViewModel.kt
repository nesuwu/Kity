package com.nesu.kity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class SharedFileViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "KityAppFileHistoryPrefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val filesListKey = "uploaded_files_list_v1"

    private val _uploadedFiles = MutableLiveData<MutableList<FileItem>>()
    val uploadedFiles: LiveData<MutableList<FileItem>> = _uploadedFiles

    init {
        loadFilesFromPrefs()
    }

    private fun loadFilesFromPrefs() {
        val json = sharedPreferences.getString(filesListKey, null)
        if (json != null) {
            try {
                val type = object : TypeToken<MutableList<FileItem>>() {}.type
                _uploadedFiles.value = gson.fromJson<MutableList<FileItem>>(json, type) ?: mutableListOf()
            } catch (e: Exception) {
                _uploadedFiles.value = mutableListOf()
                sharedPreferences.edit { remove(filesListKey) }
            }
        } else {
            _uploadedFiles.value = mutableListOf()
        }
    }

    private fun saveFilesToPrefs() {
        val json = gson.toJson(_uploadedFiles.value)
        sharedPreferences.edit {
            putString(filesListKey, json)
        }
    }

    fun addUploadedFile(fileItem: FileItem) {
        val currentList = _uploadedFiles.value ?: mutableListOf()
        if (currentList.none { it.id == fileItem.id }) {
            currentList.add(0, fileItem)
            _uploadedFiles.value = currentList
            saveFilesToPrefs()
        }
    }

    fun clearAllFiles() {
        _uploadedFiles.value = mutableListOf()
        saveFilesToPrefs()
    }

    fun removeFileItem(fileItemToRemove: FileItem) {
        val currentList = _uploadedFiles.value ?: return
        val updatedList = currentList.filterNot { it.id == fileItemToRemove.id }.toMutableList()
        if (updatedList.size < currentList.size) {
            _uploadedFiles.value = updatedList
            saveFilesToPrefs()
        }
    }
}
