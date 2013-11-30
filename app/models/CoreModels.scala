package models

import org.joda.time.DateTime
import java.util.UUID
import util.ResourceUtil

case class User(guid: UUID,
                username: String,
                email: String,
                password: String,
                project_guids: Seq[UUID],
                first_name: Option[String] = None,
                last_name: Option[String] = None,
                homepage: Option[String] = None,
                created_at: DateTime = new DateTime,
                salt: Option[String] = None)

object UrlKey {
  def generate(name: String): String = {
    name.toLowerCase.trim.replaceAll("""\s+""", "-").replaceAll("""\.+""", "").
      split("/").map(ResourceUtil.encodeFileName).mkString("/")
  }
}
object Keywords {
  def generate(values: Seq[String]): Seq[String] = {
    values.map(_.toLowerCase)
  }
}

case class Project(guid: UUID,
                   name: String,
                   description: String,
                   url_key: String,
                   repo_url: String,
                   keywords: Seq[String],
                   head_version: String,
                   versions: Seq[String],
                   author_guids: Seq[UUID],
                   created_at: DateTime,
                   updated_at: DateTime)

class ProjectAndAuthors(val project: Project, val authors: Seq[User])
class ProjectAuthorsAndBuilds(val project: Project, val authors: Seq[User], val builds: Seq[Build]) {
  def latestBuild: Option[Build] = builds.find(_.version == project.head_version)
}

case class Build(guid: UUID,
                 project_guid: UUID,
                 version: String,
                 message: String,
                 created_at: DateTime,
                 status: String = "failure")

/**
 * An actual file (in HTML) containing some documentation for
 * this project. API is to provide a title and the actual contents
 * for this file.
 */
case class File(guid: UUID,
                project_guid: UUID,
                version: String,
                title: String,
                filename: String,
                relative_path: String,
                url_key: String,
                keywords: Seq[String],
                content_guid: UUID,
                created_at: DateTime) extends Ordered[File] {

  // Explicit check for files that should come first (like index or readme)
  override def compare(other: File): Int = {
    if (url_key == other.url_key) {
      0
    } else if ("readme".equals(url_key)  || "index".equals(url_key)) {
      -1
    } else if ("readme".equals(other.url_key) || "index".equals(other.url_key)) {
      1
    } else {
      url_key.compare(other.url_key)
    }
  }

}

case class FileContent(guid: UUID, hash_key: String, content_size: Long, content: Array[Byte])

case class FileWithContent(file: File, content: String)

case class View(guid: UUID, file_guid: UUID, user_guid: Option[UUID], created_at: DateTime)

case class FileRollup(file_guid: UUID, count: Long)

case class PopularFile(file: File, project: Project)

case class UserFileRollup(user_guid: UUID, file_guid: UUID, count: Long)
