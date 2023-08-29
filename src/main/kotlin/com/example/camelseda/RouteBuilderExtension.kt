package com.example.camelseda

import org.apache.camel.Exchange
import org.apache.camel.Expression
import org.apache.camel.model.OnExceptionDefinition
import org.apache.camel.model.RouteDefinition

fun <T, R> RouteDefinition.transform(`fun`: (T) -> R): RouteDefinition = this.transform(getExpressionBody(`fun`))
fun <T, R> OnExceptionDefinition.transform(`fun`: (T) -> R): OnExceptionDefinition = this.transform(getExpressionBody(`fun`))

fun <T, R> getExpressionBody(`fun`: (T) -> R): Expression = object : Expression {
  @Suppress("UNCHECKED_CAST")
  @SuppressWarnings("unchecked")
  override fun <V> evaluate(exchange: Exchange, type: Class<V>): V {
    return `fun`(exchange.`in`.body as T) as V
  }
}

fun <T, R> RouteDefinition.transformExchange(`fun`: (T) -> R): RouteDefinition = this.transform(getExpressionExchange(`fun`))
fun <T, R> RouteDefinition.dynamicRouter(`fun`: (T) -> R): RouteDefinition = this.dynamicRouter(getExpressionExchange(`fun`)).end()

fun <T, R> getExpressionExchange(`fun`: (T) -> R): Expression = object : Expression {
  @Suppress("UNCHECKED_CAST")
  @SuppressWarnings("unchecked")
  override fun <V> evaluate(exchange: Exchange, type: Class<V>): V {
    return `fun`(exchange as T) as V
  }
}