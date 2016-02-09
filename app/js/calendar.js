var weekdays = [
    "Sunday", "Monday", "Tuesday",
    "Wednesday", "Thursday", "Friday",
    "Saturday"
];

var months = [
  "January", "February", "March",
  "April", "May", "June", "July",
  "August", "September", "October",
  "November", "December"
];

var selectedDate = new Date();

var monday = new Date();
var tuesday = new Date();
var wednesday = new Date();
var thursday = new Date();
var friday = new Date();
var saturday = new Date();
var sunday = new Date();

updateCurrentWeekdays();
selectTodayInDayList();



/* ****************** */
/*    Days Loading    */
/* ****************** */

function updateCurrentWeekdays() {
    var firstDayOfWeek = selectedDate.getDate() - selectedDate.getDay() + 1;
    if (selectedDate.getDay() == 0) {
        firstDayOfWeek = selectedDate.getDate() - 6;
    }

    var currentDate = new Date(selectedDate);

    monday = new Date(currentDate.setDate(firstDayOfWeek));
    tuesday = new Date(currentDate.setDate(monday.getDate() + 1));
    wednesday = new Date(currentDate.setDate(tuesday.getDate() + 1));
    thursday = new Date(currentDate.setDate(wednesday.getDate() + 1));
    friday = new Date(currentDate.setDate(thursday.getDate() + 1));
    saturday = new Date(currentDate.setDate(friday.getDate() + 1));
    sunday = new Date(currentDate.setDate(saturday.getDate() + 1));

    updateWeekdayNumbers();
    updateWeekDiagram();
}

function updateWeekdayNumbers() {
    getLabelForDayItem($("#monday")).replaceWith('<p>' + monday.getDate() + '</p>');
    getLabelForDayItem($("#tuesday")).replaceWith('<p>' + tuesday.getDate() + '</p>');
    getLabelForDayItem($("#wednesday")).replaceWith('<p>' + wednesday.getDate() + '</p>');
    getLabelForDayItem($("#thursday")).replaceWith('<p>' + thursday.getDate() + '</p>');
    getLabelForDayItem($("#friday")).replaceWith('<p>' + friday.getDate() + '</p>');
    getLabelForDayItem($("#saturday")).replaceWith('<p>' + saturday.getDate() + '</p>');
    getLabelForDayItem($("#sunday")).replaceWith('<p>' + sunday.getDate() + '</p>');
}

function getLabelForDayItem(dayItem) {
    return dayItem.children().first().children().first();
}

function selectTodayInDayList() {
    var weekdayName = weekdays[selectedDate.getDay()];
    $('#' + weekdayName.toLowerCase()).addClass('selected');
    updateDayContent();
}

function updateDayContent() {
    updateDateTitle();
    updateDayDiagram();
}

function updateDateTitle() {
    var weekdayName = weekdays[selectedDate.getDay()];
    var dayInMonth = selectedDate.getDate();
    var monthName = months[selectedDate.getMonth()];
    var year = selectedDate.getFullYear();
    var dayDescription = weekdayName + ', ' + dayInMonth + '. ' + monthName + ' '+ year;

    $('.date-title').replaceWith('<h1 class="date-title">' + dayDescription + '</h1>')
}



/* ****************** */
/*   Day Selection    */
/* ****************** */

$('.day-link').click(function () {
    $('.day-item').removeClass('selected');
    $(this).parent().addClass('selected');

    var dateClicked = $(this).children().first().text();
    if (dateClicked == monday.getDate().toString()) {
        selectedDate = monday;
    } else if (dateClicked == tuesday.getDate().toString()) {
        selectedDate = tuesday;
    } else if (dateClicked == wednesday.getDate().toString()) {
        selectedDate = wednesday;
    } else if (dateClicked == thursday.getDate().toString()) {
        selectedDate = thursday;
    } else if (dateClicked == friday.getDate().toString()) {
        selectedDate = friday;
    } else if (dateClicked == saturday.getDate().toString()) {
        selectedDate = saturday;
    } else if (dateClicked == sunday.getDate().toString()) {
        selectedDate = sunday;
    }
    updateDayContent();

    return false;
});

