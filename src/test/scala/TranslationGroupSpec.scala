import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{Substitution, TranslationGroup}

class TranslationGroupSpec extends AnyFlatSpec {
  "Translation group" should "return the Substitution if the locale exists" in {
    val substitutions = Map(
      "en-US" -> Substitution("hi", Nil)
    )
    val group = TranslationGroup("id", substitutions)
    assert(group("en-US").isDefined)
  }

  it should "return None if the locale does not exist, and a default is not specified" in {
    val substitutions = Map(
      "en-US" -> Substitution("hi", Nil)
    )
    val group = TranslationGroup("id", substitutions)
    assert(group("ja-JP").isEmpty)
  }


  it should "return Some if the locale does not exist, and a default is specified" in {
    val substitutions = Map(
      "en-US" -> Substitution("hi", Nil)
    )
    val group = TranslationGroup("id", substitutions)
    assert(group("ja-JP", "en-US").isDefined)
  }

}
