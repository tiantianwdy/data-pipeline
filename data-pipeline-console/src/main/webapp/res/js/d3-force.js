

var colorArray = ["white", "green", "blue", "orange", "red", "purple",  "black"]
function createDAG(elem, graph) {

  var width = 560,
      height = 400;

  var force = d3.layout.force()
      .charge(-300)
      .linkDistance(120)
      .size([width, height]);

  var svg = d3.select("#" + elem).append("svg")
      .attr("width", width)
      .attr("height", height);

  force
      .nodes(graph.nodes)
      .links(graph.links)
      .start();

  renderWithCurveArrows(svg, force, graph)


};

function renderWithCurveArrows(svg, force, graph){

  var color = d3.scale.category20();

  var div = d3.select("body").append("div")
      .attr("class", "tooltip")
      .style("opacity", 0);

      // build the arrow.
  svg.append("svg:defs").selectAll("marker")
     .data(["arrow"])      // Different link/path types can be defined here
     .enter().append("svg:marker")    // This section adds in the arrows
     .attr("id", String)
     .attr("viewBox", "0 -5 10 10")
     .attr("refX", 35)
     .attr("refY", -1.5)
     .attr("markerWidth", 5)
     .attr("markerHeight", 5)
     .attr("orient", "auto")
     .append("svg:path")
     .attr("d", "M0,-5L10,0L0,5");

  var link = svg.append("svg:g").selectAll("path")
      .data(force.links())
      .enter().append("svg:path")
      .attr("class", function(d) { return "link " + d.type; })
//      .attr("class", "link")
      .attr("marker-end", "url(#arrow)");


  // define the nodes
  var node = svg.selectAll(".node")
      .data(graph.nodes)
      .enter().append("g")
      .attr("class", "node")
      .call(force.drag);

    // add the nodes
  node.append("circle")
      .attr("r", 12)
      .on("mouseover", function(d){ return addToolTip(d, div); })
      .on("mouseout", function(d){ return hideToolTip(d, div); })
      .style("fill", function(d) { return colorArray[d.group]; });

//  node.append("title")
//      .text(function(d) { return d.name; });

//add the text
  node.append("text")
      .attr("x", 15)
      .attr("dy", ".35em")
      .attr("class", "text")
      .text(function(d) { return d.name + " # " + d.version; });

// add the curvy lines
  function tick() {
        link.attr("d", function(d) {
            var dx = d.target.x - d.source.x,
                dy = d.target.y - d.source.y,
                dr = Math.sqrt(dx * dx + dy * dy);
            return "M" +
                d.source.x + "," +
                d.source.y + "A" +
                dr + "," + dr + " 0 0,1 " +
                d.target.x + "," +
                d.target.y;
        });

        node
            .attr("transform", function(d) {
      	    return "translate(" + d.x + "," + d.y + ")"; });
   };


  force.on("tick", tick);

}

function renderWithLines(svg, force, graph) {

  var color = d3.scale.category20();
  var div = d3.select("body").append("div")
      .attr("class", "tooltip")
      .style("opacity", 0);

  svg.append("svg:defs").selectAll("marker")
     .data(["arrow"])      // Different link/path types can be defined here
     .enter().append("svg:marker")    // This section adds in the arrows
     .attr("id", String)
     .attr("viewBox", "0 -5 10 10")
     .attr("refX", 20)
     .attr("refY", -1.5)
     .attr("markerWidth", 5)
     .attr("markerHeight", 5)
     .attr("orient", "auto")
     .append("svg:path")
     .attr("d", "M0,-5L10,0L0,5");

  var link = svg.selectAll(".link")
      .data(graph.links)
      .enter().append("line")
      .attr("class", "link")
      .attr("marker-end", "url(#arrow)");
//      .style("stroke-width", function(d) { return Math.sqrt(d.value); });

  var node = svg.selectAll(".node")
      .data(graph.nodes)
      .enter().append("g")
      .append("circle")
      .attr("class", "node")
      .attr("r", 6)
      .on("mouseover", function(d){ return addToolTip(d, div); })
      .on("mouseout", function(d){ return hideToolTip(d, div); })
      .style("fill", function(d) { return colorArray[d.group]; })
      .call(force.drag);

  node.append("title")
      .text(function(d) { return d.name + "<br/>" + d.version; });

  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });
  });

}

   function addToolTip(d, div){
        lineID = d.name;

        div.transition()
           .duration(200)
           .style("opacity", 9);

        div.html("name: " + d.name
                + "<br/>" + "version: " + d.version
                + "<br/>" + "type: " + d.pipeType
                + "<br/>" + "startTime: " + d.startTime
                + "<br/>" + "endTime: " + d.endTime
                + "<br/>" + "state: " + d.status)
           .style("left", (d3.event.pageX) + "px")
           .style("top", (d3.event.pageY + 10) + "px");
   };

   function hideToolTip(d, div){
     div.transition()
     .duration(500)
     .style("opacity", 0)
   }




var graphData ={
  "nodes":[
    {"id":0, "name":"textMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"completed", "group":1},
    {"id":1, "name":"jsonMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"running", "group":2},
    {"id":2, "name":"csvMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"running", "group":2},
    {"id":3, "name":"dataJoiner", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":4, "name":"featureExtractorPy", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":5, "name":"featureExtractorSpark", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":6, "name":"analysisPy", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Python", "status":"waiting", "group":3},
    {"id":7, "name":"analysisSpark", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3}
  ],
  "links":[
    {"source":0,"target":3,"value":1},
    {"source":1,"target":3,"value":8},
    {"source":2,"target":3,"value":10},
    {"source":3,"target":4,"value":6},
    {"source":3,"target":5,"value":1},
    {"source":4,"target":6,"value":1},
    {"source":5,"target":7,"value":1}
  ]
}

var graphDataInfo ={
  "nodes":[
    {"id":0, "name":"textMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"completed", "group":1},
    {"id":1, "name":"jsonMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"running", "group":2},
    {"id":2, "name":"csvMapper", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"MR", "status":"running", "group":2},
    {"id":3, "name":"dataJoiner", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":4, "name":"featureExtractorPy", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":8, "name":"featureExtractorPy", "version":"0.0.2", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":5, "name":"featureExtractorSpark", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3},
    {"id":6, "name":"analysisPy", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Python", "status":"waiting", "group":3},
    {"id":7, "name":"analysisSpark", "version":"0.0.1", "startTime":"123", "endTime":"time", "pipeType":"Spark", "status":"waiting", "group":3}
  ],
  "links":[
    {"source":0,"target":3,"value":1},
    {"source":1,"target":3,"value":8},
    {"source":2,"target":3,"value":10},
    {"source":3,"target":4,"value":6},
    {"source":3,"target":5,"value":1},
    {"source":4,"target":6,"value":1},
    {"source":5,"target":7,"value":1}
  ]
}