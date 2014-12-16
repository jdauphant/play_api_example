package models

import formats.MongoJsonFormats
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current

trait MongoModel extends MongoJsonFormats {
  def driver = ReactiveMongoPlugin.driver
  def connection = ReactiveMongoPlugin.connection
  def db = ReactiveMongoPlugin.db
}
