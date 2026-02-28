package com.myouo.lexiflow.backup

actual class BackupManager {
    actual suspend fun createBackup(destinationPath: String): Boolean {
        return false // iOS stub
    }
    
    actual suspend fun restoreBackup(sourcePath: String): Boolean {
        return false // iOS stub
    }
}
