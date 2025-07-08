package com.test.epicorKotlin.model

data class Response(var count: Int? = null,
                    var top5Words: Map<String, Long>? = null,
                     var top50Words: Set<String>? = null) {
}