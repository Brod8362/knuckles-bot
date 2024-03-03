package pw.byakuren.knuckles.external

import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}

import java.util.concurrent.TimeUnit

object DummyAPIAnalytics extends APIAnalytics("") {
  override def updateGuilds(count: Int, shardId: Int): Unit = {}

  override def updateUsage(guildId: Long, channelId: Long): Unit = {}

  override def updateGuildName(guildId: Long, name: String): Unit = {}

  override def log(msg: String): Unit = println(s"[log] $msg")

  override def warning(msg: String): Unit = println(s"[warn] $msg")

  override def error(msg: String): Unit = println(s"[error] $msg")
}