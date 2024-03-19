package pw.byakuren.knuckles.i18n

case class ExcessVariableException(keys: Seq[String]) extends Exception {
  override def getMessage: String = s"Extraneous variables '${keys.mkString(",")}' were provided"
}
