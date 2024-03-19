import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{ExcessVariableException, MissingVariableException, Substitution, TranslationGroup}

class SubstitutionSpec extends AnyFlatSpec {

  "A substitution" should "accept the correct variables" in {
    val substitution = Substitution("abc <v1> <v2>", Seq("v1", "v2"))
    val result = substitution(("v1", ""), ("v2", ""))
    assert(result.nonEmpty)
  }

  it should "throw an exception if too few variables are provided" in {
    val substitution = Substitution("abc <v1> <v2>", Seq("v1", "v2"))
    assertThrows[MissingVariableException] {
      substitution(("v1", ""))
    }
  }

  it should "throw an exception if too many variables are provided" in {
    val substitution = Substitution("abc <v1> <v2>", Seq("v1", "v2"))
    assertThrows[ExcessVariableException] {
      substitution(("v1", ""), ("v2", ""), ("v3", ""))
    }
  }

  it should "substitute the text correctly" in {
    val substitution = Substitution("abc <v1> <v2>", Seq("v1", "v2"))
    val result = substitution(("v1", "123"), ("v2", "def"))
    assertResult("abc 123 def")(result)
  }

  it should "substitute the text correctly in different locales" in {
    val subEn = Substitution("test <v1> <v2>", Seq("v1", "v2"))
    val subJp = Substitution("〇〇 <v1> <v2>", Seq("v1", "v2"))
    val translationGroup = TranslationGroup("message", Map(
      "en-US" -> subEn,
      "ja-JP" -> subJp
    ))

    val localeAndExpectedMessage = Seq(
      ("en-US", "test abc 123"),
      ("ja-JP", "〇〇 abc 123")
    )

    for ((locale, expected) <- localeAndExpectedMessage) {
      val subMaybe = translationGroup(locale)
      assert(subMaybe.isDefined)
      val result = subMaybe.get.apply(("v1", "abc"), ("v2", "123"))
      assertResult(expected)(result)
    }
  }

}
