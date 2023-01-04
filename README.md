[![Release](https://jitpack.io/v/Constructor-io/constructorio-client-android.svg)](https://jitpack.io/#Constructor-io/constructorio-client-android) ![Android min](https://img.shields.io/badge/Android-4.4%2B-green.svg) [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Constructor-io/constructorio-client-android/blob/master/LICENSE)

# Constructor.io Android Client

An Android Client for [Constructor.io](http://constructor.io/).  [Constructor.io](http://constructor.io/) provides search as a service that optimizes results using artificial intelligence (including natural language processing, re-ranking to optimize for conversions, and user personalization).

## Documentation
Full API documentation is available on [Github Pages](https://constructor-io.github.io/constructorio-client-android/)

## 1. Install

Please follow the directions at [Jitpack.io](https://jitpack.io/#Constructor-io/constructorio-client-android/v2.19.2) to add the client to your project.

## 2. Retrieve an API key

You can find this in your [Constructor.io dashboard](https://constructor.io/dashboard).  Contact sales if you'd like to sign up, or support if you believe your company already has an account.

## 3. Create a Client Instance

```kotlin
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

// Create the client config
val config = ConstructorIoConfig(
  apiKey = "YOUR API KEY",
  serviceUrl = "ac.cnstrc.com" // default
)

// Create the client instance
ConstructorIo.init(this, config)

// Set the user ID (for a logged in user) used for cross device personalization
ConstructorIo.userId = "uid"
```

## 4. Request Autocomplete Results

```kotlin
// To specify the number of results you want to have returned from each section, you will need to
// ... configure this within the ConstructorIoConfig object before instantiating the client
var config = ConstructorIoConfig(apiKey = "YOUR_API_KEY", autocompleteResultCount = mapOf("Products" to 6, "Search Suggestions" to 10))
ConstructorIo.init(this, config)

var query = "Dav"
var selectedFacet: HashMap<String, MutableList<String>>? = null

// Using RxJava
ConstructorIo.getAutocompleteResults(query, selectedFacet?.map { it.key to it.value })
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it?.let {
      view.renderData(it)
    }
  }
}

// Using Coroutines
runBlocking {
  launch {
    try {
      val autocompleteResults = constructorIo.getAutocompleteResultsCRT(query, selectedFacet?.map { it.key to it.value })
      // Do something with autocompleteResults
    } catch (e: Exception) {
      println(e)
    }
  }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val autocompleteRequest = AutocompleteRequest.Builder("potato")
  .setNumResultsPerSection(mapOf(
    "Products" to 6,
    "Search Suggestions" to 8
  ))
  .setFilters(mapOf(
    "group_id" to listOf("G123"),
    "availability" to listOf("US", "CA")
  ))
  .build()

// Creating a request using DSL
val autocompleteRequest = AutocompleteRequest.build("potato") {
  numResultsPerSection = mapOf(
    "Products" to 6,
    "Search Suggestions" to 8
  )
  filters = mapOf(
    "group_id" to listOf("G123"),
    "availability" to listOf("US", "CA")
  )
}

constructorIo.getAutocompleteResults(autocompleteRequest)
```

## 5. Request Search Results

```kotlin
var page = 1
var perPage = 10
var query = "Dave's bread"
var selectedFacets: HashMap<String, MutableList<String>>? = null
var selectedSortOption: SortOption? = null

// Using RxJava
ConstructorIo.getSearchResults(query, selectedFacets?.map { it.key to it.value }, page = page, perPage = perPage, sortBy = selectedSortOption?.sortBy, sortOrder = selectedSortOption?.sortOrder)
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it.response?.let {
      view.renderData(it)
    }
  }
}

// Using Coroutines
runBlocking {
  launch {
    try {
      val searchResults = constructorIo.getSearchResultsCRT(query)
      // Do something with searchResults
    } catch (e: Exception) {
      println(e)
    }
  }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val searchRequest = SearchRequest.Builder("potato")
  .setFilters(mapOf(
    "group_id" to listOf("G123"),
    "Brand" to listOf("Kings Hawaiin")
  ))
  .setHiddenFields(listOf("hidden_field_1", "hidden_field_2"))
  .build()

// Creating a request using DSL
val searchRequest = SearchRequest.build("potato") {
  filters = mapOf(
    "group_id" to listOf("G123"),
    "Brand" to listOf("Kings Hawaiin")
  )
  hiddenFields = listOf("hidden_field_1", "hidden_field_2")
}

ConstructorIo.getSearchResults(searchRequest)
```

## 6. Request Browse Results

```kotlin
var page = 1
var perPage = 10
var filterName = "group_id"
var filterValue = "Beverages"
var selectedFacets: HashMap<String, MutableList<String>>? = null
var selectedSortOption: SortOption? = null

// Using RxJava
ConstructorIo.getBrowseResults(filterName, filterValue, selectedFacets?.map { it.key to it.value }, page = page, perPage = perPage, sortBy = selectedSortOption?.sortBy, sortOrder = selectedSortOption?.sortOrder)
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it.response?.let {
      view.renderData(it)
    }
  }
}

// Using Coroutines
runBlocking {
  launch {
    try {
      val browseResults = constructorIo.getBrowseResultsCRT(filterName, filterValue)
      // Do something with browseResults
    } catch (e: Exception) {
      println(e)
    }
  }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val browseRequest = BrowseRequest.Builder("group_id", "123")
  .setFilters(mapOf(
    "group_id" to listOf("G1234"),
    "Brand" to listOf("Cnstrc"),
    "Color" to listOf("Red", "Blue")
  ))
  .setHiddenFacets(listOf("hidden_facet_1", "hidden_facet_2"))
  .build()

// Creating a request using DSL
val browseRequest = BrowseRequest.build("group_id", "123") {
  filters = mapOf(
    "group_id" to listOf("G1234"),
    "Brand" to listOf("Cnstrc"),
    "Color" to listOf("Red", "Blue")
  )
  hiddenFacets = listOf("hidden_facet_1", "hidden_facet_2")
}

ConstructorIo.getBrowseResults(browseRequest)
```

## 7. Request Recommendation Results

```kotlin
var numResults = 6
var perPage = 10
var podId = "best_sellers"
var selectedFacets: HashMap<String, MutableList<String>>? = null

// Using RxJava
ConstructorIo.getRecommendationResults(podId, selectedFacets?.map { it.key to it.value }, numResults)
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it.response?.let {
      view.renderData(it)
    }
  }
}

// Using Coroutines
runBlocking {
  launch {
    try {
      val recommendationResults = constructorIo.getRecommendationResultsCRT(podId)
      // Do something with recommendationResults
    } catch (e: Exception) {
      println(e)
    }
  }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val recommendationsRequest = RecommendationsRequest.Builder("product_detail_page")
  .setItemIds(listOf("item_id_123"))
  .build()

// Creating a request using DSL
val recommendationsRequest = RecommendationsRequest.build("product_detail_page") {
  itemIds = listOf("item_id_123")
}

ConstructorIo.getRecommendationResults(recommendationsRequest)
```

## 8. Request Quiz Next Question

```kotlin
val quizId = "quiz-id-1"
val answers = listOf(
    listOf("1"), 
    listOf("1, 2"), 
    listOf("true"), 
)
val versionId = "version-1"
val sectionName = "Products"

// Using RxJava
ConstructorIo.getQuizNextQuestion(quizId, answers, versionId, sectionName)
    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    .subscribe {
        it.onValue {
            it.response?.let {
                view.renderData(it)
            }
        }
    }

// Using Coroutines
runBlocking {
    launch {
        try {
            val quizNextQuestion = constructorIo.getQuizNextQuestionCRT(quizId, answers, versionId, sectionName)
            // Render/Send quizNextQuestion
        } catch (e: Exception) {
            // Handle error
            println(e)
        }
    }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val quizRequest = QuizRequest.Builder("quiz-id-1")
    .setAnswers(listOf(
        listOf("1"),
        listOf("1, 2"),
        listOf("true"),
    ))
    .setVersionId("version-1")
    .setSection("Products")
    .build()

// Creating a request using DSL
val quizRequest = QuizRequest.build("quiz-id-1") {
    answers = listOf(
        listOf("1"),
        listOf("1, 2"),
        listOf("true"),
    )
    versionId = "version-1"
    section = "Products"
}

val quizNextQuestion = ConstructorIo.getQuizNextQuestion(quizRequest)
// Render/Send quizNextQuestion
```

## 9. Request Quiz Results

```kotlin
val quizId = "quiz-id-1"
val answers = listOf(
    listOf("1"), 
    listOf("1, 2"), 
    listOf("true"), 
)
val versionId = "version-1"
val sectionName = "Products"

// Using RxJava
ConstructorIo.getQuizResults(quizId, answers, versionId, sectionName)
    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    .subscribe {
        it.onValue {
            it.response?.let {
                view.renderData(it)
            }
        }
    }

// Using Coroutines
runBlocking {
    launch {
        try {
            val quizResults = constructorIo.getQuizResultCRT(quizId, answers, versionId, sectionName)
            // Render/Send quizResults
        } catch (e: Exception) {
            // Handle error
            println(e)
        }
    }
}
```

### Alternative using Request Builder or DSL
```kotlin
// Creating a request using Request Builder
val quizRequest = QuizRequest.Builder("quiz-id-1")
    .setAnswers(listOf(
        listOf("1"),
        listOf("1, 2"),
        listOf("true"),
    ))
    .setVersionId("version-1")
    .setSection("Products")
    .build()

// Creating a request using DSL
val quizRequest = QuizRequest.build("quiz-id-1") {
    answers = listOf(
        listOf("1"),
        listOf("1, 2"),
        listOf("true"),
    )
    versionId = "version-1"
    section = "Products"
}

val quizResults = ConstructorIo.getQuizResults(quizRequest)
// Render/Send quizResults
```

## 10. Instrument Behavioral Events

The Android Client sends behavioral events to [Constructor.io](http://constructor.io/) in order to continuously learn and improve results for future Autosuggest and Search requests.  The Client only sends events in response to being called by the consuming app or in response to user interaction . For example, if the consuming app never calls the SDK code, no events will be sent.  Besides the explicitly passed in event parameters, all user events contain a GUID based user ID that the client sets to identify the user as well as a session ID.

Three types of these events exist:

1. **General Events** are sent as needed when an instance of the Client is created or initialized
1. **Autocomplete Events** measure user interaction with autocomplete results
1. **Search Events** measure user interaction with search results
1. **Browse Events** measure user interaction with browse results
1. **Conversion Events** measure user events like `add to cart` or `purchase`

### Autocomplete Events

```kotlin
// Track when the user focuses into the search bar (searchTerm)
ConstructorIo.trackInputFocus("")

// Track when the user selects an autocomplete suggestion (searchTerm, originalQuery, sectionName)
ConstructorIo.trackAutocompleteSelect("toothpicks", "tooth", "Search Suggestions")

// Track when the user submits a search  (searchTerm, originalQuery)
ConstructorIo.trackSearchSubmit("toothpicks", "tooth")
```

### Search Events

```kotlin
// Track when search results are loaded into view (searchTerm, resultCount, customerIds of shown items)
ConstructorIo.trackSearchResultsLoaded("tooth", 789, arrayOf("1234567-AB", "1234567-AB"))

// Track when a search result is clicked (itemName, customerId, searchTerm, sectionName, resultId)
ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "tooth", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")

// v2.18.4+ only
// Track when a search result is clicked (itemName, customerId, variationId, searchTerm, sectionName, resultId)
ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "RED", "tooth", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
```

### Browse Events

```kotlin
// Track when browse results are loaded into view (filterName, filterValue, resultCount)
ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", 674)

// Track when a browse result is clicked (filterName, filterValue, customerId, resultPositionOnPage, sectionName, resultId)
ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")

// v2.18.4+ only
// Track when a browse result is clicked (filterName, filterValue, customerId, variationId, resultPositionOnPage, sectionName, resultId)
ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "RED", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
```

### Recommendation Events

```kotlin
// Track when a recommendation result is clicked (podId, strategyId, customerId, variationId, sectionName, resultId, numResultsPerPage, resultPage, resultCount, resultPositionOnPage)
ConstructorIo.trackRecommendationResultClick("Best_Sellers", "User Featured", "7654321-BA", null, "Products", "179b8a0e-3799-4a31-be87-127b06871de2", 4, 1, 4, 2)

// Track when recommendation results are loaded into view (podId, numResultsViewed, resultPage, resultCount, resultId, sectionName)
ConstructorIo.trackRecommendationResultsView("Best_Sellers", 4, 1, 4, "179b8a0e-3799-4a31-be87-127b06871de2", "Products")
```

### Conversion Events

```kotlin
// Track when an item converts (a.k.a. is added to cart) regardless of the user journey that led to adding to cart (itemName, customerId, revenue, searchTerm, section, conversionType)
ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", 12.99, "tooth", "Products", "add_to_cart")

// v2.18.4+ only
// Track when an item converts (a.k.a. is added to cart) regardless of the user journey that led to adding to cart (itemName, customerId, variationId, revenue, searchTerm, section, conversionType)
ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", "RED", 12.99, "tooth", "Products", "add_to_cart")

// Track when items are purchased (customerIds, revenue, orderId)
ConstructorIo.trackPurchase(arrayOf("1234567-AB", "1234567-AB"), 25.98, "ORD-1312343")

// v2.18.4+ only
// Track when items are purchased (PurchaseItems(itemId, variationId?, quantity?), revenue, orderId)
ConstructorIo.trackPurchase(arrayOf(PurchaseItem("TIT-REP-1997", "RED", 2), PurchaseItem("QE2-REP-1969")), 25.98, "ORD-1312343")
```
