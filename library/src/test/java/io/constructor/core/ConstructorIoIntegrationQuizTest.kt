package io.constructor.core

import android.content.Context
import io.constructor.data.builder.QuizRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConstructorIoIntegrationQuizTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private val timeBetweenTests = 2000.toLong()

    @Before
    fun setup() {
        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns "ZqXaOfXuBWD4s3XzCI1q"
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.scheme } returns "https"
        every { preferencesHelper.serviceUrl } returns "ac.cnstrc.com"
        every { preferencesHelper.quizzesServiceUrl } returns "quizzes.cnstrc.com"
        every { preferencesHelper.port } returns 443
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("ZqXaOfXuBWD4s3XzCI1q")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getQuizNextQuestionAgainstRealResponse() {
        val request = QuizRequest.Builder("test-quiz").build()
        val observer = constructorIo.getQuizNextQuestion(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.isLastQuestion, false)
        assertEquals(quizResult?.nextQuestion?.id, 1)
        assertEquals(quizResult?.nextQuestion?.title, "This is a test quiz.")
        assertEquals(quizResult?.nextQuestion?.images?.primaryUrl, "/test-asset")
        assertEquals(quizResult?.nextQuestion?.type, "single")
        assertEquals(quizResult?.nextQuestion?.ctaText, null)
        assertEquals(quizResult?.nextQuestion?.description, "This is a test description")
        assertTrue(quizResult?.nextQuestion?.options!!.isNotEmpty())
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.id, 1)
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.value, "Yes")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.attribute?.name, "group_id")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.attribute?.value, "BrandX")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.images?.primaryUrl, "/test-asset")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizNextQuestionWithSingleTypeAnswerAgainstRealResponse() {
        val answers = listOf(listOf("1"))
        val request = QuizRequest.Builder("test-quiz")
            .setAnswers(answers)
            .build()
        val observer = constructorIo.getQuizNextQuestion(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.isLastQuestion, false)
        assertEquals(quizResult?.nextQuestion?.id, 2)
        assertEquals(quizResult?.nextQuestion?.title, "This is a multiple select question")
        assertEquals(quizResult?.nextQuestion?.images?.primaryUrl, "/test-asset")
        assertEquals(quizResult?.nextQuestion?.type, "multiple")
        assertEquals(quizResult?.nextQuestion?.ctaText , null)
        assertEquals(quizResult?.nextQuestion?.description, "This is a multiple select description")
        assertTrue(quizResult?.nextQuestion?.options!!.isNotEmpty())
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.id , 1)
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.value, "Yes")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.attribute?.name, "Color")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.attribute?.value, "Blue")
        assertEquals(quizResult?.nextQuestion?.options?.get(0)?.images?.primaryUrl, "/test-asset")

        Thread.sleep(timeBetweenTests)
    }


    @Test
    fun getQuizNextQuestionWithMultipleTypeAnswerAgainstRealResponse() {
        val answers = listOf(
            listOf("1"),
            listOf("1", "2")
        )
        val request = QuizRequest.Builder("test-quiz")
                .setAnswers(answers)
                .build()
        val observer = constructorIo.getQuizNextQuestion(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.isLastQuestion, false)
        assertEquals(quizResult?.nextQuestion?.id, 3)
        assertEquals(quizResult?.nextQuestion?.title, "Test Cover")
        assertEquals(quizResult?.nextQuestion?.images?.primaryUrl, "/test-asset")
        assertEquals(quizResult?.nextQuestion?.type, "cover")
        assertEquals(quizResult?.nextQuestion?.ctaText , "Test Cover Cta")
        assertEquals(quizResult?.nextQuestion?.description, "This is a test cover")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizNextQuestionWithCoverPageTypeAnswerAgainstRealResponse() {
        val answers = listOf(
            listOf("1"),
            listOf("1", "2"),
            listOf("seen")
        )
        val request = QuizRequest.Builder("test-quiz")
                .setAnswers(answers)
                .build()
        val observer = constructorIo.getQuizNextQuestion(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.isLastQuestion, true)
        assertEquals(quizResult?.nextQuestion?.id, 4)
        assertEquals(quizResult?.nextQuestion?.title, "Test Open Text")
        assertEquals(quizResult?.nextQuestion?.images?.primaryUrl, "/test-asset")
        assertEquals(quizResult?.nextQuestion?.type, "open")
        assertEquals(quizResult?.nextQuestion?.ctaText , null)
        assertEquals(quizResult?.nextQuestion?.inputPlaceholder , "Input Placeholder test")
        assertEquals(quizResult?.nextQuestion?.description, "This is a open text test.")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizNextQuestionWithOpenTextTypeAnswerAgainstRealResponse() {
        val answers = listOf(
                listOf("1"),
                listOf("1", "2"),
                listOf("seen"),
                listOf("true")
        )
        val request = QuizRequest.Builder("test-quiz")
                .setAnswers(answers)
                .build()
        val observer = constructorIo.getQuizNextQuestion(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.isLastQuestion, true)
        assertEquals(quizResult?.nextQuestion, null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizResultsAgainstRealResponse() {
        val answers = listOf(
            listOf("1"),
            listOf("1", "2"),
            listOf("seen")
        )
        val request = QuizRequest.Builder("test-quiz")
            .setAnswers(answers)
            .build()
        val observer = constructorIo.getQuizResults(request).test()
        val quizResult = observer.values()[0].get()
        assertNotNull(quizResult?.versionId)
        assertEquals(quizResult?.result?.resultsUrl, "https://ac.cnstrc.com/browse/items?key=ZqXaOfXuBWD4s3XzCI1q&num_results_per_page=10&collection_filter_expression=%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22BrandX%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22Color%22%2C%22value%22%3A%22Blue%22%7D%2C%7B%22name%22%3A%22Color%22%2C%22value%22%3A%22red%22%7D%5D%7D%5D%7D&i=wacko-the-guid&c=cioand-2.19.4&ui=player-three&s=67")
        assertEquals(quizResult?.result?.filterExpression?.toString(), "{and=[{name=group_id, value=BrandX}, {or=[{name=Color, value=Blue}, {name=Color, value=red}]}]}")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizNextQuestionCRTAgainstRealResponse() {
        runBlocking {
            val answers = listOf(
                listOf("1"),
                listOf("1", "2"),
                listOf("seen")
            )
            val quizResult = constructorIo.getQuizNextQuestionCRT("test-quiz", answers)
            assertNotNull(quizResult?.versionId)
            assertEquals(quizResult?.isLastQuestion, true)
            assertEquals(quizResult?.nextQuestion?.id, 4)
            assertEquals(quizResult?.nextQuestion?.title, "Test Open Text")
            assertEquals(quizResult?.nextQuestion?.images?.primaryUrl, "/test-asset")
            assertEquals(quizResult?.nextQuestion?.type, "open")
            assertEquals(quizResult?.nextQuestion?.ctaText , null)
            assertEquals(quizResult?.nextQuestion?.inputPlaceholder , "Input Placeholder test")
            assertEquals(quizResult?.nextQuestion?.description, "This is a open text test.")
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getQuizResultsCRTAgainstRealResponse() {
        runBlocking {
            val answers = listOf(
                listOf("1"),
                listOf("1", "2"),
                listOf("seen")
            )
            val quizResult = constructorIo.getQuizResultsCRT("test-quiz", answers)
            assertNotNull(quizResult?.versionId)
            assertEquals(quizResult?.result?.resultsUrl, "https://ac.cnstrc.com/browse/items?key=ZqXaOfXuBWD4s3XzCI1q&num_results_per_page=10&collection_filter_expression=%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22BrandX%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22Color%22%2C%22value%22%3A%22Blue%22%7D%2C%7B%22name%22%3A%22Color%22%2C%22value%22%3A%22red%22%7D%5D%7D%5D%7D&i=wacko-the-guid&c=cioand-2.19.4&ui=player-three&s=67")
            assertEquals(quizResult?.result?.filterExpression?.toString(), "{and=[{name=group_id, value=BrandX}, {or=[{name=Color, value=Blue}, {name=Color, value=red}]}]}")
        }
        Thread.sleep(timeBetweenTests)
    }
}
