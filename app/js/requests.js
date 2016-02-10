
/* ****************** */
/*   JSON Requests    */
/* ****************** */

function makeLineChartVisualization(ids, title) {
    return {
        "type": "linechart",
        "args": {
        "ids": ids,
            "width": 512,
            "height": 350,
            "border": {"top": 10, "bottom": 50, "left": 70, "right": 20},
            "title": {
                "title": title,
                "position": "bottom",
                "fontFamily": "Calibri",
            },
            "xAxis": {
                "showLabels": false,
                "arrowSize": 5,
                "arrowFilled": true,
                "showGrid": false  
            },
            "yAxis": {
                "arrowSize": 5,
                "arrowFilled": true,
                "unitFontFamily": "Calibri"
            }
        }
    }
}

function makeBarChartVisualization(ids, title, yMax, yUnit) {
    return {
        "type": "barchart",
        "args": {
            "ids": ids,
            "width": 512,
            "height": 256,
            "border": {"top": 10, "bottom": 40, "left": 70, "right": 20},
            "xAxis": {
                "arrowSize": 5,
                "arrowFilled": true,
            },
            "yAxis": {
                "max": yMax,
                "unit": yUnit,
                "arrowSize": 5,
                "arrowFilled": true,
            },
            "descriptionLabelSize": 0,
            "colorScheme": "green",
            "fontFamily": "Calibri",
            "title": {
                "title": title,
                "position": "bottom",
                "size": 23,
                "verticalOffset": 10,
                "fontFamily": "Calibri"
            }
        }
    }
}

function makePieChartVisualization(ids, legendOffset) {
    return {
        "type": "piechart",
        "args": {
            "ids": ids,
            "width": 400,
            "height": 230,
            "border": {"top": 15, "bottom": 35, "left": 80, "right": 0},
            "colorScheme": "green",
            "innerRadiusPercent": 0.5,
            
            "legend": {
                "position": "left",
                "fontFamily": "Calibri",
                "size": 16,
                "horizontalOffset": legendOffset,
                "verticalOffset": 50
            },
            
            "title": {
                "title": "Steps per day",
                "position": "bottom",
                "size": 20,
                "fontFamily": "Calibri",
                "verticalOffset": 10
            }
        }
    }
}

function makeProcessor(method, idMapping, mode, param) {
    return {
        "method": method,
        "args": {
            "idMapping": idMapping,
            "mode": mode,
            "param": param
        }
    }
}

function makeData(id, from, to) {
    return {
        "id": id,
        "from": from,
        "to": to
    }
}

function makeVisualizationRequest(visualization, processors, data) {
    return JSON.stringify({
        "type": "visualization",
        "visualization": visualization,
        "processor": processors,
        "data": data
    })
}

function sendRequest(requestText, resultPlaceholder) {
    $.ajax({
        url: 'http://localhost:8080',
        method: 'POST',
        data: requestText,
        dataType: 'html',
        success: function (answer) {
            $('#message-field').empty();
            $(resultPlaceholder).empty().html(answer);
        },
        error: function (response) {
            console.log(response.responseText);
            $('#message-field').empty().html('<span class="error">There is currently no data available. Please try again later.</span>');
            $(resultPlaceholder).empty();
        }
    });
}

function makeDataInsertRequest(inputData) {
    return JSON.stringify({
        "type": "data",
        "action": "insert",
        "data": inputData
    })
}

function sendDataInsertRequest(request) {
    $.ajax({
        url: 'http://localhost:8080',
        method: 'POST',
        data: request,
        dataType: 'html',
        success: function (answer) {
            updateDayContent();
            updateWeekDiagram();
            $('#success-field').empty().html('<span class="success">Successfully synchronized new data.</span>');
        },
        error: function (response) {
            $('#message-field').empty().html('<span class="error">All data has already been synchronized.</span>');
            console.log(response.responseText);
        }
    })
}