package com.eye.cool.scan.decode.listener

import androidx.annotation.WorkerThread

interface PermissionChecker {

  @WorkerThread
  suspend fun checkPermission(permissions: Array<String>): Boolean

}