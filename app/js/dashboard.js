

window.addEventListener('load', function (e) {
    
     var requestText = {
        "type": "visualization",
        "visualization": {
            "type": "pichart",
            "args": {
                
                "width": 300,
                "height": 300,
                "ids": ["4ec0cdbe-32b6-4b30-8be2-6f07efeecf0b"],
                "borderRight": 0,
                "borderTop": 10,
                "borderLeft": 25,
                "innerRadiusPercent": 0.5,
                "angleStart": 50,
                "angleEnd": 360
            }
        },
        "processor": [],
        "data": ["4ec0cdbe-32b6-4b30-8be2-6f07efeecf0b"]
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