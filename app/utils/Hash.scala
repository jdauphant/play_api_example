package utils

import java.security.MessageDigest
/**
 * Created by julien on 12/10/14.
 */
object Hash {
  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).toString
  }
}
