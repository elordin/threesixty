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
selectedDate.setDate(11);

loadCurrentWeekDays();
selectTodayInDayList();

/* ****************** */
/*    Days Loading    */
/* ****************** */

function loadCurrentWeekDays() {    
    var dayInMonth = selectedDate.getDate(),
        weekday = selectedDate.getDay();
    
    var monday = dayInMonth - weekday + 1,
        tuesday = monday + 1,
        wednesday = monday + 2,
        thursday = monday + 3,
        friday = monday + 4,
        saturday = monday + 5,
        sunday = monday + 6;
    
    getLabelForDayItem($("#monday")).replaceWith('<p>' + monday + '</p>');
    getLabelForDayItem($("#tuesday")).replaceWith('<p>' + tuesday + '</p>');
    getLabelForDayItem($("#wednesday")).replaceWith('<p>' + wednesday + '</p>');
    getLabelForDayItem($("#thursday")).replaceWith('<p>' + thursday + '</p>');
    getLabelForDayItem($("#friday")).replaceWith('<p>' + friday + '</p>');
    getLabelForDayItem($("#saturday")).replaceWith('<p>' + saturday + '</p>');
    getLabelForDayItem($("#sunday")).replaceWith('<p>' + sunday + '</p>');
}

function getLabelForDayItem(dayItem) {
    return dayItem.children().first().children().first();
}

function selectTodayInDayList() {
    var weekdayName = weekdays[selectedDate.getDay()];
    $('#' + weekdayName.toLowerCase()).addClass('selected');
    updateDateTitle();
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
/*   Days Selection   */
/* ****************** */

$('.day-link').click(function () {
    $('.day-item').removeClass('selected');
    $(this).parent().addClass('selected');
    
    selectedDate.setDate(($(this).children().first().text()));
    updateDateTitle();
    
    return false;
});

$('#previous-week').click(function () {
    return false;
});

$('#next-week').click(function () {
    return false;
});