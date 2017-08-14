package controllers

import javax.inject._

import com.github.tototoshi.play2.json4s.native.Json4s

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

//import play.api.libs.json.Json
import org.json4s._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

//case class MessageAndId(id: String, message: CommentMessage)
case class Campaign(name: String, startDate: Long, category: String, bid: Int)
case class Ad(bid: Int)
case class CampaignWithAdds(name: String, startDate: Long, category: String, bid: Int,
    ads: List[Ad]) {

}
object CampaignWithAdds {
  def fromCampaign(c: Campaign): CampaignWithAdds = {
    val ads = (1 to 3).map(x => Ad(Random.nextInt(10))).toList
    CampaignWithAdds(c.name, c.startDate, c.category, c.bid, ads)
  }
}
case class CampaignWithHighestBid(name: String, startDate: Long, category: String, hisghestBid: Int)

@Singleton
class CampaignController extends Controller with Json4s {
  implicit val formats = new DefaultFormats {}
  val memPersistance = ArrayBuffer.empty[CampaignWithAdds]

  init()

  def addCampaign = Action.async(json) { implicit request =>
    Future {
      println("************************\n" +
        "******************************\n" +
        "add Campaign was called")
      val campaign = request.body.extract[Campaign]
      val campaignWithAds = CampaignWithAdds.fromCampaign(campaign)
      memPersistance += campaignWithAds
      Ok(Json.obj()).withHeaders(headers: _*)
    }
  }

  def getAddsByCampaignCategory(category: String) = Action { implicit request =>

    val campaigns = memPersistance.filter(c => (c.category.equals(category)) && (c.startDate <= System.currentTimeMillis()))
    val campaignsWithHighestBid = campaigns.map { c =>
      val highestBid = c.ads.map(a => a.bid).max
      CampaignWithHighestBid(c.name, c.startDate, c.category, highestBid)
    }
    Ok(Extraction.decompose(campaignsWithHighestBid))

  }

  def headers = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, DELETE, PUT",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, Accept, Authorization",
    "Access-Control-Allow-Credentials" -> "true"
  )
  def options(p: String) = Action { request =>
    NoContent.withHeaders(headers: _*)
  }

  def init() = {

  }

}
