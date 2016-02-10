$(function() {
    loadRecentSteps();
    loadRecentDistance();
    loadRecentWeight();
});




/* ****************** */
/*  Loading Diagrams  */
/* ****************** */

function loadRecentSteps() {
    var today = new Date();
    today.setDate(9),
    today.setHours(0,0,0,0)
    var startTime = today.getTime()
    today.setHours(23,59,59,999)
    
    var now = new Date()
    now.setDate(9);
    var endTime = Math.min(today.getTime(), (now.getTime()))

    var visualization = makeLineChartVisualization(["23551219-404e-42a7-bc95-95accb8affe5"], 'Number of Steps');
    var data = makeData("23551219-404e-42a7-bc95-95accb8affe5", startTime, endTime);
    var request = makeVisualizationRequest(visualization, [], [data]);

    sendRequest(request, '#recent-steps');
}

function loadRecentDistance() {
    var today = new Date();
    today.setDate(9),
    today.setHours(0,0,0,0)
    var startTime = today.getTime()
    today.setHours(23,59,59,999)
    
    var now = new Date()
    now.setDate(9);
    var endTime = Math.min(today.getTime(), (now.getTime()))

    var visualization = makeLineChartVisualization(["23551219-404e-42a7-bc95-95accb8affe5"], 'Distance in m');
    var idMapping = {"23551219-404e-42a7-bc95-95accb8affe5": "23551219-404e-42a7-bc95-95accb8affe5"};
    var processor = makeProcessor("accumulation", idMapping, "sum", "hour");
    var data = makeData("23551219-404e-42a7-bc95-95accb8affe5", startTime, endTime);
    var request = makeVisualizationRequest(visualization, [processor], [data]);

    sendRequest(request, '#recent-distance');
}

