package dev.vincent.geschichten.updates

import androidx.core.content.FileProvider

/** Manifest restricts this provider to cache/app_updates; no story/model files are shared. */
class UpdateFileProvider : FileProvider()
