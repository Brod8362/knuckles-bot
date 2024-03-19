package pw.byakuren.knuckles.i18n

/**
 * Represents a single message and it's template strings
 * @param messageId The ID of the message
 * @param variables The variables that this message expects
 * @param templates A map with keys set to locales (e.g, `en-US`) and the values set to the templates
 */
case class TranslationGroup(messageId: String, variables: String, templates: Map[String, Substitution]) {
  def supportsLocale(locale: String): Boolean = {
    templates.keys.exists(_ == locale)
  }

  def apply(locale: String, defaultLocale: String): Option[Substitution] = {
    this.apply(locale) match {
      case Some(t) => Some(t)
      case None => apply(defaultLocale)
    }
  }

  def apply(locale: String): Option[Substitution] = {
    templates.get(locale)
  }
}
