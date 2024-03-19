package pw.byakuren.knuckles.i18n

object Translations extends Enumeration {
  type Translations = String
  val EXAMPLE_MESSAGE_ID = ""
}

class Translations(implicit defaultLocale: String, groups: Seq[TranslationGroup]) {

  private val translationGroups: Map[String, TranslationGroup] = groups.map(k => (k.messageId, k)).toMap

  def apply(resourceString: String, substitutions: (String, String)*)(implicit locale: String): Option[String] = {
    translationGroups.get(resourceString) match {
      case Some(group) =>
        group.apply(locale, defaultLocale).map(_.apply(substitutions:_*))
      case None => {
        //invalid translation ID
        None
      }
    }
  }
}