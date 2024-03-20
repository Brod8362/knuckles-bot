package pw.byakuren.knuckles

import net.dv8tion.jda.api.interactions.DiscordLocale

object Implicits {
  implicit class StringImplicits(x: String) {
      def asDiscordLocale: Option[DiscordLocale] = {
        Option(DiscordLocale.from(x))
      }
  }
}
