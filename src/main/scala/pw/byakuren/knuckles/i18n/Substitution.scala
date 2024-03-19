package pw.byakuren.knuckles.i18n

case class Substitution(templateString: String) {

  private def replace(prev: String, pair: (String, String)): String = prev.replaceAll(s"<${pair._1}>", pair._2)
  def apply(replacements: (String, String)*): String = {
    replacements.foldLeft(templateString)(replace)
  }
}