$('#previous-week').click(function () {
    selectedDate.setDate(selectedDate.getDate() - 7);
    updateCurrentWeekdays();
    updateDayContent();
    updateWeekDiagram();

    return false;
});

$('#next-week').click(function () {
    selectedDate.setDate(selectedDate.getDate() + 7);
    updateCurrentWeekdays();
    updateDayContent();
    updateWeekDiagram();

    return false;
});


/* ****************** */
/*    Refresh Data    */
/* ****************** */

$('#refresh').click(function () {
    var request = makeDataInsertRequest(today);
    sendDataInsertRequest(request)
    
    return false;
})




/* ****************** */
/*  Loading Diagrams  */
/* ****************** */

function updateDayDiagram() {
    var today = selectedDate
    today.setHours(0,0,0,0)
    var startTime = today.getTime()
    today.setHours(23,59,59,999)
    var endTime = Math.min(today.getTime(), (new Date()).getTime())

    var visualization = makeBarChartVisualization(["23551219-404e-42a7-bc95-95accb8affe5"]);
    var idMapping = {"23551219-404e-42a7-bc95-95accb8affe5": "23551219-404e-42a7-bc95-95accb8affe5"};
    var processor = makeProcessor("aggregation", idMapping, "sum", "hour");
    var data = makeData("23551219-404e-42a7-bc95-95accb8affe5", startTime, endTime);
    var request = makeVisualizationRequest(visualization, [processor], [data]);

    sendRequest(request, '#date-activity');
}

function updateWeekDiagram() {
    var firstDayOfWeek = selectedDate.getDate() - selectedDate.getDay() + 1;
    if (selectedDate.getDay() == 0) {
        firstDayOfWeek = selectedDate.getDate() - 6;
    }
    var currentDate = new Date(selectedDate);
    monday = new Date(currentDate.setDate(firstDayOfWeek));
    sunday = new Date(currentDate.setDate(monday.getDate() + 6));

    monday.setHours(0, 0, 0, 0)
    var startTime = monday.getTime()
    sunday.setHours(23, 59, 59, 999)
    var endTime = sunday.getTime()

    var visualization = makePieChartVisualization(["23551219-404e-42a7-bc95-95accb8affe5"]);
    var data = makeData("23551219-404e-42a7-bc95-95accb8affe5", startTime, endTime);
    var idMapping = {"23551219-404e-42a7-bc95-95accb8affe5": "23551219-404e-42a7-bc95-95accb8affe5"};
    var processor = makeProcessor("aggregation", idMapping, "mean", "weekday");
    var request = makeVisualizationRequest(visualization, [processor], [data]);

    sendRequest(request, '#week-activity');
}


/* ****************** */
/*   JSON Requests    */
/* ****************** */

function makeBarChartVisualization(ids) {
    return {
        "type": "barchart",
        "args": {
            "ids": ids,
            "width": 512,
            "height": 256,
            "border": {"top": 10, "bottom": 10, "left": 70, "right": 20},
            "yMax": 900,
            "yUnit": 300,
            "xUnit": "",
            "colorScheme": "green",
            "fontFamily": "Calibri",
            "fontSize": 1
        }
    }
}

function makePieChartVisualization(ids) {
    return {
        "type": "piechart",
        "args": {
            "ids": ids,
            "width": 400,
            "height": 200,
            "border": {"top": 20, "bottom": 20, "left": 80, "right": 60},
            "colorScheme": "green",
            "innerRadiusPercent": 0.5,
            "legendPosition": "left",
            "fontFamily": "Calibri",
            "fontSize": 16
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
            $('#message-field').empty().html('<span class="success">Successfully synchronized new data.</span>');
        },
        error: function (response) {
            $('#message-field').empty().html('<span class="error">All data has already been synchronized.</span>');
            console.log(response.responseText);
        }
    })
}