# 360° - Visualization Engine


## Introduction
Project repository for the semester project of the Software Engineering lecture as part of the Software Engineering program at Augsburg University.

## Requirements


## Building


## Config


## API

### Visualization request

The most common call uses `"type": "visualization"`. It is used to request a visualization with certain parameters.

```json
{
    "type": "visualization"
    /* parameters */
}
```

#### Parameters

- __data__ _required_: A list of ids (Strings) of datasets which will be used for the visualization.
- __visualization__ _optional_: Specifies the desired visualization format. Is deduced when omitted.
- __processor__ _optional_: A list of processing steps applied to the input data.

##### `visualization`

##### `processor`


### Help requests

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
