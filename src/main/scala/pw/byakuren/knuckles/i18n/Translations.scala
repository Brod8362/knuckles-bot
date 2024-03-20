package pw.byakuren.knuckles.i18n

object Translations extends Enumeration {
  type Translations = String
  val EXAMPLE_MESSAGE_ID = ""
}

class Translations(defaultLocale: String, groups: Seq[TranslationGroup]) {

  private val translationGroups: Map[String, TranslationGroup] = groups.map(k => (k.messageId, k)).toMap

  def apply(resourceString: String, substitutions: (String, String)*)(implicit locale: String): String = {
    translationGroups.get(resourceString) match {
      case Some(group) =>
        // its safe to assume the default locale will *always* be available, as it is enforced at parse time
        group.apply(locale, defaultLocale).map(_.apply(substitutions:_*)).get
      case None => {
        throw MessageIdException(resourceString)
      }
    }
  }
}