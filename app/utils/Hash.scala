package utils

import java.security.MessageDigest

object Hash {
  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).toString
  }
}
