package formats

import models._

trait CommonJsonFormats {
   implicit def stringToError(s: String): Error = new Error(s)
}
