window.addEventListener('load', function () {

    const SERVER_URL = 'http://localhost:8080';

    var datasets = [ "data1", "data2", "data3", "lineTest" ];

    function createDataListEntry(id) {

        var dataList = document.getElementById('dataList');
        var listitem = document.createElement('li'),
            label    = document.createElement('label'),
            checkbox = document.createElement('input');

        checkbox.setAttribute('type', 'checkbox');
        checkbox.setAttribute('value', id);
        checkbox.setAttribute('id', id);
        checkbox.dataset.id = id;

        label.innerHTML = id;
        label.setAttribute('for', id);

        listitem.appendChild(checkbox);
        listitem.appendChild(label);
        dataList.appendChild(listitem);
    }

    datasets.map(createDataListEntry);


    function generateVisualizationList() {
        $.ajax({
            url: SERVER_URL,
            method: "POST",
            data: JSON.stringify({
                "type": "help",
                "for": "visualizations"
            }),
            dataType: 'json',
            success: function (response) {
                console.log(response.visualizations);

                var vizList = $('#vizList');
                response.visualizations.map(function (viz) {
                    var item = $('<li data-visualization="' + viz + '"><img src="img/placeholder.png"><span>' + viz + '</span></li>');
                    item.click(function (e) {
                        $('#vizList .active').removeClass('active');
                        $(this).addClass('active');
                    });

                    vizList.append(item);
                });

            },
            error: function (response) {
                console.log(response.responseText);
            }
        });
    }

    generateVisualizationList();


    function generateProcessingMethodList() {
        $.ajax({
            url: SERVER_URL,
            method: "POST",
            data: JSON.stringify({
                "type": "help",
                "for": "processingmethods"
            }),
            dataType: 'json',
            success: function (response) {
                console.log(response.processingmethods);
            },
            error: function (response) {
                console.log(response.responseText);
            }
        });
    }

    generateProcessingMethodList();





    function generateJSON() {

        function getSelectedData() {
            var selectedDatasets = document.querySelectorAll(
                '#dataList input[type="checkbox"]:checked');
            var selectedIDs = Array.prototype.map.call(selectedDatasets, function (checkbox) {
                return checkbox.dataset.id;
            });
            return selectedIDs;
        }

        function getProcessorConfig() {
            return null;
        }

        function getVisualizationConfig() {

            var selectedViz = document.querySelector('#vizList .active');

            if (!selectedViz) return null;

            var viz = selectedViz.dataset.visualization;

            return {
                "type": viz,
                "args": {}
            };
        }

        return {
            type: "visualization",
            data: getSelectedData(),
            processor: getProcessorConfig(),
            visualization: getVisualizationConfig()
        };
    }

    function requestPreview(request, callback) {
        return;
    }

    function displayPreview(preview) {
        return;
    }

    document.getElementById('previewBtn').addEventListener('click', function (e) {
        console.log(JSON.stringify(generateJSON()));
        requestPreview(generateJSON(), displayPreview);
    });


});
