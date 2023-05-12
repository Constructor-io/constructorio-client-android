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
    private val page = 2
    private val perPage = 30
    private val filtersToApply = mapOf(
        "Brand" to listOf("XYZ", "123"),
        "group_id" to listOf("123"),
    )

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
        val path = "/v1/quizzes/test-quiz/next?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithVersionId() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz", quizVersionId = "11db5ac7-67e1-4000-9000-414d8425cab3").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?quiz_version_id=11db5ac7-67e1-4000-9000-414d8425cab3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithSessionId() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz", quizSessionId = "31f6bdae-6f1d-482f-b37f-f7a9e346973a").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?quiz_session_id=31f6bdae-6f1d-482f-b37f-f7a9e346973a&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizNextQuestionWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_next_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getQuizNextQuestion("test-quiz", sectionName = "Products").test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
        val path = "/v1/quizzes/test-quiz/next?a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
            var quizId = it.get()!!.quizId
            quizId == "test-quiz"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
            var quizVersionId = it.get()!!.quizVersionId
            quizVersionId == "11db5ac7-67e1-4000-9000-414d8425cab3"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?a=1&a=2%2C3&quiz_version_id=11db5ac7-67e1-4000-9000-414d8425cab3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsWithSessionId() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers, quizSessionId = "31f6bdae-6f1d-482f-b37f-f7a9e346973a").test()
        observer.assertComplete().assertValue {
            var quizVersionId = it.get()!!.quizVersionId
            quizVersionId == "11db5ac7-67e1-4000-9000-414d8425cab3"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?a=1&a=2%2C3&quiz_session_id=31f6bdae-6f1d-482f-b37f-f7a9e346973a&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
        val observer = constructorIo.getQuizResults("test-quiz", answers, sectionName = "Products").test()
        observer.assertComplete().assertValue {
            var quizId = it.get()!!.quizId
            quizId == "test-quiz"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?section=Products&a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsWithPagination() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers, page = page, perPage = perPage).test()
        observer.assertComplete().assertValue {
            var quizId = it.get()!!.quizId
            quizId == "test-quiz"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?page=2&num_results_per_page=30&a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getQuizResultsWithFilters() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("quiz_results_response.json"))
        mockServer.enqueue(mockResponse)
        val answers = listOf(
            listOf("1"),
            listOf("2", "3")
        )
        val observer = constructorIo.getQuizResults("test-quiz", answers, filters = filtersToApply).test()
        observer.assertComplete().assertValue {
            var quizId = it.get()!!.quizId
            quizId == "test-quiz"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?filters%5BBrand%5D=XYZ&filters%5BBrand%5D=123&filters%5Bgroup_id%5D=123&a=1&a=2%2C3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
            .setQuizVersionId("11db5ac7-67e1-4000-9000-414d8425cab3")
            .setSection("Products")
            .build()
        val observer = constructorIo.getQuizNextQuestion(quizRequest).test()
        observer.assertComplete().assertValue {
            var quizQuestionId = it.get()!!.nextQuestion?.id
            quizQuestionId !== null
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/next?a=1&a=2%2C3&quiz_version_id=11db5ac7-67e1-4000-9000-414d8425cab3&section=Products&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
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
            .setQuizVersionId("11db5ac7-67e1-4000-9000-414d8425cab3")
            .setSection("Products")
            .build()
        val observer = constructorIo.getQuizResults(quizRequest).test()
        observer.assertComplete().assertValue {
            var quizId = it.get()!!.quizId
            quizId == "test-quiz"
        }
        val request = mockServer.takeRequest()
        val path = "/v1/quizzes/test-quiz/results?section=Products&a=1&a=2%2C3&quiz_version_id=11db5ac7-67e1-4000-9000-414d8425cab3&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }
}