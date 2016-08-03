package models

import play.api.libs.json._

case class Product 	(
					id: Long, name: String, cost: Double,
					percent: Double, price: Double, description: String,
					measureId: Long, measureName: String, currentAmount: Int,
					type_1: String
					)

object Product {
  implicit val insumoFormat = Json.format[Product]
}