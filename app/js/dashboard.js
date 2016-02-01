

window.addEventListener('load', function (e) {
    var requestText = '{"type": "visualization", "visualization": {"type": "linechart", "args": {"ids": ["92e87fc3-f718-45a1-951f-fa58ab679402"], "width": 300, "height":300}},"processor": [], "data": ["92e87fc3-f718-45a1-951f-fa58ab679402"]}';
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