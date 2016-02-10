$(function () {
    loadStepsBarChart();
    loadRecentWeight();
})

var stepsId = "23551219-404e-42a7-bc95-95accb8affe5";
var bodyWeightId = "52694b3b-ad1c-4656-99a7-8846fe7d8b4e";



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