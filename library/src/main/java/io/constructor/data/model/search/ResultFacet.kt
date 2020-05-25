package io.constructor.data.model.search

import java.io.Serializable

data class ResultFacet(val name: String, val values: List<String>?) : Serializable
