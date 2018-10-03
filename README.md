[![Release](https://jitpack.io/v/Constructor-io/constructorio-client-android.svg)](https://jitpack.io/#Constructor-io/constructorio--client-android) ![Android min](https://img.shields.io/badge/Android-4.4%2B-green.svg) [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Constructor-io/constructorio-client-android/blob/master/LICENSE)

# Constructor.io Android Client

An Android Client for [Constructor.io](http://constructor.io/).  [Constructor.io](http://constructor.io/) provides search as a service that optimizes results using artificial intelligence (including natural language processing, re-ranking to optimize for conversions, and user personalization).

## 1. Install

Please follow the directions at [Jitpack.io](https://jitpack.io/#Constructor-io/constructorio-client-android/v1.0.0) to add the client to your project.

## 2. Retrieve an API key
You can find this in your [Constructor.io dashboard](https://constructor.io/dashboard).  Contact sales if you'd like to sign up, or support if you believe your company already has an account.

## 3. Init the Constructor.io Library

In your Application class add the following code with your key:

```
    override fun onCreate() {
        super.onCreate()
        ConstructorIo.init(this, "your-key")
    }
```
## 4a. Use default out-of-the-box UI

To use the default, out-of-the-box UI, add the Sample Suggestions Fragment to your layout:

```xml
      <fragment
          android:id="@+id/fragment_suggestions"
          android:name="io.constructor.sample.SuggestionsFragment"
          android:layout_width="match_parent"
          android:layout_height="match_parent" />
```

Skip to #5 if not customizing UI

## 4b. Customize UI

### Extend the suggestion screen fragment `BaseSuggestionFragment`.

Implement the following:

|Element name|Returned Type|Description|
|--|--|--|
|`layoutId`|`Int`|Returns your custom layout resource id for the fragment.|
|`getSuggestionsInputId`|`EditText`|Returns the id of the suggestion input field.|
|`getSuggestionListId`|`RecyclerView`|Returns your suggestion list id.|
|`getSuggestionAdapter`|Class that extends from `BaseSuggestionAdapter`|Returns your custom adapter.|
|`getProgressId`|Int`|Returns progress indicator id, used when request is being in progress. Return 0 for no progress|

To see an example of usage, you can look at `SampleActivityCustom`.

### Extend the suggestion items adapter `BaseSuggestionsAdapter`.

Implement the following :

|Element name|Returned Type|Description|
|--|--|--|
|`getItemLayoutId()`|`Int`|Returns your custom adapter item layout id for suggestion.|
|`getSuggestionNameId()`|`Int`|Return your text view id - the text will be the suggestion. |
|`getSuggestionGroupNameId()`|`Int`|Return your text view id - the text will be the suggestion group name, if found. |
|`onViewTypeSuggestion`|`String` text| Triggered when inflating an item which is a suggestion. Read below for more info.|
|`styleHighlightedSpans(spannable: Spannable, spanStart: Int, spanEnd: Int)`|`Unit`| Override to apply custom styling to highlighted part of suggestions search result. `spannable` is highlighted part of suggestion name, start and end mark position of the `spannable` within whole text.|

abstract val styleHighlightedSpans: ((spannable: Spannable, spanStart: Int, spanEnd: Int) -> Unit)?

In case you need to modify something in the ViewHolder (e.g make the group name bold) you can get a reference to it using `getHolder()`

To see an example of usage, you can look at `SampleActivityCustom`.

## 5. Get a reference to the `SuggestionsFragment` and add `ConstructorListener`:

```
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_suggestions) as SuggestionsFragment
        fragment.setConstructorListener(object : ConstructorListener {
            override fun onSuggestionsRetrieved(suggestions: List<Suggestion>) {
                //got suggestions for a query
            }

            override fun onQuerySentToServer(query: String) {
                //request being made to server
            }

            override fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?) {
                //called when user taps on suggestion
            }

            override fun onErrorGettingSuggestions(error: Throwable) {
                //called when there is error getting suggestions
            }
        })
```
# Additional references

## Searching in Groups
Any data value can belong to a group. We will show the group name right below the item itself, if available.

Let's remember the selection event.

```
fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?)
```

Note `group` of type Group. It represents the group for an item and includes the following parameters:

|Type|Name|Description|
|--|--|--|
|String|groupId|The group's id.|
|String|displayName|The group's display name.|
|String|path|The path to get more data for the group.|

Let's say you search for 'apple' and the results are:


```
"suggestions": [
  {
    "data": {
      "groups": [
        {
          "display_name": "food",
          "group_id": "12",
          "path": "/0/222/344"
        },
        {
          "display_name": "gadgets",
          "group_id": "34",
          "path": "/0/252/346/350"
        }
      ]
    },
    "value": "apple"
  }
]
```

We received two groups (food and gadgets) for our suggestion (apple). This means we'll have two suggestions in total:
1. 'apple' in group 'food'
2. 'apple' in group 'gadgets'

When the user taps on (1), term will be `apple` and group name will be `food`.

When the user taps on (2), term will be `apple` and group name will be `gadgets`.

In other words, you can simply check whether the group property is null to find out if the user tapped on a search-in-group result:

```
fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String) {
        if (group == null) {
            // user tapped on an item

        } else {
            // user tapped on a group
        }
    }
```

## ConstructorListener Interface

### onQuerySentToServer

`onQuerySentToServer(query: String)`

Triggered when the query is sent to the server.

Parameter|Type|Description
|--|--|--|
`query`|String|The query made by the user.

### onSuggestionSelected

`onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?)`

Triggered when a suggestion is selected.

|Parameter|Type|Description|
|--|--|--|
|`term`|String|The suggestion selected.|
|`group`|Group|Provides data on the group the selected term belongs to. Otherwise null.|
|`autocompleteSection`|String|The autocomplete section to which the selected term belongs (e.g "Search Suggestions", "Products"...)|

### onSuggestionsRetrieved
`onSuggestionsRetrieved(suggestions: List<Suggestion>)`

Triggered when the results for the query in question is retrieved.

`suggestions` is a list of `Suggestion`s  with the following parameters:

|Parameter|Type|Description|
|--|--|--|
|`text`|String|The name of the suggestion.|
|`groups`|List<Group>|The top groups containing items that match for the query.|
|`matchedTerms`|List<String>|matched terms within the query|
|`sectionName`|String|name of the section eg. "Search Suggestions", "Products"|

### onErrorGettingSuggestions
`override fun onErrorGettingSuggestions(error: Throwable)`

Triggered when error occured while requesting suggestions.

|Parameter|Type|Description|
|--|--|--|
|`error`|Throwable|Exception thrown.|

## BaseSuggestionFragment Abstract Class

Default fragment expose two additional methods for easier implementing custom UI:

### trackSearch()

Manually track search using text in the input box.

### clearSuggestions()

Clear input box and suggestion list.

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
