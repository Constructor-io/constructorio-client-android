package io.constructor.data.builder

/**
 * Create a Quiz request object utilizing a builder
 */
class QuizRequest (
    val quizId: String,
    val a: List<String>? = null,
    val versionId: String? = null,
    val section: String? = null,
) {
    private constructor(builder: Builder) : this(
        builder.quizId,
        builder.a,
        builder.versionId,
        builder.section,
    )

    companion object {
        inline fun build(quizId: String, block: QuizRequest.Builder.() -> Unit = {}) = QuizRequest.Builder(quizId).apply(block).build()
    }

    class Builder(
        val quizId: String,
    ) {
        var a: List<String>? = null
        var versionId: String? = null
        var section: String? = null

        fun setA(a: List<String>): Builder = apply { this.a = a }
        fun setVersionId(versionId: String): Builder = apply { this.versionId = versionId }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun build(): QuizRequest = QuizRequest(this)
    }
}