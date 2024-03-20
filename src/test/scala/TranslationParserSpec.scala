import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{TranslationParser, TranslationSyntaxError}

import scala.io.Source

class TranslationParserSpec extends AnyFlatSpec {

  val translationParser = new TranslationParser("en-US")

  "A translation parser" should "correctly parse a well-formed file" in {
    val lines = Source.fromResource("good.i18n").getLines.toSeq
    val translation = translationParser.parse(lines)
    val result = translation.apply("say-hello", ("name", "Joe"))("en-US")
    assertResult("Hello, Joe!")(result)
  }

  it should "throw an exception if any message is not available in the default language" in {
    val lines = Source.fromResource("missing_default.i18n").getLines.toSeq
    assertThrows[TranslationSyntaxError] {
      translationParser.parse(lines)
    }
  }

}
