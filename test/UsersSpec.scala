import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class UsersSpec extends Specification {

  "Users" should {

    "send 404 on /users" in new WithApplication{
      route(FakeRequest(GET, "/users")) must beNone
    }

    "create a email user" in new WithApplication{
      val jsonBody: JsValue = JsObject(
        Seq("users" ->
          JsObject(Seq(
            "email" -> JsString("johndoe@siz.io"),
            "password" -> JsString("6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"),
            "username" -> JsString("johndoe"))
          )
        )
      )
      val createUser = route(FakeRequest(POST, "/users").withJsonBody(jsonBody)).get

      status(createUser) must equalTo(CREATED)
      contentType(createUser) must beSome.which(_ == "application/json")
      contentAsString(createUser) must contain ("""email""")
      contentAsString(createUser) must not contain ("""password""")
    }
  }
}
