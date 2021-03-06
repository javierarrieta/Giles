package dao

import models._
import util.{Index, MongoUtil}

import com.novus.salat._
import com.mongodb.casbah.Imports._

class UserFileViewRollupDao {

  private lazy val userFileViewsAllTime = MongoUtil.collection("user_file_views_all_time", Seq(Index(field="file_guid"), Index(field="user_guid")))

  def increment(userGuid: String, fileGuid: String) {
    userFileViewsAllTime.update(q = MongoDBObject("user_guid" -> userGuid, "file_guid" -> fileGuid),
      o = $inc("count" -> 1),
      upsert = true,
      multi = false)
  }

  def numberViews(userGuid: String, fileGuid: String): Long = {
    val result = userFileViewsAllTime.find(MongoDBObject("user_guid" -> userGuid, "file_guid" -> fileGuid)).toList.map(grater[UserFileRollup].asObject(_))
    result.headOption match {
      case Some(rollup: UserFileRollup) => rollup.count
      case None => 0
    }
  }

  def deleteFile(fileGuid: String) = {
    userFileViewsAllTime.remove(MongoDBObject("file_guid" -> fileGuid))
  }

}
