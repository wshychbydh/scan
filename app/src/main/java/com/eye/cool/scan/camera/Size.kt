package com.eye.cool.scan.camera

class Size {

  constructor(w: Int, h: Int) {
    width = w
    height = h
  }

  constructor(src: Size) {
    width = src.width
    height = src.height
  }

  override fun equals(obj: Any?): Boolean {
    if (obj !is Size) {
      return false
    }
    return width == obj.width && height == obj.height
  }

  fun size(): Int {
    return width * height
  }

  var width: Int = 0
  var height: Int = 0

  override fun toString(): String {
    return width.toString() + "x" + height
  }
}