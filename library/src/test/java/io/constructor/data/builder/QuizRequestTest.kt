package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class QuizRequestTest {
    private val quizId = "test-quiz"
    private val versionId = "11db5ac7-67e1-4000-9000-414d8425cab3"
    private val section = "Products"
    private val answers = listOf("1", "1,2")

    @Test
    fun quizRequestUsingBuilder() {
        val request = QuizRequest.Builder(quizId).build()
        assertEquals(request.quizId, quizId)
    }

    @Test
    fun quizRequestWithVersionIdUsingBuilder() {
        val request = QuizRequest.Builder(quizId)
            .setVersionId(versionId)
            .build()
        assertEquals(request.versionId, versionId)
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
    fun quizRequestUsingDSL() {
        val request = QuizRequest.build(quizId) {
            versionId = this@QuizRequestTest.versionId
            answers = this@QuizRequestTest.answers
            section = this@QuizRequestTest.section
        }
        assertEquals(request.versionId, versionId)
        assertEquals(request.answers, answers)
        assertEquals(request.section, section)
    }
}