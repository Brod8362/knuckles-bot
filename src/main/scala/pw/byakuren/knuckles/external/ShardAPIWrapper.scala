package pw.byakuren.knuckles.external

import java.util.UUID

trait ShardAPIException extends Exception

class BadRequestException extends ShardAPIException
class PoolFullException extends ShardAPIException
class UUIDMismatchException extends ShardAPIException
class SlotAlreadyEmptyException extends ShardAPIException
class IncorrectStateException extends ShardAPIException

class ShardAPIWrapper(val version: String, address: String = "http://127.0.0.1:57537", val uuid: UUID = UUID.randomUUID()) {
  var shardIdOpt: Option[Int] = None
  var maxShardsOpt: Option[Int] = None

  def join(): (Int, Int) = {
    if (shardIdOpt.isDefined) {
      throw new IncorrectStateException
    }
    val response = requests.post(address+"/join",
      data = ujson.Obj(
        "uuid" -> this.uuid.toString,
        "version" -> this.version
      ),
      check = false
    )
    response.statusCode match {
      case 200 =>
        val json = ujson.read(response.text)
        shardIdOpt = Some(json("shard_id").value.asInstanceOf[Double].toInt)
        maxShardsOpt = Some(json("max_shards").value.asInstanceOf[Double].toInt)
        (shardIdOpt.get, maxShardsOpt.get)
      case 400 =>
        throw new BadRequestException()
      case 409 =>
        throw new PoolFullException()
      case _ =>
        throw new RuntimeException(f"unexpected API response ${response.statusCode}")
    }
  }

  def leave(): Unit = {
    shardIdOpt match {
      case Some(shardId) =>
        val response = requests.post(address + "/leave",
          data = ujson.Obj(
            "shard_id" -> shardId,
            "uuid" -> this.uuid.toString
          ),
          check = false
        )
        response.statusCode match {
          case 200 =>
            shardIdOpt = None
            maxShardsOpt = None
          case 400 =>
            throw new BadRequestException()
          case 403 =>
            throw new UUIDMismatchException()
          case 409 =>
            throw new SlotAlreadyEmptyException()
          case _ =>
            throw new RuntimeException(f"unexpected API response ${response.statusCode}")
        }
    }

  }

  def ping(): Unit = {
    shardIdOpt match {
      case Some(shardId) =>
        val response = requests.post(address + "/ping",
          data = ujson.Obj(
            "uuid" -> this.uuid.toString,
            "shard_id" -> shardId
          ),
          check = false
        )
        response.statusCode match {
          case 200 =>
          //ok
          case 400 =>
            throw new BadRequestException()
          case 403 =>
            throw new UUIDMismatchException()
          case 409 =>
            throw new UUIDMismatchException()
          case _ =>
            throw new RuntimeException(f"unexpected API response ${response.statusCode}")
        }
      case None =>
        throw new IncorrectStateException
    }
  }
}
