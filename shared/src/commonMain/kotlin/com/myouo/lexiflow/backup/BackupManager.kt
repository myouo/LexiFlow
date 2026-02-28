package com.myouo.lexiflow.backup

expect class BackupManager {
    suspend fun createBackup(destinationPath: String): Boolean
    suspend fun restoreBackup(sourcePath: String): Boolean
}
