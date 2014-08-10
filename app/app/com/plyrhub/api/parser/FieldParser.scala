package com.plyrhub.api.parser

import scala.util.parsing.combinator.RegexParsers

// FieldParser and FieldSelector are taken from netflix.FieldSelectorParser and adapted to work with JsValue objects.

object FieldParser {
  def parse(expr: String) = {
    val parser = new FieldParser
    parser.parseExpr(expr)
  }
}

class FieldParser extends RegexParsers {

  def expression: Parser[FieldSelectorExpr] = keySelectExpr

  def keySelectExpr = ":(" ~> repsep(subExpr, ",") <~ ")" ^^ (values => {
    KeySelectExpr(Map.empty ++ values.map(t => {
      t._1 -> t._2.getOrElse(FixedExpr(matches = true))
    }))
  })

  def subExpr = id ~ (
    equalExpr |
      notEqualExpr |
      regexExpr |
      invRegexExpr |
      expression).?

  def equalExpr = "=" ~> literalExpr ^^ (value => EqualExpr(value))

  def notEqualExpr = "!=" ~> literalExpr ^^ (value => NotEqualExpr(value))

  def regexExpr = "~" ~> regexLiteral ^^ (value => RegexExpr(value, invert = false))

  def invRegexExpr = "!~" ~> regexLiteral ^^ (value => RegexExpr(value, invert = true))

  def id = regex("[a-zA-Z0-9_\\.\\-]*".r)

  def literalExpr =
    stringLiteral |
      nullLiteral |
      trueLiteral |
      falseLiteral |
      integerLiteral |
      floatLiteral

  def stringLiteral = regex("\"[^\"]*\"".r) ^^ (value => {
    value.substring(1, value.length - 1)
  })

  def nullLiteral = "null" ^^ (value => null)

  def trueLiteral = "true" ^^ (value => true)

  def falseLiteral = "false" ^^ (value => false)

  def integerLiteral = regex("-?[0-9]+".r) ^^ (value => value.toInt)

  def floatLiteral = regex(floatRegex) ^^ (value => value.toDouble)

  val floatRegex = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?".r

  def regexLiteral = "/" ~> regex("[^/]*".r) <~ "/"

  def parseExpr(expr: String): FieldSelectorExpr = {
    def fail(expr: String, msg: String) = {
      throw new IllegalArgumentException(
        "could not parse expression '" + expr + "': " + msg)
    }

    val result = parseAll(expression, expr) match {
      case Success(res, _) => res
      case Failure(msg, _) => fail(expr, msg)
      case Error(msg, _) => fail(expr, msg)
      case _ => fail(expr, "unknown")
    }
    result
  }
}