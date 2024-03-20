import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{ExcessVariableException, MessageIdException, MissingVariableException, Substitution, TranslationGroup, Translations}

class TranslationsSpec extends AnyFlatSpec {

  private val translationsObject: Translations = {
    val subEn = Substitution("test <v1> <v2>", Seq("v1", "v2"))
    val subJp = Substitution("〇〇 <v1> <v2>", Seq("v1", "v2"))
    val translationGroup = TranslationGroup("message", Map(
      "en-US" -> subEn,
      "ja-JP" -> subJp
    ))
    new Translations("en-US", Seq(translationGroup))
  }

  "A Translations object" should "reply with the correct translation if the locale exists" in {
    implicit val locale: String = "ja-JP"
    val result = translationsObject.apply("message", ("v1", "a"), ("v2", "b"))
    assertResult("〇〇 a b")(result)
  }

  it should "fallback to the default locale when the requested locale is not available" in {
    implicit val locale: String = "fake"
    val result = translationsObject.apply("message", ("v1", "a"), ("v2", "b"))
    assertResult("test a b")(result)
  }

  it should "throw an exception if the message ID doesn't exist" in {
    implicit val locale: String = "en-US"
    assertThrows[MessageIdException] { //TODO: more specific exception
      translationsObject.apply("fake-message", ("v1", "a"), ("v2", "b"))
    }
  }

  it should "throw an exception if too few replacements are not provided" in {
    implicit val locale: String = "en-US"
    assertThrows[MissingVariableException] {
      translationsObject.apply("message", ("v1", "a"))
    }
  }
  it should "throw an exception if too many replacements are not provided" in {
    implicit val locale: String = "en-US"
    assertThrows[ExcessVariableException] {
      translationsObject.apply("message", ("v1", "a"), ("v2", "b"), ("v3", "c"))
    }
  }


}
