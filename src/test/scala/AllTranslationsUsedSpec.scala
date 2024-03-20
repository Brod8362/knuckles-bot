import org.scalatest.flatspec.AnyFlatSpec
import pw.byakuren.knuckles.KnucklesBot
import pw.byakuren.knuckles.i18n.{TranslationParser, Translations}

import java.io.File

class AllTranslationsUsedSpec extends AnyFlatSpec {
  "All translations" should "be available in the default locale" in {
    //loading the file this way is probably kind of fragile
    val file = new File("src/main/resources/translations.i18n")
    val parser = new TranslationParser(KnucklesBot.DEFAULT_LOCALE)
    val i18n = parser.parse(file)
    for (messageId <- Translations.values) {
      assert(i18n.exists(messageId.toString, KnucklesBot.DEFAULT_LOCALE))
    }
  }

}
