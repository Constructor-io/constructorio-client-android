package io.constructor.core

import android.content.Context
import io.constructor.data.builder.QuizRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConstructorioQuizTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns "golden-key"
        every { preferencesHelper.id } returns "guido-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.quizzesServiceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 79

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getQuizNextQuestion() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithVersionId() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz", null, "11db5ac7-67e1-4000-9000-414d8425cab3").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?version_id=11db5ac7-67e1-4000-9000-414d8425cab3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz", null, null, "Products").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithAnswers() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizNextQuestion("test-quiz", answers).test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResults() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers).test()
        observer.assertComplete().assertValue {
            var quizResultsUrl = it.get()!!.result?.resultsUrl
            quizResultsUrl == "https://ac.cnstrc.com/browse/items?key=xaUaZEQHQWnrNZbq&num_results_per_page=10&collection_filter_expression=%3D%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22W123456%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Purple%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Black%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Blue%22%7D%5D%7D%5D%7D"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/finalize?a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsWithVersionId() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers, "11db5ac7-67e1-4000-9000-414d8425cab3").test()
        observer.assertComplete().assertValue {
            var quizResultsUrl = it.get()!!.result?.resultsUrl
            quizResultsUrl == "https://ac.cnstrc.com/browse/items?key=xaUaZEQHQWnrNZbq&num_results_per_page=10&collection_filter_expression=%3D%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22W123456%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Purple%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Black%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Blue%22%7D%5D%7D%5D%7D"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/finalize?a=1&a=2%2C3&version_id=11db5ac7-67e1-4000-9000-414d8425cab3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers, null, "Products").test()
        observer.assertComplete().assertValue {
            var quizResultsUrl = it.get()!!.result?.resultsUrl
            quizResultsUrl == "https://ac.cnstrc.com/browse/items?key=xaUaZEQHQWnrNZbq&num_results_per_page=10&collection_filter_expression=%3D%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22W123456%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Purple%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Black%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Blue%22%7D%5D%7D%5D%7D"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/finalize?a=1&a=2%2C3&section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val quizRequest = QuizRequest.Builder("test-quiz")
            .setAnswers(answers)
            .setVersionId("11db5ac7-67e1-4000-9000-414d8425cab3")
            .setSection("Products")
            .build()
        val observer = constructorIo.getQuizNextQuestion(quizRequest).test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?a=1&a=2%2C3&version_id=11db5ac7-67e1-4000-9000-414d8425cab3&section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val quizRequest = QuizRequest.Builder("test-quiz")
            .setAnswers(answers)
            .setVersionId("11db5ac7-67e1-4000-9000-414d8425cab3")
            .setSection("Products")
            .build()
        val observer = constructorIo.getQuizResults(quizRequest).test()
        observer.assertComplete().assertValue {
            var quizResultsUrl = it.get()!!.result?.resultsUrl
            quizResultsUrl == "https://ac.cnstrc.com/browse/items?key=xaUaZEQHQWnrNZbq&num_results_per_page=10&collection_filter_expression=%3D%7B%22and%22%3A%5B%7B%22name%22%3A%22group_id%22%2C%22value%22%3A%22W123456%22%7D%2C%7B%22or%22%3A%5B%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Purple%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Black%22%7D%2C%7B%22name%22%3A%22color%22%2C%22value%22%3A%22Blue%22%7D%5D%7D%5D%7D"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/finalize?a=1&a=2%2C3&version_id=11db5ac7-67e1-4000-9000-414d8425cab3&section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.18.6&_dt="
        assert(request.path!!.startsWith(path))
    }
}