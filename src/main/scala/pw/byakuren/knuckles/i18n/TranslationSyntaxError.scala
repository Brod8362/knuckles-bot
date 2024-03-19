package pw.byakuren.knuckles.i18n

case class TranslationSyntaxError(str: String) extends Exception {
  override def getMessage: String = str
}
