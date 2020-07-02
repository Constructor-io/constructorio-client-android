[![Release](https://jitpack.io/v/Constructor-io/constructorio-client-android.svg)](https://jitpack.io/#Constructor-io/constructorio-client-android) ![Android min](https://img.shields.io/badge/Android-4.4%2B-green.svg) [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Constructor-io/constructorio-client-android/blob/master/LICENSE)

# Constructor.io Android Client

An Android Client for [Constructor.io](http://constructor.io/).  [Constructor.io](http://constructor.io/) provides search as a service that optimizes results using artificial intelligence (including natural language processing, re-ranking to optimize for conversions, and user personalization).

## 1. Install

Please follow the directions at [Jitpack.io](https://jitpack.io/#Constructor-io/constructorio-client-android/v2.0.0) to add the client to your project.

## 2. Retrieve an API key

You can find this in your [Constructor.io dashboard](https://constructor.io/dashboard).  Contact sales if you'd like to sign up, or support if you believe your company already has an account.

## 3. Implement the Autocomplete UI

In your Application class add the following code with your key:

```kotlin
override fun onCreate() {
    super.onCreate()
    ConstructorIo.init(this, "YOUR API KEY")
    
    val fragment = supportFragmentManager.findFragmentById(R.id.fragment_suggestions) as SuggestionsFragment
    fragment.setConstructorListener(object : ConstructorListener {
        override fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?) {
            Log.d(TAG, "onSuggestionSelected")
        }
        
        override fun onQuerySentToServer(query: String) {
            Log.d(TAG, "onQuerySentToServer")
        }
        
        override fun onSuggestionsRetrieved(suggestions: List<Suggestion>) {
            Log.d(TAG, "onSuggestionsRetrieved")
        }
        
        override fun onErrorGettingSuggestions(error: Throwable) {
            Log.d(TAG, "handle network error getting suggestion")
        }
    })
}
```

### Selecting Results
To respond to a user selecting an autocomplete result, use the `onSuggestionSelected` method.  If the autocomplete result has both a suggested term to search for and a group to search within (as in Apples in Juice Drinks), the group will be passed into the method.

### Performing Searches
To respond to a user performing a search (instead of selecting an autocomplete result), use the `onQuerySentToServer` method.

## 4. Customize the Autocomplete UI

### Using the Default UI

To use the default, out-of-the-box UI, add the Sample Suggestions Fragment to your layout:

```xml
<fragment
    android:id="@+id/fragment_suggestions"
    android:name="io.constructor.sample.SuggestionsFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

### Using a Custom UI

To fully customize the UI, extend the `BaseSuggestionFragment` and the `BaseSuggestionsAdapter`

```kotlin
class CustomSearchFragment : BaseSuggestionFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.backButton?.setOnClickListener {
            view?.input?.text?.clear()
            clearSuggestions()
        }
        view?.searchButton?.setOnClickListener { triggerSearch() }
    }

    // Return your custom adapter
    override fun getSuggestionAdapter(): BaseSuggestionsAdapter {
        return CustomSuggestionsAdapter()
    }

    // Return your id of the suggestion input field
    override fun getSuggestionsInputId(): Int {
        return R.id.input
    }

    // Return your id of the suggestion list
    override fun getSuggestionListId(): Int {
        return R.id.suggestions
    }

    // Return your progress indicator id, used when request is being in progress. Return 0 for no progress
    override fun getProgressId(): Int {
        return 0
    }
    
    // Return your custom layout resource id for the fragment
    override fun layoutId(): Int {
        return R.layout.fragment_custom_suggestions
    }

}

class CustomSuggestionsAdapter() : BaseSuggestionsAdapter() {
    
    // Triggered when inflating an item which is a suggestion.
    override fun onViewTypeSuggestion(holder: ViewHolder, suggestion: String, highlightedSuggestion: Spannable, groupName: String?) {
        holder.suggestionName.text = highlightedSuggestion
        val spans = highlightedSuggestion.getSpans(0, highlightedSuggestion.length, StyleSpan::class.java)
        spans.forEach { highlightedSuggestion.setSpan(ForegroundColorSpan(Color.parseColor("#222222")), highlightedSuggestion.getSpanStart(it), highlightedSuggestion.getSpanEnd(it), 0) }
        groupName?.let { holder.suggestionGroupName.text = holder.suggestionGroupName.context.getString(R.string.suggestion_group, it) }
    }

    // Return your custom adapter item layout id for suggestion
    override val itemLayoutId: Int
        get() = R.layout.item_suggestion
   
    // Return your text view id - the text will be the suggestion.
    override val suggestionNameId: Int
        get() = R.id.suggestionName
    
    // Return your text view id - the text will be the suggestion group name, if present
    override val suggestionGroupNameId: Int
        get() = R.id.suggestionGroupName

}
```

## 5. Instrument Behavioral Events

The Android Client sends behavioral events to [Constructor.io](http://constructor.io/) in order to continuously learn and improve results for future Autosuggest and Search requests.  The Client only sends events in response to being called by the consuming app or in response to user interaction . For example, if the consuming app never calls the SDK code, no events will be sent.  Besides the explicitly passed in event parameters, all user events contain a GUID based user ID that the client sets to identify the user as well as a session ID.

Three types of these events exist:

1. **General Events** are sent as needed when an instance of the Client is created or initialized
1. **Autocomplete Events** measure user interaction with autocomplete results and extending from `BaseSuggestionFragment` sends them automatically.
1. **Search Events** measure user interaction with search results and the consuming app has to explicitly instrument them itself

### Autocomplete Events

If you decide to extend from the `BaseSuggestionFragment`, these events are sent automatically.

```kotlin
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

val config = ConstructorIoConfig("pharmacy-api-key")
ConstructorIo.init(this, config)

// Track when the user focuses into the search bar (searchTerm)
ConstructorIo.trackInputFocus("")

// Track when the user selects an autocomplete suggestion (searchTerm, originalQuery, sectionName)
ConstructorIo.trackAutocompleteSelect("toothpicks", "tooth", "Search Suggestions")

// Track when the user submits a search  (searchTerm, originalQuery)
ConstructorIo.trackSearchSubmit("toothpicks", "tooth")
```

### Search Events

These events should be sent manually by the consuming app.

```kotlin
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

val config = ConstructorIoConfig("pharmacy-api-key")
ConstructorIo.init(this, config)

// Track when search results are loaded into view (searchTerm, resultCount)
ConstructorIo.trackSearchResultsLoaded("tooth", 789)

// Track when a search result is clicked (itemName, customerId, searchTerm)
ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "tooth")

// Track when a search result converts (itemName, customerId, revenue, searchTerm)
ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", 12.99, "tooth")

// Track when products are purchased (customerIds, revenue, orderId)
ConstructorIo.trackPurchase(arrayOf("1234567-AB", "1234567-AB"), 25.98, "ORD-1312343")
```
