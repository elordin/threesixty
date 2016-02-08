$('.diagram-select-item a').click(function () {
    $('.diagram-select-item a').removeClass('selected');
    $(this).addClass('selected');
    
    return false;
})