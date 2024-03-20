package pw.byakuren.knuckles.commands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import pw.byakuren.knuckles.external.APIAnalytics
import pw.byakuren.knuckles.i18n.Translations

import java.io.File
import java.util.Random

class MemeCommand()(implicit i18n: Translations)
  extends BotCommand(Translations.MEME_COMMAND_NAME, Translations.MEME_COMMAND_DESC) {

  private val random = new Random()

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    implicit val locale: String = event.getUserLocale.getLocale
    Option(event.getOption("the_meme")) match {
      case Some(param) =>
        val opt = Math.abs(param.getAsString.hashCode() + event.getUser.getId.hashCode)
        val file = if (opt % 2 == 0) {
          approveFiles(opt % approveFiles.length)
        } else {
          denyFiles(opt % denyFiles.length)
        }

        val messageContent = i18n.sub(Translations.MEME_COMMAND_REPLY, ("input", param.getAsString.replaceAll("`", "")))
        event
          .reply(messageContent)
          .addFiles(FileUpload.fromData(file)).queue()
      case _ =>
        val file = if (random.nextBoolean()) {
          randomFromArray(approveFiles)
        } else {
          randomFromArray(denyFiles)
        }
        event.replyFiles(FileUpload.fromData(file)).queue()
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
    //TODO: implement i18n here
    val name = i18n.default(Translations.MEME_COMMAND_ARGUMENT_NAME)
    val desc = i18n.default(Translations.MEME_COMMAND_ARGUMENT_DESC)
    super.commandData.addOption(OptionType.STRING, name, desc, false)
  }
}
