package pw.byakuren.knuckles.i18n

case class TranslationsParserException(inner: Throwable, lineNumber: Int) extends Exception {
  override def getCause: Throwable = inner

  override def getMessage: String = s"Parser error on line $lineNumber (${inner.getMessage})"
}
