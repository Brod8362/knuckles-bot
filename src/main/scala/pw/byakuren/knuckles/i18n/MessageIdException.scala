package pw.byakuren.knuckles.i18n

case class MessageIdException(resourceString: String) extends Exception {
  override def getMessage: String = f"Message with id $resourceString does not exist"
}
