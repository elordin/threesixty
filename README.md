# 360° - Visualization Engine


## Introduction
Project repository for the semester project of the Software Engineering lecture as part of the Software Engineering program at Augsburg University.

## Requirements


## Building


## Config


## API

### Visualization request

Usually the most common call uses `"type": "visualization"`. It is used to request a visualization with certain parameters.

```json
{
    "type": "visualization",
    "data": [ ],
    "visualization": { },
    "processor": [ ]
}
```

#### Parameters

- __data__ _required_: A list of ids (Strings) of datasets which will be used for the visualization.
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
- `value` is an object with `type` being either `"int"` or `"double"` to define, what type it is, and `value` the actual value (__Note__ that Double values with `type` being `int` are converted to Integers)

##### `metadata`

The metadata object can contains the following keys (_all are optional_)

```json
{
    "timeframe": {
        "start": 1234,
        "end": 4567
    }
    "reliability": "Device | User | Unknown",
    "resolution": "High | Middle | Low"
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

`data` lists help on how to insert data.


### Inserting data


## Extending the Engine

### Additional visualizations

### Additional processing methods
