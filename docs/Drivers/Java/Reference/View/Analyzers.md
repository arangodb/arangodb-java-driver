# Analyzers

[HTTP Interface for Analyzers](https://www.arangodb.com/docs/stable/http/analyzers.html).


## Types

- **AnalyzerEntity**

  - **name**: `String`

    The Analyzer name.

  - **type**: `AnalyzerType`

    The Analyzer type. Can be one of: `identity`, `delimiter`, `stem`, `norm`, `ngram`, `text`

  - **properties**: `Map<String, Object>`

    The properties used to configure the specified type. Value may be a string, an object or null. The default value is null.

  - **features**: `Set<AnalyzerFeature>`

    The set of features to set on the Analyzer generated fields. The default value is an empty array.
    Values can be: `frequency`, `norm`, `position`


## ArangoDatabase.createArangoSearch

`ArangoDatabase.createAnalyzer(AnalyzerEntity options) : AnalyzerEntity`

Creates an Analyzer.


## ArangoDatabase.getAnalyzer

`ArangoDatabase.getAnalyzer(String name) : AnalyzerEntity`

Gets information about an Analyzer

**Arguments**

- **name**: `String`

  The name of the Analyzer


## ArangoDatabase.getAnalyzers

`ArangoDatabase.getAnalyzers() : Collection<AnalyzerEntity>`

Retrieves all Analyzers definitions.


## ArangoDatabase.deleteAnalyzer

`ArangoDatabase.deleteAnalyzer(String name) : void`

Deletes an Analyzer.

**Arguments**

- **name**: `String`

  The name of the Analyzer


## ArangoDatabase.deleteAnalyzer

`ArangoDatabase.deleteAnalyzer(String name, AnalyzerDeleteOptions options) : void`

Deletes an Analyzer.

**Arguments**

- **name**: `String`

  The name of the Analyzer

- **options**: `AnalyzerDeleteOptions`

  - **force**: `Boolean`

    The Analyzer configuration should be removed even if it is in-use. The default value is false.
