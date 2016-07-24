package controllers

import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._
import scala.concurrent.{ ExecutionContext, Future, Await }
import scala.collection.mutable.ListBuffer
import java.util.LinkedHashMap
import collection.mutable
import scala.collection.mutable.ArrayBuffer
import play.api.data.format.Formats._ 

import javax.inject._
import be.objectify.deadbolt.scala.DeadboltActions
import security.MyDeadboltHandler

class RequestRowController @Inject() (repo: RequestRowRepository, repoRowProductor: RequestRowProductorRepository, repoProductReq: ProductRequestRepository, repoUnit: MeasureRepository, 
                                      repoProduct: ProductRepository, repoProductor: ProductorRepository, val messagesApi: MessagesApi)
                                      (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val newForm: Form[CreateRequestRowForm] = Form {
    mapping(
      "requestId" -> longNumber,
      "productId" -> longNumber,
      "quantity" -> number,
      "status" -> text,
      "measureId" -> longNumber
    )(CreateRequestRowForm.apply)(CreateRequestRowForm.unapply)
  }

  //var unidades = scala.collection.immutable.Map[String, String]("1" -> "Unidad 1", "2" -> "Unidad 2")
  var productRequestsMap = getProductRequestsMap(0)
  var products = getProductsMap()
  var productPrice = 0.0
  var unidades = getMeasuresMap()
  var updatedRow: RequestRow = new RequestRow(0, 0, 1, "", 2, 1, 1, "", 1, "")
  var productRequestId: Long = 0
  
  def getMeasuresMap(): Map[String, String] = {
    Await.result(repoUnit.getListNames().map{ case (res1) => 
      val cache = collection.mutable.Map[String, String]()
      res1.foreach{ case (key: Long, value: String) => 
        cache put (key.toString(), value)
      }
      cache.toMap
    }, 3000.millis)
  }

  def index() = Action.async { implicit request =>
    productRequestId = 0
    repo.list().map { res =>
      Ok(views.html.requestRow_index(new MyDeadboltHandler, res))
    }
  }
  var requestIdParm: Long = 0
  def addGet(requestId: Long) = Action { implicit request =>
    unidades = getMeasuresMap()
    productRequestsMap = getProductRequestsMap(requestId)
    products = getProductsMap()
    requestIdParm = requestId
    Ok(views.html.requestRow_add(new MyDeadboltHandler, requestIdParm, searchProductForm, newForm, productRequestsMap, products, unidades))
  }

  def add = Action.async { implicit request =>
    newForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.requestRow_add(new MyDeadboltHandler, requestIdParm, searchProductForm, errorForm, productRequestsMap, products, unidades)))
      },
      res => {
        var product1 = getProductById(res.productId)
        var productMeasure =  getMeasureById(product1.measureId)
        var requestMeasure = getMeasureById(res.measureId)
        var equivalent =  requestMeasure.quantity.toDouble / productMeasure.quantity.toDouble;

        repo.create(res.requestId, res.productId, products(res.productId.toString()),
                    res.quantity, equivalent * product1.price, res.status,
                    res.measureId, res.measureId.toString).map { resNew =>
          Redirect(routes.RequestRowController.show(resNew.id))
        }
      }
    )
  }

  def getRequestRows = Action.async {
    repo.list().map { res =>
      Ok(Json.toJson(res))
    }
  }

  def getRequestRowsByParent(id: Long) = Action.async {
    repo.listByParent(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

  // update required
  val updateForm: Form[UpdateRequestRowForm] = Form {
    mapping(
      "id" -> longNumber,
      "requestId" -> longNumber,
      "productId" -> longNumber,
      "quantity" -> number,
      "price" -> of[Double],
      "status" -> text,
      "measure" -> longNumber
    )(UpdateRequestRowForm.apply)(UpdateRequestRowForm.unapply)
  }

  def getRequestRowProductos(requestRowId: Long): Seq[RequestRowProductor] = {
    Await.result(repoRowProductor.listByParent(requestRowId).map {  res =>
        res
    }, 3000.millis)
  }

  // to copy
  def show(id: Long) = Action.async { implicit request =>
    // get the productRequestRow
    // products = getProductRequestRows(id)
    val requestRowProductors = getRequestRowProductos(id)
    repo.getById(id).map { res =>
      productRequestId = res(0).requestId
      Ok(views.html.requestRow_show(new MyDeadboltHandler, res(0), requestRowProductors))
    }
  }

  // update required
  def getUpdate(id: Long) = Action.async { implicit request =>
    repo.getById(id).map {case (res) =>
      val anyData = Map(
                          "id" -> id.toString().toString(),
                          "requestId" -> res.toList(0).requestId.toString(),
                          "productId" -> res.toList(0).productId.toString(),
                          "quantity" -> res.toList(0).quantity.toString(),
                          "price" -> res.toList(0).price.toString(),
                          "status" -> res.toList(0).status.toString(),
                          "measureId" -> res.toList(0).measureId.toString()
                        )
      unidades = getMeasuresMap()
      productRequestsMap = getProductRequestsMap(res(0).requestId)
      products = getProductsMap()
      updatedRow = res(0)
      Ok(views.html.requestRow_update(new MyDeadboltHandler , updatedRow, updateForm.bind(anyData), productRequestsMap, products, unidades))
    }
  }

  def getProductRequestsMap(requestId: Long): Map[String, String] = {
    Await.result(repoProductReq.getById(requestId).map{ case (res1) => 
      val cache = collection.mutable.Map[String, String]()
      res1.foreach{ case (res) => 
        cache put (res.id.toString(), res.date.toString())
      }
      cache.toMap
    }, 3000.millis)
  }

  def getProductsMap(): Map[String, String] = {
    Await.result(repoProduct.getListNames().map{ case (res1) => 
      val cache = collection.mutable.Map[String, String]()
      res1.foreach{ case (key: Long, value: String) => 
        cache put (key.toString(), value)
      }
      
      cache.toMap
    }, 3000.millis)
  }

  def getProductPrice(id: Long): Double = {
    Await.result(repoProduct.getById(id).map{ case (res1) => 
      res1(0).price
    }, 3000.millis)
  }

  def getProductById(id: Long): Product = {
    Await.result(repoProduct.getById(id).map{ case (res1) => 
      res1(0)
    }, 3000.millis)
  }

  def getMeasureById(id: Long): Measure = {
    Await.result(repoUnit.getById(id).map{ case (res1) => 
      res1(0)
    }, 3000.millis)
  }

  def getProductorNamesMap(): Map[String, String] = {
    Await.result(repoProductor.getListNames().map{ case (res1) => 
      val cache = collection.mutable.Map[String, String]()
      res1.foreach{ case (key: Long, value: String) => 
        cache put (key.toString(), value)
      }
      
      cache.toMap
    }, 3000.millis)
  }

  def getByIdObj(id: Long): RequestRow = {
    Await.result(repo.getById(id).map { res =>
      res(0)
      }, 200.millis)
  }

// update required
  def getFill(id: Long) = Action.async {
    var row = getByIdObj(id)
    repo.fillById(id, row.productId, row.quantity).map { res =>
      Redirect(routes.ProductRequestController.show(res(0).requestId))
    }
  }

  // delete required
  def delete(id: Long) = Action.async {
    repo.delete(id).map { res =>
      if (productRequestId == 0) {
          Redirect(routes.RequestRowController.index)
        } else {
          Redirect(routes.ProductRequestController.show(productRequestId))
        }
    }
  }

  // to copy
  def getById(id: Long) = Action.async {
    repo.getById(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

  // update required
  def updatePost = Action.async { implicit request =>
    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.requestRow_update(new MyDeadboltHandler, updatedRow, errorForm, Map[String, String](), Map[String, String](), unidades)))
      },
      res => {
        var new_price = res.price
        if (res.price == 0) {
          var product1 = getProductById(res.productId)
          var productMeasure =  getMeasureById(product1.measureId)
          var requestMeasure = getMeasureById(res.measureId)
          var equivalent = requestMeasure.quantity.toDouble / productMeasure.quantity.toDouble;
          new_price = product1.price * equivalent
        }
        repo.update(  
                      res.id, res.requestId, res.productId, products(res.productId.toString),
                      res.quantity, new_price, res.status, res.measureId, res.measureId.toString
                    ).map { _ =>
          Redirect(routes.RequestRowController.show(res.id))
        }
      }
    )
  }

  val searchProductForm: Form[SearchProductForm] = Form {
    mapping(
      "search" -> text
    )(SearchProductForm.apply)(SearchProductForm.unapply)
  }

  def searchProductPost = Action.async { implicit request =>
    searchProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.requestRow_add(new MyDeadboltHandler, requestIdParm, searchProductForm, newForm, productRequestsMap, products, unidades)))
      },
      res => {
        repoProduct.searchProduct(res.search).map { resProducts =>
          val cache = collection.mutable.Map[String, String]()
          resProducts.map { product => 
            cache put (product.id.toString(), product.name.toString)
          }
          products = cache.toMap
          Ok(views.html.requestRow_add(new MyDeadboltHandler, requestIdParm, searchProductForm, newForm, productRequestsMap, products, unidades))
        }
      }
    )
  }
}

case class CreateRequestRowForm(requestId: Long, productId: Long, quantity: Int, status: String, measureId: Long)

case class UpdateRequestRowForm(id: Long, requestId: Long, productId: Long, quantity: Int, price: Double, status: String, measureId: Long)