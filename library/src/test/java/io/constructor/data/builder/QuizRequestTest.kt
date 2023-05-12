package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class QuizRequestTest {
    private val quizId = "test-quiz"
    private val quizVersionId = "e03210db-0cc6-459c-8f17-bf014c4f554d"
    private val quizSessionId = "ca380401-3805-4ded-8f28-638e5a4baa92"
    private val section = "Products"
    private val answers = listOf(
        listOf("1"),
        listOf("1", "2"),
        listOf("true"),
        listOf("seen")
    )
    private val page = 2
    private val perPage = 30
    private val filtersToApply = mapOf(
        "Brand" to listOf("XYZ", "123"),
        "group_id" to listOf("123"),
    )

    @Test
    fun quizRequestUsingBuilder() {
        val request = QuizRequest.Builder(quizId).build()
        assertEquals(request.quizId, quizId)
    }

    @Test
    fun quizRequestWithVersionIdUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setQuizVersionId(quizVersionId)
            .build()
        assertEquals(request.quizVersionId, quizVersionId)
    }

    @Test
    fun quizRequestWithAnswersUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setAnswers(answers)
            .build()
        assertEquals(request.answers, answers)
    }

    @Test
    fun quizRequestWithSectionUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setSection(section)
            .build()
        assertEquals(request.section, section)
    }

    @Test
    fun quizRequestWithSessionIdUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setQuizSessionId(quizSessionId)
            .build()
        assertEquals(request.quizSessionId, quizSessionId)
    }

    @Test
    fun browseRequestWithPageParamsUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setPage(page)
            .setPerPage(perPage)
            .build()
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
    }

    @Test
    fun quizRequestWithFiltersUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setFilters(filtersToApply)
            .build()
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun quizRequestUsingDSL() {
        val request = QuizRequest.build(quizId) {
            quizVersionId = this@QuizRequestTest.quizVersionId
            quizSessionId = this@QuizRequestTest.quizSessionId
            answers = this@QuizRequestTest.answers
            section = this@QuizRequestTest.section
            page = this@QuizRequestTest.page
            perPage = this@QuizRequestTest.perPage
            filters = this@QuizRequestTest.filtersToApply
        }
        assertEquals(request.quizVersionId, quizVersionId)
        assertEquals(request.quizSessionId, quizSessionId)
        assertEquals(request.answers, answers)
        assertEquals(request.section, section)
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
        assertEquals(request.filters, filtersToApply)
    }
}