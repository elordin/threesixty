# 360° - Visualization Engine

- [Introduction](#introduction)
- [Requirements](#requirements)
    - [Scala](#scala)
        - [Akka and Spray](#akka-and-spray)
    - [Cassandra](#cassandra)
- [Building](#building)
    - [IntelliJ IDEA](#intellij-idea)
    - [SBT](#sbt)
- [Config](#config)
- [API](#api)
    - [Visualization request](#visualization-request)
        - [Parameters](#parameters)
            - [`visualization`](#visualization)
            - [`processor`](#processor)
    - [Data requests](#data-requests)
        - [Insert](#insert)
            - [`dataPoints`](#dataPoints)
            - [`metadata`](#metadata)
        - [Get](#get)
    - [Help requests / usage info](#help-requests--usage-info)
- [Extending the Engine](#extending-the-engine)
    - [Additional visualizations](#additional-visualizations)
        - [Visualization](#visualization-1)
        - [VisualizationConfig](#visualizationconfig)
        - [VisualizationCompanion](#visualizationcompanion)
        - [Mixin](#mixin)
    - [Additional processing methods](#additional-processing-methods)
        - [Parent classes](#parent-classes)
        - [ProcessingMethodCompanion](#processingmethodcompanion)
        - [Mixin](#mixin-1)

## Introduction
Project repository for the semester project of the Software Engineering lecture as part of the Software Engineering program at Augsburg University.

- [S. Cimander](https://github.com/StefanCimander)
- [T. Engel](https://github.com/ThEngel14)
- [M. Schnappinger](https://github.com/MarkusSchnappi)
- [T. Weber](https://github.com/elordin)
- [J. Wöhrle](https://github.com/SweetyGott)

## Requirements

The visualization engine uses the following technologies:

### Scala

The engine is written in the [Scala programming language](http://scala-lang.org).

Scalas combination of object oriented with functional programming is well suited for the task. It offers good modeling capabilities to properly model the domain, as well as the high level abstraction of the functional world very useful for data processing using e.g. map reduce.

#### Akka and Spray

The underlying framework for concurrency, networking, HTTP request and response handling etc. is [spray](http://spray.io) which in turn builds on [akka](http://akka.io).

Aside from being light weight, efficient, network aware (and thus portable for cluster use) and easy to use it also offers low-on-boilerplate, type safe JSON conversions.

### Cassandra

While the engine is designed to accommodate different database systems, we choose Cassandra for our exemplary implementation.
This decision is bast mostly on the following advantages:

- __Familiar query interface__: Cassandra comes with the CQL (Cassandra Query Language), offering a SQL like syntax, thus providing a familiar interface for administrators and developers.
- __Cluster capable__: To provide scale and resilience for the engine, deployment on a cluster is an option. Cassandra is intended for use on a cluster, thus meeting this requirement as well.
- __Fast write operations__: To accommodate the requirement of _high frequency data_, Cassandras fast write operations come in handy.

Accessing Cassandra using Scala is done with the [Phantom](http://websudos.github.io/phantom/) adapter.

## Building

### IntelliJ IDEA

### SBT

Compiling, testing and running can also be done using the [Scala Build Tool](http://www.scala-sbt.org).

```
sbt compile
```

```
sbt run
```

```
sbt test
```

## Config

The `application.conf` file (in `src/main/ressources`) contains some settings:

- `server.interface` Network interface used by the server. Defaults to `127.0.0.1` if none is given.
- `server.port` Port used by the server. Defaults to `8080` if none is given.
- `database.address` Address of the database as used by the `CassandraDatabaseAdapter`
- `database.keyspace` Keyspace name as used by the `CassandraDatabaseAdapter`
- `akka` contains some settings for [Akka](http://akka.io). For details, see its documentation.


## API

### Visualization request

The most common call uses `"type": "visualization"`. It is used to request a visualization with certain parameters.

```json
{
    "type": "visualization",
    "data": [ ],
    "visualization": { },
    "processor": [ ]
}
```

#### Parameters

- __data__ _required_: A list of identifiers of datasets which will be used for the visualization. The are either simple Strings when the whole dataset should be processed (__not recommended__ for large datasets) or JSON objects defining a start and end point `{ "id": "foo", "from": 0, "to": 1000}` where `from` and `to` are timestamps and can be omitted for a partially bounded selection.
- __visualization__ _optional_: Specifies the desired visualization format. Is deduced when omitted.
- __processor__ _optional_: A list of processing steps applied to the input data. Is deduced when omitted.

##### `visualization`
JSON object with the two keys `type` and `args`.

```
{
    "type": "FooVisualization",
    "args": { }
}
```

- __type__ _required_: Name of the visualization
- __args__ _required_: Object containing visualization specific parameters.

`args` always requires a `width` and a `height`.

For specific details on _name_ and _args_ of individual visualizations, see their usage info.


##### `processor`
List of JSON objects, each describing a single step in the processing pipeline.

A definition of a processing step contains the `method`, i.e. its name, and `args`.

```json
{
    "method": "FooProcessingMethod",
    "args": { }
}
```

`args` always requires an `idMapping`. And ID-mapping specifies what datasets are processed and the IDs assigned to them after they are processed.

```json
{
    "idMapping": {
        "Foo": "FooProcessed",
        "Bar": "Bar"
    }
}
```

In the above example, the two sets with IDs __"Foo"__ and __"Bar"__ are processed.

The result of processing __"Foo"__ is stored as __"FooProcessed"__ and can be accessed that way later on.
Additionally, the original "Foo" can also still be accessed.

__"Bar"__ on the other hand is overwritten with the result of the processing method.
Other processing methods - or the visualization - accessing __"Bar"__ later on, will receive the processed data.
_The unprocessed data is no longer accessible_.


Additional parameters may be required depending on the processing method.
See their specific usage info for details.


### Data requests

Data requests can be used to send and retrieve raw data.

The `action` parameter defines which one it is:
- `"action": "insert"`
- `"action": "get"`

#### Insert

To insert data, pass the data using the `"data"` key

```json
{
    "type": "data",
    "action": "insert",
    "data": {

    }
}
```

Data follows the following format:

```json
{
    "id": "Foo",
    "measurement": "Bar",
    "dataPoints": [ ],
    "metadata": { }
}
```

- `id` is the ID of the dataset
- `measurement` is an arbitrary String describing the data
- `dataPoints` is a list of datapoints
- `metadata` is a metadata object

##### `dataPoints`

Each datapoint must follow the format:

```json
{
    "timestamp": 1234,
    "value": {
        "type": "int | double",
        "value": 12.34
    }
}
```

- `timestamp` ist the timestap for the measurement taken
- `value` is an object with the optional `type` being either `"int"` or `"double"` to define, what type it is, and `value` the actual value (__Note__ that Double values with `type` being `int` are converted to Integers; when not `type` is given, `double` is assumed.)

##### `metadata`

The metadata object can contains the following keys (_all are optional_)

```json
{
    "timeframe": {
        "start": 1234,
        "end": 4567
    },
    "reliability": "Device | User | Unknown",
    "resolution": "High | Middle | Low",
    "scaling": "Nominal | Ordinal",
    "acitivityType": {
        "name": "Foo"
    }
}
```

#### Get

Retrieveing data requires an ID of the dataset.

```json
{
    "type": "data",
    "action": "get",
    "id": "someID"
}
```

### Help requests / usage info

Calling the engine with just `"type": "help"` returns a general usage message.

```
{
    "type": "help"
}
```

Additionally specifying `"for"` gives usage messages for specific parts.

```json
{
    "type": "help",
    "for": "visualizations"
}
```

`visualizations` lists available visualizations.

```json
{
    "type": "help",
    "for": "processingmethods"
}
```

`processingmethods` lists available processing methods.


Using the name of a visualization or a processing method as returned by the two calls above, displays usage information for that specific visualization or processing method.


```json
{
    "type": "help",
    "for": "data"
}
```

`data` lists help on how to get and insert data.

## Extending the Engine

The engine is inherently built to support easy extension of its components.

Using the mini-DSL for engine creation, setup can be done as follow:

```scala
val engine = VisualizationEngine using
         new Visualizer with FooVisualization.Mixin
                        with BarVisualization.Mixin and new Processor
                        with FooProcessingMethod.Mixin
                        with BarProcessingMethod.Mixin and
                        SomeDatabaseAdapter
```

### Additional visualizations

Additional visualizations must comply with a certain format.

They must provide:

- The visualization class extending `Visualization`
- The visualizations configuration extending `VisualizationConfig`
- A companion object for the `VisualizationConfig` that extends `VisualizationCompanion`
- A mixin for adding the visualization to the `Visualizer`

#### Visualization

Being a `Renderable`, a `Visualization` only requires a `toSVG: scala.xml.Elem` method that returns the SVG representation.

#### VisualizationConfig

The `VisualizationConfig` stores all configuration parameters for that graph and acts as a factory for the actual visualization.
For this purpose it has an method

```scala
def apply(pool: DataPool): FooVisualization
```

that takes the datapool, extracts the necessary data and creates the `Visualization`.

#### VisualizationCompanion

`VisualizationCompanion` must implement:

```
// arbitrary name string
def name: String
// converts from JSON to this config
def fromString: (String) => VisualizationConfig
// metadata for this visualization
val metadata: VisualizationMetadata
// A default version of the config
def default: (Seq[Identifier], Int, Int) => VisualizationConfig
```

#### Mixin

The mixin should always comply with the following format

```scala
trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("foochart" -> FooChart)
}
```

Where `foochart` is the name used in API calls and `FooChart` is the companion object.

### Additional processing methods

Similar to adding visualizations, processing methods must also provide certain components:

- The ProcessingMethod extending either `SingleProcessingMethod` or `MultiProcessingMethod`
- A companion object for the ProcessingMethod that extends `ProcessingMethodCompanion`
- The mixing to add the method to the `Processor`

#### Parent classes

`SingleProcessingMethod`s require a single Dataset as input are applied in parallel when used for multiple datasets.
`MultiProcessingMethod` are those that operate on a Set if `ProcessedData`.

#### `ProcessingMethodCompanion`

`ProcessingMethodCompanion` must implement

```
def name: String                                                        // arbirary string name
def fromString: (String) => ProcessingStep                              // conversion from JSON transmitted via HTTP
def default(idMapping: Map[Identifier, Identifier]): ProcessingStep     // creates default version of this processing method
def computeDegreeOfFit(skeletons: InputDataSkeleton): Double            // calculates how well this method fits for processing given data
def computeDegreeOfFit(VisualizationConfig, InputDataSkeleton): Double  // calculates how well this method fits for processing given data in the context of a given visualization
```


#### Mixin

```
trait Mixin extends ProcessingMixins {
    abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
        super.processingInfos + ("foomethod" -> FooMethod)
}
```

Where `foomethod` is the name used in API calls and `FooMethod` is the companion object.
