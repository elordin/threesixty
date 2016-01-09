$("#burger-button").click(function (event) {
    toggleNavigationWidth();
});


function toggleNavigationWidth() {
    if ($("aside").width() == 64) {
        $("aside").width("260px");
    } else {
        $("aside").width("64px")
    }
}



$('#fullpage').fullpage({
    sectionSelector: '.vertical-scrolling',
    slideSelector: '.horizontal-scrolling',
    controlArrows: false
    // more options here
});