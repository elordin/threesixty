

window.addEventListener('load', function (e) {
    
     var requestText = {
        "type": "visualization",
        "visualization": {
            "type": "piechart",
            "args": {
                
                "width": 300,
                "height": 300,
                "ids": ["data1"],
                "borderRight": 0,
                "borderTop": 10,
                "borderLeft": 25,
                "innerRadiusPercent": 0.5,
                "angleStart": 50,
                "angleEnd": 360
            }
        },
        "processor": [],
        "data": ["data1", "data2"]
    }
    
    
    
    /*
    var requestText = {
        "type": "visualization",
        "visualization": {
            "type": "linechart",
            "args": {
                
                "width": 300,
                "height": 300,
                "ids": ["22b8f0b0-4451-483d-ae79-c61030e1fb29"],
                "optYMin": 30,
                "optUnitX": "minutes10",
                "minDistanceY": 10.0,
                "borderRight": 15,
                "borderTop": 10
            }
        },
        "processor": [],
        "data": ["22b8f0b0-4451-483d-ae79-c61030e1fb29"]
    }
    */
    
    requestText = JSON.stringify(requestText);
    
    $.ajax({
        url: 'http://localhost:8080',
        method: 'POST',
        data: requestText,
        dataType: 'json',
        complete: function (answer) {
            $('#daily-activity').empty().html(answer.responseText);
        }
    });
});