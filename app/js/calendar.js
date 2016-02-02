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

loadCurrentWeekDays();
selectTodayInDayList();

/* ****************** */
/*    Days Loading    */
/* ****************** */

function loadCurrentWeekDays() {    
    var dayInMonth = selectedDate.getDate(),
        weekday = selectedDate.getDay();
    
    monday.setDate(dayInMonth - weekday + 1);
    tuesday.setDate(monday.getDate() + 1);
    wednesday.setDate(monday.getDate() + 2);
    thursday.setDate(monday.getDate() + 3);
    friday.setDate(monday.getDate() + 4);
    saturday.setDate(monday.getDate() + 5);
    sunday.setDate(monday.getDate() + 6);
    
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
    } else {
        selectedDate = sunday;
    }
    
    updateDateTitle();
    
    return false;
});

$('#previous-week').click(function () {
    selectedDate.setDate(selectedDate.getDate() - 7);
    loadCurrentWeekDays();
    updateDateTitle();
    
    return false;
});

$('#next-week').click(function () {
    selectedDate.setDate(selectedDate.getDate() + 7);
    loadCurrentWeekDays();
    updateDateTitle();
    
    return false;
});