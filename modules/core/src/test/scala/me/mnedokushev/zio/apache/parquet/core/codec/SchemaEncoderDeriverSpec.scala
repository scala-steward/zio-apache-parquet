package me.mnedokushev.zio.apache.parquet.core.codec

import me.mnedokushev.zio.apache.parquet.core.Schemas.PrimitiveDef
import me.mnedokushev.zio.apache.parquet.core.{ Fixtures, Schemas }
import zio._
import zio.schema._
import zio.test._

import java.util.UUID
//import scala.annotation.nowarn

object SchemaEncoderDeriverSpec extends ZIOSpecDefault {

  sealed trait MyEnum
  object MyEnum {
    case object Started    extends MyEnum
    case object InProgress extends MyEnum
    case object Done       extends MyEnum

    implicit val schema: Schema[MyEnum] = DeriveSchema.gen[MyEnum]
  }

  case class Record(a: Int, b: Option[String])
  object Record {
    implicit val schema: Schema[Record] = DeriveSchema.gen[Record]
  }

  // Helper for being able to extract type parameter A from a given schema in order to cast the type of encoder<
  private def encode[A](encoder: SchemaEncoder[?], schema: Schema[A], name: String, optional: Boolean) =
    encoder.asInstanceOf[SchemaEncoder[A]].encode(schema, name, optional)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("SchemaEncoderDeriverSpec")(
      test("primitive") {
        def named(defs: List[PrimitiveDef], names: List[String]) =
          defs.zip(names).map { case (schemaDef, name) =>
            schemaDef.named(name)
          }

        val encoders: List[SchemaEncoder[?]] =
          List(
            Derive.derive[SchemaEncoder, String](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, Boolean](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, Byte](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, Short](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, Int](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, Long](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, UUID](SchemaEncoderDeriver.default)
          )
        val schemas: List[Schema[?]]         =
          List(
            Schema.primitive[String],
            Schema.primitive[Boolean],
            Schema.primitive[Byte],
            Schema.primitive[Short],
            Schema.primitive[Int],
            Schema.primitive[Long],
            Schema.primitive[UUID]
          )
        val names                            =
          List(
            "string",
            "boolean",
            "byte",
            "short",
            "int",
            "long",
            "uuid"
          )
        val schemaDefs                       = List(
          Schemas.string,
          Schemas.boolean,
          Schemas.byte,
          Schemas.short,
          Schemas.int,
          Schemas.long,
          Schemas.uuid
        )
        val optionalDefs                     =
          schemaDefs.map(_.optional)
        val requiredDefs                     =
          schemaDefs.map(_.required)

        val expectedOptional = named(optionalDefs, names)
        val expectedRequired = named(requiredDefs, names)

        encoders
          .zip(schemas)
          .zip(names)
          .zip(expectedOptional)
          .zip(expectedRequired)
          .map { case ((((encoder, schema), name), expOptional), expRequired) =>
            val tpeOptional = encode(encoder, schema, name, optional = true)
            val tpeRequired = encode(encoder, schema, name, optional = false)

            assertTrue(tpeOptional == expOptional, tpeRequired == expRequired)
          }
          .reduce(_ && _)
      },
      test("record") {
        val name        = "record"
        val encoder     = Derive.derive[SchemaEncoder, Record](SchemaEncoderDeriver.default)
        val tpeOptional = encoder.encode(Record.schema, name, optional = true)
        val tpeRequired = encoder.encode(Record.schema, name, optional = false)
        val schemaDef   = Schemas.record(
          Chunk(
            Schemas.int.required.named("a"),
            Schemas.string.optional.named("b")
          )
        )

        assertTrue(
          tpeOptional == schemaDef.optional.named(name),
          tpeRequired == schemaDef.required.named(name)
        )
      },
      test("record arity > 22") {
        val name        = "arity"
        val encoder     = Derive.derive[SchemaEncoder, Fixtures.Arity23](SchemaEncoderDeriver.default)
        val tpeOptional = encoder.encode(Fixtures.Arity23.schema, name, optional = true)
        val tpeRequired = encoder.encode(Fixtures.Arity23.schema, name, optional = false)
        val schemaDef   = Schemas.record(
          Chunk(
            Schemas.int.required.named("a"),
            Schemas.string.optional.named("b"),
            Schemas.int.required.named("c"),
            Schemas.int.required.named("d"),
            Schemas.int.required.named("e"),
            Schemas.int.required.named("f"),
            Schemas.int.required.named("g"),
            Schemas.int.required.named("h"),
            Schemas.int.required.named("i"),
            Schemas.int.required.named("j"),
            Schemas.int.required.named("k"),
            Schemas.int.required.named("l"),
            Schemas.int.required.named("m"),
            Schemas.int.required.named("n"),
            Schemas.int.required.named("o"),
            Schemas.int.required.named("p"),
            Schemas.int.required.named("q"),
            Schemas.int.required.named("r"),
            Schemas.int.required.named("s"),
            Schemas.int.required.named("t"),
            Schemas.int.required.named("u"),
            Schemas.int.required.named("v"),
            Schemas.int.required.named("w")
          )
        )

        assertTrue(
          tpeOptional == schemaDef.optional.named(name),
          tpeRequired == schemaDef.required.named(name)
        )
      },
      test("sequence") {
        val name                             = "mylist"
        val encoders: List[SchemaEncoder[?]] =
          List(
            Derive.derive[SchemaEncoder, List[String]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Boolean]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Byte]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Short]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Int]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Long]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[UUID]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[String]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[Boolean]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[Byte]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[Short]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[Int]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[Long]]](SchemaEncoderDeriver.default),
            Derive.derive[SchemaEncoder, List[Option[UUID]]](SchemaEncoderDeriver.default)
          )
        val schemas: List[Schema[?]]         =
          List(
            Schema.list[String],
            Schema.list[Int],
            Schema.list[Option[String]],
            Schema.list[Option[Int]]
          )
        val elements                         =
          List(
            Schemas.string,
            Schemas.boolean,
            Schemas.byte,
            Schemas.short,
            Schemas.int,
            Schemas.long,
            Schemas.uuid
          )
        val schemaDefs                       =
          (elements.map(_.required) ++ elements.map(_.optional))
            .map(_.named("element"))
            .map(Schemas.list)
        val expectedOptional                 =
          schemaDefs.map(_.optional.named(name))
        val expectedRequired                 =
          schemaDefs.map(_.required.named(name))

        encoders
          .zip(schemas)
          .zip(expectedOptional)
          .zip(expectedRequired)
          .map { case (((encoder, schema), expOptional), expRequired) =>
            val tpeOptional = encode(encoder, schema, name, optional = true)
            val tpeRequired = encode(encoder, schema, name, optional = false)

            assertTrue(
              tpeOptional == expOptional,
              tpeRequired == expRequired
            )
          }
          .reduce(_ && _)
      },
      test("map") {
        val name    = "mymap"
        val encoder = Derive.derive[SchemaEncoder, Map[String, Int]](SchemaEncoderDeriver.default)
        val tpe     = encoder.encode(Schema.map[String, Int], name, optional = true)

        assertTrue(
          tpe == Schemas
            .map(Schemas.string.required.named("key"), Schemas.int.required.named("value"))
            .optional
            .named(name)
        )
      },
      test("enum") {
        val name    = "myenum"
        val encoder = Derive.derive[SchemaEncoder, MyEnum](SchemaEncoderDeriver.default)
        val tpe     = encoder.encode(Schema[MyEnum], name, optional = true)

        assertTrue(tpe == Schemas.enum0.optional.named(name))
      }
//      test("summoned") {
      //        // @nowarn annotation is needed to avoid having 'variable is not used' compiler error
      //        @nowarn
      //        implicit val intEncoder: SchemaEncoder[Int] = new SchemaEncoder[Int] {
      //          override def encode(schema: Schema[Int], name: String, optional: Boolean): Type =
      //            Schemas.uuid.optionality(optional).named(name)
      //        }
      //
      //        val name    = "myrecord"
      //        val encoder = Derive.derive[SchemaEncoder, Record](SchemaEncoderDeriver.summoned)
      //        val tpe     = encoder.encode(Record.schema, name, optional = true)
      //
      //        assertTrue(
      //          tpe == Schemas
      //            .record(Chunk(Schemas.uuid.required.named("a"), Schemas.string.optional.named("b")))
      //            .optional
      //            .named(name)
      //        )
      //      }
    )

}
