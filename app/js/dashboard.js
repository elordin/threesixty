

window.addEventListener('load', function (e) {
    
     var requestText = {
        "type": "visualization",
        "visualization": {
            "type": "piechart",
            "args": {
                
                "width": 300,
                "height": 300,
                "ids": ["25d89f4b-bf5e-4511-b986-7500d28b4000"],
                "borderRight": 0,
                "borderTop": 10,
                "borderLeft": 25,
                "innerRadiusPercent": 0.5
            }
        },
        "processor": [],
        "data": ["25d89f4b-bf5e-4511-b986-7500d28b4000"]
    }
    
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