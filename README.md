# Kity 🐱✨

> Kity - A small Cathy you can carry around.

Kity is a lightweight Android application inspired by my old project - [Cathy-Electron](https://github.com/nesuwu/Cathy-Electron), designed for quick and easy file uploads primarily to [Catbox.moe](https://catbox.moe/). It allows users to upload files, manage an optional userhash for their Catbox account, and keep a history of their uploads.


## ✨ Features

*   **File Upload:**
    *   Upload files directly to [Catbox.moe](https://catbox.moe/).
    *   Supports anonymous uploads or uploads with a userhash.
    *   Server-side file size limit (currently 200MB) that can be increased via Donations to (Catbox)[https://catbox.moe].
    *   Determinate progress bar showing upload progress.
*   **File Selection & Preview:**
    *   Easy file selection using the system file picker.
    *   Selected file name displayed clearly.
    *   Dynamic file preview:
        *   Shows image thumbnails for common image types.
        *   Displays specific icons for videos, audio, documents, archives, and a generic icon for other file types.
*   **Upload History:**
    *   View a list of your past uploads (persisted locally).
    *   Copy direct links to uploaded files.
    *   "Download" option (opens the file link in a browser).
    *   Delete individual uploads from the history.
    *   Option to clear the entire upload history.
*   **Modern UI & UX:**
    *   **Material You (M3) Theming:**
        *   Adapts to your device's wallpaper colors on Android 12+ for a personalized experience.
        *   Automatically supports system Light and Dark themes.
    *   Swipe navigation (ViewPager2) between the "Upload" and "Files" (History) screens.
    *   User-friendly interface with clear visual feedback.
*   **User Preferences:**
    *   Option to remember your Catbox userhash.

## 🚀 Getting Started (For Developers)

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/nesuwu/Kity
    ```
2.  **Open in Android Studio:** Open the cloned project in the latest stable version of Android Studio.
3.  **Build:** Let Gradle sync and build the project.
4.  **Run:** Deploy to an emulator or a physical Android device.

## 🛠️ Built With

*   **Kotlin:** Primary programming language.
*   **Android SDK:** Native Android development platform.
*   **Material Components 3:** For modern UI elements and Material You theming.
*   **ViewPager2:** For swipeable screen navigation.
*   **RecyclerView:** For displaying the file history list efficiently.
*   **Retrofit & OkHttp:** For making network requests to the Catbox.moe API.
    *   Includes support for increased timeouts for large uploads.
    *   Custom `ProgressRequestBody` for client-side upload progress.
*   **Coil:** For efficient image loading and display.
*   **Gson:** For serializing and deserializing the file history list for persistence in SharedPreferences.
*   **SharedPreferences:** For storing userhash and file upload history.

## How to Use

1.  **Upload Screen:**
    *   Optionally, enter your Catbox.moe `userhash` if you want uploads linked to your account. You can choose to have the app remember it.
    *   Tap "Select File" to choose a file from your device. The selected filename and a preview will appear.
    *   Tap "Start uploading". A progress bar will show the upload status.
    *   Once successful, the direct link to your uploaded file will be displayed along with a copy button.
    *   The "Select File" button will be re-enabled after you copy the link (or if an upload fails).
2.  **Files Screen (History):**
    *   Swipe left from the Upload screen to access your upload history.
    *   Each item shows a thumbnail/icon, filename, and file size.
    *   **Copy Link:** Copies the file's direct URL to your clipboard.
    *   **Download:** Opens the file's URL in your default web browser.
    *   **Delete (Trash Icon):** Deletes the specific entry from your local history after confirmation.
    *   **Clear All (Toolbar Icon):** Clears your entire local upload history after confirmation.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 📜 License

This project is licensed under the [MIT License] - see the `LICENSE` file for details.

---
- Inspired by my Roman Empire -  [Cathy-Electron](https://github.com/nesuwu/Cathy-Electron).
