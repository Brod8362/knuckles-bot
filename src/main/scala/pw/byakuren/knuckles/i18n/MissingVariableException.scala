package pw.byakuren.knuckles.i18n

case class MissingVariableException(keys: Seq[String]) extends Exception {

  override def getMessage: String = s"Variables '${keys.mkString(",")}' are required but were not provided"
}
