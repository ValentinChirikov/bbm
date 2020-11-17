window.chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};

var config = {
    type: 'line',
    data: {
        datasets: [{
            label: 'Temperature',
            backgroundColor: window.chartColors.orange,
            borderColor: window.chartColors.orange,
            // pointRadius: 0,
            fill: false,
            // showLine: false,
        }]
    },
    options: {
        responsive: true,
        responsiveAnimationDuration: 0, // animation duration after a resize
        maintainAspectRatio: false,
        animation: {
            duration: 0, // general animation time
        },
        title: {
            display: false,
            text: 'Brewery process'
        },
        legend: {
            display: false,
        },
        tooltips: {
            mode: 'index',
            intersect: false,
        },
        hover: {
            animationDuration: 0, // duration of animations when hovering an item
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                type: 'time',
                time: {
                    minUnit: 'minute',
                    unit: 'minute',
                    min: new Date(),
                    // max: moment().add(8, 'hours')
                },

                display: true,
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Temperature'
                }
            }]
        }
    }
};

function initChart() {
    if (typeof document.createElement('canvas').getContext === "function") {
        var ctx = document.getElementById('chart').getContext('2d');
        window.breweryProcess = new Chart(ctx, config);
    } else {

    }

};

var colorNames = Object.keys(window.chartColors);

/** Add chart data
 */
function addChartData(label, data) {
    chart = window.breweryProcess;
    chart.data.labels.push(label);
    chart.data.datasets.forEach(function(ds) {ds.data.push(data);});
    chart.update();
}

/** Remove chart data
 */
function removeChartData(chart) {
    chart.data.labels.pop();
    chart.data.datasets.forEach(function(ds) {ds.data.pop();});
    chart.update();
}