package pw.byakuren.knuckles.commands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pw.byakuren.knuckles.APIAnalytics

import java.io.File
import java.util.Random

object MemeCommand extends BotCommand("meme", "Submit your meme for knuckles to rate!") {

  private val random = new Random()

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    val file = if (random.nextBoolean()) {
      randomFromArray(approveFiles)
    } else {
      randomFromArray(denyFiles)
    }
    Option(event.getOption("the_meme")) match {
      case Some(param) =>
        event.reply(s"Knuckles has rated `${param.getAsString.replaceAll("`", "")}`").addFile(file).queue()
      case _ =>
        event.replyFile(file).queue()
    }
    try {
      analytics.updateUsage(event.getGuild.getIdLong, event.getChannel.getIdLong)
    } catch {
      case e: Exception =>
        println(s"couldn't update analytics: $e")
    }
  }

  def randomFromArray[T](array: Array[T]): T = {
    array((array.length * Math.random).toInt)
  }

  def approveFiles: Array[File] = new File("approve").listFiles()

  def denyFiles: Array[File] = new File("deny").listFiles()

  override def commandData: SlashCommandData = {
    super.commandData.addOption(OptionType.STRING, "the_meme", "What meme should knuckles rate?", false)
  }
}
