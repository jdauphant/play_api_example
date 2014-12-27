package models

import formats.MongoJsonFormats

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import play.modules.reactivemongo.json.collection.JSONCollection

class MongoModel(collectionName: String) extends MongoJsonFormats {
  def driver = ReactiveMongoPlugin.driver
  def connection = ReactiveMongoPlugin.connection
  def db = ReactiveMongoPlugin.db
  def collection: JSONCollection = db.collection[JSONCollection](collectionName)
}
