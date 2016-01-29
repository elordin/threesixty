window.addEventListener('load', function (e) {
    var requestText = '{"type": "visualization", "visualization": {"type": "barchart", "args": {"ids": ["data1"], "width": 300, "height":300}},"processor": [], "data": ["data1", "data2", "data3", "lineTest"]}';
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