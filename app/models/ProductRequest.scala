package models

import play.api.libs.json._

case class ProductRequest (
                            id: Long, date: String, veterinario: Long, veterinarioName: String,
                            storekeeper: Long, storekeeperName: String, status: String,
                            detail: String, type_1: String, userId: Long, userName: String,
                            moduleId: Long, moduleName: String
                          )

object ProductRequest {
  implicit val ProductRequestFormat = Json.format[ProductRequest]
}