package org.oil

import scala.collection.immutable.ListMap
import scala.slick.util.TupleSupport
import play.api.libs.json._
import play.api.mvc.{Request, AnyContent, MultipartFormData}

/**
 * It can be a format error or a validation error
 */
case class FormError(messageKey: String, args: Any*) {
  import play.api.i18n.{Lang, Messages}
  def translate(implicit lang: Lang) = Messages(messageKey, args)
}

object Forms {
  private def productToListMap(product: Product): ListMap[String, Field[_]] = product.productIterator.foldLeft(ListMap.empty[String, Field[_]]) { (map, element) =>
    map + element.asInstanceOf[(String, Field[_])]
  }

  implicit final class FormConverter1[A1](val formTuple: ((String, Field[A1]))) {
    @inline def <>[M](toModel: A1 => M, toProduct: M => Option[Tuple1[A1]]): Form[M, Tuple1[A1]] = {
      new Form[M, Tuple1[A1]]({ case Tuple1(v) => toModel(v.asInstanceOf[A1]) }, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter2[A1, A2](val formTuple: ((String, Field[A1]), (String, Field[A2]))) {
    @inline def <>[M](toModel: (A1, A2) => M, toProduct: M => Option[(A1, A2)]): Form[M, (A1, A2)] = {
      new Form[M, (A1, A2)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter3[A1, A2, A3](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]))) {
    @inline def <>[M](toModel: (A1, A2, A3) => M, toProduct: M => Option[(A1, A2, A3)]): Form[M, (A1, A2, A3)] = {
      new Form[M, (A1, A2, A3)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter4[A1, A2, A3, A4](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4) => M, toProduct: M => Option[(A1, A2, A3, A4)]): Form[M, (A1, A2, A3, A4)] = {
      new Form[M, (A1, A2, A3, A4)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter5[A1, A2, A3, A4, A5](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5) => M, toProduct: M => Option[(A1, A2, A3, A4, A5)]): Form[M, (A1, A2, A3, A4, A5)] = {
      new Form[M, (A1, A2, A3, A4, A5)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter6[A1, A2, A3, A4, A5, A6](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6)]): Form[M, (A1, A2, A3, A4, A5, A6)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter7[A1, A2, A3, A4, A5, A6, A7](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7)]): Form[M, (A1, A2, A3, A4, A5, A6, A7)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter8[A1, A2, A3, A4, A5, A6, A7, A8](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter9[A1, A2, A3, A4, A5, A6, A7, A8, A9](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter13[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter14[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter15[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter16[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter17[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter18[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]), (String, Field[A18]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]), (String, Field[A18]), (String, Field[A19]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter20[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]), (String, Field[A18]), (String, Field[A19]), (String, Field[A20]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter21[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]), (String, Field[A18]), (String, Field[A19]), (String, Field[A20]), (String, Field[A21]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
  implicit final class FormConverter22[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22](val formTuple: ((String, Field[A1]), (String, Field[A2]), (String, Field[A3]), (String, Field[A4]), (String, Field[A5]), (String, Field[A6]), (String, Field[A7]), (String, Field[A8]), (String, Field[A9]), (String, Field[A10]), (String, Field[A11]), (String, Field[A12]), (String, Field[A13]), (String, Field[A14]), (String, Field[A15]), (String, Field[A16]), (String, Field[A17]), (String, Field[A18]), (String, Field[A19]), (String, Field[A20]), (String, Field[A21]), (String, Field[A22]))) {
    @inline def <>[M](toModel: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => M, toProduct: M => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)]): Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)] = {
      new Form[M, (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)](toModel.tupled, toProduct.andThen(_.get), productToListMap(formTuple))
    }
  }
}

case class Form[M, T <: Product](toModel: T => M, toProduct: M => T, fields: ListMap[String, Field[_]], value: Option[M] = None) {

  /**
   * Retrieves a field.
   *
   * For example:
   * {{{
   * val usernameField = userForm("username")
   * }}}
   *
   * @param fieldName the field name
   * @return the field, returned even if the field does not exist
   */
  def apply(fieldName: String): Field[_] = fields.getOrElse(fieldName, Fields.empty)

  //TODO: review the bind methods, not sure if it will work

  /**
   * Binds data to this form, i.e. handles form submission.
   *
   * @param data the data to submit
   * @return a copy of this form, filled with the new data
   */
  def bind(data: Map[String, String]): Form[M, T] = {
    var a = new Array[Any](0)
    val newFields: ListMap[String, Field[_]] = fields.map { case (name, field) =>
      val newField = field.withData(data.get(name).filter(_.nonEmpty))
      newField.value.foreach(value => a :+= value)
      name -> newField
    }(collection.breakOut[ListMap[String, Field[_]], (String, Field[_]), ListMap[String, Field[_]]])

    if (a.length != fields.size) {
      //If the lengths are different that means one of the fields value was a None,
      //which in turn means the field has errors
      this.copy(fields = newFields, value = None)
    } else {
      val product = TupleSupport.buildTuple(a).asInstanceOf[T]
      this.copy(fields = newFields, value = Some(toModel(product)))
    }
  }

  /**
   * Binds data to this form, i.e. handles form submission.
   *
   * @param data Json data to submit
   * @return a copy of this form, filled with the new data
   */
  def bind(data: JsValue): Form[M, T] = bind(fromJson(js = data))

  def fromJson(prefix: String = "", js: JsValue): Map[String, String] = js match {
    case JsObject(fields) => {
      fields.map { case (key, value) => fromJson(Option(prefix).filterNot(_.isEmpty).map(_ + ".").getOrElse("") + key, value) }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsArray(values) => {
      values.zipWithIndex.map { case (value, i) => fromJson(prefix + "[" + i + "]", value) }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsNull => Map.empty
    case JsUndefined() => Map.empty
    case JsBoolean(value) => Map(prefix -> value.toString)
    case JsNumber(value) => Map(prefix -> value.toString)
    case JsString(value) => Map(prefix -> value.toString)
  }

  def bindFromRequest(data: Map[String, Seq[String]]): Form[M, T] = bind {
    data.foldLeft(Map.empty[String, String]) {
      case (s, (key, values)) if key.endsWith("[]") => s ++ values.zipWithIndex.map {
        case (v, i) => (key.dropRight(2) + "[" + i + "]") -> v
      }
      case (s, (key, values)) => s + (key -> values.headOption.getOrElse(""))
    }
  }

  def bindFromRequest()(implicit request: Request[_]): Form[M, T] = bindFromRequest {
    (request.body match {
      case body: AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: AnyContent if body.asMultipartFormData.isDefined => body.asMultipartFormData.get.asFormUrlEncoded
      case body: AnyContent if body.asJson.isDefined => fromJson(js = body.asJson.get).mapValues(Seq(_))
      case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
      case body: MultipartFormData[_] => body.asFormUrlEncoded
      case body: JsValue => fromJson(js = body).mapValues(Seq(_))
      case _ => Map.empty[String, Seq[String]]
    }) ++ request.queryString
  }

  /**
   * Fills this form with a existing value, used for edit forms.
   *
   * @param value an existing value of type `M`, used to fill this form
   * @return a copy of this form filled with the new data
   */
  def fill(value: M): Form[M, T] = {
    val product = toProduct(value)
    var i = 0
    var hasErrors = false
    val newFields: ListMap[String, Field[_]] = fields.map { case (name, field) =>
      val tupleValue: Any = product.productElement(i)
      //We need to cast it because productElement returns a Any
      val newFieldConstructor = (field.withValue _).asInstanceOf[Any => Field[_]]
      val newField = newFieldConstructor(tupleValue)
      hasErrors = hasErrors || newField.hasErrors
      i += 1
      name -> newField
    }(collection.breakOut[ListMap[String, Field[_]], (String, Field[_]), ListMap[String, Field[_]]])
    this.copy(fields = newFields, value = if(hasErrors) None else Some(value))
  }

  /**
   * Handles form results. Either the form has errors, or the submission was a success and a
   * concrete value is available.
   *
   * For example:
   * {{{
   *   anyForm.bindFromRequest().fold(
   *      f => redisplayForm(f),
   *      t => handleValidFormSubmission(t)
   *   )
   * }}}
   *
   * @tparam R common result type
   * @param hasErrors a function to handle forms with errors
   * @param success a function to handle form submission success
   * @return a result `R`.
   */
  def fold[R](hasErrors: Form[M, T] => R, success: M => R): R = value.fold(hasErrors(this))(success)

  def forField[R](name: String)(handler: Field[_] => R): R = handler(this.apply(name))

  /**
   * Returns the concrete value, if the submission was a success.
   *
   * Note that this method fails with an Exception if this form has errors.
   */
  def get: M = value.get

  /**
   * Retrieve the first error for the field with the given {@code name}.
   *
   * @param name field name.
   */
  def error(name: String): Option[FormError] = errors(name).headOption

  /**
   * Retrieve all errors for the field with the given {@code name}.
   *
   * @param name field name.
   */
  def errors(name: String): Seq[FormError] = this.apply(name).errors

  /**
   * Returns the form errors serialized as Json.
   */
  /*def errorsAsJson(implicit lang: play.api.i18n.Lang): play.api.libs.json.JsValue = {
    import play.api.libs.json._
    Json.toJson(
      errors.groupBy(_.key).mapValues { errors =>
        errors.map(e => play.api.i18n.Messages(e.message, e.args: _*))
      }
    )
  }*/

  /**
   * Returns `true` if there is an error related to this form.
   */
  def hasErrors: Boolean = fields.exists{ case (name, field) => field.hasErrors }
}
