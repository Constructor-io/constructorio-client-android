package io.constructor.data.builder

/**
 * Create a Quiz request object utilizing a builder
 */
class QuizRequest (
    val quizId: String,
    val quizVersionId: String? = null,
    val quizSessionId: String? = null,
    val answers: List<List<String>>? = null,
    val section: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    val filters: Map<String, List<String>>? = null,
    ) {
    private constructor(builder: Builder) : this(
        builder.quizId,
        builder.quizVersionId,
        builder.quizSessionId,
        builder.answers,
        builder.section,
        builder.page,
        builder.perPage,
        builder.filters,
    )

    companion object {
        inline fun build(quizId: String, block: QuizRequest.Builder.() -> Unit = {}) = QuizRequest.Builder(quizId).apply(block).build()
    }

    class Builder(
        val quizId: String,
    ) {
        var answers: List<List<String>>? = null
        var quizVersionId: String? = null
        var quizSessionId: String? = null
        var section: String? = null
        var page: Int? = null
        var perPage: Int? = null
        var filters: Map<String, List<String>>? = null

        fun setAnswers(answers: List<List<String>>): Builder = apply { this.answers = answers }
        fun setQuizVersionId(quizVersionId: String): Builder = apply { this.quizVersionId = quizVersionId }
        fun setQuizSessionId(quizSessionId: String): Builder = apply { this.quizSessionId = quizSessionId }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun setPage(page: Int): Builder = apply { this.page = page }
        fun setPerPage(perPage: Int): Builder = apply { this.perPage = perPage }
        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun build(): QuizRequest = QuizRequest(this)
    }
}