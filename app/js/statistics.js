$(function () {
    loadStepsBarChart();
    loadHeartRate();
})

var stepsId = "23551219-404e-42a7-bc95-95accb8affe5";
var heartRateId = "8313587b-8c79-45fd-b244-913dc8e5dfb9";
// var yesterdayHeartRateId = "12c8615b-44eb-473c-a38d-bcb4bca3c221";
var yesterdayHeartRateId = "141abea1-9abf-415d-870a-39bff608e0c5";


function loadStepsBarChart() {
    var today = new Date();
    var firstDayOfWeek = today.getDate() - today.getDay() + 1;
    if (today.getDay() == 0) {
        firstDayOfWeek = today.getDate() - 6;
    }
    var currentDate = new Date(today);
    monday = new Date(currentDate.setDate(firstDayOfWeek));
    sunday = new Date(currentDate.setDate(monday.getDate() + 6));

    monday.setHours(0, 0, 0, 0)
    var startTime = monday.getTime()
    sunday.setHours(23, 59, 59, 999)
    var endTime = sunday.getTime()

    // var visualization = makeBarChartVisualization([stepsId]);
    var visualization = makeBarChartVisualization([stepsId], "Steps per day", 120, 60)
    var data = makeData("23551219-404e-42a7-bc95-95accb8affe5", startTime, endTime);
    var idMapping = {"23551219-404e-42a7-bc95-95accb8affe5": "23551219-404e-42a7-bc95-95accb8affe5"};
    var processor = makeProcessor("aggregation", idMapping, "mean", "hour");
    var request = makeVisualizationRequest(visualization, [processor], [data]);

    sendRequest(request, '#steps');
}

function loadStepsPieChart() {
    var today = new Date();
    var firstDayOfWeek = today.getDate() - today.getDay() + 1;
    if (today.getDay() == 0) {
        firstDayOfWeek = today.getDate() - 6;
    }
    var currentDate = new Date(today);
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

    sendRequest(request, '#steps');
    $('#steps').addClass("piechart-selected")
}


$('.diagram-select-item a').click(function () {
    $('.diagram-select-item a').removeClass('selected');
    $(this).addClass('selected');
    
    return false;
})

$('#piechart').click(function () {
    loadStepsPieChart();
});

$('#barchart').click(function () {
    loadStepsBarChart();
})



function loadRecentWeight() {
    var firstDay = new Date(1454281200000).getTime;
    var lastDay = new Date(1455145199000).getTime;
    
    var visualization = makeLineChartVisualization([bodyWeightId], "Weight in kg");
    var data = makeData(bodyWeightId, firstDay, lastDay);
    var request = makeVisualizationRequest(visualization, [], [data]);
    
    sendRequest(request, '#body-weight');
}

function loadHeartRate() {
    var firstDay = new Date(1451602800000).getTime;
    var lastDay = new Date(1451602800000).getTime;
    
    var visualization = makeLineChartVisualization([heartRateId, yesterdayHeartRateId], "Heartbeats per min", 30);
    var data1 = makeData(heartRateId, firstDay, lastDay);
    var data2 = makeData(yesterdayHeartRateId, firstDay, lastDay);
    var request = makeVisualizationRequest(visualization, [], [data1, data2]);
    
    sendRequest(request, '#heart-rate');
}