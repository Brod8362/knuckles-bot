package pw.byakuren.knuckles.i18n

case class Substitution(templateString: String, variables: Seq[String]) {

  private def replace(prev: String, pair: (String, String)): String = prev.replaceAll(s"<${pair._1}>", pair._2)
  def apply(replacements: (String, String)*): String = {
    //find variables which are required but not provided in the replacements set
    val notAccountedFor = variables.filter(v => !replacements.exists(_._1 == v))
    val extraVariables = replacements.filter(r => !variables.contains(r._1))
    if (notAccountedFor.nonEmpty) {
      throw MissingVariableException(notAccountedFor)
    }
    if (extraVariables.nonEmpty) {
      throw ExcessVariableException(extraVariables.map(_._1))
    }
    replacements.foldLeft(templateString)(replace)
  }
}
