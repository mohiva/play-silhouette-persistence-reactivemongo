/**
 * Copyright 2016 Mohiva Organisation (license at mohiva dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mohiva.play.silhouette.persistence

import java.io.InputStream

import de.flapdoodle.embed.mongo.config.{ MongodConfigBuilder, Net, RuntimeConfigBuilder }
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{ Command, MongodProcess, MongodStarter }
import de.flapdoodle.embed.process.runtime.Network
import org.apache.commons.io.IOUtils
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Around
import org.specs2.specification.core.Fragments
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsObject, Json, Reads }
import play.api.test.{ PlaySpecification, WithApplication }
import play.api.{ Environment, Logger }
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoModule }
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.commands.DropDatabase
import reactivemongo.api.commands.bson.BSONDropDatabaseImplicits._
import reactivemongo.api.commands.bson.CommonImplicits._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * Runs a fake application with a MongoDB database.
 */
class WithMongo(
  applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder,
  config: Map[String, Any] = MongoConfig.additionalConfig)
  extends WithApplication(
    applicationBuilder
      .configure(config)
      .bindings(new ReactiveMongoModule)
      .build()
  )

/**
 * Executes a before method in the context of the around method.
 */
trait BeforeWithinAround extends Around {
  def before: Any
  abstract override def around[T: AsResult](t: => T): Result = super.around {
    before; t
  }
}

/**
 * Executes a after method in the context of the around method.
 */
trait AfterWithinAround extends Around {
  def after: Any
  abstract override def around[T: AsResult](t: => T): Result = super.around {
    try { t } finally { after }
  }
}

/**
 * Executes before and after methods in the context of the around method.
 */
trait BeforeAfterWithinAround extends Around {
  def before: Any
  def after: Any
  abstract override def around[T: AsResult](t: => T): Result = super.around {
    try { before; t } finally { after }
  }
}

/**
 * A custom specification which starts a MongoDB instance before all the tests, and stops it after all of them.
 *
 * Note: This is handled like a global setup/teardown procedure. So you must clean the database after each test,
 * to get an isolated test case.
 */
trait MongoSpecification extends PlaySpecification {
  sequential
  override def map(fs: => Fragments) = step(start()) ^ fs ^ step(stop())

  /**
   * Defines the port on which the embedded Mongo instance should listen.
   *
   * @return The port on which the embedded Mongo instance should listen.
   */
  def embedConnectionPort(): Int = { MongoConfig.MongoPort }

  /**
   * Defines the Mongo version to start.
   *
   * @return The Mongo version to start.
   */
  def embedMongoDBVersion(): Version.Main = { Version.Main.PRODUCTION }

  /**
   * The MongoDB executable.
   */
  lazy val mongodExecutable = MongodStarter
    .getInstance(new RuntimeConfigBuilder()
      .defaultsWithLogger(Command.MongoD, Logger(this.getClass).logger)
      .build()
    )
    .prepare(new MongodConfigBuilder()
      .version(embedMongoDBVersion())
      .net(new Net(embedConnectionPort(), Network.localhostIsIPv6))
      .build
    )

  /**
   * The mongod process.
   */
  var process: Option[MongodProcess] = None

  /**
   * Starts the MongoDB instance.
   */
  private def start(): Unit = {
    process = Some(mongodExecutable.start)
  }

  /**
   * Stops the MongoDB instance.
   */
  private def stop(): Unit = {
    process.foreach(_.stop)
    mongodExecutable.stop()
  }
}

/**
 * The MongoDB scope.
 */
trait MongoScope extends BeforeAfterWithinAround {
  self: WithApplication =>

  /**
   * Some test fixtures to insert into the database.
   */
  val fixtures: Map[String, Seq[String]] = Map()

  /**
   * The ReactiveMongo API.
   */
  lazy val reactiveMongoAPI = app.injector.instanceOf[ReactiveMongoApi]

  /**
   * The application environment.
   */
  implicit val env = app.injector.instanceOf[Environment]

  /**
   * Inserts the test fixtures.
   */
  def before: Unit = {
    import play.modules.reactivemongo.json._
    Await.result(reactiveMongoAPI.database.flatMap { db =>
      Future.sequence(fixtures.flatMap {
        case (c, files) =>
          val collection = db.collection[JSONCollection](c)
          files.map { file =>
            val json = Helper.loadJson(file)
            collection.insert(Json.toJson(json).as[JsObject])
          }
      })
    }, Duration(60, SECONDS))
  }

  /**
   * Drops the database after the test runs to get an isolated environment.
   */
  def after: Unit = {
    Await.result(reactiveMongoAPI.database.flatMap { db =>
      db.runCommand(DropDatabase, FailoverStrategy.default)
    }, Duration(60, SECONDS))
  }
}

/**
 * Provides some helper methods.
 */
object Helper {

  /**
   * Converts the fixture in different types.
   *
   * @param inputStream The input stream of the fixture.
   */
  case class FixtureConverter(inputStream: InputStream) {
    def asString = scala.io.Source.fromInputStream(inputStream)(scala.io.Codec("UTF-8")).mkString
    def asJson = Json.parse(asString)
    def byteArray = IOUtils.toByteArray(inputStream)
    def as[T](implicit fjs: Reads[T]): T = asJson.as[T]
  }

  /**
   * Loads a test fixture.
   *
   * @param file The test fixture to load.
   * @param env The application environment.
   * @return The test fixture as string.
   */
  def loadFixture(file: String)(implicit env: Environment): FixtureConverter = {
    FixtureConverter(env.resourceAsStream(file).getOrElse {
      throw new Exception("Cannot load test fixture: " + file)
    })
  }

  /**
   * Loads a test fixture as JSON object.
   *
   * @param file The test fixture to load.
   * @param env The application environment.
   * @return The test fixture as JSON object.
   */
  def loadJson(file: String)(implicit env: Environment) = loadFixture(file).asJson
}
