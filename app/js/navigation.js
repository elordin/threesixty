$("#burger-button").click(function (event) {
    toggleNavigationWidth();
    toggleNavigationDescription();
});


function toggleNavigationWidth() {
    var $aside = $("aside")
    if ($aside.width() == 64) {
        $aside.width("264px");
    } else {
        $aside.width("64px")
    }
}

function toggleNavigationDescription() {
    var $description = $(".nav-description")
    if ($description.hasClass("visible")) {
        $description.removeClass("visible")
    } else {
        $description.addClass("visible")
    }
}