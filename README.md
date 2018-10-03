[![Release](https://jitpack.io/v/Constructor-io/constructorio-client-android.svg)](https://jitpack.io/#Constructor-io/constructorio--client-android) ![Android min](https://img.shields.io/badge/Android-4.4%2B-green.svg) [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Constructor-io/constructorio-client-android/blob/master/LICENSE)

# Constructor.io Android Client

An Android Client for [Constructor.io](http://constructor.io/).  [Constructor.io](http://constructor.io/) provides search as a service that optimizes results using artificial intelligence (including natural language processing, re-ranking to optimize for conversions, and user personalization).

## 1. Install

Please follow the directions at [Jitpack.io](https://jitpack.io/#Constructor-io/constructorio-client-android/v1.0.0) to add the client to your project.

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

### 4a. Use the Default UI

To use the default, out-of-the-box UI, add the Sample Suggestions Fragment to your layout:

```xml
      <fragment
          android:id="@+id/fragment_suggestions"
          android:name="io.constructor.sample.SuggestionsFragment"
          android:layout_width="match_parent"
          android:layout_height="match_parent" />
```

### 4b. Use a Custom UI

To fully customize the UI, extend the `BaseSuggestionFragment`

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

    // Returns your custom adapter
    override fun getSuggestionAdapter(): BaseSuggestionsAdapter {
        return SuggestionsAdapter()
    }

    // Returns the id of the suggestion input field
    override fun getSuggestionsInputId(): Int {
        return R.id.input
    }

    // Returns the id of the suggestion list
    override fun getSuggestionListId(): Int {
        return R.id.suggestions
    }

    // Returns progress indicator id, used when request is being in progress. Return 0 for no progress
    override fun getProgressId(): Int {
        return 0
    }
    
    // Returns your custom layout resource id for the fragment
    override fun layoutId(): Int {
        return R.layout.fragment_custom_suggestions
    }

}
```

## 5. Instrument Behavioral Events

The Android Client sends behavioral events to [Constructor.io](http://constructor.io/) in order to continuously learn and improve results for future Autosuggest and Search requests.  The Client only sends events in response to being called by the consuming app or in response to user interaction . For example, if the consuming app never calls the SDK code, no events will be sent.  Besides the explicitly passed in event parameters, all user events contain a GUID based user ID that the client sets to identify the user as well as a session ID.

Three types of these events exist:

1. **General Events** are sent as needed when an instance of the Client is created or initialized
1. **Autocomplete Events** measure user interaction with autocomplete results and the `CIOAutocompleteViewController` sends them automatically.
1. **Search Events** measure user interaction with search results and the consuming app has to explicitly instrument them itself

```kotlin
import io.constructor.core.ConstructorIo

// Track search results loaded (term, resultCount)
ConstructorIo.trackSearchResultLoaded("a search term", 123)

// Track search result click (term, itemId, position)
ConstructorIo.trackSearchResultClickThrough("a search term", "an item id", "1")

// Track conversion (item id, term, revenue)
constructorIO.trackConversion("an item id", "a search term", "45.00")
```
