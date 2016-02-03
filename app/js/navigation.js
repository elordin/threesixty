var mediaQuery = window.matchMedia('(min-width: 800px)');
var $aside = $('aside');
var $description = $('.nav-description');

$(window).load(checkWindowWidth);

$(window).resize(checkWindowWidth);


function checkWindowWidth() {
    if (mediaQuery.matches) {
        showSideMenu();
        $aside.addClass('extended');
    } else {
        hideSideMenu();
        $aside.removeClass('extended');
        
    }
}



function showSideMenu() {
    $aside.width('264px');
    $description.addClass('visible');
}

function hideSideMenu() {
    $aside.width('64px');
    $description.removeClass('visible');
}

$("#burger-button").click(function (event) {
    toggleNavigationWidth();
});


function toggleNavigationWidth() {
    if ($aside.width() == 64) {
        showSideMenu();
    } else {
        hideSideMenu();
    }
}