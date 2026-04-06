package org.monogram.presentation.core.util

import java.io.File
import androidx.core.net.toUri

fun safeLocalMediaModel(path: String?): Any? {
    if (path.isNullOrBlank()) return null

    return when {
        path.startsWith("content://", ignoreCase = true) -> path.toUri()
        path.startsWith("file://", ignoreCase = true) -> path.toUri()
        else -> File(path).takeIf { it.exists() }
    }
}
