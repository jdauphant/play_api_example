package models

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current

trait MongoModel {
  def driver = ReactiveMongoPlugin.driver
  def connection = ReactiveMongoPlugin.connection
  def db = ReactiveMongoPlugin.db
}
