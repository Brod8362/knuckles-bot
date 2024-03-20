import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{TranslationParser, TranslationSyntaxError}

import scala.io.Source

class TranslationParserSpec extends AnyFlatSpec {

  "A translation parser" should "correctly parse a well-formed file" in {
    val translationParser = new TranslationParser("en-US")
    val lines = Source.fromResource("good.i18n").getLines.toSeq
    val translation = translationParser.parse(lines)
    val result = translation.sub("say-hello", ("name", "Joe"))("en-US")
    assertResult("Hello, Joe!")(result)
  }

  it should "throw an exception if any message is not available in the default language" in {
    val translationParser = new TranslationParser("en-US")
    val lines = Source.fromResource("missing_default.i18n").getLines.toSeq
    assertThrows[TranslationSyntaxError] {
      translationParser.parse(lines)
    }
  }

  it should "allow empty argument lists" in {
    val translationParser = new TranslationParser("en-US")
    val lines = Source.fromResource("empty_arg_list.i18n").getLines.toSeq
    val translation = translationParser.parse(lines)
    val result = translation.sub("empty-arg-list")("en-US")
    assertResult("placeholder")(result)
  }
}
