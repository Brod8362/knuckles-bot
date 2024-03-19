import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.TranslationParser

import scala.io.Source

class TranslationParserSpec extends AnyFlatSpec {

  val translationParser = new TranslationParser()

  "A translation parser" should "correctly parse a well-formed file" in {
    val lines = Source.fromResource("good.i18n").getLines.toSeq
    val translation = translationParser.parse(lines, "en-US")
    val result = translation.apply("say-hello", ("name", "Joe"))("en-US")
    assert(result.isDefined)
    assertResult("Hello, Joe!")(result.get)
  }

  it should "throw an exception if a message is not available in the default language" in {
    fail("not implemented")
  }

}
