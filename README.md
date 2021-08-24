[![Release](https://jitpack.io/v/Constructor-io/constructorio-client-android.svg)](https://jitpack.io/#Constructor-io/constructorio-client-android) ![Android min](https://img.shields.io/badge/Android-4.4%2B-green.svg) [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Constructor-io/constructorio-client-android/blob/master/LICENSE)

# Constructor.io Android Client

An Android Client for [Constructor.io](http://constructor.io/).  [Constructor.io](http://constructor.io/) provides search as a service that optimizes results using artificial intelligence (including natural language processing, re-ranking to optimize for conversions, and user personalization).

Full API documentation is available on [Github Pages](https://constructor-io.github.io/constructorio-client-android/)

## 1. Install

Please follow the directions at [Jitpack.io](https://jitpack.io/#Constructor-io/constructorio-client-android/v2.6.0) to add the client to your project.

## 2. Retrieve an API key

You can find this in your [Constructor.io dashboard](https://constructor.io/dashboard).  Contact sales if you'd like to sign up, or support if you believe your company already has an account.

## 3. Create a Client Instance

```kotlin
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

// Create the client config
let config = ConstructorIoConfig("YOUR API KEY")

// Create the client instance
ConstructorIo.init(this, config)

// Set the user ID (if a logged in and known user) for cross device personalization
ConstructorIo.userId = "uid"
```

## 4. Request Autocomplete Results

```kotlin
var query = "Dav"
var selectedFacet: HashMap<String, MutableList<String>>? = null

ConstructorIo.getAutocompleteResults(query, selectedFacet?.map { it.key to it.value })
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it?.let {
      view.renderData(it)
    }
  }
}
```

## 5. Request Search Results

```kotlin
var page = 1
var perPage = 10
var query = "Dave's bread"
var selectedFacets: HashMap<String, MutableList<String>>? = null
var selectedSortOption: SortOption? = null

ConstructorIo.getSearchResults(query, selectedFacets?.map { it.key to it.value }, page = page, perPage = limit, sortBy = selectedSortOption?.sortBy, sortOrder = selectedSortOption?.sortOrder)
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it.response?.let {
      view.renderData(it)
    }
  }
}
```

## 6. Request Browse Results

```kotlin
var page = 1
var perPage = 10
var filterName = "group_id"
var filterValue = "Beverages"
var selectedFacets: HashMap<String, MutableList<String>>? = null
var selectedSortOption: SortOption? = null

ConstructorIo.getBrowseResults(filterName, filterValue, selectedFacets?.map { it.key to it.value }, page = page, perPage = limit, sortBy = selectedSortOption?.sortBy, sortOrder = selectedSortOption?.sortOrder)
.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
.subscribe {
  it.onValue {
    it.response?.let {
      view.renderData(it)
    }
  }
}
```

## 7. Instrument Behavioral Events

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
```

### Browse Events

```kotlin
// Track when browse results are loaded into view (filterName, filterValue, resultCount)
ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", 674)

// Track when a browse result is clicked (filterName, filterValue, customerId, resultPositionOnPage, sectionName, resultId)
ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
```

### Conversion Events

```kotlin
// Track when an item converts (a.k.a. is added to cart) regardless of the user journey that led to adding to cart (itemName, customerId, revenue, searchTerm, section, conversionType)
ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", 12.99, "tooth", "Products", "add_to_cart")

// Track when items are purchased (customerIds, revenue, orderId)
ConstructorIo.trackPurchase(arrayOf("1234567-AB", "1234567-AB"), 25.98, "ORD-1312343")
```
