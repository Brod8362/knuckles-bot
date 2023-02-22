package pw.byakuren.knuckles.external

import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}

import java.util.concurrent.TimeUnit

class APIAnalytics(botName: String,
                   influxUri: String = "http://127.0.0.1:8086",
                   database: String = "discord",
                   credentials: Option[(String, String)] = None
                  ) {

  val influx: InfluxDB = credentials match {
    case Some((username, password)) => InfluxDBFactory.connect(influxUri, username, password)
    case _ => InfluxDBFactory.connect(influxUri)
  }
  influx.setDatabase(database)

  def updateGuilds(count: Int, shardId: Int): Unit = {
    influx.write(
      Point.measurement("guild_count")
        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .tag("bot", botName)
        .tag("shard_id", shardId.toString)
        .addField("value", count.toString)
        .build()
    )
  }

  def updateUsage(guildId: Long, channelId: Long): Unit = {
    influx.write(
      Point.measurement("use")
        .tag("bot", botName)
        .tag("channel", channelId.toString)
        .tag("guild", guildId.toString)
        .addField("value", "1")
        .build()
    )
  }

  def updateGuildName(guildId: Long, name: String): Unit = {
    influx.write(
      Point.measurement("guild_names")
        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .tag("guild", guildId.toString)
        .addField("value", name)
        .build()
    )
  }

  private def logGeneric(msg: String, level: String): Unit = {
    println(f"[$level] $msg")
    influx.write(
      Point.measurement("log")
        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .tag("bot", botName)
        .tag("level", level)
        .addField("value", msg)
        .build()
    )
  }

  def log(msg: String): Unit = {
    logGeneric(msg, "log")
  }

  def warning(msg: String): Unit = {
    logGeneric(msg, "warn")
  }

  def error(msg: String): Unit = {
    logGeneric(msg, "error")
  }
}