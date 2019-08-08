package com.pointr.tcp.util

object StringUtils {

  def countOccurrences(src: String, pat: String): Int = src.sliding(pat.length).count(window => window == pat)
}
