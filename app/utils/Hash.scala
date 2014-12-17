package utils

import java.security.MessageDigest
import org.mindrot.jbcrypt.BCrypt

object Hash {
  def sha256(s: String): String = {
    MessageDigest.getInstance("SHA256").digest(s.getBytes).toString
  }
  def bcrypt(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt())
  }
  def bcrypt_compare(password: String, passwordHash: String): Boolean = {
    BCrypt.checkpw(password, passwordHash)
  }
}
