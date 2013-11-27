package dao

import models._

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import java.util.UUID

class FilesDao(files: MongoCollection) {

  def create(guid: UUID, project: Project, version: String, title: String, html: String): File = {
    val urlKey = UrlKey.generate(title)
    val file = File(guid = guid,
      project_guid = project.guid,
      version = version,
      title = title,
      url_key = urlKey,
      keywords = Keywords.generate(Seq(guid.toString, title, html, urlKey)),
      html = html,
      created_at = new DateTime())

    files.insert(grater[File].asDBObject(file))

    file
  }

  def update(file: File) {
    val obj = MongoDBObject("html" -> file.html)
    files.update(q = MongoDBObject("guid" -> file.guid),
      o = MongoDBObject("$set" -> obj),
      upsert = false,
      multi = false)
  }

  def findByGuid(guid: UUID): Option[File] = {
    search(FileQuery(guid = Some(guid))).headOption
  }

  def findAllByProjectGuidAndVersion(projectGuid: UUID, version: String): Iterable[File] = {
    search(FileQuery(project_guid = Some(projectGuid), version = Some(version)))
  }

  def delete(guid: UUID) = {
    // TODO: Soft delete?
    files.remove(MongoDBObject("guid" -> guid))
  }

  def search(query: FileQuery): Iterable[File] = {
    val builder = MongoDBObject.newBuilder
    query.guid.foreach { v => builder += "guid" -> v }
    query.project_guid.foreach { v => builder += "application_guid" -> v }
    query.version.foreach { v => builder += "version" -> v }
    query.title.foreach { v => builder += "title" -> v }
    query.url_key.foreach { v => builder += "url_key" -> v }
    query.query.foreach { v => builder += "keywords" -> v.toLowerCase.r }

    files.find(builder.result()).
      skip(query.pagination.offsetOrDefault).
      limit(query.pagination.limitOrDefault).
      toList.map(grater[File].asObject(_)).sorted
  }

}