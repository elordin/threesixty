

window.addEventListener('load', function (e) {

     var requestText = {
        "type": "visualization",
        "visualization": {
            "type": "piechart",
            "args": {

                "width": 300,
                "height": 300,
                "ids": ["1874ba06-24c5-4d04-9d8a-1afd0aee9b77"],
                "borderRight": 0,
                "borderTop": 10,
                "borderLeft": 25,
                "innerRadiusPercent": 0.5
            }
        },
        "processor": [],
        "data": ["1874ba06-24c5-4d04-9d8a-1afd0aee9b77"]
    }

    requestText = JSON.stringify(requestText);

    $.ajax({
        url: 'http://localhost:8080',
        method: 'POST',
        data: requestText,
        dataType: 'html',
        success: function (answer) {
            $('#daily-activity').empty().html(answer);
        },
        error: function (response) {
            console.log(response.responseText);
            $('#daily-activity').empty().html('<span class="error">An Error occurred. Please try again later.</span>');
        }
    });
});
