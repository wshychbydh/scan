package com.eye.cool.scan.decode.supprot

/**
 *  Created by cool 2020/10/12
 */
class DecodeException(val code: Int, val message: String) {

  companion object {

    const val CAMERA_FAILED = 20001
    const val DECODE_FAILED = 20101
    const val IMAGE_PATH_ERROR = 20201
    const val PERMISSION_FAILED = 20301
    const val PARAMS_INVALID = 20401
    const val PARSE_FAILED = 20501
  }
}