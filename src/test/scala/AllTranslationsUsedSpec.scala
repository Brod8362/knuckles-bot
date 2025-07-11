import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.i18n.{TranslationParser, Translations}

import java.io.File

class AllTranslationsUsedSpec extends AnyFlatSpec {
  "All translations" should "be available in the default locale" in {
    //loading the file this way is probably kind of fragile
    val file = new File("src/main/resources/translations.i18n")
    val parser = new TranslationParser("en-US")
    val i18n = parser.parse(file)
    //TODO: this test is not working properly
    for (messageId <- Translations.values) {
      print(messageId)
      assert(i18n.exists(messageId.toString, "en-US"))
    }
  }

}
